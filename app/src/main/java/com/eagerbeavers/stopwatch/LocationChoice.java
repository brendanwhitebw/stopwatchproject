package com.eagerbeavers.stopwatch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class LocationChoice extends AppCompatActivity  {

    ListView StopListView, RouteListView;
    ArrayList StopArray = new ArrayList();
    ArrayList RouteArray = new ArrayList();
    Button CustomRoute;

    // Initialising here so that it can be passed between button click calls.
    String item;

    double[] stopLat;
    double[] stopLong;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_choice);

        // Toolbar and back button setup.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /* Check shared preferences to see if this is the first time to activate the app.
        If it is, create busStop Database.
         */
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("firstTime", false)) {
            Toast.makeText(this, "First!", Toast.LENGTH_LONG).show();

            databaseSetup();// fills data base on first install of app

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstTime", true);
            editor.commit();
        }

        //databaseSetup();// While fiddling with database always set it up.

        StopListView = (ListView) findViewById(R.id.StopListView);
        RouteListView = (ListView) findViewById(R.id.RouteListView);
        CustomRoute = (Button) findViewById(R.id.CustomRoute);

        String[] routeL = getApplicationContext().databaseList(); // get names of all databases of app

        for(int i = 0; i < routeL.length; i += 2) { // new array to filter out second database name that is returned by databaselist
            RouteArray.add(routeL[i]);
        }


        ArrayAdapter<String>  routeAdapter = new ArrayAdapter<String>(this, R.layout.customlocationlistview, android.R.id.text1,RouteArray);
        RouteListView.setAdapter(routeAdapter); // populate route list

        RouteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View vies, int position, long id) {
                item = (String) parent.getItemAtPosition(position); // when item in route list is clicked set item to route/database name
                StopArray.clear(); // clear array list
                int rowNum = rowCount(item); // get number of rows in route table

                stopLat = new double[rowNum];
                stopLong = new double[rowNum];

                for (int i = 1; i <= rowNum; i++) {
                    StopArray.add(GetStopName(item, i)); // add each stop name in the route table to array list
                    stopLat[i - 1] = GetStopCoords(item, i)[0];
                    stopLong[i - 1] = GetStopCoords(item, i)[1];
                }

                ArrayAdapter<String> stopAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.customlocationlistview, android.R.id.text1 , StopArray);
                StopListView.setAdapter(stopAdapter); // populate stop list

                StopListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View vies, int position, long id) {
                        String StopID = (String) parent.getItemAtPosition(position); // when item in route list is clicked set item to route/database name

                        Intent geofenceIntent = new Intent(getApplicationContext(), MapActivity.class);
                        geofenceIntent.putExtra("id", StopID);

                        geofenceIntent.putExtra("lat", GetStopCoords(item, position + 1)[0]); // +1 because the array starts at zero, but the SQL DB starts at 1.
                        geofenceIntent.putExtra("lng", GetStopCoords(item, position + 1)[1]);

                        startActivity(geofenceIntent);
                    }
                });

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        String[] routeL = getApplicationContext().databaseList(); // get names of all databases of app
        RouteArray.clear();
        for(int i = 0; i < routeL.length; i += 2) { // new array to filter out second database name that is returned by databaselist
            RouteArray.add(routeL[i]);
        }

        ArrayAdapter<String>  routeAdapter = new ArrayAdapter<String>(this, R.layout.customlocationlistview, android.R.id.text1,RouteArray);
        RouteListView.setAdapter(routeAdapter); // populate route list
    }

    public void Custom(View view) // button listener
    {
        Intent CustomIntent = new Intent(getApplicationContext(), CustomInput.class);
        startActivity(CustomIntent);
    }

    public void Insert(String route, String stop, double lat, double lng){
        BusDB entry = new BusDB(getApplicationContext(), route);
        entry.open();
        entry.createEntry(stop, lat, lng); // create new entry in db
        entry.close();
    }

    public String GetStopName(String route, int whichRow){
        BusDB entry = new BusDB(getApplicationContext(), route);
        entry.open();
        String stopName = entry.getStop((long)whichRow); // returns stop name for given row
        entry.close();
        return stopName;
    }

    public double[] GetStopCoords(String route, int whichRow){
        BusDB entry = new BusDB(getApplicationContext(), route);
        entry.open();
        double[] coords = new double[2];
        coords[0] = entry.getLat((long) whichRow);
        coords[1] = entry.getLong((long) whichRow);
        entry.close();
        return coords;
    }

    public int rowCount(String route){
        BusDB entry = new BusDB(getApplicationContext(), route);
        entry.open();
        int count = entry.rowCount(route); // returns no. of rows in route table
        entry.close();
        return count;
    }

    public void databaseSetup() { // hard code attributes to db
        String route = "Maynooth_Campus";
        String stop = "Eolas";
        double lat = 53.384594;
        double lng = -6.601688;

        Insert(route, stop, lat, lng);

        route = "Maynooth_Campus";
        stop = "Library";
        lat = 53.381249;
        lng = -6.600168;

        Insert(route, stop, lat, lng);

        stop = "Callan";
        lat = 53.382900;
        lng = -6.602038;

        Insert(route, stop, lat, lng);

        route = "Kildare";
        stop = "Naas, East";
        lat = 53.214048;
        lng = -6.663412;

        Insert(route, stop, lat, lng);

        route = "Kildare";
        stop = "Newbridge, Maxol";
        lat = 53.167480;
        lng = -6.816529;



        Insert(route, stop, lat, lng);

        stop = "Clane, Esso";
        lat =53.2933;
        lng = -6.6864;

        Insert(route, stop, lat, lng);

        stop = "Clane, Loughbollard";
        lat =53.29925;
        lng = -6.68978;

        Insert(route, stop, lat, lng);

        route = "Bray";
        stop = "Ferndale Rd.";
        lat = 53.209249;
        lng = -6.133353;

        Insert(route, stop, lat, lng);

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Go to Map activity method
    public void gotToMap(View view)
    {
        Intent intentMap = new Intent(LocationChoice.this, MapActivity.class);
        startActivity(intentMap);
    }

    //Go to Alarms activity method
    public void goToAlarms(View view)
    {
        Intent intentAlarms = new Intent(LocationChoice.this, LocationChoice.class);
        startActivity(intentAlarms);
    }
}
