package fr.lescavistes.lescavistes.activities;


import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fr.lescavistes.lescavistes.utils.JSONObjectUtf8;
import fr.lescavistes.lescavistes.MainApplication;
import fr.lescavistes.lescavistes.R;
import fr.lescavistes.lescavistes.core.Shop;
import fr.lescavistes.lescavistes.fragments.ShopListViewFragment;
import fr.lescavistes.lescavistes.fragments.ShopsFragmentPagerAdapter;


public class DisplayShopListActivity extends AppCompatActivity
        implements ShopListViewFragment.OnShopSelectedListener {

    private static final String WHERE = "where";
    private static final String WHAT = "what";
    private static final String LAT = "lat";
    private static final String LNG = "lng";
    private static final String SHOPS = "shops";

    private static final String TAG = "Display Shop List";
    ShopsFragmentPagerAdapter mShopsFragmentPagerAdapter;
    ViewPager mViewPager;

    private String base_URL = "http://192.168.0.12:8181/";
    private String lat, lng, where, what;
    private TextView textView, textView2;
    private ArrayList shopList;
    private ShopListViewFragment listViewFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_display_shop_list);

        Intent intent = getIntent();
        String where = intent.getStringExtra(SearchActivity.WHERE_MESSAGE);
        String what = intent.getStringExtra(SearchActivity.WHAT_MESSAGE);
        lng = intent.getStringExtra(SearchActivity.LNG_MESSAGE);
        lat = intent.getStringExtra(SearchActivity.LAT_MESSAGE);

        String get_url = base_URL + "getwineshops/?format=json&lat=" + lat + "&lng=" + lng + "&q=" + what;

        mViewPager = (ViewPager) findViewById(R.id.pager);

        // Request the shop list from the url.
        shopList = new ArrayList();
        JsonArrayRequest jsonRequest = new JsonArrayRequest(get_url,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {

                        try {
                            // Parsing json array response
                            // loop through each json object
                            for (int i = 0; i < response.length(); i++) {

                                JSONObjectUtf8 jsonShop = new JSONObjectUtf8((JSONObject) response.get(i));
                                Shop shop = new Shop(jsonShop);

                                shopList.add(shop);

                            }


                            // ViewPager and its adapters use support library
                            // fragments, so use getSupportFragmentManager.
                            mShopsFragmentPagerAdapter =
                                    new ShopsFragmentPagerAdapter(getSupportFragmentManager());
                            mShopsFragmentPagerAdapter.setContent(shopList, lat, lng);
                            mViewPager.setAdapter(mShopsFragmentPagerAdapter);


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

        // action bar
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
                // When the tab is selected, switch to the
                // corresponding page in the ViewPager.
                mViewPager.setCurrentItem(tab.getPosition());
            }

            public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // hide the given tab
            }

            public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
                // probably ignore this event
            }
        };

        // Add 2 tabs
        ActionBar.Tab tab = actionBar.newTab()
                .setIcon(R.drawable.ic_action_list)
                .setTabListener(tabListener);
        actionBar.addTab(tab);

        tab=actionBar.newTab()
                .setIcon(R.drawable.ic_tab_location)
                .setTabListener(tabListener);
        actionBar.addTab(tab);



        mViewPager.setOnPageChangeListener(
                new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        // When swiping between pages, select the
                        // corresponding tab.
                        actionBar.setSelectedNavigationItem(position);
                    }
                });

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

    public void onShopSelected(int id) {

    }
}



