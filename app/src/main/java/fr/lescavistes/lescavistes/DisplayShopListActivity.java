package fr.lescavistes.lescavistes;


import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;



public class DisplayShopListActivity extends ActionBarActivity {

    private static final String TAG = "Display Shop List";
    String base_URL = "http://192.168.0.12:8181/";
    String lat, lng, where, what;
    TextView textView, textView2;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String where = intent.getStringExtra(SearchActivity.WHERE_MESSAGE);
        String what = intent.getStringExtra(SearchActivity.WHAT_MESSAGE);
        lng = intent.getStringExtra(SearchActivity.LNG_MESSAGE);
        lat = intent.getStringExtra(SearchActivity.LAT_MESSAGE);

        textView = new TextView(this);
        textView2 = new TextView(this);
        textView.setTextSize(40);
        textView2.setTextSize(40);

        String get_url = base_URL + "getwineshops/?format=json&lat=" + lat + "&lng=" + lng + "&q=" + what;
        //String get_url = "http://192.168.1.78:8181/static/wineshops/style.css";

        // Request a string response from the provided URL.
        JsonArrayRequest jsonRequest = new JsonArrayRequest(get_url,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {

                        textView2.setText("Response is: \n"+ response.toString());
                        setContentView(textView2);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                textView2.setText(error.getMessage());
                setContentView(textView2);
            }
        });
        // Add the request to the RequestQueue.
        MainApplication.getInstance().getRequestQueue().add(jsonRequest);

        //textView.setText("a");
        //setContentView(textView);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_search:
                openSearch();
                return true;
            case R.id.action_settings:
                openSettings();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void openSearch() {

    }

    private void openSettings() {

    }
}

