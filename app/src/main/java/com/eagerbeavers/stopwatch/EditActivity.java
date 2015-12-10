package com.eagerbeavers.stopwatch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class EditActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
    }

    //Button buttonLocationChoice = (Button) findViewById(R.id.buttonLocationChoice);

    //Go to LocationChoice activity method
    public void goToLocationChoice(View view)
    {
        Intent intentAlarms = new Intent(EditActivity.this, LocationChoice.class);
        startActivity(intentAlarms);
    }
}
