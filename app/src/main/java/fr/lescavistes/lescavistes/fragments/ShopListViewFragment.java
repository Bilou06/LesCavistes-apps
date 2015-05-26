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
import fr.lescavistes.lescavistes.R;
import fr.lescavistes.lescavistes.activities.DisplayShopInfoActivity;
import fr.lescavistes.lescavistes.activities.DisplayShopListActivity;
import fr.lescavistes.lescavistes.core.Results;
import fr.lescavistes.lescavistes.core.SelectionChangedEvent;
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

    private static final String TAG = "List Fragment";

    OnShopSelectedListener mCallback;

    private List<Shop> mItems;        // ListView items list
    private float lat, lng;
    private String what;

    private ShopListAdapter mAdapter;

    EventBus bus = EventBus.getDefault();

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

        bus.register(this);

        if (mItems != null) {
            return;
        }

        mItems = new ArrayList<>();
        if (getArguments() != null) {

            ArrayList<Shop> shopList = (ArrayList<Shop>) getArguments().getSerializable(DisplayShopListActivity.SHOPS_KEY);
            if (shopList != null)
                for (Shop shop : shopList) {
                    mItems.add(shop);
                }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //read data
        if (getArguments() != null) {
            lat = getArguments().getFloat(DisplayShopListActivity.LAT_KEY);
            lng = getArguments().getFloat(DisplayShopListActivity.LNG_KEY);
            what = getArguments().getString(DisplayShopListActivity.WHAT_KEY);
        }

        return super.onCreateView(inflater, container, savedInstanceState);
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

                if (totalItemsCount - 1 >  mCallback.getmShops().size)
                    return;

                ((DisplayShopListActivity) getActivity()).loadMoreDataFromApi(totalItemsCount-2);
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (position > 0) {

            position = position - 1;//due to header

            if ( mCallback.getmShops().selected != position) {
                //tell the activity

                mCallback.onShopSelected(position);

                // retrieve theListView item
                Shop item = mItems.get(position);

                // change the layout
                v.setSelected(true);

                mAdapter.selectedItem(position);
                mAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            mItems = (List<Shop>) savedInstanceState.getSerializable(ITEMS);
        }

        //add header
        View v = getActivity().getLayoutInflater().inflate(R.layout.listview_shop_header, null);
        TextView tv = (TextView) v.findViewById(R.id.nbResults);
        ;
        if ( mCallback.getmShops().size == 0) {
            tv.setText("Aucun résultat");
        } else if (mCallback.getmShops().size == 1) {
            tv.setText("1 résultat");
        } else {
            tv.setText(String.valueOf(mCallback.getmShops().size) + " résultats");
        }

        this.getListView().addHeaderView(v);

        // initialize and set the list adapter

        mAdapter = new ShopListAdapter(getActivity(), mItems);
        mAdapter.setServerListSize(mCallback.getmShops().size);
        mAdapter.selectedItem(mCallback.getmShops().selected);
        setListAdapter(mAdapter);

        // select the element
        int selected = mCallback.getmShops().selected;
        if (selected > -1 && selected < mItems.size()) {
            Shop selectedItem = mItems.get(selected);
            if (selectedItem != null) {
                getListView().setSelection(selected);
                getListView().setItemChecked(selected, true);
                getListView().getAdapter().getView(selected, null, null).setSelected(true);
                getListView().requestFocus();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(ITEMS, (Serializable) mItems);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(mCallback!=null) {
            mAdapter.selectedItem(mCallback.getmShops().selected);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void addContent(int size, ArrayList<Shop> shopList) {
        if (mItems == null)
            mItems = new ArrayList<Shop>();
        // initialize the items list
        if (shopList != null)
            for (Shop shop : shopList) {
                mItems.add(shop);
            }

        int index = 0;
        int top = 0;
        if (getListView()!=null) {
            index = getListView().getFirstVisiblePosition();
            View v = getListView().getChildAt(0);
            top = (v == null) ? 0 : (v.getTop() - getListView().getPaddingTop());
        }
        mAdapter = new ShopListAdapter(getActivity(), mItems);
        mAdapter.setServerListSize(size);
        setListAdapter(mAdapter);

        if (getListView()!=null) {
            getListView().setSelectionFromTop(index, top);
        }
    }

    public void onEvent(SelectionChangedEvent event) {
        mAdapter.selectedItem(mCallback.getmShops().selected);
        mAdapter.notifyDataSetChanged();
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
        public Results getmShops();
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
                open.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), DisplayShopInfoActivity.class);

                        intent.putExtra(DisplayShopListActivity.LAT_MESSAGE, lat);
                        intent.putExtra(DisplayShopListActivity.LNG_MESSAGE, lng);

                        intent.putExtra(DisplayShopListActivity.WHAT_MESSAGE, what);
                        intent.putExtra(DisplayShopListActivity.SHOP_MESSAGE, item);

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