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
import fr.lescavistes.lescavistes.core.Shop;

public class ShopMapsViewFragment extends Fragment {

    MapView mapView;
    GoogleMap map;

    private List<MarkerOptions> mMarkerOptions;
    private float lat, lng;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map_shops, container, false);

        //read data
        if (mMarkerOptions == null)
            mMarkerOptions = new ArrayList<MarkerOptions>();
        if (getArguments() != null) {
            lat = getArguments().getFloat("LAT");
            lng = getArguments().getFloat("LNG");

            ArrayList<Shop> shopList = (ArrayList<Shop>) getArguments().getSerializable("SHOPS");
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

        LatLngBounds.Builder bounds = new LatLngBounds.Builder();

        // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
        MapsInitializer.initialize(this.getActivity());

        map.addMarker(new MarkerOptions().position(new LatLng(lat, lng)));
        bounds.include(new LatLng(lat, lng));

        for(MarkerOptions m: mMarkerOptions){
            map.addMarker(m.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            bounds.include(m.getPosition());
        }


        // Updates the location and zoom of the MapView
        //LatLngBounds b = bounds.build();
        //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(b, 50);
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
}
