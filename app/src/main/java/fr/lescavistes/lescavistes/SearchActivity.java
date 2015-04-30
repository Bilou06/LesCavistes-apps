package fr.lescavistes.lescavistes;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;


public class SearchActivity extends ActionBarActivity {
    String URL = "http://192.168.1.78:8181/static/wineshops/style.css";

    public final static String WHERE_MESSAGE = "fr.lescavistes.lescavistes.WHERE_MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_search);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(true);

        Tab tab = actionBar.newTab()
                .setIcon(R.drawable.ic_tab_location)
                .setTabListener(new TabListener<SearchLocationFragment>(
                        this, "Location", SearchLocationFragment.class));
        actionBar.addTab(tab);

        tab = actionBar.newTab()
                .setIcon(R.drawable.ic_tab_location_wine)
                .setTabListener(new TabListener<SearchLocationAndWineFragment>(
                        this, "Location and Wine", SearchLocationAndWineFragment.class));
        actionBar.addTab(tab);
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
/*
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, URL, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        String message = response.toString();
                        go(message);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub

                    }
                });


        // Access the RequestQueue through your singleton class.
        SingletonWebRequest.getInstance(this).addToRequestQueue(jsObjRequest);

*/
        // Instantiate the RequestQueue.
        RequestQueue queue = SingletonWebRequest.getInstance(this.getApplicationContext()).getRequestQueue();

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        go(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // TODO Auto-generated method stub
            }
        });
// Add the request to the RequestQueue.
        //queue.add(stringRequest);
        SingletonWebRequest.getInstance(this).addToRequestQueue(stringRequest);

    }

    private void go(String message2) {
        Intent intent = new Intent(this, DisplayShopListActivity.class);
        EditText editText = (EditText) findViewById(R.id.query_where);
        String message = editText.getText().toString();
        intent.putExtra(WHERE_MESSAGE, message2);
        startActivity(intent);
    }
}

