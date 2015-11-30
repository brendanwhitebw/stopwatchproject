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

public class AlarmFragment extends DialogFragment {

    final String TAG = "stopwatch.AlarmFragment";
    boolean stopped = false;

    PowerManager pm;
    PowerManager.WakeLock wakeLock;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_alarm, container, false);
        getDialog().setTitle("Arrived!");

        pm = (PowerManager) getActivity().getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wakeLock.acquire();

        callback.play();

        Button stopBtn = (Button) rootView.findViewById(R.id.StopAlarmBtn);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.cancelPendingIntent();
                callback.stopAlarm();
                stopped = true;
                wakeLock.release();
                dismiss();
            }
        });

        return rootView;
    }

    public interface FragmentCallBack {
        public void cancelPendingIntent();
        public void play();
        public void stopAlarm();
    }

    private FragmentCallBack callback;

    @Override
    public void onAttach(Activity activity) {
        callback = (FragmentCallBack) activity;
        super.onAttach(activity);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.v(TAG, "onDestroyView");
        if (!stopped) {
            Toast.makeText(getActivity(), "You haven't stopped the Alarm correctly!!!", Toast.LENGTH_SHORT).show();
            callback.stopAlarm();
        }
        wakeLock.release();
    }
}
