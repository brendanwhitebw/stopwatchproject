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
import android.widget.TextView;
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

        // Settinng up Toolbar.

        Toolbar toolbar = (Toolbar) findViewById(R.id.settingsActToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // The buttons and numberpicker used in the radius settings.

        Button radSetBtn = (Button) findViewById(R.id.setAlarmRadiusButton);
        radSetBtn.setOnClickListener(this);
        Button radDefBtn = (Button) findViewById(R.id.setAlarmRadiusDefBtn);
        radDefBtn.setOnClickListener(this);
        radPicker = (NumberPicker) findViewById(R.id.radNumberPicker);
        radPicker.setMaxValue(20);
        radPicker.setMinValue(1);

        // The track name text view for the sound settings. Displays the current alarm.

        TextView trackName = (TextView) findViewById(R.id.textViewCurrentAlarmSound);

        // Preference manager is used to import and export saved settings.

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        /* If the user already set a radius, it is used here instead of the default. Note the
         * conversion from metres in the actual stored value, and kilometres in the displayed values.
         */

        if (prefs.contains("Radius")) {
            radPicker.setValue(prefs.getInt("Radius", 4000)/1000);
        } else {
            radPicker.setValue(4);
        }

        /* If the user has changed the default alarm, their current alarm will be set in track name. */

        if (prefs.contains("CurrentAlarmTrackName")) {
            trackName.setText(prefs.getString("CurrentAlarmTrackName", "Phone's Alarm Sound"));
        } else {
            trackName.setText("Phone's Alarm Sound");
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        // Again using shared preferences to store and access saved settings info.

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        switch (id) {
            case R.id.setAlarmRadiusButton:

                /* Set alarm button takes the value currently on the number picker, and saves it
                 * times 1000 as a metre value to be accessed by HomeScreen when creating geofences.
                 */

                if (prefs.contains("Radius")) { // Clear current value first.
                    editor.remove("Radius");
                    editor.apply();
                }
                editor.putInt("Radius", (1000*radPicker.getValue()));
                editor.apply();

                Toast.makeText(getApplication(), "Radius set!", Toast.LENGTH_LONG).show(); //Inform user of success.
                break;

            case R.id.setAlarmRadiusDefBtn:

                /* Resetting the value to it's default, removing the saved value, and setting the
                 * picker to 4.
                 */

                if (prefs.contains("Radius")) {
                    editor.remove("Radius");
                    editor.apply();
                }
                radPicker.setValue(4);

                Toast.makeText(getApplication(), "Radius reset to 4 km!", Toast.LENGTH_LONG).show();
                break;
        }
    }

    /* This button will take the user to the SoundChoice activity where they can browse and select audio files from the system. */

    public void chooseSound (View v) {
        Intent changeSoundIntent = new Intent(this, SoundChoice.class);
        changeSoundIntent.putExtra("AlarmToBeChanged", "DefaultAlarm");
        startActivity(changeSoundIntent);
    }

    /* This button removes the alarm id (a long reference to a URI) from Shared preferences,
     * as well as the stored track name used to set the text view here. It then manually resets
     * the track name display to the default value.
     */

    public void resetAlarmSound (View v) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        if (prefs.contains("DefaultAlarm")) {
            editor.remove("DefaultAlarm");
            editor.apply();
        }

        if (prefs.contains("CurrentAlarmTrackName")) {
            editor.remove("CurrentAlarmTrackName");
            editor.apply();
        }

        TextView trackName = (TextView) findViewById(R.id.textViewCurrentAlarmSound);
        trackName.setText("Phone's Alarm Sound");
    }
}
