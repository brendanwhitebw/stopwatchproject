package com.eagerbeavers.stopwatch;


import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapActivity extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnCameraChangeListener, AlarmFragment.FragmentCallBack {

    private final String TAG = "MAPS";

    private GoogleMap Map;

    // Current location stuff.

    GoogleApiClient aGoogleApiClient;
    Location lastLocation;

    int RadDef = 100;


    /**
     * Geofence Data
     */

    /**
     * Geofences Array
     */
    ArrayList<Geofence> GeofenceList;

    /**
     * Geofence Coordinates
     */
    ArrayList<LatLng> Coordinates;

    /**
     * Geofence Radius'
     */
    ArrayList<Integer> RadiusList;

    /**
     * Geofence Store, custom class, which sets pending intents and activates the geofences using the
     * API client.
     */
    private GeofenceStore GeofenceListtore;

    /* Shared Prefs! */

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Log.v(TAG, "created");

        GeofenceList = new ArrayList<Geofence>();
        Coordinates = new ArrayList<LatLng>();
        RadiusList = new ArrayList<Integer>();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Intent gotHereFrom = this.getIntent();

        int i = 0;

        if (gotHereFrom.hasExtra("id")) {

            double lat = gotHereFrom.getDoubleExtra("lat", 0);
            double lng = gotHereFrom.getDoubleExtra("lng", 0);

            Coordinates.add(new LatLng(lat, lng));

            // Adding associated geofence radius' to array.
            RadiusList.add(RadDef);

            GeofenceList.add(new Geofence.Builder()
                    .setRequestId(gotHereFrom.getStringExtra("id"))
                            // The coordinates of the center of the geofence and the radius in meters.
                    .setCircularRegion(Coordinates.get(i).latitude, Coordinates.get(i).longitude, RadiusList.get(i).intValue())
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                            // Required when we use the transition type of GEOFENCE_TRANSITION_DWELL
                    .setLoiteringDelay(30000)
                    .setTransitionTypes(
                            Geofence.GEOFENCE_TRANSITION_ENTER
                                    | Geofence.GEOFENCE_TRANSITION_DWELL
                                    | Geofence.GEOFENCE_TRANSITION_EXIT).build());

            GeofenceListtore = new GeofenceStore(this, GeofenceList);


            // Storing current coords. Replace existing coords first.

            SharedPreferences.Editor editor = prefs.edit();

            if (prefs.getBoolean("savedCoords", false)) {
                editor.remove("savedCoords");
                editor.remove("ID");
                editor.remove("Lat");
                editor.remove("Lng");
                editor.apply();
            }

            editor.putBoolean("savedCoords", true);
            editor.putString("ID", gotHereFrom.getStringExtra("id"));
            editor.putString("Lat", "" + lat);
            editor.putString("Lng", "" + lng);
            editor.apply();

        } else {
            double lat = Double.parseDouble(prefs.getString("Lat", "0"));
            double lng = Double.parseDouble(prefs.getString("Lng", "0"));

            Coordinates.add(new LatLng(lat, lng));
            RadiusList.add(RadDef);
        }

        if (gotHereFrom.hasExtra("Alert")) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            AlarmFragment newAlarmFrag = new AlarmFragment();
            newAlarmFrag.show(ft, "Alarm");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "started");
    }

    @Override
    protected void onStop() {
        if (GeofenceListtore!= null) {
            GeofenceListtore.disconnect();
        }
        super.onStop();

        aGoogleApiClient.disconnect();

        Log.v(TAG, "stopped");
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.v(TAG, "resumed");

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            setUpMapIfNeeded();
        } else {
            GooglePlayServicesUtil.getErrorDialog(
                    GooglePlayServicesUtil.isGooglePlayServicesAvailable(this),
                    this, 0);
        }
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the
        // map.
        if (Map == null) {
            // Try to obtain the map from the SupportMapFragment.
            Map = ((SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map)).getMap();

            // Check if we were successful in obtaining the map.
            if (Map != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the
     * camera. In this case, we just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #Map}
     * is not null.
     */
    private void setUpMap() {
        /* Extra code to set current location */

        aGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext()).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(LocationServices.API).build();

        if (aGoogleApiClient != null) {
            aGoogleApiClient.connect();
        } else {
            Toast.makeText(getApplicationContext(), "not connected api client", Toast.LENGTH_LONG).show();
        }

        // Hide labels.
        Map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        Map.setIndoorEnabled(false);
        Map.setMyLocationEnabled(true);

        Map.setOnCameraChangeListener(this);
    }

    @Override
    public void onCameraChange(CameraPosition position) {
        Map.clear();
        // Makes sure the visuals remain when zoom changes.
        for(int i = 0; i < Coordinates.size(); i++) {
            Map.addCircle(new CircleOptions().center(Coordinates.get(i))
                    .radius(RadiusList.get(i).intValue())
                    .fillColor(0x88ffa500)
                    .strokeColor(Color.TRANSPARENT).strokeWidth(2));
            Map.addMarker(new MarkerOptions().position(Coordinates.get(i)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Setting camera to current position.
        lastLocation = LocationServices.FusedLocationApi.getLastLocation(aGoogleApiClient);

        Map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 15));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public void cancelPendingIntent() {
        Toast.makeText(getApplicationContext(), "it's a start", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, GeofenceIntentService.class);
        PendingIntent mPendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mPendingIntent.cancel();
    }
}
