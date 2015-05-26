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

import java.util.HashMap;

import de.greenrobot.event.EventBus;
import fr.lescavistes.lescavistes.R;
import fr.lescavistes.lescavistes.activities.DisplayShopListActivity;
import fr.lescavistes.lescavistes.core.Results;
import fr.lescavistes.lescavistes.core.SelectionChangedEvent;
import fr.lescavistes.lescavistes.core.Shop;


public class ShopMapViewFragment extends Fragment {

    OnShopSelectedListener mCallback;
    private MapView mapView;
    private GoogleMap map;
    private LatLngBounds.Builder bounds;
    private boolean boundsSet;
    private HashMap<Marker, Shop> shopsMarkerMap;

    private float lat, lng;
    private Button leftButton, rightButton;
    private TextView selectedView;

    private static final String TAG = "Map Fragment";

    EventBus bus = EventBus.getDefault();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bus.register(this);

        if (shopsMarkerMap == null)
            shopsMarkerMap = new HashMap<Marker, Shop>();

        boundsSet = false;
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
        rightButton = (Button) getActivity().findViewById(R.id.right_button);
        selectedView = (TextView) getActivity().findViewById(R.id.selected_view);

        if (leftButton != null)
            leftButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    int selected = mCallback.getmShops().selected;
                    if (selected == 0)
                        return;
                    selected--;

                    mCallback.onShopSelected(selected);
                }
            });

        if (rightButton != null)
            rightButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    int selected = mCallback.getmShops().selected;
                    if (selected >= mCallback.getmShops().size)
                        return;
                    selected++;

                    if (selected >= mCallback.getmShops().items.size()) {
                        ((DisplayShopListActivity) getActivity()).loadMoreDataFromApi(mCallback.getmShops().items.size());
                        return;
                    }

                    mCallback.onShopSelected(selected);
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

        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                bounds = new LatLngBounds.Builder();
                bounds.include(map.getProjection().getVisibleRegion().latLngBounds.northeast);
                bounds.include(map.getProjection().getVisibleRegion().latLngBounds.southwest);
            }
        });

        setMarkers();
    }

    private void setMarkers() {
        //map.addMarker(new MarkerOptions().position(new LatLng(lat, lng)));

        boolean camToUpdate = false;
        if (bounds == null) {
            bounds = new LatLngBounds.Builder();
            bounds.include(new LatLng(lat, lng));
            camToUpdate = true;
        }
        LatLngBounds previousBounds = bounds.build();

        for (int i = 0; i < mCallback.getmShops().items.size(); i++) {
            Shop shop = (Shop)mCallback.getmShops().items.get(i);
            LatLng pos = new LatLng(shop.getLat(), shop.getLng());
            MarkerOptions m = new MarkerOptions()
                    .position(pos)
                    .title(shop.getName());
            Marker marker;
            if (i != mCallback.getmShops().selected) {
                //map.addMarker(m.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                marker = map.addMarker(m.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_small_location)));
                if (!boundsSet) {
                    camToUpdate = camToUpdate || !previousBounds.contains(pos);
                    bounds.include(pos);

                }
            } else {
                marker = map.addMarker(m.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                camToUpdate = camToUpdate || !previousBounds.contains(pos);
                bounds.include(pos);
                boundsSet = true;
            }
            shopsMarkerMap.put(marker, shop);
        }

        if (camToUpdate)
            map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 20));
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

        int selected = mCallback.getmShops().selected;

        if (leftButton != null)
            if (selected != 0) {
                leftButton.setEnabled(true);
                leftButton.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_left_enabled));
            } else {
                leftButton.setEnabled(false);
                leftButton.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_left));
            }

        if (selectedView != null && mCallback.getmShops().items != null && selected < mCallback.getmShops().items.size())
            selectedView.setText(((Shop) mCallback.getmShops().items.get(selected)).getName());

        if (rightButton != null)
            if (selected != mCallback.getmShops().size - 1) {
                rightButton.setEnabled(true);
                rightButton.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_right_enabled));
            } else {
                rightButton.setEnabled(false);
                rightButton.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.ic_action_right));
            }
    }

    public void onClick(Marker m) {
        Shop shop = shopsMarkerMap.get(m);
        mCallback.onShopSelected(mCallback.getmShops().items.indexOf(shop));
    }

    public void refresh() {
        map.clear();
        setMarkers();
        updateButtons();
    }

    public void onEvent(SelectionChangedEvent event) {
        refresh();
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


    //Container activity must implement this interface
    public interface OnShopSelectedListener {
        public void onShopSelected(int id);
        public Results getmShops();
    }
}
