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
    public static final String KEY_Long = "longitude"; // sets strings for create table statement

    private static final int DATABASE_VERSION = 1;

    private DbHelper ourHelper;
    private final Context ourContext;
    private SQLiteDatabase ourDatabase;
    private static String RouteN; // route name this is used for database name and table table

    private static class DbHelper extends SQLiteOpenHelper {
        public DbHelper(Context context) {
            super(context, RouteN, null, DATABASE_VERSION); // creates a new database for each new route name passed in
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + RouteN + "(" +
                            KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            KEY_Stop + " TEXT NOT NULL, " +
                            KEY_Lat + " REAL NOT NULL, " +
                            KEY_Long + " REAL NOT NULL);"
            );
        }/* Create table statement for bus routes database, contains the stop name, latitude/longitude and
           * a primary key that increments with each stop loaction added to the table.
           * For each route entered by the user a new database is created with a table with the same name
           */
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + RouteN); // deletes table if there is already a table with the same name
            onCreate(db);
        }

    }

    public BusDB (Context c, String Routen){ // constructor
        ourContext = c; // sets context
        RouteN = Routen; // sets route name(name of database) and table name
    }

    public BusDB open() throws SQLException {
        ourHelper = new DbHelper(ourContext); // creates database helper object
        ourDatabase = ourHelper.getWritableDatabase(); // creates database
        return this;
    }

    public void close(){
        ourHelper.close(); // closes database helper
    }


    public long createEntry(String stop, double lat, double lng) {

        ContentValues cv = new ContentValues();
        cv.put(KEY_Stop, stop);
        cv.put(KEY_Lat, lat);
        cv.put(KEY_Long, lng); // inserts stop name, latitude and longitude into content values
        return ourDatabase.insert(RouteN, null, cv);
        // creates new row with data passed into content values in RouteN table
    }

    public int rowCount(String route) {
        Cursor mCount = ourDatabase.rawQuery("select count(*) from " + route, null);
        mCount.moveToFirst(); // Move the cursor to the first row
        int count = mCount.getInt(0);// gets number of rows in table
        mCount.close(); // close cursor
        return count; // returns number of rows in table
    }

    public String getData() {
        String[] columns = new String[]{ KEY_ROWID, KEY_Stop, KEY_Lat, KEY_Long}; // creates string array with column names
        Cursor c = ourDatabase.query(RouteN, columns, null, null, null, null, null);
        String result = "";

        int iRow = c.getColumnIndex(KEY_ROWID);
        int iStop = c.getColumnIndex(KEY_Stop); // Returns the zero-based index for the given column name

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()){
        // cursor cycles through rows until cursor is pointing to the position after the last row.
            result = result + c.getString(iRow) + " " + c.getString(iStop) + "\n";
        }

        return result;
    }


    public String getStop(long l) throws SQLException{
        String[] columns = new String[]{ KEY_ROWID, KEY_Stop, KEY_Lat, KEY_Long};
        Cursor c = ourDatabase.query(RouteN, columns, KEY_ROWID + "=" + l, null, null, null, null);
        // selects which row by number passed in (l) (key_ROWID)
        if (c != null){
            c.moveToFirst(); // Move the cursor to the first row
            String stopName = c.getString(1);  // points cursor to key_stop(stop name) returns string at column 1
            return stopName; // returns the stop name of choosen row
        }
        return null; // return null if row not found
    }

    public double getLat(long l) throws SQLException {

        String[] columns = new String[]{ KEY_ROWID, KEY_Stop, KEY_Lat, KEY_Long};
        Cursor c = ourDatabase.query(RouteN, columns, KEY_ROWID + "=" + l, null, null, null, null);
        // selects which row by number passed in (l) (key_ROWID)
        if (c != null){
            c.moveToFirst(); // Move the cursor to the first row
            double latitude = c.getDouble(2);  // points cursor to key_Lat(latitude), returns double at column 2
            return latitude; // returns the latitude of choosen row
        }
        return 0; // return zero if row not found
    }

    public double getLong(long l) throws SQLException {
        String[] columns = new String[]{ KEY_ROWID, KEY_Stop, KEY_Lat, KEY_Long};
        Cursor c = ourDatabase.query(RouteN, columns, KEY_ROWID + "=" + l, null, null, null, null);
        // selects which row by number passed in (l) (key_ROWID)
        if (c != null){
            c.moveToFirst(); // Move the cursor to the first row
            double longtitude = c.getDouble(3);  // points cursor to key_Long(longitude). returns double at column 3
            return longtitude; // returns the longitude of choosen row
        }
        return 0; // return zero if row not found
    }

    /*public void updateEntry(long lRow, String mStop, double mlat, double mlong) throws SQLException{
        ContentValues cvUpdate = new ContentValues();
        cvUpdate.put(KEY_Stop, mStop);
        cvUpdate.put(KEY_Lat, mlat);
        cvUpdate.put(KEY_Long, mlong);
        ourDatabase.update(RouteN, cvUpdate, KEY_ROWID + "=" + lRow, null);
    }

    public void deleteEntry(long lRow1) throws SQLException{
        ourDatabase.delete(RouteN, KEY_ROWID + "=" + lRow1, null);
    }
    Does not use these methods
    */
}


