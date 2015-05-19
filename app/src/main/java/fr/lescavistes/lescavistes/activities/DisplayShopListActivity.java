package fr.lescavistes.lescavistes.activities;


import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
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
        implements ShopListViewFragment.OnShopSelectedListener,
                    ShopMapViewFragment.OnShopSelectedListener{

    public static final String SHOPS_KEY = "SHOPS_KEY";
    public static final String LAT_KEY = "LAT_KEY";
    public static final String LNG_KEY = "LNG_KEY";
    public static final String SIZE_KEY = "SIZE_KEY";

    private static final String TAG = "Display Shop List";
    ShopsFragmentPagerAdapter mShopsFragmentPagerAdapter;
    ViewPager mViewPager;

    private ShopListViewFragment mListViewFragment;
    private ShopMapViewFragment mMapsViewFragment;

    private String mLat, mLng, mWhere, mWhat, mSize;
    private ArrayList mShopList;

    private Boolean mSwipeLayout;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_display_shop_list);

        Intent intent = getIntent();
        mWhere = intent.getStringExtra(SearchActivity.WHERE_MESSAGE);
        mWhat = intent.getStringExtra(SearchActivity.WHAT_MESSAGE);
        mLng = intent.getStringExtra(SearchActivity.LNG_MESSAGE);
        mLat = intent.getStringExtra(SearchActivity.LAT_MESSAGE);
        mShopList = (ArrayList) intent.getSerializableExtra(SearchActivity.SHOPS_MESSAGE);
        mSize = intent.getStringExtra(SearchActivity.NB_RESULTS);

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
        args.putSerializable(SHOPS_KEY, mShopList);
        args.putFloat(LAT_KEY, Float.parseFloat(mLat));
        args.putFloat(LNG_KEY, Float.parseFloat(mLng));
        args.putInt(SIZE_KEY, Integer.parseInt(mSize));

        if (mSwipeLayout) {
            mShopsFragmentPagerAdapter =
                    new ShopsFragmentPagerAdapter(getSupportFragmentManager());
            mShopsFragmentPagerAdapter.setContent(args);
            mViewPager.setAdapter(mShopsFragmentPagerAdapter);

        } else if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            mListViewFragment = new ShopListViewFragment();
            mListViewFragment.setArguments(args);
            transaction.add(R.id.list_shops, mListViewFragment);
            transaction.addToBackStack(null);
            transaction.commit();

            transaction = getSupportFragmentManager().beginTransaction();
            mMapsViewFragment = new ShopMapViewFragment();
            mMapsViewFragment.setArguments(args);
            transaction.add(R.id.map_shops, mMapsViewFragment);
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

        ShopListViewFragment listFrag = getListFragment();
        if (listFrag!=null) {listFrag.setSelected(id);}
        ShopMapViewFragment mapFrag = getMapFragment();
        if (mapFrag!=null) {
            mapFrag.setSelected(id);}
    }

    // Append more data into the adapter
    public void loadMoreDataFromApi(int offset) {
        // Request the shop list from the url.
        String get_url = MainApplication.baseUrl() + "getwineshops/?format=json&lat=" + mLat + "&lng=" + mLng + "&q=" + mWhat + "&c=" + String.valueOf(offset);
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

                            getListFragment().addContent(size, shopList);
                            getMapFragment().addContent(size, shopList);


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

    //helpers
    private ShopListViewFragment getListFragment() {
        if (mSwipeLayout) {
            return mShopsFragmentPagerAdapter.getListFragment();
        } else {
            return mListViewFragment;
        }
    }

    private ShopMapViewFragment getMapFragment() {
        if (mSwipeLayout) {
            return mShopsFragmentPagerAdapter.getMapFragment();
        } else {
            return mMapsViewFragment;
        }
    }


    /**
     * Created by Sylvain on 06/05/2015.
     */
    public static class ShopsFragmentPagerAdapter extends FragmentPagerAdapter {

        static final int NUM_ITEMS = 2;

        private Bundle args;

        private ShopListViewFragment mListFragment;
        private ShopMapViewFragment mMapFragment;

        public ShopsFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
            args = new Bundle();
        }

        public void setContent(Bundle args) {
            this.args = args;
        }

        @Override
        public Fragment getItem(int i) {

            if (i == 0) {
                mListFragment = new ShopListViewFragment();
                mListFragment.setArguments(args);
                return mListFragment;
            } else {
                mMapFragment = new ShopMapViewFragment();
                mMapFragment.setArguments(args);
                return mMapFragment;
            }
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == 0)
                return "Liste des magasins";
            else
                return "Carte des magasins";
        }

        public ShopListViewFragment getListFragment() {
            if (mListFragment!= null) return mListFragment;
            else return (ShopListViewFragment) getItem(0);
        }

        public ShopMapViewFragment getMapFragment() {
            if (mMapFragment!= null) return mMapFragment;
            else return (ShopMapViewFragment) getItem(1);
        }

    }

}



