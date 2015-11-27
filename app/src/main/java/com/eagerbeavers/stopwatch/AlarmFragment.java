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
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

public class AlarmFragment extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_alarm, container, false);
        getDialog().setTitle("Arrived!");

        Button stopBtn = (Button) rootView.findViewById(R.id.StopAlarmBtn);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "It lives!", Toast.LENGTH_SHORT).show();

                callback.cancelPendingIntent();

                dismiss();
            }
        });

        return rootView;
    }

    public interface FragmentCallBack {
        public void cancelPendingIntent();
    }

    private FragmentCallBack callback;

    @Override
    public void onAttach(Activity activity) {
        callback = (FragmentCallBack) activity;
        super.onAttach(activity);
    }

}
