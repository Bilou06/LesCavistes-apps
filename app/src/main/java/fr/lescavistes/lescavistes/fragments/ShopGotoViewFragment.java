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
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import fr.lescavistes.lescavistes.R;
import fr.lescavistes.lescavistes.activities.DisplayShopInfoActivity;
import fr.lescavistes.lescavistes.activities.DisplayShopListActivity;
import fr.lescavistes.lescavistes.core.Shop;

/**
 * Created by Sylvain on 26/05/2015.
 */

public class ShopGotoViewFragment extends Fragment {

    private static final String TAG = "GOTO Fragment";
    private MapView mapView;
    private GoogleMap map;
    private float lat, lng;
    private Shop shop;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map_shops, container, false);

        //read data

        if (getArguments() != null) {
            lat = getArguments().getFloat(DisplayShopInfoActivity.LAT_KEY);
            lng = getArguments().getFloat(DisplayShopInfoActivity.LNG_KEY);
            shop = (Shop)getArguments().getSerializable(DisplayShopInfoActivity.SHOP_KEY);
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

        // Updates the location and zoom of the MapView
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 14);
        map.moveCamera(cameraUpdate);

        setMarkers();
    }

    private void setMarkers() {
        //map.addMarker(new MarkerOptions().position(new LatLng(lat, lng)));

        final LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        bounds.include(new LatLng(lat, lng));

        LatLng pos = new LatLng(shop.getLat(), shop.getLng());
        MarkerOptions m = new MarkerOptions()
                .position(pos)
                .title(shop.getName());
        Marker marker = map.addMarker(m.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        bounds.include(pos);


        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 20));
            }
        });

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

