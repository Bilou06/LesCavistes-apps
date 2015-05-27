package fr.lescavistes.lescavistes.core;

import org.json.JSONObject;

import java.io.Serializable;

import fr.lescavistes.lescavistes.R;
import fr.lescavistes.lescavistes.utils.PriceFormat;

/**
 * Created by Sylvain on 26/05/2015.
 */
public class Wine implements Serializable {

    private String producer, country, region, area, classification, color, varietal;
    private int vintage;
    private Double capacity, price_min, price_max;


    public Wine(JSONObject wine) {
        producer = wine.optString("producer");
        country = wine.optString("country");
        region = wine.optString("region");
        area = wine.optString("area");
        classification = wine.optString("classification");
        color = wine.optString("color");
        varietal = wine.optString("varietal");
        vintage = wine.optInt("varietal");
        capacity = wine.optDouble("capacity");
        price_min = wine.optDouble("price_min");
        price_max = wine.optDouble("price_max");
    }

    public String getCapacity() {
        if (capacity !=0 && !capacity.isNaN())
            return String.valueOf(capacity)+ " cl";
        else
            return "";
    }


    public String getPrice(){
        if (price_max.isNaN() && price_min.isNaN()) {
            return "Prix inconnu";
        } else if (price_min.isNaN()) {
            return PriceFormat.format(price_max);
        } else if (price_max.isNaN()) {
            return PriceFormat.format(price_min);
        } else if (price_max == price_min) {
            return PriceFormat.format(price_max);
        } else {
            return String.format("de "+ PriceFormat.format(price_min) +" à " +PriceFormat.format(price_max));
        }

    }

    public String getVintage() {
        if (vintage !=0)
            return String.valueOf(vintage);
        else
            return "";
    }

    public String getArea() {
        return area;
    }

    public String getClassification() {
        return classification;
    }

    public String getColor() {
        return color;
    }

    public String getProducer() {
        return producer;
    }

    public String getCountry() {
        return country;
    }

    public String getRegion() {
        return region;
    }

    public String getVarietal() {
        return varietal;
    }
}
