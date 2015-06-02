package fr.lescavistes.lescavistes.persistent;

import android.provider.BaseColumns;

/**
 * Created by Sylvain on 02/06/2015.
 */
public final class RequestsContract {

    public RequestsContract(){}

    public static abstract class RequestWhat implements BaseColumns {
        public static final String TABLE_NAME = "what_request";
        public static final String COLUMN_NAME_QUERY = "query";
        public static final String COLUMN_NAME_COUNT = "count";
    }
}
