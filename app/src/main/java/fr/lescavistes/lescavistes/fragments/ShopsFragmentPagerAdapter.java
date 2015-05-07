package fr.lescavistes.lescavistes.fragments;

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

    private Bundle args;

    public ShopsFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
        args = new Bundle();
    }

    public void setContent(Bundle args){
        this.args = args;
    }

    @Override
    public Fragment getItem(int i) {

        if(i == 0) {
            ShopListViewFragment listFragment = new ShopListViewFragment();
            listFragment.setArguments(args);
            return listFragment;
        }
        else
        {
            ShopMapViewFragment mapFragment = new ShopMapViewFragment();
            mapFragment.setArguments(args);
            return mapFragment;
        }
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if(position==0)
            return "Liste des magasins";
        else
            return "Carte des magasins";
    }
}