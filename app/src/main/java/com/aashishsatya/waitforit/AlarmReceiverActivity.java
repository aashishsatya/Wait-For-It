package com.aashishsatya.waitforit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import java.io.File;
import java.io.IOException;
import java.util.Set;

public class AlarmReceiverActivity extends Activity {
    private MediaPlayer mMediaPlayer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_alarm_receiver);

        playSound(this, getAlarmUri());

        // cancel the other repeating alarm
        try
        {
            ActiveAlarm.alarmManager.cancel(ActiveAlarm.updateAlarmPIntent);
            Log.d("ARACldUpdateAlarm>", "Cancelled the repeating alarm after ringing");
            ActiveAlarm.alarmManager.cancel(ActiveAlarm.updateAlarmPIntent);
            Log.d("ARACldSingleAlarm>", "Cancelled the single alarm after ringing");
        }
        catch (Exception e)
        {
            Log.d("Error cancelling alarm>", e.getMessage());
        }

        // delete the file that stores the alarm details
        File dir = getFilesDir();
        File file = new File(dir, SetTrainAndStation.FILENAME);
        boolean deleted = file.delete();

        Log.d("Deleted file>", "file deleted");
    }

    private void playSound(Context context, Uri alert) {
        mMediaPlayer = new MediaPlayer();
        try {
            mMediaPlayer.setDataSource(context, alert);
            final AudioManager audioManager = (AudioManager) context
                    .getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            }
        } catch (IOException e) {
            System.out.println("OOPS");
        }
    }

    //Get an alarm sound. Try for an alarm. If none set, try notification, 
    //Otherwise, ringtone.
    private Uri getAlarmUri() {
        Uri alert = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_ALARM);
        if (alert == null) {
            alert = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            if (alert == null) {
                alert = RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            }
        }
        return alert;
    }

    public void onStopAlarm(View view)
    {
        mMediaPlayer.stop();

        // go to the activity that lets users set another alarm
        Intent setTrainAndStationIntent = new Intent(this, SetTrainAndStation.class);
        startActivity(setTrainAndStationIntent);

    }
}