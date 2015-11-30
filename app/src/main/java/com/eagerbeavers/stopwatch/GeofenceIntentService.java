package com.eagerbeavers.stopwatch;

import java.util.List;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

public class GeofenceIntentService extends IntentService {

    private final String TAG = this.getClass().getCanonicalName();

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

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        Log.v(TAG, "onHandleIntent");
        if(!geofencingEvent.hasError()) {
            int transition = geofencingEvent.getGeofenceTransition();
            String notificationTitle;

            switch(transition) {
                case Geofence.GEOFENCE_TRANSITION_ENTER:
                    notificationTitle = "Geofence Entered";
                    Log.v(TAG, "Geofence Entered");
                    break;
                case Geofence.GEOFENCE_TRANSITION_DWELL:
                    notificationTitle = "Geofence Dwell";
                    Log.v(TAG, "Dwelling in Geofence");
                    break;
                case Geofence.GEOFENCE_TRANSITION_EXIT:
                    notificationTitle = "Geofence Exit";
                    Log.v(TAG, "Geofence Exited");
                    break;
                default:
                    notificationTitle = "Geofence Unknown";
            }

            sendNotification(this, getTriggeringGeofences(intent), notificationTitle);
        }
    }

    private void sendNotification(Context context, String notificationText,
                                  String notificationTitle) {

        PowerManager pm = (PowerManager) context
                .getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, "");
        wakeLock.acquire();

        /* EXTRA!! */
        /* There seems to be an issue with this that crashes the program.

        Intent notificationIntent = new Intent(getApplicationContext(), MapActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MapActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        */

        /* END EXTRA */

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                context).setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setDefaults(Notification.DEFAULT_ALL)
                //.setContentIntent(notificationPendingIntent) // EXTRA EXTRA!!!
                .setAutoCancel(false);

        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
        wakeLock.release();

        Intent mapIntent = new Intent(getBaseContext(), MapActivity.class);
        mapIntent.putExtra("Alert", true);
        mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mapIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        getApplication().startActivity(mapIntent);
    }

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