/**
 * Created by Sylvain on 05/05/2015.
 */

package fr.lescavistes.lescavistes;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class Shop {

    private String name;
    private int id;
    private String address;
    private String phone;
    private String email;
    private String web;
    private String description;
    private Double lat, lng;
    private Double dist;

    //number of references
    private int nb;
    private Double price_min;
    private Double price_max;

    public Shop(JSONObject shop) {
        name = shop.optString("name", "nom inconnu");
        id = shop.optInt("id", 0);
        address = shop.optString("address");
        phone = shop.optString("phone");
        email = shop.optString("mail");
        web = shop.optString("web");
        description = shop.optString("desc");
        lat = shop.optDouble("lat");
        lng = shop.optDouble("lng");

        dist = shop.optDouble("dist", 0);
        nb = shop.optInt("nb", 0);
        JSONObject price = shop.optJSONObject("price");
        price_min = price.optDouble("price_min__min");
        price_max = price.optDouble("price_max__max");
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public Double getDist() {
        return dist;
    }

    public int getNbReferences() {
        return nb;
    }

    public Double getPrice_max() {
        return price_max;
    }

    public Double getPrice_min() {
        return price_min;
    }

    public String getAddress() {
        return address;
    }
}

