package com.eagerbeavers.stopwatch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

/* This fragment has two main functions, it displays a fragment telling the user the alarm is on
 * and a button allowing them to turn it off. The fragment also has a Fragment Callback, which
 * allows the fragment to call static methods from the class it's attached to, i.e. HomeScreen.
 * It uses these methods to control the media player, and cancel the pending intent that triggered
 * the alarm in the first place.
 */

public class AlarmFragment extends DialogFragment {

    final String TAG = "stopwatch.AlarmFragment";

    // Boolean recording whether Alarm has been stopped.

    boolean stopped = false;

    PowerManager pm;
    PowerManager.WakeLock wakeLock;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_alarm, container, false);
        getDialog().setTitle("YOU HAVE ARRIVED!");


        /* Access the power service, wake up the phone, and keep it awake until the wakelock is
         * unlocked.
         */

        pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wakeLock.acquire();

        /* Access the play method from the HomeScreen using the Fragment Callback interface.
         * This starts the alarm sound playing.
         */

        callback.play();

        /* The button used to stop the alarm playing and cancel it's geofence intent.
         * */

        Button stopBtn = (Button) rootView.findViewById(R.id.StopAlarmBtn);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.cancelPendingIntent();
                callback.stopAlarm();
                stopped = true; // We have stopped the alarm deliberately now.
                wakeLock.release(); // The phone may sleep once the alarm is off again.
                dismiss();
            }
        });

        return rootView;
    }

    /* This interface is implemented by HomeScreen, so that once this Fragment attachs to that
     * class we can access the static methods found therein. This makes dealing with pending intents
     * and Media players much easier than trying to access them directly from this Fragment.
     */

    public interface FragmentCallBack {
        public void cancelPendingIntent(); // See homeScreen for the details, the purpose of each is self-explanatory.
        public void play();
        public void stopAlarm();
    }

    private FragmentCallBack callback; // An instance of the callback interface we then attach to HomeScreen once the Fragment attaches.

    @Override
    public void onAttach(Activity activity) {
        callback = (FragmentCallBack) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.v(TAG, "onDestroyView");

        /* If the user accidentally leaves the alarm fragment, they are told as much, and the alarm
         * will be able to trigger again. This won't happen if they have deliberately cancelled the
         * alarm.
         */

        if (!stopped) {
            Toast.makeText(getActivity(), "You haven't stopped the Alarm correctly!!!", Toast.LENGTH_SHORT).show();
            callback.stopAlarm();
        }

        /* For power preservation reasons, we also need to cancel the wakelock if they accidentally
         * the screen so their device isn't trapped awake. In fact wakelocks that can be indefinite
         * provide errors themselves.
         */

        if (wakeLock != null) {
            try {
                wakeLock.release();
            } catch (Exception e) {
                Log.e(TAG, "Wakelock under locked, couldn't release.");
            }
        }
    }
}
