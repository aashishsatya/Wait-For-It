package com.aashishsatya.waitforit;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Set;


public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* The idea is to check for the existence of the file "waitdetails.txt".
        If the file exists, then the user has already set an alarm, and if not,
        then we need to take the user to the activity that will let him/her set one.
         */

        Context context = this;

        try
        {
            FileInputStream fis = context.openFileInput(SetTrainAndStation.FILENAME);

            // file is available
            // send user to ActiveAlarm

            Intent intent = new Intent(this, ActiveAlarm.class);
            startActivity(intent);
        }
        catch (Exception e)
        {
            // no file
            // so send user to the activity that sets an alarm

            Intent intent = new Intent(this, SetTrainAndStation.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
