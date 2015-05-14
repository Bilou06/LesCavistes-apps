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
import android.widget.Toast;


import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fr.lescavistes.lescavistes.fragments.ShopMapViewFragment;
import fr.lescavistes.lescavistes.utils.JSONObjectUtf8;
import fr.lescavistes.lescavistes.MainApplication;
import fr.lescavistes.lescavistes.R;
import fr.lescavistes.lescavistes.core.Shop;
import fr.lescavistes.lescavistes.fragments.ShopListViewFragment;


public class DisplayShopListActivity extends AppCompatActivity
        implements ShopListViewFragment.OnShopSelectedListener {

    public static final String SHOPS_KEY = "shops_key";
    public static final String LAT_KEY = "LAT_KEY";
    public static final String LNG_KEY = "LNG_KEY";
    public static final String SIZE_KEY = "size_key";


    private static final String WHERE = "where";
    private static final String WHAT = "what";
    private static final String LAT = "lat";
    private static final String LNG = "lng";


    private static final String TAG = "Display Shop List";
    ShopListViewFragment.ShopsFragmentPagerAdapter mShopsFragmentPagerAdapter;
    ViewPager mViewPager;

    private ShopListViewFragment listViewFragment;
    private ShopMapViewFragment mapsViewFragment;

    private String lat, lng, where, what, size;
    private ArrayList shopList;

    private Boolean mSwipeLayout;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_display_shop_list);

        Intent intent = getIntent();
        where = intent.getStringExtra(SearchActivity.WHERE_MESSAGE);
        what = intent.getStringExtra(SearchActivity.WHAT_MESSAGE);
        lng = intent.getStringExtra(SearchActivity.LNG_MESSAGE);
        lat = intent.getStringExtra(SearchActivity.LAT_MESSAGE);
        shopList = (ArrayList) intent.getSerializableExtra(SearchActivity.SHOPS_MESSAGE);
        size = intent.getStringExtra(SearchActivity.NB_RESULTS);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mSwipeLayout = (mViewPager != null);


        if (mSwipeLayout) {
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

            tab = actionBar.newTab()
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


        // Create the content fragments : map and list
        Bundle args = new Bundle();
        args.putSerializable(SHOPS_KEY, shopList);
        args.putFloat(LAT_KEY, Float.parseFloat(lat));
        args.putFloat(LNG_KEY, Float.parseFloat(lng));
        args.putInt(SIZE_KEY, Integer.parseInt(size));

        if (mSwipeLayout) {
            mShopsFragmentPagerAdapter =
                    new ShopListViewFragment.ShopsFragmentPagerAdapter(getSupportFragmentManager());
            mShopsFragmentPagerAdapter.setContent(args);
            mViewPager.setAdapter(mShopsFragmentPagerAdapter);

        } else if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            listViewFragment = new ShopListViewFragment();
            listViewFragment.setArguments(args);
            transaction.add(R.id.list_shops, listViewFragment);
            transaction.addToBackStack(null);
            transaction.commit();

            transaction = getSupportFragmentManager().beginTransaction();
            mapsViewFragment = new ShopMapViewFragment();
            mapsViewFragment.setArguments(args);
            transaction.add(R.id.map_shops, mapsViewFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }

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


    // Append more data into the adapter
    public void loadMoreDataFromApi(int offset) {
        // Request the shop list from the url.
        String get_url = MainApplication.baseUrl() + "getwineshops/?format=json&lat=" + lat + "&lng=" + lng + "&q=" + what + "&c=" + String.valueOf(offset);
        JsonArrayRequest jsonRequest = new JsonArrayRequest(get_url,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {

                        try {
                            // Parsing json array response

                            int size = Integer.parseInt(response.get(0).toString());

                            ArrayList shopList = new ArrayList();
                            for (int i = 1; i < response.length(); i++) {

                                JSONObjectUtf8 jsonShop = new JSONObjectUtf8((JSONObject) response.get(i));
                                Shop shop = new Shop(jsonShop);

                                shopList.add(shop);

                            }

                            if (mSwipeLayout) {
                                mShopsFragmentPagerAdapter.getListFragment().addContent(size, shopList);
                                mShopsFragmentPagerAdapter.getMapFragment().addContent(size, shopList);
                            }
                            else{
                                listViewFragment.addContent(size, shopList);
                                mapsViewFragment.addContent(size, shopList);
                            }

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

    }


}



