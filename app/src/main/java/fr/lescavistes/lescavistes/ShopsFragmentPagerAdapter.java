package fr.lescavistes.lescavistes;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Sylvain on 06/05/2015.
 */
public class ShopsFragmentPagerAdapter extends FragmentPagerAdapter {

    static final int NUM_ITEMS = 2;


    public ShopsFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    public void setContent(ArrayList content){
        shopList = content;
    }

    private ArrayList shopList;

    @Override
    public Fragment getItem(int i) {
        Bundle args = new Bundle();
        args.putSerializable("SHOPS", (Serializable) shopList);
        if(i == 0) {
            ShopListViewFragment listFragment = new ShopListViewFragment();

            listFragment.setContent(shopList);
            listFragment.setArguments(args);
            //listFragment.setContent(shopList);
            return listFragment;
        }
        else
        {
            ShopListViewFragment mapFragment = new ShopListViewFragment();

            mapFragment.setContent(shopList);
            mapFragment.setArguments(args);
            //mapFragment.setContent(shopList);
            return mapFragment;
        }
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "OBJECT " + (position + 1);
    }
}