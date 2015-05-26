package fr.lescavistes.lescavistes.core;

import java.io.Serializable;

/**
 * Created by Sylvain on 26/05/2015.
 */
public class Model implements Serializable {

    public Double lng, lat;
    public String where, what;
    public Results<Shop> shopList;
    public Results<Wine> wineList;

    public Model(){
        wineList = new Results<Wine>();
        shopList = new Results<Shop>();
    }

    public String getLat(){
        return String.valueOf(lat);
    }

    public String getLng(){
        return String.valueOf(lng);
    }
}
