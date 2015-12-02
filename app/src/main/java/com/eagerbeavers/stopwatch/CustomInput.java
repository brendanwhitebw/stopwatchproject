package com.eagerbeavers.stopwatch;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class CustomInput extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_custom_input);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);


    }

    //Go to Map activity method
    public void gotToMap(View view)
    {
        Intent intentMap = new Intent(CustomInput.this, MapActivity.class);
        startActivity(intentMap);
    }

    //Go to Alarms activity method
    public void goToAlarms(View view)
    {
        Intent intentAlarms = new Intent(CustomInput.this, LocationChoice.class);
        startActivity(intentAlarms);
    }

}
