package fr.lescavistes.lescavistes.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import fr.lescavistes.lescavistes.R;
import fr.lescavistes.lescavistes.activities.DisplayShopListActivity;

/**
 * Created by Sylvain on 06/05/2015.
 */
public class ShopMapsViewFragment extends Fragment {

    private static GoogleMap map;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle saved) {
        if (group == null) {
            return null;
        }

        View v = (RelativeLayout) inflater.inflate(R.layout.fragment_map_shops, group, false);

        DisplayShopListActivity a = (DisplayShopListActivity) getActivity();
        FragmentManager fm = a.getSupportFragmentManager();

        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.location_map);
        map = mapFragment.getMap();

        Marker hamburg = map.addMarker(new MarkerOptions().position(new LatLng(53.558, 9.927))
                .title("Hamburg"));
        Marker kiel = map.addMarker(new MarkerOptions()
                .position(new LatLng(53.551, 9.993))
                .title("Kiel")
                .snippet("Kiel is cool")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.ic_tab_location)));

        // Move the camera instantly to hamburg with a zoom of 15.
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(53.558, 9.927), 15));

        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);

        return v;
    }

    /**** The mapfragment's id must be removed from the FragmentManager
     **** or else if the same it is passed on the next time then
     **** app will crash ****/
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (map != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .remove(getActivity().getSupportFragmentManager().findFragmentById(R.id.location_map)).commit();
            map = null;
        }
    }

}
