package com.aashishsatya.waitforit;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

// almost copied from http://javatechig.com/android/repeat-alarm-example-in-android
// also refer http://www.101apps.co.za/articles/scheduling-android-s-repeating-alarms.html

public class AlarmReceiver extends BroadcastReceiver {

    Context thisContext;

    @Override
    public void onReceive(Context context, Intent intent) {

        thisContext = context;

        Log.d("AlarmFlag>", "Repeating alarm sounded");

        new UpdateDetails().execute();

    }

    public class UpdateDetails extends AsyncTask<Void, Void, Void>
    {

        String jsonStr;
        String trainNameStr;
        String trainNoStr;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0)
        {
            // lots of things to be done

            // get the old details and the station number

            String actTimeArrivalStr = "";   // actual time of arrival
            String filename = SetTrainAndStation.FILENAME;
            int stationPosition = 0;
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
                Log.d("ARActiveAlarmIOError", e.getMessage());
                e.printStackTrace();
            }

            // load the details and light 'em up

            try
            {
                JSONObject jsonObject = new JSONObject(jsonStr);

                stationPosition = jsonObject.getInt(SetTrainAndStation.SELECTED_STATION_POSITION);
                trainNoStr = jsonObject.getString(SetTrainAndStation.TAG_TRAIN_NO);
                trainNameStr = jsonObject.getString(SetTrainAndStation.TRAIN_NAME);

                Log.d("ARTrain no>", trainNoStr);
                Log.d("ARStation position>", Integer.toString(stationPosition));
            }

            catch (JSONException e)
            {
                e.printStackTrace();
            }

            // cancel the set alarm

            try
            {
                ActiveAlarm.alarmManager.cancel(ActiveAlarm.destReachedPIntent);
                Log.d("ARErrorCancelledAlarm>", "Successfully cancelled single alarm");
            }
            catch (Exception e)
            {
                Log.d("ARErrorCancelingAlarm>", e.getMessage());
            }

            // delete the old file

            File dir = thisContext.getFilesDir();
            File file = new File(dir, SetTrainAndStation.FILENAME);
            boolean deleted = file.delete();

            // get the new details

