package fr.lescavistes.lescavistes.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import fr.lescavistes.lescavistes.MainApplication;
import fr.lescavistes.lescavistes.R;
import fr.lescavistes.lescavistes.activities.DisplayShopInfoActivity;
import fr.lescavistes.lescavistes.core.Model;
import fr.lescavistes.lescavistes.core.Shop;
import fr.lescavistes.lescavistes.core.Wine;
import fr.lescavistes.lescavistes.utils.EndlessScrollListener;
import fr.lescavistes.lescavistes.utils.GenericAdapter;

/**
 * Created by Sylvain on 26/05/2015.
 */
public class WineListViewFragment extends ListFragment {
    private static final String TAG = "Wine List Fragment";

    private Model model;
    private WineListAdapter mAdapter;

    private TextView headertv;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        model = MainApplication.getModel();
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

                if (totalItemsCount - 1 > model.getWineList().size)
                    return;

                ((DisplayShopInfoActivity) getActivity()).loadMoreDataFromApi(totalItemsCount - 2);
            }
        });
        if(model.getWineList().size<1)
            ((DisplayShopInfoActivity) getActivity()).loadMoreDataFromApi(0);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (position > 0) {

            position = position - 1;//due to header
            synchronized (model.getWineList()) {
                if (model.getWineList().selected != position) {
                    model.getWineList().selected = position;

                    // retrieve theListView item
                    Wine item = model.getWineList().items.get(position);

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
        View v = getActivity().getLayoutInflater().inflate(R.layout.listview_wine_header, null);
        headertv = (TextView) v.findViewById(R.id.nbResults);
        updateHeader();
        try {
            this.getListView().addHeaderView(v);
        }
        catch(IllegalStateException e){}

        // initialize and set the list adapter

        synchronized (model.getWineList()) {
            mAdapter = new WineListAdapter(getActivity(), model.getWineList().items);
            mAdapter.setServerListSize(model.getWineList().size);
            mAdapter.selectedItem(model.getWineList().selected);
            setListAdapter(mAdapter);


            // select the element
            int selected = model.getWineList().selected;
            if (selected > -1 && selected < model.getWineList().items.size()) {
                Wine selectedItem = model.getWineList().items.get(selected);
                if (selectedItem != null) {
                    getListView().setSelection(selected);
                    getListView().setItemChecked(selected, true);
                    getListView().getAdapter().getView(selected, null, null).setSelected(true);
                    getListView().requestFocus();
                }
            }
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
        synchronized (model.getWineList()) {
            mAdapter = new WineListAdapter(getActivity(), model.getWineList().items);
            mAdapter.setServerListSize(model.getWineList().size);
            setListAdapter(mAdapter);
        }

        if (getListView() != null) {
            getListView().setSelectionFromTop(index, top);
        }
        updateHeader();
    }

    private void updateHeader(){
        Shop shop = model.shopList.getSelected();
        switch (shop.getNbReferences()) {
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
                headertv.setText(String.format(getActivity().getString(R.string.results), shop.getNbReferences()));
                break;
        }
    }

    public class WineListAdapter extends GenericAdapter<Wine> {

        private int mSelected;

        public WineListAdapter(Activity activity, List<Wine> items) {
            super(activity, R.layout.listview_wine_item, items);
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
                convertView = inflater.inflate(R.layout.listview_selected_wine_item, parent, false);
            } else {
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.listview_wine_item, parent, false);
            }

            // initialize the view holder
            viewHolder.producer = (TextView) convertView.findViewById(R.id.Producer);
            viewHolder.capacity = (TextView) convertView.findViewById(R.id.capacity);
            viewHolder.area = (TextView) convertView.findViewById(R.id.area);
            viewHolder.vintage = (TextView) convertView.findViewById(R.id.vintage);
            viewHolder.regionCountry = (TextView) convertView.findViewById(R.id.Region_Country);
            viewHolder.color = (TextView) convertView.findViewById(R.id.color);
            viewHolder.price = (TextView) convertView.findViewById(R.id.price);

            // update the item view
            Wine item = getItem(position);
            if(item.getProducer().length()!= 0)
                viewHolder.producer.setText(item.getProducer());
            else
                viewHolder.producer.setVisibility(View.GONE);

            if(item.getCapacity().length()!=0)
                viewHolder.capacity.setText(item.getCapacity());
            else
                viewHolder.capacity.setVisibility(View.GONE);

            if(item.getArea().length()!=0)
                viewHolder.area.setText(item.getArea());
            else
                viewHolder.area.setVisibility(View.GONE);

            if(item.getVintage().length()!=0)
                viewHolder.vintage.setText(item.getVintage());
            else
                viewHolder.vintage.setVisibility(View.GONE);

            if(item.getRegion().length()!=0 && item.getCountry().length()!=0)
                viewHolder.regionCountry.setText(item.getRegion() + " - " + item.getCountry());
            else
                if(item.getRegion().length()!=0)
                    viewHolder.regionCountry.setText(item.getRegion());
            else
                if(item.getCountry().length()!=0)
                    viewHolder.regionCountry.setText(item.getCountry());
            else
                    viewHolder.regionCountry.setVisibility(View.GONE);

            if(item.getColor().length()!=0)
                viewHolder.color.setText(item.getColor());
            else
                viewHolder.color.setVisibility(View.GONE);

            if(item.getPrice().length()!=0)
                viewHolder.price.setText(item.getPrice());
            else
                viewHolder.price.setVisibility(View.GONE);

            if (position == mSelected) {

                // initialize the view holder
                SelectedViewHolder selectedViewHolder = (SelectedViewHolder) viewHolder;
                selectedViewHolder.classification = (TextView) convertView.findViewById(R.id.classification);
                selectedViewHolder.varietal = (TextView) convertView.findViewById(R.id.varietal);

                if(item.getClassification().length()!=0)
                    selectedViewHolder.classification.setText(item.getClassification());
                else
                    selectedViewHolder.classification.setVisibility(View.GONE);

                if(item.getVarietal().length()!=0)
                    selectedViewHolder.varietal.setText(item.getVarietal());
                else
                    selectedViewHolder.varietal.setVisibility(View.GONE);

            }
            convertView.setTag(viewHolder);
            return convertView;
        }


        /**
         * The view holder design pattern prevents using findViewById()
         * repeatedly in the getView() method of the adapter.
         */
        private class ViewHolder {
            TextView producer;
            TextView capacity;
            TextView area;
            TextView vintage;
            TextView regionCountry;
            TextView color;
            TextView price;
        }

        private class SelectedViewHolder extends ViewHolder {
            TextView classification;
            TextView varietal;
        }
    }


}
