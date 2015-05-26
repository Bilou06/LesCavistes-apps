package fr.lescavistes.lescavistes.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import fr.lescavistes.lescavistes.MainApplication;
import fr.lescavistes.lescavistes.R;
import fr.lescavistes.lescavistes.activities.DisplayShopInfoActivity;
import fr.lescavistes.lescavistes.core.Shop;

/**
 * Created by Sylvain on 26/05/2015.
 */
public class ShopInfoViewFragment extends Fragment {

    private Shop mShop;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_info_shop, container, false);

        if (getArguments() != null) {
            mShop = MainApplication.getModel().shopList.getSelected();

            TextView tvTitle = (TextView) v.findViewById(R.id.tvTitle);
            tvTitle.setText(mShop.getName());

            TextView tvDistance = (TextView) v.findViewById(R.id.tvDistance);
            tvDistance.setText(String.valueOf(mShop.getDist()) + " km");

            TextView tvAddress = (TextView) v.findViewById(R.id.tvAddress);
            tvAddress.setText(mShop.getAddress());

            Button bMail = (Button) v.findViewById(R.id.bMail);
            if (mShop.getEmail().length() != 0) {
                bMail.setText(mShop.getEmail());
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
            if (mShop.getPhone().length() != 0) {
                bPhone.setText(mShop.getPhone());
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
            if (mShop.getWeb().length() != 0) {
                bWeb.setText(mShop.getWeb());
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
            tvDescription.setText(mShop.getDescription());

            //get image from server
            final ImageView image = (ImageView) v.findViewById(R.id.ivImage);
            final ProgressBar progressBar = (ProgressBar) v.findViewById(R.id.ivImageProgress);

            String get_url = MainApplication.baseUrl() + "getwineshopimage/" + String.valueOf(mShop.getId());
            ImageRequest imageRequest = new ImageRequest(get_url,
                    new Response.Listener<Bitmap>() {

                        @Override
                        public void onResponse(Bitmap response) {

                            try {
                                image.setImageBitmap(response);
                            } catch (Exception e) {
                            }
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }, 0, 0, null,
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    }
            );

            // Add the request to the RequestQueue.
            MainApplication.getInstance().getRequestQueue().add(imageRequest);

        }
        return v;
    }
}
