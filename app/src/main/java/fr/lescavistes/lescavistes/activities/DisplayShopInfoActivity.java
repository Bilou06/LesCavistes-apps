package fr.lescavistes.lescavistes.activities;

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

import java.net.URLEncoder;
import java.util.ArrayList;

import fr.lescavistes.lescavistes.MainApplication;
import fr.lescavistes.lescavistes.R;
import fr.lescavistes.lescavistes.core.Model;
import fr.lescavistes.lescavistes.core.Wine;
import fr.lescavistes.lescavistes.fragments.SearchDialogFragment;
import fr.lescavistes.lescavistes.fragments.ShopGotoViewFragment;
import fr.lescavistes.lescavistes.fragments.ShopInfoGotoViewFragment;
import fr.lescavistes.lescavistes.fragments.ShopInfoViewFragment;
import fr.lescavistes.lescavistes.fragments.WineListViewFragment;
import fr.lescavistes.lescavistes.utils.JSONObjectUtf8;

import static android.text.Html.escapeHtml;


/**
 * Created by Sylvain on 26/05/2015.
 */
public class DisplayShopInfoActivity extends AppCompatActivity implements SearchDialogFragment.SearchDialogListener {

    private ShopFragmentPagerAdapter mShopFragmentPagerAdapter;
    private ViewPager mViewPager;
    private Menu menu;

    private Model model;


    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_display_shop_info);

        model = MainApplication.getModel();

        mViewPager = (ViewPager) findViewById(R.id.pager);
        final Boolean bSmallLayout = (mViewPager != null);
        if (!bSmallLayout)
            mViewPager = (ViewPager) findViewById(R.id.pager_large);


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

        // Add tabs
        ActionBar.Tab tab;
        tab = actionBar.newTab()
                .setIcon(R.drawable.ic_action_info)
                .setTabListener(tabListener);
        actionBar.addTab(tab);

        if (bSmallLayout) {
            tab = actionBar.newTab()
                    .setIcon(R.drawable.ic_tab_goto)
                    .setTabListener(tabListener);
            actionBar.addTab(tab);
        }

        tab = actionBar.newTab()
                .setIcon(R.drawable.ic_tab_wine_list)
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


        // Create the content fragments : map and list
        mShopFragmentPagerAdapter =
                new ShopFragmentPagerAdapter(getSupportFragmentManager());
        mShopFragmentPagerAdapter.setLayout(bSmallLayout);
        mViewPager.setAdapter(mShopFragmentPagerAdapter);

        mViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (!bSmallLayout) {
                    if (position == 1)
                        menu.setGroupVisible(0, true);
                    else
                        menu.setGroupVisible(0, false);
                } else {
                    if (position == 2)
                        menu.setGroupVisible(0, true);
                    else
                        menu.setGroupVisible(0, false);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        //loadMoreDataFromApi(0);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_search:
                openSearch();
                return true;
            case R.id.action_filter:
                openFilter();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_shop_info_actions, menu);
        boolean result = super.onCreateOptionsMenu(menu);
        this.menu = menu;
        menu.setGroupVisible(0, false);
        return result;
    }

    private void openSearch() {
        SearchDialogFragment dialog = new SearchDialogFragment();
        dialog.show(getSupportFragmentManager(), "SearchDialogFragment");
    }

    private void openFilter() {

    }


    // Append more data into the adapter
    public void loadMoreDataFromApi(final int offset) {
        // Request the wine list from the url.
        String get_url = MainApplication.baseUrl() + "getwines/?format=json&shop=" + String.valueOf(model.shopList.getSelected().getId()) + "&q=" + URLEncoder.encode(model.getWhat()) + "&c=" + String.valueOf(offset);
        JsonArrayRequest jsonRequest = new JsonArrayRequest(get_url,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {

                        try {
                            // Parsing json array response
                            synchronized (model.getWineList()) {
                                model.getWineList().size = Integer.parseInt(response.get(0).toString());

                                ArrayList<Wine> newWines = new ArrayList<Wine>();
                                for (int i = 1; i < response.length(); i++) {

                                    JSONObjectUtf8 jsonWine = new JSONObjectUtf8((JSONObject) response.get(i));
                                    Wine wine = new Wine(jsonWine);

                                    model.getWineList().items.add(wine);
                                    newWines.add(wine);
                                }
                                mShopFragmentPagerAdapter.getWineListFragment().refresh();
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

    public void onSearchClick(SearchDialogFragment dialog){
        search(dialog.getQuery());
    }

    private void search(String query) {
        if (!model.getWhat().equals(query)) {
            model.setWhat(query);
            loadMoreDataFromApi(0);
        }
    }


    public class ShopFragmentPagerAdapter extends FragmentPagerAdapter {

        private boolean smallLayout;
        private WineListViewFragment wineListViewFragment;

        public ShopFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public void setLayout(boolean small) {
            smallLayout = small;
        }

        @Override
        public Fragment getItem(int i) {
            if (smallLayout)
                switch (i) {
                    case 0:
                        return new ShopInfoViewFragment();
                    case 1:
                        return new ShopGotoViewFragment();
                    default: //beware to getWineListFragment if you modify this
                        wineListViewFragment = new WineListViewFragment();
                        return wineListViewFragment;
                }
            else
                switch (i) {
                    case 0:
                        return  new ShopInfoGotoViewFragment();
                    default: //beware to getWineListFragment if you modify this
                        wineListViewFragment = new WineListViewFragment();
                        return wineListViewFragment;
                }
        }

        @Override
        public int getCount() {
            if (smallLayout)
                return 3;
            else
                return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (smallLayout)
            switch (position) {
                case 0:
                    return getString(R.string.shop_info);
                case 1:
                    return getString(R.string.itinerary);
                default:
                    return "Liste des vins";
            }
            else
                switch (position) {
                    case 0:
                        return getString(R.string.shop_info);
                    default:
                        return "Liste des vins";
                }
        }


        public WineListViewFragment getWineListFragment() {
            if (wineListViewFragment != null) return wineListViewFragment;
            else return (WineListViewFragment) getItem(2);
        }
    }
}