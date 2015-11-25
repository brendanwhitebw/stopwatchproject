package com.eagerbeavers.stopwatch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class BusDB {
    public static final String KEY_ROWID = "_id";
    public static final String KEY_Stop = "stop";
    public static final String KEY_Lat = "latitude";
    public static final String KEY_Long = "longitude";

    //private static final String DATABASE_NAME = "busroutes";

    private static final int DATABASE_VERSION = 1;

    private DbHelper ourHelper;
    private final Context ourContext;
    private SQLiteDatabase ourDatabase;
    private static String RouteN;

    private static class DbHelper extends SQLiteOpenHelper {
        public DbHelper(Context context) {
            super(context, RouteN, null, DATABASE_VERSION); // creates new database for each new route name passed in
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + RouteN + "(" +
                            KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            KEY_Stop + " TEXT NOT NULL, " +
                            KEY_Lat + " REAL NOT NULL, " +
                            KEY_Long + " REAL NOT NULL);"
            );
        } // table name an database name the same

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + RouteN);
            onCreate(db);
        }

    }

    public BusDB (Context c, String Routen){
        ourContext = c;
        RouteN = Routen; // takes in route name
    }

    public BusDB open() throws SQLException {
        ourHelper = new DbHelper(ourContext);
        ourDatabase = ourHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        ourHelper.close();
    }


    public long createEntry(String stop, double lat, double lng) {

        ContentValues cv = new ContentValues();
        cv.put(KEY_Stop, stop);
        cv.put(KEY_Lat, lat);
        cv.put(KEY_Long, lng);
        return ourDatabase.insert(RouteN, null, cv); // creates new row with dta passed in

    }

    public int rowCount(String route) {
        Cursor mCount = ourDatabase.rawQuery("select count(*) from " + route, null);
        mCount.moveToFirst();
        int count = mCount.getInt(0);// gets number of rows in table
        mCount.close();
        return count;
    }

    public String getData() {
        // TODO Auto-generated method stub
        String[] columns = new String[]{ KEY_ROWID, KEY_Stop, KEY_Lat, KEY_Long};
        Cursor c = ourDatabase.query(RouteN, columns, null, null, null, null, null);
        String result = "";

        int iRow = c.getColumnIndex(KEY_ROWID);
        int iStop = c.getColumnIndex(KEY_Stop);

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
            result = result + c.getString(iRow) + " " + c.getString(iStop) + "\n";
        }

        return result;
    }


    public String getStop(long l) throws SQLException{
        // TODO Auto-generated method stub
        String[] columns = new String[]{ KEY_ROWID, KEY_Stop, KEY_Lat, KEY_Long};
        Cursor c = ourDatabase.query(RouteN, columns, KEY_ROWID + "=" + l, null, null, null, null);
        // selects which row by number passed in (key_ROWID)
        if (c != null){
            c.moveToFirst();
            String stopName = c.getString(1);  // points cursor to key_stop(stop name)
            return stopName;
        }
        return null;
    }

    public double getLat(long l) throws SQLException {

        String[] columns = new String[]{ KEY_ROWID, KEY_Stop, KEY_Lat, KEY_Long};
        Cursor c = ourDatabase.query(RouteN, columns, KEY_ROWID + "=" + l, null, null, null, null);
        // selects which row by number passed in (key_ROWID)
        if (c != null){
            c.moveToFirst();
            double lattitude = c.getDouble(2);  // points cursor to key_stop(stop name)
            return lattitude;
        }
        return 0;
    }

    public double getLong(long l) throws SQLException {
        String[] columns = new String[]{ KEY_ROWID, KEY_Stop, KEY_Lat, KEY_Long};
        Cursor c = ourDatabase.query(RouteN, columns, KEY_ROWID + "=" + l, null, null, null, null);
        // selects which row by number passed in (key_ROWID)
        if (c != null){
            c.moveToFirst();
            double longtitude = c.getDouble(3);  // points cursor to key_stop(stop name)
            return longtitude;
        }
        return 0;
    }

    public void updateEntry(long lRow, String mStop, double mlat, double mlong) throws SQLException{
        // TODO Auto-generated method stub
        ContentValues cvUpdate = new ContentValues();
        cvUpdate.put(KEY_Stop, mStop);
        cvUpdate.put(KEY_Lat, mlat);
        cvUpdate.put(KEY_Long, mlong);
        ourDatabase.update(RouteN, cvUpdate, KEY_ROWID + "=" + lRow, null);
    }

    public void deleteEntry(long lRow1) throws SQLException{
        // TODO Auto-generated method stub
        ourDatabase.delete(RouteN, KEY_ROWID + "=" + lRow1, null);
    }
}


