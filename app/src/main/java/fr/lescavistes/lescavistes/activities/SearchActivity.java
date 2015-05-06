package fr.lescavistes.lescavistes.activities;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


import java.io.IOException;
import java.util.List;
import java.util.Locale;

import fr.lescavistes.lescavistes.MainApplication;
import fr.lescavistes.lescavistes.R;
import fr.lescavistes.lescavistes.activities.DisplayShopListActivity;


public class SearchActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener {

    public final static String WHERE_MESSAGE = "fr.lescavistes.lescavistes.WHERE_MESSAGE";
    public final static String WHAT_MESSAGE = "fr.lescavistes.lescavistes.WHAT_MESSAGE";
    public final static String LAT_MESSAGE = "fr.lescavistes.lescavistes.LAT_MESSAGE";
    public final static String LNG_MESSAGE = "fr.lescavistes.lescavistes.LNG_MESSAGE";
    protected static final String TAG = "search-activity";
    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;
    protected boolean mGoogleApiConnected = false;

    Geocoder geocoder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        buildGoogleApiClient();

        //Transform address in latlng
        if (MainApplication.isDebug()) {
            try {
                geocoder = new Geocoder(this, Locale.getDefault());
            } catch (Exception e) {
                geocoder = null;
            }
        } else {
            geocoder = new Geocoder(this, Locale.getDefault());
        }
    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Runs when a GoogleApiClient object successfully connects.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        mGoogleApiConnected = true;
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might be returned in
        // onConnectionFailed.
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when the user clicks the button
     */
    public void searchWhere(View view) {

        Intent intent = new Intent(this, DisplayShopListActivity.class);

        EditText editText = (EditText) findViewById(R.id.query_where);
        String where = editText.getText().toString();

        // if empty, use current location
        if (where.length() == 0) {
            if (mGoogleApiConnected) {
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {
                    String lat = String.valueOf(mLastLocation.getLatitude());
                    String lng = String.valueOf(mLastLocation.getLongitude());
                    intent.putExtra(LAT_MESSAGE, lat);
                    intent.putExtra(LNG_MESSAGE, lng);
                } else {
                    Toast.makeText(this, R.string.impossible_to_connect, Toast.LENGTH_LONG).show();
                    return;
                }
            } else {
                Toast.makeText(this, R.string.no_location_detected, Toast.LENGTH_LONG).show();
                return;
            }

        } else {
            List<Address> addresses;
            if (geocoder==null) {
                intent.putExtra(LAT_MESSAGE, "44");
                intent.putExtra(LNG_MESSAGE, "3");
            } else {
                try {
                    addresses = geocoder.getFromLocationName(where, 1);
                } catch (IOException e) {
                    Toast.makeText(this, R.string.ununderstable_address, Toast.LENGTH_LONG).show();
                    return;
                }

                if (addresses.size() > 0) {
                    String lat = String.valueOf(addresses.get(0).getLatitude());
                    String lng = String.valueOf(addresses.get(0).getLongitude());
                    intent.putExtra(LAT_MESSAGE, lat);
                    intent.putExtra(LNG_MESSAGE, lng);
                } else {
                    if (MainApplication.isDebug()){
                        intent.putExtra(LAT_MESSAGE, "44");
                        intent.putExtra(LNG_MESSAGE, "3");
                    } else {
                        Toast.makeText(this, R.string.ununderstable_address, Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }

        }

        editText = (EditText) findViewById(R.id.query_what);
        String what = editText.getText().toString();


        intent.putExtra(WHERE_MESSAGE, where);
        intent.putExtra(WHAT_MESSAGE, what);

        startActivity(intent);

    }

}
