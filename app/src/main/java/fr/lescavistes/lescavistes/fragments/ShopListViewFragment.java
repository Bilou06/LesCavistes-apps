package fr.lescavistes.lescavistes.fragments;

import android.app.Activity;

//import android.app.ListFragment;
import android.os.Bundle;

import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.lescavistes.lescavistes.R;
import fr.lescavistes.lescavistes.activities.DisplayShopListActivity;
import fr.lescavistes.lescavistes.core.Shop;

/**
 * Created by Sylvain on 05/05/2015.
 */
public class ShopListViewFragment extends ListFragment {

    private static final String SELECTED = "SELECTED";
    private static final String ITEMS = "items";
    private static final String SIZE = "size";

    OnShopSelectedListener mCallback;
    private List<ShopListItem> mItems;        // ListView items list

    private int selected;
    private int size;

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
                size = getArguments().getInt(DisplayShopListActivity.SIZE_KEY);

                ArrayList<Shop> shopList = (ArrayList<Shop>) getArguments().getSerializable(DisplayShopListActivity.SHOPS_KEY);
                if (shopList != null)
                    for (Shop shop : shopList) {
                        mItems.add(new ShopListItem(shop));
                    }
            }
        }

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
        if (position>0) {
            position = position - 1;//due to header
            // retrieve theListView item
            ShopListItem item = mItems.get(position);

            // Send the event to the host activity
            mCallback.onShopSelected(item.id);
            selected = position;

            // change the layout
            v.setSelected(true);

            // do something
            Toast.makeText(getActivity(), item.title, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            mItems = (List<ShopListItem>) savedInstanceState.getSerializable(ITEMS);
            selected = savedInstanceState.getInt(SELECTED);
            size = savedInstanceState.getInt(SIZE);
        }

        //add header
        View v = getActivity().getLayoutInflater().inflate(R.layout.listview_shop_header, null);
        TextView tv = (TextView) v.findViewById(R.id.nbResults);
;
        if (size == 0){
            tv.setText("Aucun résultat");
        } else if (size == 1){
            tv.setText("1 résultat");
        } else {
            tv.setText(String.valueOf(size) + " résultats");
        }



        this.getListView().addHeaderView(v);

        // initialize and set the list adapter
        setListAdapter(new ShopListAdapter(getActivity(), mItems));

        if (selected>-1){
            ShopListItem selectedItem = mItems.get(selected);
            if(selectedItem != null){
                getListView().setSelection(selected);
                getListView().setItemChecked(selected, true);
                getListView().getAdapter().getView(selected, null, null).setSelected(true);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED, selected);
        outState.putSerializable(ITEMS, (Serializable) mItems);
        outState.putInt(SIZE, size);
    }

    //Container activity must implement this interface
    public interface OnShopSelectedListener {
        public void onShopSelected(int id);
    }
}