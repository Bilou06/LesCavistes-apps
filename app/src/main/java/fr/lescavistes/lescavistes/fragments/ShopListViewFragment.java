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

/**
 * Created by Sylvain on 05/05/2015.
 */
public class ShopListViewFragment extends ListFragment {

    private static final String TAG = "List Fragment";

    OnShopSelectedListener mCallback;
    EventBus bus = EventBus.getDefault();
    private Model model;
    private ShopListAdapter mAdapter;

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

        //add header
        View v = getActivity().getLayoutInflater().inflate(R.layout.listview_shop_header, null);
        TextView tv = (TextView) v.findViewById(R.id.nbResults);
        switch (model.shopList.size) {
            case 0:
                tv.setText("Aucun résultat");
                break;
            case 1:
                tv.setText("1 résultat");
                break;
            default:
                tv.setText(String.valueOf(model.shopList.size) + " résultats");
                break;
        }

        this.getListView().addHeaderView(v);

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
        }
    }

    public void onEvent(SelectionChangedEvent event) {
        synchronized (model.shopList) {
            mAdapter.selectedItem(model.shopList.selected);
            mAdapter.notifyDataSetChanged();
        }
    }

    private View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition) {
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

            if (position != mSelected) {
                ViewHolder viewHolder;
                if (convertView == null || convertView.getTag() instanceof SelectedViewHolder) {
                    // inflate the GridView item layout
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    convertView = inflater.inflate(R.layout.listview_shop_item, parent, false);

                    // initialize the view holder
                    viewHolder = new ViewHolder();
                    viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
                    viewHolder.tvDistance = (TextView) convertView.findViewById(R.id.tvDistance);
                    viewHolder.tvAddress = (TextView) convertView.findViewById(R.id.tvAddress);
                    convertView.setTag(viewHolder);
                } else {
                    // recycle the already inflated view
                    viewHolder = (ViewHolder) convertView.getTag();
                }

                // update the item view
                Shop item = getItem(position);
                viewHolder.tvTitle.setText(item.getName());
                viewHolder.tvDistance.setText(item.getDistance());
                viewHolder.tvAddress.setText(item.getAddress());

                return convertView;
            } else {
                SelectedViewHolder selectedViewHolder;
                // inflate the GridView item layout
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.listview_selected_shop_item, parent, false);

                // initialize the view holder
                selectedViewHolder = new SelectedViewHolder();
                selectedViewHolder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
                selectedViewHolder.tvDistance = (TextView) convertView.findViewById(R.id.tvDistance);
                selectedViewHolder.tvAddress = (TextView) convertView.findViewById(R.id.tvAddress);
                selectedViewHolder.bMail = (Button) convertView.findViewById(R.id.bMail);
                selectedViewHolder.bPhone = (Button) convertView.findViewById(R.id.bTel);
                convertView.setTag(selectedViewHolder);

                // update the item view
                final Shop item = getItem(position);
                selectedViewHolder.tvTitle.setText(item.getName());
                selectedViewHolder.tvDistance.setText(item.getDistance());
                selectedViewHolder.tvAddress.setText(item.getAddress());

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
            TextView tvDistance;
            TextView tvAddress;
        }


        private class SelectedViewHolder extends ViewHolder {
            Button bMail;
            Button bPhone;

        }

    }


}