package com.eagerbeavers.stopwatch;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;

/* This class, which is always executed in the Context of the HomeScreen, does the actual
 * work of setting up the geofence, and it's related service, using google API clients.
 */

public class GeofenceStore implements ConnectionCallbacks,
        OnConnectionFailedListener, ResultCallback<Status> {

    private final String TAG = this.getClass().getSimpleName();

    /* Context, again note, context here means the HomeScreen the user has started. */

    private Context Context;

    /* Google API client object. */

    private GoogleApiClient GoogleApiClient;

    /* Geofencing PendingIntent, allowing the service to hold an intent at the ready for activation
     * at a later time.
     */

    public PendingIntent PendingIntent;

    /* List of geofences to monitor. */

    private ArrayList<Geofence> Geofences;

    /* Geofence request. */

    private GeofencingRequest GeofencingRequest;

    /* Constructor for the geofenceStore. It takes in the context (HomeScreen) a list of Geofences,
    * as built in HomeScreen. */

    public GeofenceStore(Context context, ArrayList<Geofence> geofences) {
        // Initialisation of variables.

        Context = context;
        Geofences = new ArrayList<Geofence>(geofences);
        PendingIntent = null;

        /* Build a new GoogleApiClient, specify that we want to use LocationServices by adding the
         * API to the client, specify the connection callbacks are in this class as well as the
         * OnConnectionFailed method.
         */

        GoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //Connect to our just defined API client.

        GoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        /* If the Google API client successfully connects, we create a GeofencingRequest with the
         * geofences we been passed from HomeScreen.
         */

        GeofencingRequest = new GeofencingRequest.Builder().addGeofences(Geofences).build();

        /* We then call a method to set up our pending intent and return it to us. */

        PendingIntent = createRequestPendingIntent();

        /* We set up a pending result, telling the app to wait for the activation of the geofence
         * just established, and when it is triggered to activate the pending intent we just created.
         */

        PendingResult<Status> pendingResult = LocationServices.GeofencingApi
                .addGeofences(GoogleApiClient, GeofencingRequest, PendingIntent);

        //  We then set the result callbacks listener to this class.
        pendingResult.setResultCallback(this);
    }

    // The following methods are a part of implementing the API client and it's various functions.

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v(TAG, "Connection failed.");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.v(TAG, "Connection suspended.");
    }

    /**
     * This creates, and returns, a PendingIntent that is to be used when geofence transitions take
     * place. In this instance, we are using an IntentService, GeofenceIntentService, to handle the
     * transitions.
     */

    private PendingIntent createRequestPendingIntent() {
        if (PendingIntent == null) {
            Log.v(TAG, "Creating PendingIntent");

            /* It is important to note the pending intent is passing the context of HomeScreen
             * to the Service, which means the service can issue instructions as if it were an
             * intent in HomeScreen.
             */

            Intent intent = new Intent(Context, GeofenceIntentService.class);
            PendingIntent = PendingIntent.getService(Context, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        return PendingIntent;
    }


     /* This method relates to PendingResult in the onConnected method, and logs that result. It is
      * part of the ResultCallbacks interface.
      */

    @Override
    public void onResult(Status result) {
        if (result.isSuccess()) {
            Log.v(TAG, "Success!");
        } else if (result.hasResolution()) {
            // TODO Handle resolution
        } else if (result.isCanceled()) {
            Log.v(TAG, "Canceled");
        } else if (result.isInterrupted()) {
            Log.v(TAG, "Interrupted");
        } else {

        }
    }

    /* This method is called from HomeScreen when that activity is stopped to ensure the API client
     * disconnects cleanly and doesn't interfere with other processes or future calls.
     */

    public void disconnect() {
        Log.v(TAG, "API client disconnected");
        GoogleApiClient.disconnect();
    }
}

