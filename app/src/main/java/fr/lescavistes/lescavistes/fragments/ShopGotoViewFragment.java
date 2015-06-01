package fr.lescavistes.lescavistes.fragments;

import android.graphics.AvoidXfermode;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.lescavistes.lescavistes.MainApplication;
import fr.lescavistes.lescavistes.R;
import fr.lescavistes.lescavistes.activities.DisplayShopInfoActivity;
import fr.lescavistes.lescavistes.activities.DisplayShopListActivity;
import fr.lescavistes.lescavistes.core.Model;
import fr.lescavistes.lescavistes.core.Shop;

/**
 * Created by Sylvain on 26/05/2015.
 */

public class ShopGotoViewFragment extends Fragment {

    private static final String TAG = "GOTO Fragment";
    private MapView mapView;
    private GoogleMap map;
    private Model model;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        model = MainApplication.getModel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_goto_shop, container, false);

        // Gets the MapView from the XML layout and creates it
        mapView = (MapView) v.findViewById(R.id.mapview);
        mapView.onCreate(savedInstanceState);

        initMap();

        return v;
    }


    private void initMap() {
        // Gets to GoogleMap from the MapView and does initialization stuff
        map = mapView.getMap();
        if (map != null || !MainApplication.isDebug()) {
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.setMyLocationEnabled(true);

            // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
            MapsInitializer.initialize(this.getActivity());

            // Updates the location and zoom of the MapView
            CameraUpdate cameraUpdate;
            synchronized (model) {
                cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(model.lat, model.lng), 14);
            }
            map.moveCamera(cameraUpdate);


            setMarkers();

            setPath();
        }
    }

    private void setPath(){
        String url;
        synchronized (model) {
            Shop shop = model.shopList.getSelected();

            synchronized (shop) {
                url = makepathURL(model.lat, model.lng, shop.getLat(), shop.getLng());
            }
        }

        StringRequest stringRequest = new StringRequest(url,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        try {
                            drawPath(response);

                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(),
                                    getActivity().getString(R.string.impossible_to_draw_path),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError || error instanceof AuthFailureError || error instanceof ServerError || error instanceof ParseError) {
                    Toast.makeText(getActivity(),
                            getActivity().getString(R.string.impossible_to_draw_path),
                            Toast.LENGTH_LONG).show();
                } else if (error instanceof NoConnectionError || error instanceof NetworkError) {
                    Toast.makeText(getActivity(),
                            getActivity().getString(R.string.connection_error),
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        // Add the request to the RequestQueue.
        MainApplication.getInstance().getRequestQueue().add(stringRequest);

    }

    private void setMarkers() {
        //map.addMarker(new MarkerOptions().position(new LatLng(lat, lng)));

        final LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        synchronized (model) {
            bounds.include(new LatLng(model.lat, model.lng));
        }

        Shop shop = model.shopList.getSelected();
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

    private String makepathURL (double sourcelat, double sourcelog, double destlat, double destlog ){
        StringBuilder urlString = new StringBuilder();
        urlString.append("http://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString
                .append(Double.toString( sourcelog));
        urlString.append("&destination=");// to
        urlString
                .append(Double.toString( destlat));
        urlString.append(",");
        urlString.append(Double.toString(destlog));
        urlString.append("&sensor=false&mode=walking&alternatives=true");
        return urlString.toString();
    }

    private void drawPath(String result) {

        try {
            //Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);

            for(int z = 0; z<list.size()-1;z++){
                LatLng src= list.get(z);
                LatLng dest= list.get(z+1);
                Polyline line = map.addPolyline(new PolylineOptions()
                        .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude,   dest.longitude))
                        .width(2)
                        .color(Color.BLUE).geodesic(true));
            }

        }
        catch (JSONException e) {

        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }

        return poly;
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

