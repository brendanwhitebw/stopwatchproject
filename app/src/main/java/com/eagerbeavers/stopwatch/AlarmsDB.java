package com.eagerbeavers.stopwatch;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Allan on 13/12/2015.
 */
public class AlarmsDB extends SQLiteOpenHelper
{
    public static final String DATABASE_NAME = "Alarms_DB";
    public static final String TABLE_NAME = "AlarmsTable";
    public static final String col_0 = "ID";
    public static final String col_1 = "OnOrOff";
    public static final String col_2 = "Destination";
    public static final String col_3 = "Distance";
    public static final String col_4 = "Ringtone";
    public static final String col_5 = "Vibrate";
    public static final String col_6 = "Label";


    public AlarmsDB(Context context)
    {
        super(context, DATABASE_NAME, null, 1);
        //SQLiteDatabase db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase alDB)
    {
        alDB.execSQL("CREATE TABLE " + TABLE_NAME + "(" +
                col_0 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                col_1 + " INTEGER NOT NULL, " +
                col_2 + " TEXT NOT NULL, " +
                col_3 + " TEXT NOT NULL, " +
                col_4 + " TEXT NOT NULL, " +
                col_5 + " INTEGER NOT NULL, " +
                col_6 + " TEXT NOT NULL);"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase alDB, int oldVersion, int newVersion)
    {
        alDB.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(alDB);
    }

    public boolean insertData(Integer OnOrOff, String Destination, String Distance, String Ringtone, Integer Vibrate, String Label)
    {
        SQLiteDatabase alDB = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(col_1, OnOrOff);
        contentValues.put(col_2, Destination);
        contentValues.put(col_3, Distance);
        contentValues.put(col_4, Ringtone);
        contentValues.put(col_5, Vibrate);
        contentValues.put(col_6, Label);
        long result = alDB.insert(TABLE_NAME, null, contentValues);
        if(result == -1) {
            return false;
        }
        else {
            return true;
        }
    }



}
