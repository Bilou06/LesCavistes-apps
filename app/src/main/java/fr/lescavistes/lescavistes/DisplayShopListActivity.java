package fr.lescavistes.lescavistes;


import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class DisplayShopListActivity extends ActionBarActivity
        implements ShopListViewFragment.OnShopSelectedListener {

    private static final String TAG = "Display Shop List";
    String base_URL = "http://192.168.0.12:8181/";
    String lat, lng, where, what;
    TextView textView, textView2;

    ShopListViewFragment listViewFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_display_shop_list);

        listViewFragment = new ShopListViewFragment();
        // Add the fragment to the 'fragment_container' FrameLayout
        getSupportFragmentManager().beginTransaction()
                .add(R.id.shoplist_fragment, listViewFragment).commit();


        Intent intent = getIntent();
        String where = intent.getStringExtra(SearchActivity.WHERE_MESSAGE);
        String what = intent.getStringExtra(SearchActivity.WHAT_MESSAGE);
        lng = intent.getStringExtra(SearchActivity.LNG_MESSAGE);
        lat = intent.getStringExtra(SearchActivity.LAT_MESSAGE);

        String get_url = base_URL + "getwineshops/?format=json&lat=" + lat + "&lng=" + lng + "&q=" + what;
        //String get_url = "http://192.168.1.78:8181/static/wineshops/style.css";

        // Request a string response from the provided URL.
        final ArrayList shopList = new ArrayList();
        JsonArrayRequest jsonRequest = new JsonArrayRequest(get_url,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {

                        try {
                            // Parsing json array response
                            // loop through each json object
                            for (int i = 0; i < response.length(); i++) {

                                JSONObjectUtf8 jsonShop = new JSONObjectUtf8( (JSONObject) response.get(i) );
                                Shop shop = new Shop(jsonShop);

                                shopList.add(shop);

                            }

                            listViewFragment.setContent(shopList);
                            //refresh
                            final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                            ft.detach(listViewFragment);
                            ft.attach(listViewFragment);
                            ft.commit();

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
                Toast.makeText(getApplicationContext(),
                        "Impossible de retrouver les informations, veuillez réessayer",
                        Toast.LENGTH_LONG).show();
            }
        });
        // Add the request to the RequestQueue.
        MainApplication.getInstance().getRequestQueue().add(jsonRequest);

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

    public void onShopSelected(int id){

    }
}

