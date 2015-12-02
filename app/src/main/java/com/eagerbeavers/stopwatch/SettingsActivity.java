package com.eagerbeavers.stopwatch;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.settingsActToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button radSetBtn = (Button) findViewById(R.id.setAlarmRadiusButton);
        Button radDefBtn = (Button) findViewById(R.id.setAlarmRadiusDefBtn);
        NumberPicker radPicker = (NumberPicker) findViewById(R.id.radNumberPicker);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (prefs.contains("Radius")) {
            radPicker.setValue(prefs.getInt("Radius", 4000)/1000);
        } else {
            radPicker.setValue(4);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        switch (id) {
            case R.id.setAlarmRadiusButton:
                NumberPicker radPicker = (NumberPicker) findViewById(R.id.radNumberPicker);

                if (prefs.contains("Radius")) {
                    editor.remove("Radius");
                    editor.apply();
                }
                editor.putInt("Radius", (1000*radPicker.getValue()));
                editor.apply();

                Toast.makeText(getApplication(), "Radius changed!", Toast.LENGTH_LONG).show();
                break;
            case R.id.setAlarmRadiusDefBtn:
                if (prefs.contains("Radius")) {
                    editor.remove("Radius");
                    editor.apply();
                }
                editor.putInt("Radius", 4000);
                editor.apply();
                Toast.makeText(getApplication(), "Radius reset to 4 km!", Toast.LENGTH_LONG).show();

        }
    }
}
