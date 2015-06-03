package fr.lescavistes.lescavistes.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.google.android.gms.common.images.ImageManager;
import com.squareup.picasso.Picasso;

import java.net.URLEncoder;

import fr.lescavistes.lescavistes.MainApplication;
import fr.lescavistes.lescavistes.R;
import fr.lescavistes.lescavistes.activities.DisplayShopInfoActivity;
import fr.lescavistes.lescavistes.core.Shop;

/**
 * Created by Sylvain on 26/05/2015.
 */
public class ShopInfoViewFragment extends Fragment {

    private String TAG = "ShopInfoViewFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_info_shop, container, false);

        Shop shop = MainApplication.getModel().shopList.getSelected();

        TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);
        tvTitle.setText(shop.getName());

        TextView tvDistance = (TextView) v.findViewById(R.id.tvDistance);
        tvDistance.setText(String.valueOf(shop.getDist()) + " km");

        TextView tvAddress = (TextView) v.findViewById(R.id.tvAddress);
        tvAddress.setText(shop.getAddress());

        Button bMail = (Button) v.findViewById(R.id.bMail);
        if (shop.getEmail().length() != 0) {
            bMail.setText(shop.getEmail());
            bMail.setOnClickListener(new View.OnClickListener() {
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
            bMail.setVisibility(View.GONE);
        }

        Button bPhone = (Button) v.findViewById(R.id.bTel);
        if (shop.getPhone().length() != 0) {
            bPhone.setText(shop.getPhone());
            bPhone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button b = (Button) v;
                    String phno = "tel:" + b.getText().toString();

                    Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse(phno));
                    startActivity(i);
                }
            });
        } else {
            bPhone.setVisibility(View.GONE);
        }

        Button bWeb = (Button) v.findViewById(R.id.bWeb);
        if (shop.getWeb().length() != 0) {
            bWeb.setText(shop.getWeb());
            bWeb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button b = (Button) v;
                    String url = b.getText().toString();
                    if (!url.startsWith("http://") && !url.startsWith("https://"))
                        url = "http://" + url;

                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);
                }
            });
        } else {
            bWeb.setVisibility(View.GONE);
        }

        TextView tvDescription = (TextView) v.findViewById(R.id.tvDescription);
        tvDescription.setText(shop.getDescription());

        //get image from server
        final ImageView image = (ImageView) v.findViewById(R.id.ivImage);
        String get_url = MainApplication.baseUrl() + "getwineshopimage/" + String.valueOf(shop.getId())+'/'+getDPI()+'/'+ URLEncoder.encode(shop.getImg());
        Picasso.with(getActivity())
                .load(get_url)
                .placeholder(R.attr.indeterminateProgressStyle)
                .into(image);
        
        return v;
    }

    private String getDPI(){
        float density = getResources().getDisplayMetrics().density;
        if (density < 0.8)
            return "ldpi";
        if (density < 1.2)
            return "mdpi";
        if (density < 1.8)
            return "hdpi";
        else
            return "xhdpi";
    }
}
