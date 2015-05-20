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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;
import fr.lescavistes.lescavistes.R;
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

    private List<ShopListItem> mItems;        // ListView items list
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

                if (totalItemsCount - 1 >  mCallback.getResults().size)
                    return;

                ((DisplayShopListActivity) getActivity()).loadMoreDataFromApi(totalItemsCount-2);
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (position > 0) {

            position = position - 1;//due to header

            if ( mCallback.getResults().selected != position) {
                //tell the activity

                mCallback.onShopSelected(position);

                // retrieve theListView item
                ShopListItem item = mItems.get(position);


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
            mItems = (List<ShopListItem>) savedInstanceState.getSerializable(ITEMS);
        }

        //add header
        View v = getActivity().getLayoutInflater().inflate(R.layout.listview_shop_header, null);
        TextView tv = (TextView) v.findViewById(R.id.nbResults);
        ;
        if ( mCallback.getResults().size == 0) {
            tv.setText("Aucun résultat");
        } else if (mCallback.getResults().size == 1) {
            tv.setText("1 résultat");
        } else {
            tv.setText(String.valueOf(mCallback.getResults().size) + " résultats");
        }

        this.getListView().addHeaderView(v);

        // initialize and set the list adapter

        mAdapter = new ShopListAdapter(getActivity(), mItems);
        mAdapter.setServerListSize(mCallback.getResults().size);
        mAdapter.selectedItem(mCallback.getResults().selected);
        setListAdapter(mAdapter);

        // select the element
        int selected = mCallback.getResults().selected;
        if (selected > -1 && selected < mItems.size()) {
            ShopListItem selectedItem = mItems.get(selected);
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
            mAdapter.selectedItem(mCallback.getResults().selected);
            mAdapter.notifyDataSetChanged();
        }
    }

    public void addContent(int size, ArrayList<Shop> shopList) {
        if (mItems == null)
            mItems = new ArrayList<ShopListItem>();
        // initialize the items list
        if (shopList != null)
            for (Shop shop : shopList) {
                mItems.add(new ShopListItem(shop));
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
        mAdapter.selectedItem(mCallback.getResults().selected);
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
        public Results getResults();
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

                if (item.email.length() != 0) {
                    selectedViewHolder.bMail.setText(item.email);
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

                if (item.phone.length() != 0) {
                    selectedViewHolder.bPhone.setText(item.phone);
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
            Button bMail;
            Button bPhone;

        }

    }


}