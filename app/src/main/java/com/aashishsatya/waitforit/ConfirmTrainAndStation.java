package com.aashishsatya.waitforit;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;


public class ConfirmTrainAndStation extends ActionBarActivity {

    String jsonStr;
    String trainNameStr;
    String stationNameStr;
    String trainNoStr;
    String etaStr;

    // needed for setting the alarm
    String actTimeArrivalStr;

    Context thisContext = this;

    int selectedStationPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_train_and_station);

        // get the three text fields that need setting
        TextView trainNo = (TextView) findViewById(R.id.train_no);
        TextView stationName = (TextView) findViewById(R.id.station_name);
        TextView trainName = (TextView) findViewById(R.id.train_name);
        TextView eta = (TextView) findViewById(R.id.eta_tv);

        // get the strings and position
        Intent intent = getIntent();

        jsonStr = intent.getStringExtra(SetTrainAndStation.ALL_STATION_DETAILS_JSON_STR);
        trainNameStr = intent.getStringExtra(SetTrainAndStation.TRAIN_NAME);
        stationNameStr = intent.getStringExtra(SetTrainAndStation.SELECTED_STATION_NAME);
        trainNoStr = intent.getStringExtra(SetTrainAndStation.TRAIN_NO);
        etaStr = intent.getStringExtra(SetTrainAndStation.STATION_ETA);
        selectedStationPosition = intent.getIntExtra(SetTrainAndStation.SELECTED_STATION_POSITION, 0);
        Log.d("CTASetaStr>", etaStr);

        // set the relevant text fields
        stationName.setText(stationNameStr);
        trainNo.setText(trainNoStr);
        trainName.setText(trainNameStr);
        eta.setText(etaStr);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_confirm_details, menu);
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

    public void onConfirm(View view)
    {
        // write details to file
        new WriteToFile().execute();
    }

    public class WriteToFile extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            // set the alarm

            // write the details to file as OUR OWN (NEW) JSON string
            // which contains just the relevant details

            /* We need to write
            Position
            Train number
            Actual departure
            Scheduled departure
            Actual arrival
            Scheduled arrival
            Station name
             */

            String stringToWriteToFile = "";

            try
            {
                // unfortunately we need the JSONObject as such
                JSONObject jsonObject = new JSONObject(jsonStr);
                String trainStatus = jsonObject.getString(SetTrainAndStation.TAG_TRAIN_STATUS);

                JSONArray allStationDetails = jsonObject.getJSONArray(SetTrainAndStation.TAG_ALL_STATION_DETAILS);
                JSONObject reqdStationJSONObj = allStationDetails.getJSONObject(selectedStationPosition);

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
                stringToWriteToFile += ":" + Integer.toString(selectedStationPosition);

                stringToWriteToFile += "}" + '\n';

                Log.d("CTASstringToWrite>", stringToWriteToFile);

                try
                {
                    JSONObject test = new JSONObject(stringToWriteToFile);
                    Log.d("CTASJSON>", "Successful JSON String generated");
                }
                catch (JSONException e)
                {
                    Log.d("CTASJSONError>", "Error in generated string for JSON");
                }

                FileOutputStream fOut;
                try
                {
                    fOut = openFileOutput(SetTrainAndStation.FILENAME, MODE_WORLD_READABLE);
                    try
                    {
                        fOut.write(stringToWriteToFile.getBytes());
                        fOut.close();
                    }
                    catch (java.io.IOException e)
                    {
                        Log.d("File Error: ", e.getMessage());
                        e.printStackTrace();
                    }
                }
                // this catch is for checking exceptions while opening the file
                catch (FileNotFoundException e)
                {
                    Log.d("File not found: ", e.getMessage());
                    e.printStackTrace();
                }

            }
            catch (JSONException e)
            {
                Log.d("Error writing file:", e.getMessage());
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            // start the ActiveAlarm activity
            Intent intent = new Intent(thisContext, ActiveAlarm.class);
            startActivity(intent);

        }
    }
}
