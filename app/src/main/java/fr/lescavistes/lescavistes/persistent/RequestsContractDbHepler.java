package fr.lescavistes.lescavistes.persistent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import fr.lescavistes.lescavistes.persistent.RequestsContract.RequestWhat;

/**
 * Created by Sylvain on 02/06/2015.
 */
public class RequestsContractDbHepler extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "Requests.db";

    public RequestsContractDbHepler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public Cursor getMostUsedQueries(){
        SQLiteDatabase db = getReadableDatabase();

        String[] projection ={
                RequestWhat._ID,
                RequestWhat.COLUMN_NAME_QUERY,
                RequestWhat.COLUMN_NAME_COUNT
        };

        String sortOrder =
                RequestWhat.COLUMN_NAME_COUNT + " DESC";

        Cursor c = db.query(
                RequestWhat.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        return c;
    }

    public Cursor getMostRecentQueries(String filter){
        SQLiteDatabase db = getReadableDatabase();

        String[] projection ={
                RequestWhat._ID,
                RequestWhat.COLUMN_NAME_QUERY,
                RequestWhat.COLUMN_NAME_DATE
        };

        String sortOrder =
                RequestWhat.COLUMN_NAME_DATE + " DESC";

        String selection = RequestWhat.COLUMN_NAME_QUERY + " LIKE?";
        String[] selectionArgs = {'%'+filter+'%'};

        Cursor c = db.query(
                RequestWhat.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        return c;
    }

    public Cursor getMostRecentQueries(){
        SQLiteDatabase db = getReadableDatabase();

        String[] projection ={
                RequestWhat._ID,
                RequestWhat.COLUMN_NAME_QUERY,
                RequestWhat.COLUMN_NAME_DATE
        };

        String sortOrder =
                RequestWhat.COLUMN_NAME_DATE + " DESC";

        Cursor c = db.query(
                RequestWhat.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        return c;
    }


    public void addOrUpdateEntry(String query){
        SQLiteDatabase db = getReadableDatabase();

        String[] projection ={
                RequestWhat._ID,
                RequestWhat.COLUMN_NAME_QUERY,
                RequestWhat.COLUMN_NAME_COUNT
        };

        String selection = RequestWhat.COLUMN_NAME_QUERY + "=?";
        String[] selectionArgs = {query};

        Cursor c = db.query(
                RequestWhat.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        ContentValues values = new ContentValues();
        values.put(RequestWhat.COLUMN_NAME_DATE, System.currentTimeMillis());

        if(c.moveToFirst()){
            //update
            values.put(RequestWhat.COLUMN_NAME_COUNT, c.getLong(c.getColumnIndex(RequestWhat.COLUMN_NAME_COUNT))+1);

            db.update(
                    RequestWhat.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs);
        }else{
            //insert
            values.put(RequestWhat.COLUMN_NAME_COUNT, 1);
            values.put(RequestWhat.COLUMN_NAME_QUERY, query);

            db.insert(
                    RequestWhat.TABLE_NAME,
                    null,
                    values);
        }


    }


    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + RequestWhat.TABLE_NAME + " (" +
                    RequestWhat._ID + " INTEGER PRIMARY KEY," +
                    RequestWhat.COLUMN_NAME_QUERY + TEXT_TYPE + COMMA_SEP +//the position of this column matters! it is used for an adapter
                    RequestWhat.COLUMN_NAME_COUNT + INTEGER_TYPE + COMMA_SEP +
                    RequestWhat.COLUMN_NAME_DATE + INTEGER_TYPE +
            " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + RequestWhat.TABLE_NAME;
}
