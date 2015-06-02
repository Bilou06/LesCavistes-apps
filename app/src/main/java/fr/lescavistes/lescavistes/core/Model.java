package fr.lescavistes.lescavistes.core;

import android.os.AsyncTask;
import android.widget.Toast;

import java.io.Serializable;
import java.util.HashMap;

import fr.lescavistes.lescavistes.MainApplication;
import fr.lescavistes.lescavistes.persistent.RequestsContractDbHepler;

/**
 * Created by Sylvain on 26/05/2015.
 */
public class Model implements Serializable {

    public volatile Double lng, lat;
    public volatile String where;
    public volatile Results<Shop> shopList;
    private volatile String what;
    private volatile HashMap<QueryData, Results<Wine>> wineLists;

    public Model() {
        wineLists = new HashMap<QueryData, Results<Wine>>();
        shopList = new Results<Shop>();
    }

    public String getLat() {
        return String.valueOf(lat);
    }

    public String getLng() {
        return String.valueOf(lng);
    }

    public String getWhat() {
        return what;
    }

    public synchronized void setWhat(final String what) {
        this.what = what;
        if (what != null && what.length() != 0)
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    RequestsContractDbHepler dbHepler = new RequestsContractDbHepler(MainApplication.getInstance());
                    dbHepler.addOrUpdateEntry(what);
                    return null;
                }
            }.execute();
    }

    public synchronized Results<Wine> getWineList() {
        QueryData data = new QueryData();
        data.shop = shopList.selected;
        data.what = what;
        if (wineLists.containsKey(data))
            return wineLists.get(data);

        Results<Wine> wineList = new Results<Wine>();
        wineLists.put(data, wineList);
        return wineList;
    }

    private class QueryData {
        public int shop;
        public String what;

        @Override
        public int hashCode() {
            return what.hashCode() + shop;
        }

        //Compare only account numbers
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            QueryData other = (QueryData) obj;
            return other.shop == shop && other.what == what;
        }
    }

}
