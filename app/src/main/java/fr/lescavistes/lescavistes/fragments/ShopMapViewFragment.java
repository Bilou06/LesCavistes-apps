/**
 * Created by Sylvain on 06/05/2015.
 */

package fr.lescavistes.lescavistes.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.lescavistes.lescavistes.R;
import fr.lescavistes.lescavistes.activities.DisplayShopListActivity;
import fr.lescavistes.lescavistes.core.Shop;


public class ShopMapViewFragment extends Fragment {

    private MapView mapView;
    private GoogleMap map;
    private LatLngBounds.Builder bounds;

    OnShopSelectedListener mCallback;

    private List<Shop> mShops;
    private HashMap<Marker, Shop> shopsMarkerMap;
    private int mSize;
    private int mSelected;

    private float lat, lng;
    private Button leftButton, rigthButton;
    private TextView selectedView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (shopsMarkerMap == null)
            shopsMarkerMap = new HashMap<Marker, Shop>();

        mSelected = 0;
    }

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map_shops, container, false);

        //read data

        if (getArguments() != null) {
            lat = getArguments().getFloat(DisplayShopListActivity.LAT_KEY);
            lng = getArguments().getFloat(DisplayShopListActivity.LNG_KEY);
            mSize = getArguments().getInt(DisplayShopListActivity.SIZE_KEY);

            mShops = (ArrayList<Shop>) getArguments().getSerializable(DisplayShopListActivity.SHOPS_KEY);
        }

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) v.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        initMap();

        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        leftButton = (Button) getActivity().findViewById(R.id.left_button);
        rigthButton = (Button) getActivity().findViewById(R.id.right_button);
        selectedView = (TextView) getActivity().findViewById(R.id.selected_view);

        if (leftButton != null)
            leftButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if(mSelected == 0)
                        return;
                    mSelected--;
                    refresh();
                }
            });

        if (rigthButton != null)
            rigthButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if(mSelected >= mSize)
                        return;
                    mSelected++;

                    if(mSelected>=mShops.size()){
                        ((DisplayShopListActivity) getActivity()).loadMoreDataFromApi(mShops.size());
                        return;
                    }
                    refresh();
                }
            });

        updateButtons();
    }


    private void initMap() {
        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this.getActivity());

        // Updates the location and zoom of the MapView
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 14);
        map.moveCamera(cameraUpdate);

        setMarkers();
    }

    private void setMarkers() {
        //map.addMarker(new MarkerOptions().position(new LatLng(lat, lng)));

        bounds = new LatLngBounds.Builder();
        for (int i = 0; i < mShops.size(); i++) {
            Shop shop = mShops.get(i);
            LatLng pos = new LatLng(shop.getLat(), shop.getLng());
            bounds.include(pos);
            MarkerOptions m = new MarkerOptions()
                    .position(pos)
                    .title(shop.getName());
            Marker marker;
            if (i != mSelected) {
                //map.addMarker(m.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                marker = map.addMarker(m.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_small_location)));
            } else {
                marker = map.addMarker(m.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            }
            shopsMarkerMap.put(marker, shop);
        }

        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 50));
            }
        });

        //set listeners
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker arg0) {
                onClick(arg0);
                return true;
            }
        });
    }

    private void updateButtons() {

        if (leftButton != null)
            if (mSelected != 0) {
                leftButton.setEnabled(true);
                leftButton.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_left_enabled));
            } else {
                leftButton.setEnabled(false);
                leftButton.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_left));
            }

        if (selectedView != null && mShops != null && mSelected < mShops.size())
            selectedView.setText(mShops.get(mSelected).getName());

        if (rigthButton != null)
            if (mSelected != mSize-1) {
                rigthButton.setEnabled(true);
                rigthButton.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_right_enabled));
            } else {
                rigthButton.setEnabled(false);
                rigthButton.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_right));
            }
    }

    public void onClick(Marker m) {
        Shop shop = shopsMarkerMap.get(m);

        mSelected = mShops.indexOf(shop);

        refresh();

        mCallback.onShopSelected(mSelected);
    }

    private void refresh(){
        map.clear();
        setMarkers();
        updateButtons();
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    public void addContent(int size, ArrayList<Shop> shopList) {
        this.mSize = size;

        for (Shop shop : shopList) {
            mShops.add(shop);
        }

        refresh();
    }

    public void setSelected(int position) {
        mSelected = position;

    }


    //Container activity must implement this interface
    public interface OnShopSelectedListener {
        public void onShopSelected(int id);
    }
}
