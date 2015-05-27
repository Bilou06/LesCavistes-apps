package fr.lescavistes.lescavistes.activities;


import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
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


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import fr.lescavistes.lescavistes.MainApplication;
import fr.lescavistes.lescavistes.R;
import fr.lescavistes.lescavistes.core.Model;
import fr.lescavistes.lescavistes.core.Results;
import fr.lescavistes.lescavistes.core.Shop;
import fr.lescavistes.lescavistes.utils.JSONObjectUtf8;


public class SearchActivity extends AppCompatActivity implements
        ConnectionCallbacks, OnConnectionFailedListener {

    protected static final String TAG = "search-activity";

    /**
     * Provides the entry point to Google Play services.
     */
    protected GoogleApiClient mGoogleApiClient;
    protected boolean mGoogleApiConnected = false;

    private Geocoder mGeocoder = null;

    private Model model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        buildGoogleApiClient();

        //Transform address in latlng
        //Transform address in latlng
        if (MainApplication.isDebug()) {
            try {
                mGeocoder = new Geocoder(this, Locale.getDefault());
            } catch (Exception e) {
                mGeocoder = null;
            }
        } else {
            mGeocoder = new Geocoder(this, Locale.getDefault());
        }

        model = MainApplication.getModel();

        if (model.what != null && model.what.length() != 0){
            EditText what = (EditText) findViewById(R.id.query_what);
            what.setText(model.what);
        }

        if (model.where != null && model.where.length() != 0){
            EditText what = (EditText) findViewById(R.id.query_where);
            what.setText(model.where);
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

        final Intent intent = new Intent(this, DisplayShopListActivity.class);

        EditText editText = (EditText) findViewById(R.id.query_what);
        model.what = editText.getText().toString();

        editText = (EditText) findViewById(R.id.query_where);
        model.where = editText.getText().toString();

        // if empty, use current location
        if (model.where.length() == 0) {
            if (mGoogleApiConnected) {
                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                if (mLastLocation != null) {
                    model.lat = mLastLocation.getLatitude();
                    model.lng = mLastLocation.getLongitude();
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
            if (mGeocoder == null) {
                if (MainApplication.isDebug()) {
                    model.lat = 44.0;
                    model.lng = 3.0;
                } else {
                    Toast.makeText(this, R.string.no_connection_geocoder, Toast.LENGTH_LONG).show();
                    return;
                }

            } else {
                try {
                    addresses = mGeocoder.getFromLocationName(model.where, 1);
                } catch (IOException e) {
                    Toast.makeText(this, R.string.ununderstable_address, Toast.LENGTH_LONG).show();
                    return;
                }

                if (addresses.size() > 0) {
                    model.lat = addresses.get(0).getLatitude();
                    model.lng = addresses.get(0).getLongitude();

                } else {
                    if (MainApplication.isDebug()) {
                        model.lat = 44.0;
                        model.lng = 3.0;
                    } else {
                        Toast.makeText(this, R.string.ununderstable_address, Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }
        }

        // Request the shop list from the url.
        String get_url = MainApplication.baseUrl() + "getwineshops/?format=json&lat=" + model.getLat() + "&lng=" + model.getLng() + "&q=" + model.what +"&c=0";
        JsonArrayRequest jsonRequest = new JsonArrayRequest(get_url,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {

                        try {
                            synchronized (model.shopList) {
                                // Parsing json array response
                                model.shopList = new Results<Shop>();
                                model.shopList.size = Integer.parseInt(response.get(0).toString());

                                for (int i = 1; i < response.length(); i++) {

                                    JSONObjectUtf8 jsonShop = new JSONObjectUtf8((JSONObject) response.get(i));
                                    Shop shop = new Shop(jsonShop);
                                    model.shopList.items.add(shop);

                                }
                            }

                            startActivity(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (error instanceof TimeoutError || error instanceof AuthFailureError || error instanceof ServerError || error instanceof ParseError) {
                    Toast.makeText(getApplicationContext(),
                            "Le site est en maintenance. Merci de réessayer dans quelques minutes",
                            Toast.LENGTH_LONG).show();
                } else if (error instanceof NoConnectionError || error instanceof NetworkError) {
                    Toast.makeText(getApplicationContext(),
                            "Impossible de se connecter à internet. Merci de vérifier votre connexion.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        // Add the request to the RequestQueue.
        MainApplication.getInstance().getRequestQueue().add(jsonRequest);

    }
}

