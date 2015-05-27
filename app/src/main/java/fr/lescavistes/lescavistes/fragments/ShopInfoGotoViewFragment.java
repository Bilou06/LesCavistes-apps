package fr.lescavistes.lescavistes.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fr.lescavistes.lescavistes.MainApplication;
import fr.lescavistes.lescavistes.R;
import fr.lescavistes.lescavistes.core.Model;

/**
 * Created by Sylvain on 27/05/2015.
 */
public class ShopInfoGotoViewFragment extends Fragment {


    private static final String TAG = "Fragment Shop Info Goto";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_info_goto_shop, container, false);


        // Create the content fragments : map and info

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            ShopInfoViewFragment mInfoViewFragment = new ShopInfoViewFragment();
            transaction.add(R.id.info_shop, mInfoViewFragment);
            transaction.addToBackStack(null);
            transaction.commit();

            transaction = getChildFragmentManager().beginTransaction();
            ShopGotoViewFragment mGotoViewFragment = new ShopGotoViewFragment();
            transaction.add(R.id.goto_shop, mGotoViewFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }

        return v;
    }
}

