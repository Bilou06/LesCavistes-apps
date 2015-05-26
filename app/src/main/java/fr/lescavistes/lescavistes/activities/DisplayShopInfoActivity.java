package fr.lescavistes.lescavistes.activities;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import fr.lescavistes.lescavistes.MainApplication;
import fr.lescavistes.lescavistes.R;
import fr.lescavistes.lescavistes.core.Results;
import fr.lescavistes.lescavistes.core.Shop;
import fr.lescavistes.lescavistes.core.Wine;
import fr.lescavistes.lescavistes.fragments.ShopGotoViewFragment;
import fr.lescavistes.lescavistes.fragments.ShopInfoViewFragment;
import fr.lescavistes.lescavistes.utils.JSONObjectUtf8;


/**
 * Created by Sylvain on 26/05/2015.
 */
public class DisplayShopInfoActivity extends AppCompatActivity {

    public static final String LAT_KEY = "LAT_KEY";
    public static final String LNG_KEY = "LNG_KEY";
    public static final String WHAT_KEY = "WHAT_KEY";
    public static final String SHOP_KEY = "SHOP_KEY";

    ShopFragmentPagerAdapter mShopFragmentPagerAdapter;
    ViewPager mViewPager;

    private ShopInfoViewFragment mInfoViewFragment;
    private ShopGotoViewFragment mGotoViewFragment;

    private Boolean mSwipeLayout;

    private String mWhere, mWhat;
    private Shop mShop;
    private float mLat, mLng;

    private Results mWines;


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_display_shop_info);

        Intent intent = getIntent();
        mWhat = intent.getStringExtra(DisplayShopListActivity.WHAT_MESSAGE);
        mLng = intent.getFloatExtra(DisplayShopListActivity.LNG_MESSAGE, 0);
        mLat = intent.getFloatExtra(DisplayShopListActivity.LAT_MESSAGE, 0);
        mShop = (Shop) intent.getSerializableExtra(DisplayShopListActivity.SHOP_MESSAGE);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mSwipeLayout = (mViewPager != null);

        if (mSwipeLayout) {
            // action bar
            final ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

            // Create a tab listener that is called when the user changes tabs.
            ActionBar.TabListener tabListener = new ActionBar.TabListener() {
                public void onTabSelected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
                    // When the tab is selected, switch to the
                    // corresponding page in the ViewPager.
                    mViewPager.setCurrentItem(tab.getPosition());
                }

                public void onTabUnselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
                    // hide the given tab
                }

                public void onTabReselected(ActionBar.Tab tab, android.support.v4.app.FragmentTransaction ft) {
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
                        public void onPageSelected(int newPosition) {
                            // When swiping between pages, select the
                            // corresponding tab.
                            actionBar.setSelectedNavigationItem(newPosition);
                        }
                    });

        }


        // Create the content fragments : map and list
        Bundle args = new Bundle();
        args.putFloat(LAT_KEY, mLat);
        args.putFloat(LNG_KEY, mLng);
        args.putString(WHAT_KEY, mWhat);
        args.putSerializable(SHOP_KEY, mShop);

        if (mSwipeLayout) {
            mShopFragmentPagerAdapter =
                    new ShopFragmentPagerAdapter(getSupportFragmentManager());
            mShopFragmentPagerAdapter.setContent(args);
            mViewPager.setAdapter(mShopFragmentPagerAdapter);

        } else if (savedInstanceState == null) {
            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            mInfoViewFragment = new ShopInfoViewFragment();
            mInfoViewFragment.setArguments(args);
            transaction.add(R.id.list_shops, mInfoViewFragment);
            transaction.addToBackStack(null);
            transaction.commit();

            transaction = getSupportFragmentManager().beginTransaction();
            mGotoViewFragment = new ShopGotoViewFragment();
            mGotoViewFragment.setArguments(args);
            transaction.add(R.id.map_shops, mGotoViewFragment);
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


    // Append more data into the adapter
    public void loadMoreDataFromApi(int offset) {
        // Request the wine list from the url.
        String get_url = MainApplication.baseUrl() + "getwines/?format=json&shop=" + String.valueOf(mShop.getId()) + "&q=" + mWhat + "&c=" + String.valueOf(offset);
        JsonArrayRequest jsonRequest = new JsonArrayRequest(get_url,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {

                        try {
                            // Parsing json array response

                            mWines.size = Integer.parseInt(response.get(0).toString());

                            ArrayList<Wine> newWines = new ArrayList<Wine>();
                            for (int i = 1; i < response.length(); i++) {

                                JSONObjectUtf8 jsonWine = new JSONObjectUtf8((JSONObject) response.get(i));
                                Wine wine = new Wine(jsonWine);

                                mWines.items.add(wine);
                                newWines.add(wine);

                            }

                            //getWineListFragment().addContent(mWines.size, newWines);


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
                            getString(R.string.maintenance_error),
                            Toast.LENGTH_LONG).show();
                } else if (error instanceof NoConnectionError || error instanceof NetworkError) {
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.connection_error),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        // Add the request to the RequestQueue.
        MainApplication.getInstance().getRequestQueue().add(jsonRequest);

    }


    public class ShopFragmentPagerAdapter extends FragmentPagerAdapter {

        static final int NUM_ITEMS = 2;

        private Bundle args;

        private ShopInfoViewFragment mInfoFragment;
        private ShopGotoViewFragment mGotoFragment;

        public ShopFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
            args = new Bundle();
        }

        public void setContent(Bundle args) {
            this.args = args;
        }

        @Override
        public Fragment getItem(int i) {
            if (i == 0) {
                mInfoFragment = new ShopInfoViewFragment();
                mInfoFragment.setArguments(args);
                return mInfoFragment;
            } else {
                mGotoFragment = new ShopGotoViewFragment();
                mGotoFragment.setArguments(args);
                return mGotoFragment;
            }
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0)
                return getString(R.string.shop_info);
            else
                return getString(R.string.itinerary);
        }


    }

}