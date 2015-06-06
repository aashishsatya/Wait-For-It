package com.aashishsatya.waitforit;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

// this alarm shows the activated alarm and the countdown timer
// the countdown timer is used to update on a frequent basis

public class ActiveAlarm extends ActionBarActivity {

    String trainNoStr;
    String trainNameStr;
    String stationNameStr;
    String jsonStr;
    String actTimeArrivalStr;
    String schTimeArrivalStr;
    String actTimeDeptStr;
    String schTimeDeptStr;

    long TEN_MINUTES_IN_MILLI_SECONDS = 8000;
    Context thisContext = this;

    static PendingIntent destReachedPIntent;
    static PendingIntent updateAlarmPIntent;

    static AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_alarm);

        /* Read the details from the file.
        This is needed because we could be coming straight to this activity
        from MainActivity once it detects that the alarms have already been set
        */

        new ReadFromFile().execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_set_alarm, menu);
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

    public void onCancel(View view)
    {
        // cancel the set alarm

        if (alarmManager != null)
            alarmManager.cancel(destReachedPIntent);

        // delete the file with the details
        File dir = getFilesDir();
        File file = new File(dir, SetTrainAndStation.FILENAME);
        boolean deleted = file.delete();

        // go to the SetTrainAndStation activity to enable users to select a new alarm
        // Intent startAgainIntent = new Intent(this, SetTrainAndStation.class);
        // startActivity(startAgainIntent);
    }

    public class ReadFromFile extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            String filename = SetTrainAndStation.FILENAME;
            jsonStr = "";

            try
            {
                FileInputStream fis = thisContext.openFileInput(filename);
                InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(isr);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null)
                {
                    sb.append(line).append("\n");
                }
                jsonStr = sb.toString();
            }
            catch (IOException e)
            {
                Log.d("ActiveAlarmIOError", e.getMessage());
                e.printStackTrace();
            }

            // load the details and light 'em up

            try
            {
                JSONObject jsonObject = new JSONObject(jsonStr);

                actTimeArrivalStr = jsonObject.getString(SetTrainAndStation.TAG_ACT_ARR);
                schTimeArrivalStr = jsonObject.getString(SetTrainAndStation.TAG_SCH_ARR);
                actTimeDeptStr = jsonObject.getString(SetTrainAndStation.TAG_ACT_DEPT);
                schTimeDeptStr = jsonObject.getString(SetTrainAndStation.TAG_SCH_DEPT);
                trainNameStr = jsonObject.getString(SetTrainAndStation.TRAIN_NAME);
                trainNoStr = jsonObject.getString(SetTrainAndStation.TAG_TRAIN_NO);
                stationNameStr = jsonObject.getString(SetTrainAndStation.TAG_STATION_NAME);

                Log.d("actTimeArr>", actTimeArrivalStr);
                Log.d("schTimeArr>", schTimeArrivalStr);
                Log.d("actTimeDept>", actTimeDeptStr);
                Log.d("schTimeDept>", schTimeDeptStr);
                Log.d("Train name>", trainNameStr);
                Log.d("Train no>", trainNoStr);

            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            Log.d("actTimeArrivalAlarm>", actTimeArrivalStr);

            // set the alarm
            if (actTimeArrivalStr.contains("E.T.A.: "))
            {
                Log.d("ETA Flag:", "ETA detected");
                // remove the ETA sign
                actTimeArrivalStr = actTimeArrivalStr.substring("E.T.A.: ".length());
                Log.d("UpdatedArrivalTime>", actTimeArrivalStr);

            }

            // debug

            String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
            actTimeArrivalStr = date + " " + "8:20 PM"; // replace time by actTimeArrivalStr
            Log.d("ETAWithDate>", actTimeArrivalStr);

            // convert the date to SimpleDateFormat first
            SimpleDateFormat sdff = new SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.ENGLISH);
            Calendar arrivalTimeForAlarm = Calendar.getInstance();
            try
            {
                arrivalTimeForAlarm.setTime(sdff.parse(actTimeArrivalStr));
            }
            catch (ParseException e)
            {
                Log.d("TimeParseError>", e.getMessage());
                e.printStackTrace();
            }

                /* Retrieve a PendingIntent that will perform a broadcast
                alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                Intent alarmIntent = new Intent(thisContext, AlarmReceiver.class);
                pendingIntent = PendingIntent.getBroadcast(ActiveAlarm.this, 0, alarmIntent, 0);


                int interval = 8000;

                // set the single alarm

                //alarmManager.set(AlarmManager.RTC_WAKEUP, arrivalTimeForAlarm.getTime() - TEN_MINUTES_IN_MILLI_SECONDS, pendingIntent);
                //Toast.makeText(thisContext, "Single alarm set for " + actTimeArrivalStr, Toast.LENGTH_SHORT).show();

                // set the multiple alarm
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, pendingIntent);*/

            Log.d("CurrentSysTimeMS:", Long.toString(System.currentTimeMillis()));
            Log.d("ActTimeMS:", Long.toString(arrivalTimeForAlarm.getTimeInMillis()));


            // set the single alarm
            //Create a new PendingIntent and add it to the AlarmManager
            Intent destReachedIntent = new Intent(ActiveAlarm.this, AlarmReceiverActivity.class);
            destReachedPIntent = PendingIntent.getActivity(ActiveAlarm.this,
                    12345, destReachedIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            alarmManager =
                    (AlarmManager)getSystemService(Activity.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC_WAKEUP, arrivalTimeForAlarm.getTimeInMillis(),
                    destReachedPIntent);

            // set the multiple alarm
            Intent updateAlarmIntent = new Intent(ActiveAlarm.this, AlarmReceiver.class);
            updateAlarmPIntent = PendingIntent.getBroadcast(ActiveAlarm.this, 0, updateAlarmIntent, 0);
            alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, System.currentTimeMillis() + TEN_MINUTES_IN_MILLI_SECONDS,
                    TEN_MINUTES_IN_MILLI_SECONDS, updateAlarmPIntent);





            // set the text in the TextViews
            // set the train name
            TextView trainNameTV = (TextView) findViewById(R.id.train_name);
            trainNameTV.setText(trainNameStr);
            // set the train number
            TextView trainNoTV = (TextView) findViewById(R.id.train_no);
            trainNoTV.setText(trainNoStr);
            // set the station name
            TextView stationNameTV = (TextView) findViewById(R.id.station_name);
            stationNameTV.setText(stationNameStr);

        }
    }
}
