package fr.lescavistes.lescavistes.fragments;

import android.app.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    private static final String ITEMS = "ITEMS";
    private static final String SIZE = "SIZE";

    OnShopSelectedListener mCallback;
    private List<ShopListItem> mItems;        // ListView items list
    private ShopListAdapter mAdapter;

    private int mSelected;
    private int mSize;

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

        mSelected = -1;
        mItems = new ArrayList<>();
        if (getArguments() != null) {
            mSize = getArguments().getInt(DisplayShopListActivity.SIZE_KEY);

            ArrayList<Shop> shopList = (ArrayList<Shop>) getArguments().getSerializable(DisplayShopListActivity.SHOPS_KEY);
            if (shopList != null)
                for (Shop shop : shopList) {
                    mItems.add(new ShopListItem(shop));
                }
        }

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // remove the dividers from the ListView of the ListFragment
        getListView().setDivider(null);

        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        // Attach the listener to the AdapterView in order to load data when needed
        getListView().setOnScrollListener(new EndlessScrollListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                // Triggered only when new data needs to be appended to the list

                if (totalItemsCount > mSize)
                    return;

                ((DisplayShopListActivity) getActivity()).loadMoreDataFromApi(totalItemsCount);
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (position > 0) {

            position = position - 1;//due to header

            if(mSelected != position) {
                //tell the activity

                mCallback.onShopSelected(position);

                // retrieve theListView item
                ShopListItem item = mItems.get(position);

                mSelected = position;

                // change the layout
                v.setSelected(true);

                mAdapter.selectedItem(position);
                mAdapter.notifyDataSetChanged();

                // do something
                Toast.makeText(getActivity(), item.title, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            mItems = (List<ShopListItem>) savedInstanceState.getSerializable(ITEMS);
            mSelected = savedInstanceState.getInt(SELECTED);
            mSize = savedInstanceState.getInt(SIZE);
        }

        //add header
        View v = getActivity().getLayoutInflater().inflate(R.layout.listview_shop_header, null);
        TextView tv = (TextView) v.findViewById(R.id.nbResults);
        ;
        if (mSize == 0) {
            tv.setText("Aucun résultat");
        } else if (mSize == 1) {
            tv.setText("1 résultat");
        } else {
            tv.setText(String.valueOf(mSize) + " résultats");
        }

        this.getListView().addHeaderView(v);

        // initialize and set the list adapter

        mAdapter = new ShopListAdapter(getActivity(), mItems);
        mAdapter.setServerListSize(mSize);
        mAdapter.selectedItem(mSelected);
        setListAdapter(mAdapter);

        // select the element
        if (mSelected > -1 && mSelected < mItems.size()) {
            ShopListItem selectedItem = mItems.get(mSelected);
            if (selectedItem != null) {
                getListView().setSelection(mSelected);
                getListView().setItemChecked(mSelected, true);
                getListView().getAdapter().getView(mSelected, null, null).setSelected(true);
                getListView().requestFocus();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED, mSelected);
        outState.putSerializable(ITEMS, (Serializable) mItems);
        outState.putInt(SIZE, mSize);
    }

    public void addContent(int size, ArrayList<Shop> shopList) {
        if (mItems == null)
            mItems = new ArrayList<ShopListItem>();
        // initialize the items list
        if (shopList != null)
            for (Shop shop : shopList) {
                mItems.add(new ShopListItem(shop));
            }
        this.mSize = size;

        int index = getListView().getFirstVisiblePosition();
        View v = getListView().getChildAt(0);
        int top = (v == null) ? 0 : (v.getTop() - getListView().getPaddingTop());
        mAdapter = new ShopListAdapter(getActivity(), mItems);
        mAdapter.setServerListSize(size);
        setListAdapter(mAdapter);
        getListView().setSelectionFromTop(index, top);
    }

    public void setSelected(int position) {
        if (position == mSelected)
            return;

        //set new selection
        mSelected = position;
    }

    private View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

    //Container activity must implement this interface
    public interface OnShopSelectedListener {
        public void onShopSelected(int id);
    }

    public class ShopListAdapter extends GenericAdapter<ShopListItem> {

        private int mSelected;

        public ShopListAdapter(Activity activity, List<ShopListItem> items) {
            super(activity, R.layout.listview_shop_item, items);
            mSelected = -1;
        }

        public void selectedItem(int selected) {
            this.mSelected = selected;
        }

        @Override
        public View getDataRow(int position, View convertView, ViewGroup parent) {

            if (position != mSelected) {
                ViewHolder viewHolder;
                if (convertView == null || convertView.getTag() instanceof SelectedViewHolder) {
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
            } else {
                SelectedViewHolder selectedViewHolder;
                // inflate the GridView item layout
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.listview_selected_shop_item, parent, false);

                // initialize the view holder
                selectedViewHolder = new SelectedViewHolder();
                selectedViewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
                selectedViewHolder.tvDescription = (TextView) convertView.findViewById(R.id.tvDescription);
                selectedViewHolder.tvAddress = (TextView) convertView.findViewById(R.id.tvAddress);
                selectedViewHolder.bMail = (Button) convertView.findViewById(R.id.bMail);
                selectedViewHolder.bPhone = (Button) convertView.findViewById(R.id.bTel);
                convertView.setTag(selectedViewHolder);

                // update the item view
                ShopListItem item = getItem(position);
                selectedViewHolder.tvTitle.setText(item.title);
                selectedViewHolder.tvDescription.setText(item.description);
                selectedViewHolder.tvAddress.setText(item.address);

                if(item.email.length() != 0){
                    selectedViewHolder.bMail.setText(item.email);
                    selectedViewHolder.bMail.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Button b = (Button)v;
                            String to = b.getText().toString();

                            Intent i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                    "mailto", to, null));
                            startActivity(i);
                        }
                    });
                } else {
                    selectedViewHolder.bMail.setVisibility(View.GONE);
                }

                if(item.phone.length() != 0){
                    selectedViewHolder.bPhone.setText(item.phone);
                    selectedViewHolder.bPhone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Button b = (Button)v;
                            String phno = "tel:"+b.getText().toString();

                            Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse(phno));
                            startActivity(i);
                        }
                    });
                } else {
                    selectedViewHolder.bPhone.setVisibility(View.GONE);
                }

                return convertView;

            }
        }


        /**
         * The view holder design pattern prevents using findViewById()
         * repeatedly in the getView() method of the adapter.
         */
        private class ViewHolder {
            ImageView ivIcon;
            TextView tvTitle;
            TextView tvDescription;
            TextView tvAddress;
        }


        private class SelectedViewHolder extends ViewHolder {
            Button  bMail;
            Button bPhone;

        }

    }

    /**
     * Created by Sylvain on 05/05/2015.
     */
    public static class ShopListItem implements Serializable {

        public final String title;        // the text for the ListView item title
        public final String description;  // the text for the ListView item description
        public final String address;  // the text for the ListView item description
        public final int id;
        public final String email;
        public final String phone;

        public ShopListItem(Shop shop) {
            this.title = shop.getName();
            this.description = String.valueOf(shop.getDist()) + " km";
            this.id = shop.getId();
            this.address = shop.getAddress();
            this.email = shop.getEmail();
            this.phone = shop.getPhone();
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
            return mListFragment;
        }

        public ShopMapViewFragment getMapFragment() {
            return mMapFragment;
        }
    }
}