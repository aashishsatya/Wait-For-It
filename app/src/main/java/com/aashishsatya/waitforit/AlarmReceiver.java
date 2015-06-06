package com.aashishsatya.waitforit;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

// almost copied from http://javatechig.com/android/repeat-alarm-example-in-android
// also refer http://www.101apps.co.za/articles/scheduling-android-s-repeating-alarms.html

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // lots of things to be done

        // get the old details and the station name

        // get the new details

        // update the alarm

        // delete the old file

        // write the new file

        // but for now let's just show a toast

        Log.d("AlarmFlag>", "Alarm was here");
        Toast.makeText(context, "Alarm sounded", Toast.LENGTH_LONG).show();

    }
}
