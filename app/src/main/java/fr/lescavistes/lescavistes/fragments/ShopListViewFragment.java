package fr.lescavistes.lescavistes.fragments;

import android.app.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import fr.lescavistes.lescavistes.MainApplication;
import fr.lescavistes.lescavistes.R;
import fr.lescavistes.lescavistes.activities.DisplayShopInfoActivity;
import fr.lescavistes.lescavistes.activities.DisplayShopListActivity;
import fr.lescavistes.lescavistes.core.Model;
import fr.lescavistes.lescavistes.core.Results;
import fr.lescavistes.lescavistes.core.SelectionChangedEvent;
import fr.lescavistes.lescavistes.core.Shop;
import fr.lescavistes.lescavistes.utils.EndlessScrollListener;
import fr.lescavistes.lescavistes.utils.GenericAdapter;
import fr.lescavistes.lescavistes.utils.PriceFormat;

/**
 * Created by Sylvain on 05/05/2015.
 */
public class ShopListViewFragment extends ListFragment {

    private static final String TAG = "Shop List Fragment";

    OnShopSelectedListener mCallback;
    EventBus bus = EventBus.getDefault();
    private Model model;
    private ShopListAdapter mAdapter;
    boolean viewCreated, refreshRequested;

    private TextView headertv;

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

        refreshRequested = false;
        viewCreated = false;
        model = MainApplication.getModel();
        bus.register(this);
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

                if (totalItemsCount - 1 > model.shopList.size)
                    return;

