package com.eagerbeavers.stopwatch;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

public class SoundChoice extends AppCompatActivity {

    SharedPreferences prefs;

    String item;

    long TrackID;

    MediaPlayer mMediaPlayer;

    Intent gotHereFrom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sound_choice);

        Toolbar toolbar = (Toolbar) findViewById(R.id.soundToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        gotHereFrom = this.getIntent();

        if (gotHereFrom.hasExtra("AlarmToBeChanged")) {
            SharedPreferences.Editor editor = prefs.edit();

            String alarmName = gotHereFrom.getStringExtra("AlarmToBeChanged");

            if (prefs.getLong(alarmName, 0) != 0) {
                editor.remove(alarmName);
                editor.apply();
            }

            /*editor.putLong(alarmName, TrackID);
            editor.apply();*/
        }

        ListView albumList = (ListView) findViewById(R.id.AlbumListView);

        final ArrayList<Long> mediaID = new ArrayList<Long>();

        final ArrayList<String> mediaTitle = new ArrayList<String>();

        ArrayList<String> mediaAlbum = new ArrayList<String>();

        ContentResolver contentResolver = getContentResolver();

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        Cursor cursor = contentResolver.query(uri, null, null, null, null);

        if (cursor == null) {

        } else if (!cursor.moveToFirst()) {

        } else {
            int album = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);

            do {
                String thisAlbum = cursor.getString(album);

                boolean albumThere = false;

                for (int i = 0; i < mediaAlbum.size(); i++) {
                    if (mediaAlbum.get(i).equals(thisAlbum)) {
                        albumThere = true;
                    }
                }

                if (!albumThere) {
                    mediaAlbum.add(thisAlbum);
                }
            } while (cursor.moveToNext());
        }

        Collections.sort(mediaAlbum);

        ArrayAdapter<String> titleAdapter = new ArrayAdapter<String>(this, R.layout.customlocationlistview, android.R.id.text1, mediaAlbum);

        albumList.setAdapter(titleAdapter);

        albumList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View vies, int position, long id) {
                item = (String) parent.getItemAtPosition(position); // when item in route list is clicked set item to route/database name

                mediaTitle.clear();
                mediaID.clear();

                ContentResolver contentResolver = getContentResolver();

                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

                Cursor cursor = contentResolver.query(uri, null, null, null, null);

                if (cursor == null) {

                } else if (!cursor.moveToFirst()) {

                } else {
                    int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                    int idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                    int album = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);

                    do {
                        long thisID = cursor.getLong(idColumn);
                        String thisTitle = cursor.getString(titleColumn);
                        String thisAlbum = cursor.getString(album);

                        if (thisAlbum.equals(item)) {
                            mediaID.add(thisID);
                            mediaTitle.add(thisTitle);
                        }
                    } while (cursor.moveToNext());
                }

                ListView trackList = (ListView) findViewById(R.id.TrackListView);

                ArrayAdapter<String> songAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.customlocationlistview, android.R.id.text1, mediaTitle);
                trackList.setAdapter(songAdapter); // populate stop list

                trackList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View vies, int position, long id) {
                        String song = (String) parent.getItemAtPosition(position);

                        TrackID = mediaID.get(position);

                        if (gotHereFrom.hasExtra("AlarmToBeChanged")) {
                            SharedPreferences.Editor editor = prefs.edit();

                            String alarmName = gotHereFrom.getStringExtra("AlarmToBeChanged");

                            if (prefs.getLong(alarmName, 0) != 0) {
                                editor.remove(alarmName);
                                editor.apply();
                            }

                            editor.putLong(alarmName, TrackID);
                            editor.apply();

                            Toast.makeText(getApplicationContext(), alarmName + " is set to " + song, Toast.LENGTH_LONG).show();
                        } else {
                            Log.e("stopwatch", "Not sent here with alarm name...");
                        }
                    }
                });

            }
        });
    }
}
