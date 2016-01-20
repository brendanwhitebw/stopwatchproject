package com.eagerbeavers.stopwatch;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

/* This activity appears when the user opens the app. It displays a Splash screen, a full screen
 * image of the app's logo for an amount of time, then redirects the user to the main, or HomeScreen,
 * activity.
 */

public class SplashScreen extends Activity {

    // Tag used for console logs.

    private final String TAG = "stopwatch.splash";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The layout is a fullscreen image of the app logo which takes up the entire screen

        setContentView(R.layout.activity_splash_screen);

        /* When the app starts a seperate thread, a fork in the program flow is created.
         * This thread runs a method with a timer, which runs for 3000 milliseconds (3 s),
         * then initialises and uses and intent to direct the user to the HomeScreen.
         */

        Thread timer = new Thread() {
            public void run() {
                try {
                    sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Timer interrupted");
                } finally {
                    Intent start = new Intent(SplashScreen.this, HomeScreen.class);
                    startActivity(start);
                    finish();
                }
            }
        };

        /* Once the timer's contents and flow are determined, we have it initialised, the main
         * program flow won't actually do anything else, so the user will simply see the logo for
         * 3 seconds then be re-directed.
         */

        timer.start();

        // We tell the user that they must enable gps settings for this app.

        Toast toast = Toast.makeText(SplashScreen.this, "Remember: You must enable your GPS settings to use this app.", Toast.LENGTH_LONG);
        toast.show();

    }
}
