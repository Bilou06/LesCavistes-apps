/**
 * Created by Sylvain on 06/05/2015.
 */

package fr.lescavistes.lescavistes.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import fr.lescavistes.lescavistes.R;
import fr.lescavistes.lescavistes.activities.DisplayShopListActivity;
import fr.lescavistes.lescavistes.core.Shop;

public class ShopMapViewFragment extends Fragment {

    MapView mapView;
    GoogleMap map;

    private List<MarkerOptions> mMarkerOptions;
    private int size;
    private int selected;

    private float lat, lng;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map_shops, container, false);

        //read data
        if (mMarkerOptions == null) mMarkerOptions = new ArrayList<>();
        if (getArguments() != null) {
            lat = getArguments().getFloat(DisplayShopListActivity.LAT_KEY);
            lng = getArguments().getFloat(DisplayShopListActivity.LNG_KEY);
            size = getArguments().getInt(DisplayShopListActivity.SIZE_KEY);

            ArrayList<Shop> shopList = (ArrayList<Shop>) getArguments().getSerializable(DisplayShopListActivity.SHOPS_KEY);
            if (shopList != null)
                for (Shop shop : shopList) {
                    LatLng pos = new LatLng(shop.getLat(), shop.getLng());
                    mMarkerOptions.add(new MarkerOptions()
                            .position(pos)
                            .title(shop.getName()));
                }
        }

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) v.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        initMap();

        return v;
    }

    private void initMap() {
        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.setMyLocationEnabled(true);

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this.getActivity());

        map.addMarker(new MarkerOptions().position(new LatLng(lat, lng)));

        for(MarkerOptions m: mMarkerOptions){
            //map.addMarker(m.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            map.addMarker(m.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_small_location)));
        }

        // Updates the location and zoom of the MapView
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 14);
        map.moveCamera(cameraUpdate);
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
        this.size = size;

        for (Shop shop : shopList) {
            LatLng pos = new LatLng(shop.getLat(), shop.getLng());
            mMarkerOptions.add(new MarkerOptions()
                    .position(pos)
                    .title(shop.getName()));
        }
        for(MarkerOptions m: mMarkerOptions){
            map.addMarker(m.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_small_location)));
        }
    }

    public void setSelected(int position){
        if(position == selected)
            return;
        selected = position;

        if (selected>-1){

        }

    }
}
