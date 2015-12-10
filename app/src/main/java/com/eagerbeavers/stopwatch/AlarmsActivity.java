package com.eagerbeavers.stopwatch;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;

public class AlarmsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarms);
    }



    //Button buttonEditActivity = (Button) findViewById(R.id.buttonEditActivity);



    //Go to Edit activity method
    public void goToEdit(View view)
    {
        Intent intentAlarms = new Intent(AlarmsActivity.this, EditActivity.class);
        startActivity(intentAlarms);
    }
}
