package com.aashishsatya.waitforit;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;


public class ConfirmDetails extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_details);

        // get the three text fields that need setting
        TextView trainNo = (TextView) findViewById(R.id.train_no);
        TextView stationName = (TextView) findViewById(R.id.station_name);
        TextView trainName = (TextView) findViewById(R.id.train_name);

        // get the strings
        Intent intent = getIntent();
        trainNo.setText(intent.getStringExtra(NewAlarm.TRAIN_NO));

        try
        {
            JSONObject jsonObject = new JSONObject(intent.getStringExtra(NewAlarm.STATION_DETAILS));
            JSONArray stationsDetails = jsonObject.getJSONArray(NewAlarm.TAG_STATIONS);
            JSONObject currentStation = stationsDetails.getJSONObject(intent.getIntExtra(NewAlarm.SELECTED_STATION, 0));
            // set the station name
            stationName.setText(currentStation.getString(NewAlarm.TAG_STATION_NAME));

            // find the train name
            String trainNameStr = "";
            String trainStatus = jsonObject.getString("position");
            if (trainStatus.equals("null"))
            {
                trainName.setText("N/A");
            }
            else
            {
                for (int i = 0; i < trainStatus.length() - 2; i++) {
                    if (trainStatus.charAt(i) == 'i' && trainStatus.charAt(i + 1) == 's' && trainStatus.charAt(i + 2) == ' ') {
                        // we've reached an is
                        // we can stop
                        break;
                    }
                    trainNameStr += trainStatus.charAt(i);
                }
                trainName.setText(trainNameStr);
            }

        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }


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
        // set the alarm

        Intent intent = new Intent(this, SetAlarm.class);
        startActivity(intent);
    }
}
