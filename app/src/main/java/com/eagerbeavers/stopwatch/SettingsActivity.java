package com.eagerbeavers.stopwatch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    SharedPreferences prefs;
    NumberPicker radPicker;
    private String[] arraySpinner;
    Spinner ThemeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.settingsActToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Button ThemeButton = (Button) findViewById(R.id.ThemeButton);
        ThemeButton.setOnClickListener(this);
        Button radSetBtn = (Button) findViewById(R.id.setAlarmRadiusButton);
        radSetBtn.setOnClickListener(this);
        Button radDefBtn = (Button) findViewById(R.id.setAlarmRadiusDefBtn);
        radDefBtn.setOnClickListener(this);
        radPicker = (NumberPicker) findViewById(R.id.radNumberPicker);
        radPicker.setMaxValue(20);
        radPicker.setMinValue(1);

        this.arraySpinner = new String[] {"Theme 1", "Theme 2", "Theme 3"};  // setting up spinner
        ThemeSpinner = (Spinner) findViewById(R.id.ThemeSpinner);
        ArrayAdapter<String> ThemeAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, arraySpinner);
        ThemeSpinner.setAdapter(ThemeAdapter);

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

                if (prefs.contains("Radius")) {
                    editor.remove("Radius");
                    editor.apply();
                }
                editor.putInt("Radius", (1000*radPicker.getValue()));
                editor.apply();

                Toast.makeText(getApplication(), "Radius set!", Toast.LENGTH_LONG).show();
                break;
            case R.id.setAlarmRadiusDefBtn:
                if (prefs.contains("Radius")) {
                    editor.remove("Radius");
                    editor.apply();
                }
                radPicker.setValue(4);
                editor.putInt("Radius", 4000);
                editor.apply();
                Toast.makeText(getApplication(), "Radius reset to 4 km!", Toast.LENGTH_LONG).show();
                break;
            case R.id.ThemeButton:
                String SpinnerText;
                SpinnerText = ThemeSpinner.getSelectedItem().toString();
                if(SpinnerText.equals("Theme 1")){

                    // change theme 1 here
                }
                else if(SpinnerText.equals("Theme 2")){

                    // change theme 2 here
                }
                else{

                    // change to theme 3 here
                }

                break;
        }
    }

    public void chooseSound (View v) {
        Intent changeSoundIntent = new Intent(this, SoundChoice.class);
        changeSoundIntent.putExtra("AlarmToBeChanged", "DefaultAlarm");
        startActivity(changeSoundIntent);
    }
}
