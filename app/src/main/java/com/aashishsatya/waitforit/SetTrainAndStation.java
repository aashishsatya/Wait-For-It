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
    String url;
    String jsonStr;

    // these are mostly for passing strings to other activities
    static String FILENAME = "waitdetails.txt";
    static String ALL_STATION_DETAILS_JSON_STR = "allstationdetailsjsonstr";
    static String SELECTED_STATION_POSITION = "selectedstation";
    static String SELECTED_STATION_NAME = "selectedstationname";
    static String TRAIN_NAME = "trainname";
    static String TRAIN_NO = "trainno";

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
        // read text from file waitdetails.txt and send to next activity
        // String jsonStr = "";
        /*
        try
        {
            FileInputStream fin = openFileInput(FILENAME);
            int c;
            try
            {
                while( (c = fin.read()) != -1)
                {
                    jsonStr = jsonStr + Character.toString((char)c);
                }
                fin.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            // delete file for now
            // we will remove this later and delete the file only when the alarm has
            // been set off
            File dir = getFilesDir();
            File file = new File(dir, FILENAME);
            boolean deleted = file.delete();


            //Toast.makeText(thisContext, jsonStr, Toast.LENGTH_LONG).show();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        */

        // we still need the position number of the item selected on the spinner
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        int itemPosition = spinner.getSelectedItemPosition();
        String selectedStationNameStr = (String) spinner.getSelectedItem();

        Intent confirmDetailsIntent = new Intent(this, ConfirmTrainAndStation.class);

        Log.d("jsonStrAash>:", jsonStr);
        Log.d("trainNameStr:>", trainNameStr);

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

        // start the activity that lets user confirm the details
        startActivity(confirmDetailsIntent);

    }

    public void getStations(View view)
    {
        // we need to get the details off the JSON server
        String API_KEY = "bin87gv4273";

        // get the train number that was entered
        EditText trainNo = (EditText) findViewById(R.id.train_no);
        trainNoStr = trainNo.getText().toString();

        /*

        Calendar date = Calendar.getInstance();

        Toast.makeText(this, String.valueOf(date.YEAR), Toast.LENGTH_LONG).show();
        Toast.makeText(this, String.valueOf(date.MONTH), Toast.LENGTH_LONG).show();
        Toast.makeText(this, String.valueOf(date.DAY_OF_MONTH), Toast.LENGTH_LONG).show();

        String date_str = "" + String.valueOf(date.YEAR) + String.valueOf(date.MONTH) + String.valueOf(date.DAY_OF_MONTH);

        */

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String date_str = sdf.format(new Date());

        url = "http://api.railwayapi.com/live/train/" + trainNoStr + "/doj/"
                + date_str + "/apikey/" + API_KEY;

        new GetTrainDetails().execute();

    }

    /**
     * Async task class to get json by making HTTP call
     * */
    public class GetTrainDetails extends AsyncTask<Void, Void, Void> {

        //String jsonStr;    // the string obtained after the JSON Query

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
            //Log.d("jsonStr", "> " + jsonStr);

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

            /*

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    // Getting JSON Array node
                    contacts = jsonObj.getJSONArray(TAG_CONTACTS);

                    // looping through All Contacts
                    for (int i = 0; i < contacts.length(); i++) {
                        JSONObject c = contacts.getJSONObject(i);

                        String id = c.getString(TAG_ID);
                        String name = c.getString(TAG_NAME);
                        String email = c.getString(TAG_EMAIL);
                        String address = c.getString(TAG_ADDRESS);
                        String gender = c.getString(TAG_GENDER);

                        // Phone node is JSON Object
                        JSONObject phone = c.getJSONObject(TAG_PHONE);
                        String mobile = phone.getString(TAG_PHONE_MOBILE);
                        String home = phone.getString(TAG_PHONE_HOME);
                        String office = phone.getString(TAG_PHONE_OFFICE);

                        // tmp hashmap for single contact
                        HashMap<String, String> contact = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        contact.put(TAG_ID, id);
                        contact.put(TAG_NAME, name);
                        contact.put(TAG_EMAIL, email);
                        contact.put(TAG_PHONE_MOBILE, mobile);

                        // adding contact to contact list
                        contactList.add(contact);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }
            */

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

                    // write the details about the required station in a file
                    // this checks if an alarm exists or not from the first activity

                    // these exceptions probably won't even arise

                    /*

                    FileOutputStream fOut;
                    try
                    {
                        fOut = openFileOutput(FILENAME, MODE_WORLD_READABLE);
                        try
                        {
                            fOut.write(jsonStr.getBytes());
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

                    */
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }

            });

        }

    }
}
