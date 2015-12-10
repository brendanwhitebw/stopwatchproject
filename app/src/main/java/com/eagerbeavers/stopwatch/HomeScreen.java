package com.eagerbeavers.stopwatch;

import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;

public class HomeScreen extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, GoogleMap.OnCameraChangeListener,
        AlarmFragment.FragmentCallBack, GoogleMap.OnMarkerClickListener {

    // The tag for all of our Log outputs.

    private final String TAG = "stopwatch";

    // We use a google map in this activity, simply called map.

    private GoogleMap Map;

    /* In order to have the camera start at our current location we need to access the google
    API client, and use the fused location api to return the last location that received information
    from our device. */

    GoogleApiClient aGoogleApiClient;
    Location lastLocation;

    /* This is the default radius of the geofences, it will later be decided by user input, but for
     testing purposes is currently set to 100 m. */

    int RadDef;

    /* Shared Preferences, which we use here to store certain states of the activity, like whether
    the alarm is on, and where the current geofence is. */

    SharedPreferences prefs;

    /**
     * Geofence Data, all of the variables here are used to set up our geofence/s.
     */

    /**
     * Geofences Array, is an arrayList of geofences, so that we can later expand the app to hold
     * more than one geofence at a time (the API allows up to 100).
     */
    ArrayList<Geofence> GeofenceList;

    /**
     * Geofence Coordinates, this list of coordiantes is used to set up the geofences, and draw the
     * markers and Circles on the map. It's values are stored in shared preferences to they appear
     * on the map as long as the geofence is active.
     */

    ArrayList<LatLng> Coordinates;

    /**
     * Geofence Radius', used in conjunction with the Coordinates list.
     */

    ArrayList<Integer> RadiusList;

    /**
     * Geofence place names, used in conjunction with the Coordinates list.
     */

    ArrayList<String> StopNames;

    /**
     * Geofence Store, custom class, which sets pending intents and activates the geofences using the
     * API client.
     */

    private GeofenceStore GeofenceListtore;

    /* Alarm stuff. */

    private MediaPlayer player;

    AlarmFragment newAlarmFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.home_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /* Initialisation of the arrays needed for the geofence, and a call to the default shared
         preferences. */

        GeofenceList = new ArrayList<Geofence>();
        Coordinates = new ArrayList<LatLng>();
        RadiusList = new ArrayList<Integer>();
        StopNames = new ArrayList<String>();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        RadDef = prefs.getInt("Radius", 4000); //Default should be set to 4000 in future.

        /* Determining which activity sent the user here, and what extra information it sent is a
        core part of this activity, so we always get the intent that started this activity. */

        Intent gotHereFrom = this.getIntent();

        /* This variable will allow us iterate through the various array lists, but for now is more
        or less a dummy variable which remains as 0.
         */

        int i = 0;

        /* If the intent that sent the user here has an id tag, it is sending coordinates for a new
        geofence. */

        if (gotHereFrom.hasExtra("id")) {

            /* Along with the id, a lattitude and longtitude will be attached. This is where we will
            set up our geofence after we add the values to our coordinate list.*/

            StopNames.add(gotHereFrom.getStringExtra("id"));

            double lat = gotHereFrom.getDoubleExtra("lat", 0);
            double lng = gotHereFrom.getDoubleExtra("lng", 0);

            Coordinates.add(new LatLng(lat, lng));

            // We also add the associated geofence radius' to array.
            RadiusList.add(RadDef);

            /* Now we add the geofence object based on these values to it's own list.
            *
            * We set the id to match the id extra that we received from the intent, coordinates and
            * radius to the values used above. Currently the geofence is set to never expire on it's
            * own, which is useful for including a DWELL type transition, for testing, but can be
            * removed from the final version. The loitering delay, in milliseconds is how long you
            * spend within the geofence before the dwell transition triggers.
            * */

            GeofenceList.add(new Geofence.Builder()
                    .setRequestId(StopNames.get(i))
                    .setCircularRegion(Coordinates.get(i).latitude, Coordinates.get(i).longitude, RadiusList.get(i).intValue())
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setLoiteringDelay(30000)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build());

            /* Now that we've built our geofence object, we call the GeofenceStore object and pass
            in our list of geofences and the context of this activity. That context is used to set
            up the pending intent that handles our alarm and notifications, and eventually to cancel
            that pending intent. */

            GeofenceListtore = new GeofenceStore(this, GeofenceList);

            /* In order to display the marker and radius of the geofence on the map at all times
            once the geofence is active, we need to store the coordinates and radius in shared
            preferences, first deleting any existing values. */

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

        } else { // In other words, if the intent did not include new coordinates.

            /* We fetch the saved coordinates, and put them in the coordinates and radius list.
            The geofence itself doesn't need to be remade, it's running away in the background
            itself, but the map needs this values for the visual elements like markers and circles
            that would dissappear between instances of this map activity.
             */
            double lat = Double.parseDouble(prefs.getString("Lat", "0"));
            double lng = Double.parseDouble(prefs.getString("Lng", "0"));

            Coordinates.add(new LatLng(lat, lng));
            RadiusList.add(RadDef);
            StopNames.add(prefs.getString("ID", "Place!"));
        }

        /* If we are sent to this activity as a result of the pendingintent activating the geofence
         * intent service, the intent will have an Alert extra. This will cause the alarm dialog
          * fragment to show.*/

        if (gotHereFrom.hasExtra("Alert")) {
            SharedPreferences.Editor editor = prefs.edit();
            if (!(prefs.getBoolean("AlarmOn", false))) {
                editor.putBoolean("AlarmOn", true);
                editor.apply();
            }

            FragmentTransaction ft = getFragmentManager().beginTransaction();
            newAlarmFrag = new AlarmFragment();
            newAlarmFrag.show(ft, "Alarm");
        } else if (prefs.getBoolean("AlarmOn", false)) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            newAlarmFrag = new AlarmFragment();
            newAlarmFrag.show(ft, "Alarm");
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "onStart");

        /* We need to disconnect the API clients to stop confusion for future calls. This if there
        was a new geofence attached we disconnect it's API, and we always dissconnect the API which
        finds our initial position.
         */

        if (GeofenceListtore!= null) {
            GeofenceListtore.disconnect();
        }

        aGoogleApiClient.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "onResume");

        /* When we resume this activity, we if Google play services are available, and if they are
        we set up the map if there isn't one already running. */

        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            setUpMapIfNeeded();
        } else {
            Log.e(TAG, "GooglePlayServices not available.");
            GooglePlayServicesUtil.getErrorDialog(
                    GooglePlayServicesUtil.isGooglePlayServicesAvailable(this),
                    this, 0);
        }
    }

    private void setUpMapIfNeeded() {
        /* We first check if there is alread and instance of the map */
        Log.v(TAG, "Checking map setup.");
        if (Map == null) {

            // If there isn't we try to obtain the map from the SupportMapFragment.
            Map = ((SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.home_map)).getMap();

            // Check if we were successful in obtaining the map.
            if (Map != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        Log.v(TAG, "Setting up map.");

        /* We connect to the Api client, and request the location services API. If this is built
        correctly we connect to the API client, and the result of that connection, and the camera
        sweep to the current location is handled there. If that doesn't happen the default position
        of the camera is set to Westmoreland St. Dublin.
         */

        aGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext()).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(LocationServices.API).build();

        if (aGoogleApiClient != null) {
            aGoogleApiClient.connect();
        } else {
            Toast.makeText(getApplicationContext(), "You are NOT connected to the Api client.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Google Api Client build failed.");
        }

        //53.346612, -6.259147

        /* The Hybrid map has: Satellite photograph data with road maps added. Road and feature
        labels are also visible. */
        Map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        /* Indoor enabling is off so that zooming doesn't trigger transitions to floor plans or
        extra complications un-needed in this map. */
        Map.setIndoorEnabled(false);

        /* This adds the compass button that allows the user to always center the map on their
        current position. We will later add a button that moves to map to their intended stop.
         */
        Map.setMyLocationEnabled(true);

        /* This adds the + and - buttons for zooming. */

        Map.getUiSettings().setZoomControlsEnabled(true);

        // Camera starts over Dublin, but should quickly move to user's location.
        Map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(53.346612, -6.259147), 15));

        /* The map is responsive to changes in camera orientation, position, etc. This is
        implemented in onCameraChange. */
        Map.setOnCameraChangeListener(this);

        /*Allow marker clicks to be heard. */
        Map.setOnMarkerClickListener(this);
    }

    /* API Client set-up methods. Used for the two GoogleApi implementations. */

    @Override
    public void onConnected(Bundle bundle) {
        Log.v(TAG, "Google Api Client connected, accessing location data.");

        /* We used the FusedLocationAPI to find the user's location, then move the map's camera
        there. */

        lastLocation = LocationServices.FusedLocationApi.getLastLocation(aGoogleApiClient);
        Map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 15));
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v(TAG, "Google Api Client connection suspended.");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "Google Api Client connection failed.");
    }

    @Override
    public void onCameraChange(CameraPosition position) {

        /* We clear old Circles and markers that remain after camera changes, then redraw them,
        to avoid unpredictable dissappearances of these UI features. */

        Map.clear();
        if (Coordinates != null) {
            for (int i = 0; i < Coordinates.size(); i++) {
                Map.addCircle(new CircleOptions().center(Coordinates.get(i))
                        .radius(RadiusList.get(i).intValue())
                        .fillColor(0x88ffa500) // Orange.
                        .strokeColor(Color.TRANSPARENT).strokeWidth(2));
                Map.addMarker(new MarkerOptions().position(Coordinates.get(i))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .title(StopNames.get(i)));
            }
        }
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        Toast.makeText(HomeScreen.this, "This is " + marker.getTitle() + " isn't that cool!?", Toast.LENGTH_SHORT).show();
        return false;
    }


    /* This method is called in the alarm fragment, it recreates and overrides the pending intent
    that triggers the alarm then deletes it so that no more transition events will trigger the alarm.
    It also removes the saved values from the coordinates in shared preferences.
     */

    public void cancelPendingIntent() {
        Log.v(TAG, "Cancelling pending intent, and clearing shared preferences.");
        Intent intent = new Intent(this, GeofenceIntentService.class);
        PendingIntent mPendingIntent = PendingIntent.getService(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mPendingIntent.cancel();

        // We remove values from the coordinates list and markers immediately so the map updates nicely.

        Coordinates = null;
        RadiusList = null;
        Map.clear();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("savedCoords");
        editor.remove("ID");
        editor.remove("Lat");
        editor.remove("Lng");
        editor.apply();
    }

    /* ALARM STUFF */

    /* This method tells the MediaPlayer where to get it's file from, then sets up an AudioManager
     * which allows control of volume and ring services.
     * It then checks that alarm volume isn't zero, before player the Uri alert in the alarm stream.*/

    public void play() {
        //Instantiate player.

        Context context = this;

        Uri alert = getAlarmSound();

        player = new MediaPlayer();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        try {
            player.setDataSource(context, alert);
            final AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audio.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                player.setAudioStreamType(AudioManager.STREAM_ALARM);
                player.setLooping(true);

                player.prepare();
                player.start();
            }
        } catch (IOException e) {
            Log.e("Error....", "Check code...");
        }

    }

    /* This method finds the alarm sound set on the system, and returns a Uri link to that alarm
    sound. */

    private Uri getAlarmSound() {

        //RingtoneManager fetchs the default ringtone for the system.
        Uri alertSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);

        // If there isn't a default alarm resource...
        if (alertSound == null) {
            // Use the notification sound.
            alertSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            // If there also isn't a notification sound...
            if (alertSound == null) {
                // Use the ringtone.
                alertSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }
        return alertSound;
    }

    public void stopAlarm() {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        if (prefs.getBoolean("AlarmOn", false)) {
            editor.remove("AlarmOn");
            editor.apply();
        }

        player.stop();
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

    //Go to Alarms activity method
    public void goToAlarms(View view)
    {
        Intent intentAlarms = new Intent(HomeScreen.this, AlarmsActivity.class);
        startActivity(intentAlarms);
    }

    //Go to Settings activity method
    public void goToSettings (View view)
    {
        Intent intentSettings = new Intent(HomeScreen.this, SettingsActivity.class);
        startActivity(intentSettings);
    }
}
