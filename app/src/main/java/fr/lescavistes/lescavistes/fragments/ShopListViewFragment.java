package fr.lescavistes.lescavistes.fragments;

import android.app.Activity;

//import android.app.ListFragment;
import android.os.Bundle;

import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.lescavistes.lescavistes.core.Shop;

/**
 * Created by Sylvain on 05/05/2015.
 */
public class ShopListViewFragment extends ListFragment {

    private static final String ITEMS = "items";
    OnShopSelectedListener mCallback;
    private List<ShopListItem> mItems;        // ListView items list

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnShopSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnShopSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mItems == null) {
            mItems = new ArrayList<ShopListItem>();
            if (getArguments() != null) {
                ArrayList<Shop> shopList = (ArrayList<Shop>) getArguments().getSerializable("SHOPS");
                if (shopList != null)
                    for (Shop shop : shopList) {
                        mItems.add(new ShopListItem(shop));
                    }
            }
        }

        // initialize and set the list adapter
        setListAdapter(new ShopListAdapter(getActivity(), mItems));
    }

    public void setContent(ArrayList<Shop> shopList) {
        if (mItems == null)
            mItems = new ArrayList<ShopListItem>();
        // initialize the items list
        if (shopList != null)
            for (Shop shop : shopList) {
                mItems.add(new ShopListItem(shop));
            }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // remove the dividers from the ListView of the ListFragment
        getListView().setDivider(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // retrieve theListView item
        ShopListItem item = mItems.get(position);

        // Send the event to the host activity
        mCallback.onShopSelected(item.id);

        // do something
        Toast.makeText(getActivity(), item.title, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            mItems = (List<ShopListItem>) savedInstanceState.getSerializable(ITEMS);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(ITEMS, (Serializable) mItems);
    }

    //Container activity must implement this interface
    public interface OnShopSelectedListener {
        public void onShopSelected(int id);
    }
}