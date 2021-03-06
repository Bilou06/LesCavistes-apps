/**
 * Created by Sylvain on 04/05/2015.
 */

package fr.lescavistes.lescavistes;

import android.app.Application;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import fr.lescavistes.lescavistes.core.Model;


public class MainApplication extends Application {

    private static MainApplication sInstance;
    private static boolean DEBUG = true;
    private static String URL = "http://192.168.0.12:8181/";

    private RequestQueue mRequestQueue;

    private static Model model;

    @Override
    public void onCreate() {
        super.onCreate();

        mRequestQueue = Volley.newRequestQueue(this);

        model = new Model();

        sInstance = this;
    }

    public synchronized static MainApplication getInstance() {
        return sInstance;
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public static boolean isDebug() { return DEBUG; }

    public static String baseUrl() {return URL;}

    public synchronized static Model getModel(){
        return model;
    }
}