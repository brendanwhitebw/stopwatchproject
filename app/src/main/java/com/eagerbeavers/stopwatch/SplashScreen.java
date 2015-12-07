package com.eagerbeavers.stopwatch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class SplashScreen extends Activity {

    private final String TAG = "stopwatch.splash";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

    }

    @Override
    protected void onResume() {
        super.onResume();

        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Timer interrupted");
                }
                finally {
                    Intent start = new Intent(SplashScreen.this,HomeScreen.class);
                    startActivity(start);
                }
            }
        };

        timer.start();
    }
}
