package fr.lescavistes.lescavistes.core;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Sylvain on 26/05/2015.
 */
public class Wine implements Serializable {

    private String name;

    public Wine(JSONObject wine) {
        name = wine.optString("name");
    }
}
