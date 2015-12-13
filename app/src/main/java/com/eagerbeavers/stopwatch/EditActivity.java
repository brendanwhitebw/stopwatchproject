package com.eagerbeavers.stopwatch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class EditActivity extends Activity {

    //Alarms DB

    AlarmsDB alDB;
    CheckBox onoff, vibrate;
    EditText editlabel;
    Button savealarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        alDB = new AlarmsDB(this);

        onoff = (CheckBox) findViewById(R.id.onoffbox);
        vibrate = (CheckBox) findViewById(R.id.vibratebox);
        editlabel = (EditText) findViewById(R.id.labeltext);
        savealarm = (Button) findViewById(R.id.savealarmbutton);

        AddAlarmsData();
    }

    //Button buttonLocationChoice = (Button) findViewById(R.id.buttonLocationChoice);

    //Go to LocationChoice activity method
    public void goToLocationChoice(View view)
    {
        Intent intentAlarms = new Intent(EditActivity.this, LocationChoice.class);
        startActivity(intentAlarms);
    }

    //Alarms Database Stuff

    public void AddAlarmsData() {
        savealarm.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int onffy = 0;
                        int vibry = 0;

                        if(onoff.isChecked() == true)
                        {
                            onffy = 1;
                        }
                        if(vibrate.isChecked() == true)
                        {
                            vibry = 1;
                        }

                        boolean isInserted = alDB.insertData(onffy, "Destination", "10", "Ringtone", vibry, editlabel.getText().toString());
                        if(isInserted == true)
                        {
                            Toast.makeText(EditActivity.this, "Alarm Saved", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

    }
}
