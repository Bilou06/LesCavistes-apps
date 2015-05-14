package fr.lescavistes.lescavistes.fragments;

import android.app.Activity;

//import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import fr.lescavistes.lescavistes.R;
import fr.lescavistes.lescavistes.activities.DisplayShopListActivity;
import fr.lescavistes.lescavistes.core.Shop;
import fr.lescavistes.lescavistes.utils.EndlessScrollListener;
import fr.lescavistes.lescavistes.utils.GenericAdapter;

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

        if (mItems != null) {
            return;
        }
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);

        return v;
    }


    public void addContent(int size, ArrayList<Shop> shopList) {
        if (mItems == null)
            mItems = new ArrayList<ShopListItem>();
        // initialize the items list
        if (shopList != null)
            for (Shop shop : shopList) {
                mItems.add(new ShopListItem(shop));
            }
        this.size=size;

        int index = getListView().getFirstVisiblePosition();
        View v = getListView().getChildAt(0);
        int top = (v == null) ? 0 : (v.getTop() - getListView().getPaddingTop());
        ShopListAdapter adapter = new ShopListAdapter(getActivity(), mItems);
        adapter.setServerListSize(size);
        setListAdapter(adapter);
        getListView().setSelectionFromTop(index, top);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // remove the dividers from the ListView of the ListFragment
        getListView().setDivider(null);

        // Attach the listener to the AdapterView in order to load data when needed
        getListView().setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list

                if (totalItemsCount == size)
                    return;

                ((DisplayShopListActivity) getActivity()).loadMoreDataFromApi(totalItemsCount);
            }
        });
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

        ShopListAdapter adapter = new ShopListAdapter(getActivity(), mItems);
        adapter.setServerListSize(size);
        setListAdapter(adapter);

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

    public static class ShopListAdapter extends GenericAdapter<ShopListItem> {

        public ShopListAdapter(Activity activity, List<ShopListItem> items) {
            super(activity, R.layout.listview_shop_item, items);
        }

        @Override
        public View getDataRow(int position, View convertView, ViewGroup parent){
        //public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                // inflate the GridView item layout
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.listview_shop_item, parent, false);

                // initialize the view holder
                viewHolder = new ViewHolder();
                viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
                viewHolder.tvDescription = (TextView) convertView.findViewById(R.id.tvDescription);
                viewHolder.tvAddress = (TextView) convertView.findViewById(R.id.tvAddress);
                convertView.setTag(viewHolder);
            } else {
                // recycle the already inflated view
                viewHolder = (ViewHolder) convertView.getTag();
            }

            // update the item view
            ShopListItem item = getItem(position);
            viewHolder.tvTitle.setText(item.title);
            viewHolder.tvDescription.setText(item.description);
            viewHolder.tvAddress.setText(item.address);

            return convertView;
        }



        /**
         * The view holder design pattern prevents using findViewById()
         * repeatedly in the getView() method of the adapter.
         *
         */
        private static class ViewHolder {
            ImageView ivIcon;
            TextView tvTitle;
            TextView tvDescription;
            TextView tvAddress;
        }
    }

    /**
     * Created by Sylvain on 05/05/2015.
     */
    public static class ShopListItem {

        public final String title;        // the text for the ListView item title
        public final String description;  // the text for the ListView item description
        public final String address;  // the text for the ListView item description
        public final int id;

        public ShopListItem(int id, String title, String description, String address) {
            this.title = title;
            this.description = description;
            this.address = address;
            this.id = id;
        }

        public ShopListItem(Shop shop) {
            this.title = shop.getName();
            this.description = String.valueOf(shop.getDist()) + " km";
            this.id = shop.getId();
            this.address = shop.getAddress();
        }
    }

    /**
     * Created by Sylvain on 06/05/2015.
     */
    public static class ShopsFragmentPagerAdapter extends FragmentPagerAdapter {

        static final int NUM_ITEMS = 2;

        private Bundle args;

        private ShopListViewFragment listFragment;
        private ShopMapViewFragment mapFragment;

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
                listFragment = new ShopListViewFragment();
                listFragment.setArguments(args);
                return listFragment;
            }
            else
            {
                mapFragment = new ShopMapViewFragment();
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

        public ShopListViewFragment getListFragment(){
            return listFragment;
        }

        public ShopMapViewFragment getMapFragment(){
            return mapFragment;
        }
    }
}