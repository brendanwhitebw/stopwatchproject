package com.eagerbeavers.stopwatch;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class CustomInput extends AppCompatActivity implements OnMapReadyCallback {

    EditText StopText,RouteText;
    Button AddStop,FindButton;
    private GoogleMap mMap;
    Double lat, lng;
    Marker myMarker;
    int offY = 425;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_input);

        Toolbar toolbar = (Toolbar) findViewById(R.id.cInputToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // set up toolbar

        RouteText = (EditText)findViewById(R.id.RouteText);
        StopText = (EditText)findViewById(R.id.StopText);
        AddStop = (Button)findViewById(R.id.AddStop);
        FindButton = (Button)findViewById(R.id.findButton);  // link objects to layout widgets
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this); // set up map

        Toast toast = Toast.makeText(getApplicationContext(), "Press and hold a location on the map or enter an address", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, offY); // sets toast message to centre of screen
        toast.show();
    }



    public void AddButton(View view) // save button listener
    {
        if(!RouteText.getText().toString().equals("") && RouteText.getText().toString() != null && lat != null)
        { // if text field is not empty and latitude is not null

            String route = RouteText.getText().toString();
            String stop = StopText.getText().toString(); // set strings to text in text fields

            route = route.replace(' ', '_'); // replace any spaces with underscore as table name cant have spaces

            BusDB entry = new BusDB(getApplicationContext(), route); // creates new database object
            entry.open(); // creates database unless it already exists
            entry.createEntry(stop, lat, lng); // create new entry in database with these values
            entry.close(); //closes database helper
            AddStop.setEnabled(false); // disable save button
            Toast toast = Toast.makeText(getApplicationContext(), "route/stop added to database", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, offY);
            toast.show();
        }
        else{
            Toast toast = Toast.makeText(getApplicationContext(), "Enter route name and stop name", Toast.LENGTH_SHORT);
            toast.show(); // else show error message
        }
    }

    public void FindButton(View view) // find button listener
    {
        String location = StopText.getText().toString();
        mMap.clear(); // clear markers
        List<Address> addressList = null;  // create address list
        if(location != null && !location.equals("")) // if location text field is not empty
        {
            Locale locale = new Locale("IE"); // locale set to ireland
            Geocoder geocoder = new Geocoder(this, locale);
            // create geocoder object which converts text addresses to latitude and longitude co-ordinates
            try {

                addressList = geocoder.getFromLocationName(location, 10, 51.416527, -10.598744, 55.438535, -5.391225);

                /* returns up to 10 addresses thats are known to discribe the named location within specifed co-ordinates (UK and Ireland)
                * this is so there are no unnessary results returned for the address in far away countries
                * */
            }
            catch (IOException e) // catch if exception is thrown
            {
                e.printStackTrace();
            }

            if (addressList.size() > 0 ) {// if geofence returns 1 or more results

                AddStop.setEnabled(true); // enable save button
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                for(int i = 0; i< addressList.size(); i++) { // loop for each geocoder address found
                    address = addressList.get(i); // address class stores latitude and longitude
                    latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLng).title("Marker")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                            //adds marker for each address
                }
                lat = address.getLatitude();
                lng = address.getLongitude();

                Toast toast = Toast.makeText(getApplicationContext(), "Select alarm location, or choose custom location on map", Toast.LENGTH_SHORT);
                toast.show();

                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng)); // makes camera focus on marker
            }
            else{
                Toast toast = Toast.makeText(getApplicationContext(), "No address found", Toast.LENGTH_SHORT);
                toast.show();
            }

        }
        else{
            Toast toast = Toast.makeText(getApplicationContext(), "Enter stop name", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMyLocationEnabled(true); // allows map to center on location
        LocationManager mng = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Location myLocation = mMap.getMyLocation(); // initiates myLocation
        try {
            myLocation = mng.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            // stores current location in myLocation, has to be handled by exception
        }
        catch(SecurityException e)
        {
            e.printStackTrace();
        }

        final LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
        // gets co-ordinates of current location

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        // moves camera to current location on start up(15 is zoom level)

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() { // listener for long map click
            @Override
            public void onMapLongClick(LatLng latLng) { //place marker on map where user clicks
                mMap.clear(); // clear markers
                myMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Custom location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))); // add marker to map
                lat = myMarker.getPosition().latitude;
                lng = myMarker.getPosition().longitude; // gets latitude and longitude of marker
                StopText.getText().clear();
                RouteText.getText().clear(); // clears stop and route text so user can enter new names for selected marker
                Toast toast = Toast.makeText(getApplicationContext(), "Enter route name and stop name for new marker", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, offY);
                toast.show();
                AddStop.setEnabled(true); // save button enabled
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
        // marker click listener for when several markers are returned when user enters address
            @Override
            public boolean onMarkerClick(Marker myMarker) {
                myMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                // sets marker blue(azure) colour
                lat = myMarker.getPosition().latitude;
                lng = myMarker.getPosition().longitude; // get lat and long of marker selected ready to add to database
                Toast toast = Toast.makeText(getApplicationContext(), "Marker selected", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, offY);
                toast.show();
                AddStop.setEnabled(true); // save button enabled
                return true;
            }
        });

    }

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
            Intent intentSettings = new Intent(CustomInput.this, SettingsActivity.class);
            startActivity(intentSettings);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}