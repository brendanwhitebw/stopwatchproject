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

            databaseSetup();// fills database on first install of app

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstTime", true);
            // sets shared preference firsttime to true so this will not run again once database is
            // created with hard coded routes and stops
            editor.commit();
        }

        StopListView = (ListView) findViewById(R.id.StopListView);
        RouteListView = (ListView) findViewById(R.id.RouteListView);
        CustomRoute = (Button) findViewById(R.id.CustomRoute);

        String[] routeL = getApplicationContext().databaseList(); // get names of all databases of app

        for(int i = 0; i < routeL.length; i += 2) {
        // new array to filter out every second database name that is returned by databaselist
            RouteArray.add(routeL[i]); // add database name to array
        }


        ArrayAdapter<String>  routeAdapter = new ArrayAdapter<String>(this, R.layout.customlocationlistview, android.R.id.text1,RouteArray);
        RouteListView.setAdapter(routeAdapter); // populate route list

        RouteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View vies, int position, long id) {
                item = (String) parent.getItemAtPosition(position); // when item in route list is clicked set item to route/database name
                StopArray.clear(); // clear stop array list
                int rowNum = rowCount(item); // get number of rows in route table

                stopLat = new double[rowNum];
                stopLong = new double[rowNum]; // initilze array size to the number of rows in table

                for (int i = 1; i <= rowNum; i++) {
                    StopArray.add(GetStopName(item, i)); // add each stop name in the route table to stop array list
                    stopLat[i - 1] = GetStopCoords(item, i)[0];
                    stopLong[i - 1] = GetStopCoords(item, i)[1]; // returns co-ordinates for route clicked
                }

                ArrayAdapter<String> stopAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.customlocationlistview, android.R.id.text1 , StopArray);
                StopListView.setAdapter(stopAdapter); // populate stop list

                StopListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View vies, int position, long id) {
                        String StopID = (String) parent.getItemAtPosition(position);
                        // when item in stop list is clicked set stopID to stop name

                        Intent geofenceIntent = new Intent(getApplicationContext(), HomeScreen.class); // creates intent to map screen
                        geofenceIntent.putExtra("id", StopID);

                        geofenceIntent.putExtra("lat", GetStopCoords(item, position + 1)[0]); // +1 because the array starts at zero, but the SQL DB starts at 1.
                        geofenceIntent.putExtra("lng", GetStopCoords(item, position + 1)[1]);
                        // passes data with intent to homescreen
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
        RouteArray.clear(); // clears route list
        for(int i = 0; i < routeL.length; i += 2) { // new array to filter out second database name that is returned by databaselist
            RouteArray.add(routeL[i]); // populates route array
        }

        ArrayAdapter<String>  routeAdapter = new ArrayAdapter<String>(this, R.layout.customlocationlistview, android.R.id.text1,RouteArray);
        RouteListView.setAdapter(routeAdapter); // populate route list with route array content
    }

    /* We can move CustomInput to the toolbar to match the button on the Homescreen, and settings
     * can be accessed from settings bar.
     * These methods, and the entire bottom bar should be deleted, because they are unneccesary.
     */

    public void Custom(View view) // button listener
    {
        Intent CustomIntent = new Intent(getApplicationContext(), CustomInput.class);
        startActivity(CustomIntent);
    }
    public void Settingsbutton(View view)
    {
        Intent intent = new Intent(LocationChoice.this, SettingsActivity.class);
        startActivity(intent);
    }


    public void Insert(String route, String stop, double lat, double lng){
        BusDB entry = new BusDB(getApplicationContext(), route); // creates new database object
        entry.open(); // creates database unless it already exists
        entry.createEntry(stop, lat, lng); // create new entry in db
        entry.close(); //closes database helper
    }

    public String GetStopName(String route, int whichRow){
        BusDB entry = new BusDB(getApplicationContext(), route); // creates new database object
        entry.open(); // creates database unless it already exists
        String stopName = entry.getStop((long)whichRow); // returns stop name for given row
        entry.close(); //closes database helper
        return stopName;
    }

    public double[] GetStopCoords(String route, int whichRow){
        BusDB entry = new BusDB(getApplicationContext(), route);// creates new database object
        entry.open(); // creates database unless it already exists
        double[] coords = new double[2];
        coords[0] = entry.getLat((long) whichRow); // gets latitude for row past in
        coords[1] = entry.getLong((long) whichRow); // gets longitude for row past in
        entry.close(); //closes database helper
        return coords; // returns [lat],[long]
    }

    public int rowCount(String route){
        BusDB entry = new BusDB(getApplicationContext(), route); // creates new database object
        entry.open();// creates database unless it already exists
        int count = entry.rowCount(route); // returns no. of rows in route table
        entry.close();//closes database helper
        return count;
    }

    public void databaseSetup() { // hard code routes/stops and attributes to database
        /*
        Various Routes and stops for testing purposes

        String route = "Maynooth_Campus";
        String stop = "Eolas";
        double lat = 53.384594;
        double lng = -6.601688;

        Insert(route, stop, lat, lng);


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
        */

        String route = "Dublin_Bus_67";
        String stop = "Maynooth (67A Terminus)";
        double lat = 53.379631;
        double lng = -6.589113;

        Insert(route, stop, lat, lng);
        /* creates database 'Dublin_Bus_67' if it does not already exist, with table name 'Dublin_Bus_67'
        *   stop name 'Maynooth (67A Terminus)' and latitude and longitude values
        */

        stop = "Straffan Rd";
        lat = 53.376533;
        lng = -6.587171;

        Insert(route, stop, lat, lng);

        stop = "RailPark Estate";
        lat = 53.374001;
        lng = -6.586522;

        Insert(route, stop, lat, lng);

        stop = "Griffin Rath";
        lat = 53.369662;
        lng = -6.580246;

        Insert(route, stop, lat, lng);

        stop = "Cellbridge Road";
        lat = 53.366645;
        lng = -6.565880;

        Insert(route, stop, lat, lng);

        stop = "Salesian College";
        lat = 53.359115;
        lng = -6.551169;

        Insert(route, stop, lat, lng);

        stop = "Crodaun Forest Park";
        lat = 53.354235;
        lng = -6.547092;

        Insert(route, stop, lat, lng);

        stop = "Thornhill Court Estate";
        lat = 53.352460;
        lng = -6.546867;

        Insert(route, stop, lat, lng);

        stop = "Beatty Park Estate";
        lat = 53.342779;
        lng = -6.539781;

        Insert(route, stop, lat, lng);

        stop = "Chestnut Grove";
        lat = 53.346046;
        lng = -6.541712;

        Insert(route, stop, lat, lng);

        stop = "Celbridge, Main Street";
        lat = 53.338638;
        lng = -6.539610;

        Insert(route, stop, lat, lng);

        stop = "Don Cowper Riding School";
        lat = 53.339791;
        lng = -6.529525;

        Insert(route, stop, lat, lng);

        stop = "Ballyoulster Estate";
        lat = 53.341969;
        lng = -6.518625;

        Insert(route, stop, lat, lng);

        stop = "Cellbridge Football Park";
        lat = 53.343378;
        lng = -6.511586;

        Insert(route, stop, lat, lng);

        stop = "Backweston Laboratory";
        lat = 53.344633;
        lng = -6.506222;

        Insert(route, stop, lat, lng);

        stop = "Backweston Park";
        lat = 53.345914;
        lng = -6.499656;

        Insert(route, stop, lat, lng);

        stop = "Lucan, Department of Agricultutre";
        lat = 53.346978;
        lng = -6.494613;

        Insert(route, stop, lat, lng);


        stop = "Weston Airfield";
        lat = 53.348995;
        lng = -6.489431;

        Insert(route, stop, lat, lng);

        stop = "Cooldrinagh Lane";
        lat = 53.353094;
        lng = -6.480762;

        Insert(route, stop, lat, lng);

        stop = "Weston Estate";
        lat = 53.356135;
        lng = -6.475173;

        Insert(route, stop, lat, lng);

        stop = "Liffey Valley Golf Course";
        lat = 53.359183;
        lng = -6.473209;

        Insert(route, stop, lat, lng);

        stop = "Lucan, Leixlip Road (near Primrose Lane)";
        lat = 53.355684;
        lng = -6.454144;

        Insert(route, stop, lat, lng);

        stop = "Lucan Village";
        lat = 53.356544;
        lng = -6.447910;

        Insert(route, stop, lat, lng);

        stop = "Lucan Heights";
        lat = 53.358084;
        lng = -6.441843;

        Insert(route, stop, lat, lng);

        stop = "St Mary's Church";
        lat = 53.359621;
        lng = -6.439498;

        Insert(route, stop, lat, lng);


        stop = "St Edmundsbury Hospital";
        lat = 53.359725;
        lng = -6.430929;

        Insert(route, stop, lat, lng);


        stop = "Old Lucan Rd";
        lat = 53.359347;
        lng = -6.424379;

        Insert(route, stop, lat, lng);

        stop = "Hermitage Hospital";
        lat = 53.358518;
        lng = -6.408849;

        Insert(route, stop, lat, lng);

        stop = "Liffey Valley";
        lat = 53.357762;
        lng = -6.405201;

        Insert(route, stop, lat, lng);

    } // this method is only called on first install of app and populates database

    // Setting the menu in the toolbar to have the appropriate values.

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_screen, menu);
        return true;
    }

    // Allowing selection from the menu.

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.toolbar_settings) {
            Intent intentSettings = new Intent(LocationChoice.this, SettingsActivity.class);
            startActivity(intentSettings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
