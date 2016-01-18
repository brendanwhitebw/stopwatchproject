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

    // Initialising here so that it can be passed between button click calls.
    String item;

    double[] stopLat;
    double[] stopLong;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_choice);

        // Toolbar and back button setup.
        Toolbar toolbar = (Toolbar) findViewById(R.id.lChoiceToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /* Check shared preferences to see if this is the first time to activate the app.
        If it is, create busStop Database.
         */
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (!prefs.getBoolean("firstTime", false)) {
            databaseSetup();// fills database on first install of app

            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("firstTime", true);
            // sets shared preference firsttime to true so this will not run again once database is
            // created with hard coded routes and stops
            editor.commit();
        }

        StopListView = (ListView) findViewById(R.id.StopListView);
        RouteListView = (ListView) findViewById(R.id.RouteListView);


        //CustomRoute = (Button) findViewById(R.id.CustomRoute);



        String[] routeL = getApplicationContext().databaseList(); // get names of all databases of app

        for(int i = 0; i < routeL.length; i += 2) {
        // new array to filter out every second database name that is returned by databaselist
            RouteArray.add(routeL[i].replace('_', ' ')); // add database name to array
        }


        ArrayAdapter<String>  routeAdapter = new ArrayAdapter<String>(this, R.layout.customlocationlistview, android.R.id.text1,RouteArray);
        RouteListView.setAdapter(routeAdapter); // populate route list

        RouteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View vies, int position, long id) {
                item = (String) parent.getItemAtPosition(position);
                // when item in route list is clicked set item to route/database name
                item = item.replace(" ", "_"); // database has to have underscore in database name
                StopArray.clear(); // clear stop array list
                int rowNum = rowCount(item); // get number of rows in route table

                stopLat = new double[rowNum];
                stopLong = new double[rowNum]; // initilze array size to the number of rows in table

                for (int i = 1; i <= rowNum; i++) {
                    StopArray.add(GetStopName(item, i)); // add each stop name in the route table to stop array list
                    stopLat[i - 1] = GetStopCoords(item, i)[0];
                    stopLong[i - 1] = GetStopCoords(item, i)[1]; // returns co-ordinates for route clicked
                }

                ArrayAdapter<String> stopAdapter = new ArrayAdapter<String>(getApplicationContext(),
                        R.layout.customlocationlistview, android.R.id.text1 , StopArray);
                StopListView.setAdapter(stopAdapter); // populate stop list

                StopListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View vies, int position, long id) {
                        String StopID = (String) parent.getItemAtPosition(position);
                        // when item in stop list is clicked set stopID to stop name

                        Intent geofenceIntent = new Intent(getApplicationContext(), HomeScreen.class);
                        // creates intent to map screen
                        geofenceIntent.putExtra("id", StopID);

                        geofenceIntent.putExtra("lat", GetStopCoords(item, position + 1)[0]);
                        // +1 because the array starts at zero, but the SQL DB starts at 1.
                        geofenceIntent.putExtra("lng", GetStopCoords(item, position + 1)[1]);
                        // passes data with intent to homescreen
                        geofenceIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        // destroys all previous actvities so the app cannot go into infinate loop by click set stop button on map screen
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
        for(int i = 0; i < routeL.length; i += 2) {
        // new array to filter out second database name that is returned by databaselist
            RouteArray.add(routeL[i].replace('_', ' ')); // populates route array and replaces underscore with space
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

        route = "Irish_rail_Dublin_Cork";
        stop = "Heuston";
        lat = 53.346363;
        lng =  -6.294125;

        Insert(route, stop, lat, lng);

        stop = "Park West and Cherry Orchard";
        lat = 53.333992;
        lng =  -6.378706;

        Insert(route, stop, lat, lng);

        stop = "Clondalkin";
        lat = 53.332960;
        lng =  -6.396376;

        Insert(route, stop, lat, lng);

        stop = "Adamstown";
        lat = 53.336045;
        lng =  -6.469478;

        Insert(route, stop, lat, lng);

        stop = "Cellbridge and Hazelhatch";
        lat = 53.322429;
        lng = -6.523309;

        Insert(route, stop, lat, lng);

        stop = "Sallins and Naas";
        lat = 53.246816;
        lng = -6.664614;

        Insert(route, stop, lat, lng);


        stop = "Newbridge";
        lat = 53.185637;
        lng =  -6.808215;

        Insert(route, stop, lat, lng);

        stop = "Kildare";
        lat = 53.163101;
        lng =  -6.907886;

        Insert(route, stop, lat, lng);

        stop = "Monasterevin";
        lat = 53.145352;
        lng =   -7.063964;

        Insert(route, stop, lat, lng);

        stop = "Portarlington";
        lat = 53.145928;
        lng =  -7.180565;

        Insert(route, stop, lat, lng);

        stop = "Portlaoise";
        lat = 53.037095;
        lng = -7.301114;

        Insert(route, stop, lat, lng);

        stop = "Templemore";
        lat = 52.787845;
        lng = -7.822458;

        Insert(route, stop, lat, lng);

        stop = "Thurles";
        lat = 52.676793;
        lng = -7.821825;

        Insert(route, stop, lat, lng);

        stop = "Limerick junction";
        lat = 52.501219;
        lng = -8.199894;

        Insert(route, stop, lat, lng);

        stop = "Charleville";
        lat = 52.347434;
        lng = -8.653363;

        Insert(route, stop, lat, lng);

        stop = "Mallow";
        lat = 52.138939;
        lng =  -8.655080;

        Insert(route, stop, lat, lng);

        stop = "Cork Kent";
        lat = 51.901870;
        lng =  -8.457938;

        Insert(route, stop, lat, lng);

        route = "Bus_Eireann_126";
        stop = "Dublin DCU";
        lat = 53.387059;
        lng =   -6.257003;

        Insert(route, stop, lat, lng);

        stop = "Dublin, Belfield";
        lat = 53.296167;
        lng =   -6.256304;

        Insert(route, stop, lat, lng);

        stop = "Dublin, Kildare street";
        lat = 53.340191;
        lng =   -6.255608;

        Insert(route, stop, lat, lng);

        stop = "Dublin Busáras";
        lat = 53.349825;
        lng = -6.251936;

        Insert(route, stop, lat, lng);

        stop = "Castlewarden";
        lat = 53.255866;
        lng = -6.554220;

        Insert(route, stop, lat, lng);

        stop = "Kill";
        lat = 53.248188;
        lng = -6.591907;

        Insert(route, stop, lat, lng);

        stop = "Johnstown Village";
        lat = 53.235613;
        lng = -6.625992;

        Insert(route, stop, lat, lng);

        stop = "Naas";
        lat = 53.217659;
        lng = -6.663827;

        Insert(route, stop, lat, lng);

        stop = "Newbridge";
        lat = 53.180724;
        lng = -6.797518;

        Insert(route, stop, lat, lng);

        stop = "Milltown";
        lat = 53.204931;
        lng =  -6.860660;

        Insert(route, stop, lat, lng);

        stop = "Rathangan";
        lat = 53.220760;
        lng = -6.993253;

        Insert(route, stop, lat, lng);

        stop = "Brownstown";
        lat = 53.139124;
        lng = -6.839529;

        Insert(route, stop, lat, lng);

        stop = "Kildare";
        lat = 53.157286;
        lng = -6.910556;

        Insert(route, stop, lat, lng);

        route = "Luas_Green_Line";
        stop = "St.Stephens Green";
        lat = 53.339059;
        lng =  -6.261247;

        Insert(route, stop, lat, lng);

        stop = "Harcourt";
        lat = 53.333678;
        lng =  -6.262706;

        Insert(route, stop, lat, lng);

        stop = "Charlemont";
        lat = 53.330603;
        lng =  -6.258618;

        Insert(route, stop, lat, lng);

        stop = "Beechwood";
        lat = 53.320888;
        lng =  -6.254605;

        Insert(route, stop, lat, lng);


        stop = "Cowper";
        lat = 53.316370;
        lng =   -6.253393;

        Insert(route, stop, lat, lng);

        stop = "Windy Arbour";
        lat = 53.301716;
        lng =  -6.250668;

        Insert(route, stop, lat, lng);

        stop = "Dundrum";
        lat = 53.292425;
        lng =  -6.245153;

        Insert(route, stop, lat, lng);

        stop = "Balally";
        lat = 53.286050;
        lng =  -6.236752;

        Insert(route, stop, lat, lng);

        stop = "Kilmacud";
        lat = 53.282971;
        lng =  -6.224146;

        Insert(route, stop, lat, lng);

        stop = "Stillorgan";
        lat = 53.279327;
        lng =  -6.210274;

        Insert(route, stop, lat, lng);

        stop = "Sandyford";
        lat = 53.277627;
        lng =  -6.204588;

        Insert(route, stop, lat, lng);


        stop = "Central Park";
        lat = 53.270157;
        lng =   -6.203827;

        Insert(route, stop, lat, lng);

        stop = "The Gallops";
        lat = 53.261162;
        lng =  -6.205814;

        Insert(route, stop, lat, lng);

        stop = "Lepardstown Valley";
        lat = 53.258300;
        lng =  -6.198357;

        Insert(route, stop, lat, lng);


        stop = "Carrickmines";
        lat = 53.254346;
        lng =   -6.171567;

        Insert(route, stop, lat, lng);

        stop = "Laughanstown";
        lat = 53.250623;
        lng =  -6.154969;

        Insert(route, stop, lat, lng);

        stop = "Cherrywood";
        lat = 53.245394;
        lng =  -6.145797;

        Insert(route, stop, lat, lng);

        stop = "Brides Glen";
        lat = 53.241895;
        lng =  -6.142761;

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
