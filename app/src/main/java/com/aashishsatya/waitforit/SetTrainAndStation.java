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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class SetTrainAndStation extends ActionBarActivity {

    /* these (global?) variables (I felt) are needed because their values are computed in GetTrainDetails().execute().
    They need to be passed to the next activity (ConfirmTrainAndStation), for which we need the onConfirm function of this
    class to be aware of their details.
    I'm aware that using global constants is discouraged, so if someone has a better (by which I mean cleaner :-))
    idea as to how to go about this, I'll be happy to implement it.
     */

    String trainNoStr;
    String trainNameStr;
    static String url;
    String jsonStr;
    String etaStr;  // ETA in string

    static String API_KEY = "bin87gv4273";
    static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    static String date_str = sdf.format(new Date());

    // these are mostly for passing strings to other activities
    static String FILENAME = "waitdetails.txt";
    static String ALL_STATION_DETAILS_JSON_STR = "allstationdetailsjsonstr";
    static String SELECTED_STATION_POSITION = "selectedstationposition";
    static String SELECTED_STATION_NAME = "selectedstationname";
    static String TRAIN_NAME = "trainname";
    static String TRAIN_NO = "trainno";
    static String STATION_ETA = "stationeta";

    // these are for getting the required field from the JSON Object
    static String TAG_TOTAL = "total";
    static String TAG_ALL_STATION_DETAILS = "route";
    static String TAG_STATION_NAME = "station";
    static String TAG_TRAIN_STATUS = "position";
    static String TAG_ACT_DEPT = "actdep";
    static String TAG_SCH_DEPT = "schdep";
    static String TAG_ACT_ARR = "actarr";
    static String TAG_SCH_ARR = "scharr";
    static String TAG_TRAIN_NO = "train_number";
    static String TAG_STATION_STATUS = "status";

    Context thisContext = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_train_and_station);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_alarm, menu);
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

    public void onClick(View view)
    {

        // we still need the position number of the item selected on the spinner
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        int itemPosition = spinner.getSelectedItemPosition();
        String selectedStationNameStr = (String) spinner.getSelectedItem();

        // get the ETA

        Log.d("STASJSONStr>", jsonStr);

        etaStr = "";
        try
        {
            JSONObject jsonObject = new JSONObject(jsonStr);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_ALL_STATION_DETAILS);
            JSONObject reqdStationJSONObj = jsonArray.getJSONObject(itemPosition);
            etaStr = reqdStationJSONObj.getString(TAG_ACT_ARR);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }


        Intent confirmDetailsIntent = new Intent(this, ConfirmTrainAndStation.class);

        // put all the extras
        // put the JSON String that we obtained
        confirmDetailsIntent.putExtra(ALL_STATION_DETAILS_JSON_STR, jsonStr);
        // put the position number of the item that was selected
        confirmDetailsIntent.putExtra(SELECTED_STATION_POSITION, itemPosition);
        // put the train number
        confirmDetailsIntent.putExtra(TRAIN_NO, trainNoStr);
        // put the train name
        confirmDetailsIntent.putExtra(TRAIN_NAME, trainNameStr);
        // put the station name
        confirmDetailsIntent.putExtra(SELECTED_STATION_NAME, selectedStationNameStr);
        // put the ETA
        confirmDetailsIntent.putExtra(STATION_ETA, etaStr);

        // start the activity that lets user confirm the details
        startActivity(confirmDetailsIntent);

    }

    public void getStations(View view)
    {
        // we need to get the details off the JSON server

        // get the train number that was entered
        EditText trainNo = (EditText) findViewById(R.id.train_no);
        trainNoStr = trainNo.getText().toString();

        url = "http://api.railwayapi.com/live/train/" + trainNoStr + "/doj/"
                + date_str + "/apikey/" + API_KEY;

        new GetTrainDetails().execute();

    }

    /**
     * Async task class to get json by making HTTP call
     * */
    public class GetTrainDetails extends AsyncTask<Void, Void, Void> {

        public ArrayList<String> stationsListStrForSpinner;
        JSONArray allStationsDetailsJSONArr;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
            Log.d("STASjsonStr>", "Fresh from server: " + jsonStr);

            // jsonStr now has the string in JSON
            // now to create the spinner for selecting the station

            // build a new ArrayList for setting as options to the spinner
            stationsListStrForSpinner = new ArrayList<String>();
            JSONObject singleStationDetailJSONObj;

            try
            {
                JSONObject jsonObject = new JSONObject(jsonStr);
                allStationsDetailsJSONArr = jsonObject.getJSONArray(TAG_ALL_STATION_DETAILS);
                int noOfStations = jsonObject.getInt(TAG_TOTAL);

                //Log.d("StationDetails: ", allStationsDetailsJSONArr.toString());

                for (int i = 0; i < noOfStations; i++)
                {
                    singleStationDetailJSONObj = allStationsDetailsJSONArr.getJSONObject(i);
                    //Log.d("Adding ", singleStationDetailJSONObj.getString(TAG_STATION_NAME));
                    stationsListStrForSpinner.add(singleStationDetailJSONObj.getString(TAG_STATION_NAME));
                    //Log.d("Done ", "adding.");
                }

                // find the train name
                trainNameStr = "";
                String trainStatus = jsonObject.getString(TAG_TRAIN_STATUS);
                if (trainStatus.equals("null"))
                {
                    trainNameStr = "N/A";
                }
                else
                {
                    for (int i = 1; i < trainStatus.length() - 2; i++) {
                        if (trainStatus.charAt(i) == 'i' && trainStatus.charAt(i + 1) == 's' && trainStatus.charAt(i + 2) == ' ') {
                            // we've reached an 'is'
                            // we can stop
                            break;
                        }
                        trainNameStr += trainStatus.charAt(i);
                    }
                }

                //Toast.makeText(thisContext, "Making spinner", Toast.LENGTH_LONG).show();

            }
            catch (Exception e)
            {
                Log.d("JSON: ", jsonStr);
                Log.d("Error: ", e.getMessage());
                //Toast.makeText(thisContext, "Sorry, try again", Toast.LENGTH_LONG).show();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            Spinner spinner = (Spinner) findViewById(R.id.spinner);
            //Log.d("stationsListStrForSpinner = ", stationsListStrForSpinner.toString());

            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(thisContext, android.R.layout.simple_spinner_item, stationsListStrForSpinner);
            //Log.d("Here ", "is not the error");
            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
            spinner.setAdapter(spinnerArrayAdapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {

                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }

            });

        }

    }
}
