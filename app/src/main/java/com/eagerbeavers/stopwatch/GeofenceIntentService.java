package com.eagerbeavers.stopwatch;

import java.util.List;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

public class GeofenceIntentService extends IntentService {

    // Log tag for this activity.

    private final String TAG = this.getClass().getCanonicalName();

    /* Default methods for the IntentService class. The service is created and runs away in the
    * background regardless of the users activities unless the user stops the app manually. */

    public GeofenceIntentService() {
        super("GeofenceIntentService");
        Log.v(TAG, "Constructor.");
    }

    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "onCreate");
    }

    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
    }

    /* When the pending intent given to the intent service is triggered, this method is run. */

    @Override
    protected void onHandleIntent(Intent intent) {

        /* A geofencing event must have triggered the intent's activation, so we get it's information
         * as passed here from GeofenceStore.
         */

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        Log.v(TAG, "onHandleIntent");

        // If there was nor error in the Geofence Event...

        if(!geofencingEvent.hasError()) {

            // Fetch the int used to denote a particular transition/trigger.
            int transition = geofencingEvent.getGeofenceTransition();
            String notificationTitle;

            // Use a switch statement to set a String as appropriate to the trigger's id constant.

            switch(transition) {
                case Geofence.GEOFENCE_TRANSITION_ENTER:
                    notificationTitle = "You've arrived at your destination!";
                    Log.v(TAG, notificationTitle);
                    break;
                case Geofence.GEOFENCE_TRANSITION_DWELL:
                    notificationTitle = "You're close!";
                    Log.v(TAG, notificationTitle);
                    break;
                case Geofence.GEOFENCE_TRANSITION_EXIT:
                    notificationTitle = "You may have missed your stop...";
                    Log.v(TAG, notificationTitle);
                    break;
                default:
                    notificationTitle = "Strange intent just triggered the alarm...";
            }

            // Call the sendNotification method, along with the newly assigned string.

            sendNotification(this, getTriggeringGeofences(intent), notificationTitle);
        }
    }

    /* This method is responsible for the actual actions resulting from a successfully identified
     * geofence event.
     */

    private void sendNotification(Context context, String notificationText,
                                  String notificationTitle) {

        // If the phone was asleep we wake it up and lock it awake for a few moments.

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wakeLock.acquire();

        /* We build a notification using the notification builder. This notification will appear
         * in the phone's notification bar, along with the notification title established in the
         * handle intent method.
         */

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_beaver_button_2) //Icon set to app logo.
                .setContentTitle(notificationTitle) //Title set to identify Geofence event.
                .setContentText(notificationText) //Text set to location of triggering event.
                .setDefaults(Notification.DEFAULT_ALL)
                .setAutoCancel(true); //Notification cancelled when clicked.

        /* The notification manager let's us actual send a notification using the system services. */

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());

        // Once the notification is built and sent we release the wakelock, so the device can sleep normally.

        wakeLock.release();

        /* Finally we send the user to the HomeScreen with an Alert extra, that will trigger the
         * alarm fragment in that activity.
         */

        Intent mapIntent = new Intent(getBaseContext(), HomeScreen.class);
        mapIntent.putExtra("Alert", true);
        mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mapIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        getApplication().startActivity(mapIntent);
    }

    /* This method goes through the list of geofences originally passed to this intent service
     * and finds the names of locations where the Geofence's where triggered. This is another
     * section designed to future proof against the need for multiple geofences at once.
     */

    private String getTriggeringGeofences(Intent intent) {
        GeofencingEvent geofenceEvent = GeofencingEvent.fromIntent(intent);
        List<Geofence> geofences = geofenceEvent
                .getTriggeringGeofences();

        String[] geofenceIds = new String[geofences.size()];

        for (int i = 0; i < geofences.size(); i++) {
            geofenceIds[i] = geofences.get(i).getRequestId();
        }

        return TextUtils.join(", ", geofenceIds);
    }
}