                ((DisplayShopListActivity) getActivity()).loadMoreDataFromApi(totalItemsCount - 2);
            }
        });

        viewCreated = true;
        if(refreshRequested)
            refresh();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (position > 0) {

            position = position - 1;//due to header
            synchronized (model.shopList) {
                if (model.shopList.selected != position) {
                    //tell the activity

                    mCallback.onShopSelected(position);

                    // retrieve theListView item
                    Shop item = model.shopList.items.get(position);

                    // change the layout
                    v.setSelected(true);

                    mAdapter.selectedItem(position);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // initialize and set the list adapter
        synchronized (model.shopList) {
            mAdapter = new ShopListAdapter(getActivity(), model.shopList.items);
            mAdapter.setServerListSize(model.shopList.size);
            mAdapter.selectedItem(model.shopList.selected);
            setListAdapter(mAdapter);


            // select the element
            int selected = model.shopList.selected;
            if (selected > -1 && selected < model.shopList.items.size()) {
                Shop selectedItem = model.shopList.items.get(selected);
                if (selectedItem != null) {
                    getListView().setSelection(selected);
                    getListView().setItemChecked(selected, true);
                    getListView().getAdapter().getView(selected, null, null).setSelected(true);
                    getListView().requestFocus();
                }
            }
        }

        //add header
        View v = getActivity().getLayoutInflater().inflate(R.layout.listview_shop_header, null);
        headertv = (TextView) v.findViewById(R.id.nbResults);
        updateHeader();
        this.getListView().addHeaderView(headertv);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mCallback != null) {
            mAdapter.selectedItem(model.shopList.selected);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void refresh() {

        if(!viewCreated) {
            refreshRequested = true;
            return;
        }
        int index = 0;
        int top = 0;
        if (getListView() != null) {
            index = getListView().getFirstVisiblePosition();
            View v = getListView().getChildAt(0);
            top = (v == null) ? 0 : (v.getTop() - getListView().getPaddingTop());
        }
        synchronized (model.shopList) {
            mAdapter = new ShopListAdapter(getActivity(), model.shopList.items);
            mAdapter.setServerListSize(model.shopList.size);
            setListAdapter(mAdapter);
        }

        if (getListView() != null) {
            getListView().setSelectionFromTop(index, top);
            updateHeader();
        }

        refreshRequested=false;
    }

    private void updateHeader(){
        switch (model.shopList.size) {
            case -1:
                headertv.setText(R.string.Loading);
                break;
            case 0:
                headertv.setText(R.string.no_result);
                break;
            case 1:
                headertv.setText(R.string.one_result);
                break;
            default:
                headertv.setText(String.format("%d résultats", model.shopList.size));
                break;
        }
    }


    public void onEvent(SelectionChangedEvent event) {
        synchronized (model.shopList) {
            mAdapter.selectedItem(model.shopList.selected);
            mAdapter.notifyDataSetChanged();
        }
    }

    //Container activity must implement this interface
    public interface OnShopSelectedListener {
        public void onShopSelected(int id);
    }


    public class ShopListAdapter extends GenericAdapter<Shop> {

        private int mSelected;

        public ShopListAdapter(Activity activity, List<Shop> items) {
            super(activity, R.layout.listview_shop_item, items);
            mSelected = -1;
        }

        public void selectedItem(int selected) {
            this.mSelected = selected;
        }

        @Override
        public View getDataRow(int position, View convertView, ViewGroup parent) {

            ViewHolder viewHolder;
            LayoutInflater inflater = LayoutInflater.from(getContext());

            if (position == mSelected) {
                viewHolder = new SelectedViewHolder();
                convertView = inflater.inflate(R.layout.listview_selected_shop_item, parent, false);
            } else {
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.listview_shop_item, parent, false);
            }

            // initialize the view holder
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            viewHolder.tvDistance = (TextView) convertView.findViewById(R.id.tvDistance);
            viewHolder.tvAddress = (TextView) convertView.findViewById(R.id.tvAddress);
            viewHolder.tvNbReferences = (TextView) convertView.findViewById(R.id.nbReferences);
            viewHolder.tvPrice = (TextView) convertView.findViewById(R.id.price);

            // update the item view
            Shop item = getItem(position);
            viewHolder.tvTitle.setText(item.getName());
            viewHolder.tvDistance.setText(item.getDistance());
            viewHolder.tvAddress.setText(item.getAddress());

            if (model.getWhat().length() != 0) {
                switch (item.getNbReferences()) {
                    case 0:
                        viewHolder.tvNbReferences.setText(mActivity.getString(R.string.no_result));
                        break;
                    case 1:
                        viewHolder.tvNbReferences.setText(mActivity.getString(R.string.one_result));
                        break;
                    default:
                        viewHolder.tvNbReferences.setText(String.valueOf(item.getNbReferences()) + mActivity.getString(R.string.results));
                        break;
                }


                if (item.getPrice_max().isNaN() && item.getPrice_min().isNaN()) {
                    viewHolder.tvPrice.setText("Prix inconnu");
                } else if (item.getPrice_min().isNaN()) {
                    viewHolder.tvPrice.setText(PriceFormat.format(item.getPrice_max()));
                } else if (item.getPrice_max().isNaN()) {
                    viewHolder.tvPrice.setText(PriceFormat.format(item.getPrice_min()));
                } else if (item.getPrice_max() == item.getPrice_min()) {
                    viewHolder.tvPrice.setText(PriceFormat.format(item.getPrice_max()));
                } else {
                    viewHolder.tvPrice.setText("de " + PriceFormat.format(item.getPrice_min()) + " à " + PriceFormat.format(item.getPrice_max()));
                }

            } else {
                viewHolder.tvNbReferences.setVisibility(View.GONE);
                viewHolder.tvPrice.setVisibility(View.GONE);
            }


            if (position == mSelected) {

                // initialize the view holder
                SelectedViewHolder selectedViewHolder = (SelectedViewHolder) viewHolder;
                selectedViewHolder.bMail = (Button) convertView.findViewById(R.id.bMail);
                selectedViewHolder.bPhone = (Button) convertView.findViewById(R.id.bTel);

                // update the item view
                if (item.getEmail().length() != 0) {
                    selectedViewHolder.bMail.setText(item.getEmail());
                    selectedViewHolder.bMail.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Button b = (Button) v;
                            String to = b.getText().toString();

                            Intent i = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                    "mailto", to, null));
                            startActivity(i);
                        }
                    });
                } else {
                    selectedViewHolder.bMail.setVisibility(View.GONE);
                }

                if (item.getPhone().length() != 0) {
                    selectedViewHolder.bPhone.setText(item.getPhone());
                    selectedViewHolder.bPhone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Button b = (Button) v;
                            String phno = "tel:" + b.getText().toString();

                            Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse(phno));
                            startActivity(i);
                        }
                    });
                } else {
                    selectedViewHolder.bPhone.setVisibility(View.GONE);
                }

                Button open = (Button) convertView.findViewById(R.id.bOpen);
                open.setEnabled(true);
                open.setClickable(true);
                open.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), DisplayShopInfoActivity.class);
                        startActivity(intent);
                    }
                });
            }
            convertView.setTag(viewHolder);
            return convertView;
        }


        /**
         * The view holder design pattern prevents using findViewById()
         * repeatedly in the getView() method of the adapter.
         */
        private class ViewHolder {

            TextView tvTitle;
            TextView tvDistance;
            TextView tvAddress;
            TextView tvNbReferences;
            TextView tvPrice;
        }

        private class SelectedViewHolder extends ViewHolder {
            ImageView ivIcon;
            Button bMail;
            Button bPhone;
        }
    }


}