            String url = "http://api.railwayapi.com/live/train/" + trainNoStr + "/doj/"
                    + SetTrainAndStation.date_str + "/apikey/" + SetTrainAndStation.API_KEY;

            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);

            JSONObject jsonObject = null;
            try
            {
                jsonObject = new JSONObject(jsonStr);
                JSONArray allStationsDetailsJSONArr = jsonObject.getJSONArray(SetTrainAndStation.TAG_ALL_STATION_DETAILS);
                JSONObject reqdStationJSONObj = allStationsDetailsJSONArr.getJSONObject(stationPosition);

                String stringToWriteToFile = "";

                try
                {

                    // write main details about the train
                    // CAREFUL WITH THE COMA
                    // write the train name
                    stringToWriteToFile += "{" + '"' + SetTrainAndStation.TRAIN_NAME + '"';
                    stringToWriteToFile += ":" + '"' + trainNameStr + '"' + ',';
                    // write the train number
                    stringToWriteToFile += '"' + SetTrainAndStation.TAG_TRAIN_NO + '"';
                    stringToWriteToFile += ":" + '"' + trainNoStr + '"' + ',';

                    // this is needed for setting the alarm
                    actTimeArrivalStr = reqdStationJSONObj.getString(SetTrainAndStation.TAG_ACT_ARR);

                    // get each of the station details one by one
                    // write "status" and station status
                    stringToWriteToFile += '"' + SetTrainAndStation.TAG_STATION_STATUS + '"';
                    stringToWriteToFile += ":" + '"' + reqdStationJSONObj.getString(SetTrainAndStation.TAG_STATION_STATUS) + '"' + ',';
                    // write "station" and station name
                    stringToWriteToFile += '"' + SetTrainAndStation.TAG_STATION_NAME + '"';
                    stringToWriteToFile += ":" + '"' + reqdStationJSONObj.getString(SetTrainAndStation.TAG_STATION_NAME) + '"' + ',';
                    // write "scharr" and scheduled arriving time
                    stringToWriteToFile += '"' + SetTrainAndStation.TAG_SCH_ARR + '"';
                    stringToWriteToFile += ":" + '"' + reqdStationJSONObj.getString(SetTrainAndStation.TAG_SCH_ARR) + '"' + ',';
                    // write "schdep" and scheduled departing time
                    stringToWriteToFile += '"' + SetTrainAndStation.TAG_SCH_DEPT + '"';
                    stringToWriteToFile += ":" + '"' + reqdStationJSONObj.getString(SetTrainAndStation.TAG_SCH_DEPT) + '"' + ',';
                    // write "actarr" and actual arriving time
                    stringToWriteToFile += '"' + SetTrainAndStation.TAG_ACT_ARR + '"';
                    stringToWriteToFile += ":" + '"' + reqdStationJSONObj.getString(SetTrainAndStation.TAG_ACT_ARR) + '"' + ',';
                    // write "schdep" and scheduled departing time
                    stringToWriteToFile += '"' + SetTrainAndStation.TAG_ACT_DEPT + '"';
                    stringToWriteToFile += ":" + '"' + reqdStationJSONObj.getString(SetTrainAndStation.TAG_ACT_DEPT) + '"' + ',';
                    // write "stationNo" and station position
                    stringToWriteToFile += '"' + SetTrainAndStation.SELECTED_STATION_POSITION + '"';
                    stringToWriteToFile += ":" + Integer.toString(stationPosition);

                    stringToWriteToFile += "}" + '\n';

                    Log.d("ARstringToWrite>", stringToWriteToFile);

                    try
                    {
                        JSONObject test = new JSONObject(stringToWriteToFile);
                        Log.d("ARJSON>", "Successful JSON String generated");
                    }
                    catch (JSONException e)
                    {
                        Log.d("ARJSONError>", "Error in generated string for JSON");
                    }

                    FileOutputStream fOut;
                    try
                    {
                        fOut = thisContext.openFileOutput(SetTrainAndStation.FILENAME, thisContext.MODE_WORLD_READABLE);
                        try
                        {
                            fOut.write(stringToWriteToFile.getBytes());
                            fOut.close();
                        }
                        catch (java.io.IOException e)
                        {
                            Log.d("ARFile Error: ", e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    // this catch is for checking exceptions while opening the file
                    catch (FileNotFoundException e)
                    {
                        Log.d("ARFile not found: ", e.getMessage());
                        e.printStackTrace();
                    }

                }
                catch (JSONException e)
                {
                    Log.d("ARError writing file:", e.getMessage());
                    e.printStackTrace();
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }

            // update the single alarm

            Log.d("ARactTimeArrivalAlarm>", actTimeArrivalStr);

            // set the alarm
            if (actTimeArrivalStr.contains("E.T.A.: "))
            {
                Log.d("ARETA Flag:", "ETA detected");
                // remove the ETA sign
                actTimeArrivalStr = actTimeArrivalStr.substring("E.T.A.: ".length());
                Log.d("ARUpdatedArrivalTime>", actTimeArrivalStr);

            }

            // debug

            String date = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
            actTimeArrivalStr = date + " " + actTimeArrivalStr; // replace time by actTimeArrivalStr
            Log.d("ARETAWithDateRepUpd>", actTimeArrivalStr);

            // convert the date to SimpleDateFormat first
            SimpleDateFormat sdff = new SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.ENGLISH);
            Calendar arrivalTimeForAlarm = Calendar.getInstance();
            try
            {
                arrivalTimeForAlarm.setTime(sdff.parse(actTimeArrivalStr));
            }
            catch (ParseException e)
            {
                Log.d("ARTimeParseError>", e.getMessage());
                e.printStackTrace();
            }

            // set the single alarm
            //Create a new PendingIntent and add it to the AlarmManager
            ActiveAlarm.alarmManager.set(AlarmManager.RTC_WAKEUP, arrivalTimeForAlarm.getTimeInMillis() - AlarmManager.INTERVAL_FIFTEEN_MINUTES,
                    ActiveAlarm.destReachedPIntent);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            //Toast.makeText(thisContext, "Done updation", Toast.LENGTH_LONG).show();
        }
    }
}