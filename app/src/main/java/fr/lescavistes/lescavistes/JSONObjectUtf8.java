package fr.lescavistes.lescavistes;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by Sylvain on 06/05/2015.
 */
public class JSONObjectUtf8 extends JSONObject {

    public JSONObjectUtf8(JSONObject parent) throws JSONException {
        super(parent.toString());
    }

    @Override
    public String optString(String name) {
        String ret = super.optString(name);
        try {
            ret=  new String(ret.getBytes("ISO-8859-1"),"UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        return ret;
    }
}
