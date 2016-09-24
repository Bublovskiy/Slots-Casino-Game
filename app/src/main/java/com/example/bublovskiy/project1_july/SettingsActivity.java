package com.example.bublovskiy.project1_july;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    TextView textViewMinBid,textViewMaxBid,textViewSoundOnOff,textViewBack,textViewSave;
    EditText editTextMinBid,editTextMaxBid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        //prevent from going into landscape mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //init all views on the screen
        initAllViews();
    }


    @Override
    protected void onResume() {
        super.onResume();

        //start playing music if flag allows us to do so and the service is running
        if (MainActivity.isSoundOn.equals("yes") && new isMyServiceRunning(this).isRunning(MusicService.class) ) {
            MusicService.mediaPlayer.start();
        }

    }//end onResume

    @Override
    protected void onPause() {
        super.onPause();

        //if the service is running - pause the music
        if (new isMyServiceRunning(this).isRunning(MusicService.class) ) {
           // stopService(new Intent(this, MusicService.class));
            MusicService.mediaPlayer.pause();
        }

    }//end onPause



    //init all views
    private void initAllViews() {
        textViewMinBid = (TextView) findViewById(R.id.textViewMinBid);
        textViewMaxBid = (TextView) findViewById(R.id.textViewMaxBid);

        textViewSoundOnOff = (TextView) findViewById(R.id.textViewSoundOnOff);

        //check if sound is on or off to set the sign
        if (MainActivity.isSoundOn.equals("yes")) {
            //Sign: Sound on
            textViewSoundOnOff.setText(getString(R.string.textViewSoundOn));
        }
        else
        {
            //Sign: Sound off
            textViewSoundOnOff.setText(getString(R.string.textViewSoundOff));
        }

        textViewBack = (TextView) findViewById(R.id.textViewSettingsCancel);
        textViewSave = (TextView) findViewById(R.id.textViewSettingsDone);

        editTextMinBid = (EditText) findViewById(R.id.editTextMinBid);
        editTextMaxBid = (EditText) findViewById(R.id.editTextMaxBid);

        //set min and max bids to current values
        editTextMinBid.setText(MainActivity.minBid+"");
        editTextMaxBid.setText(MainActivity.maxBid+"");

        //set custom font to all of them
        textViewMinBid.setTypeface(MainActivity.customFont);
        textViewMaxBid.setTypeface(MainActivity.customFont);
        textViewSoundOnOff.setTypeface(MainActivity.customFont);
        textViewBack.setTypeface(MainActivity.customFont);
        textViewSave.setTypeface(MainActivity.customFont);
        editTextMinBid.setTypeface(MainActivity.customFont);
        editTextMaxBid.setTypeface(MainActivity.customFont);

    }//end initAllViews

    //back to the main screen
    public void back(View view) {
        this.finish();
    }

    //save options
    public void save(View view) {
        //set min bid
        if (!editTextMinBid.getText().toString().equals("")) {
            int newMinBid = Integer.parseInt(editTextMinBid.getText().toString());
            //set new min bid from the main activity
            MainActivity.minBid = newMinBid;
        } else {
            //Message: Min Bid was empty -(
            Toast.makeText(this,getString(R.string.emptyMinBidMessage),Toast.LENGTH_SHORT).show();
        }

        //set max bid
        if (!editTextMaxBid.getText().toString().equals("")) {
            int newMaxBid = Integer.parseInt(editTextMaxBid.getText().toString());
            //set new max bid from the main activity
            MainActivity.maxBid = newMaxBid;
        } else {
            //Message: Max Bid was empty -(
            Toast.makeText(this,getString(R.string.emptyMaxBidMessage),Toast.LENGTH_SHORT).show();
        }

        //finish the activity
        this.finish();

    }//end save

    //turn sound on-ff without affecting sound setting
    public void soundOnOff(View view) {
        //check if sound on - turn it off
        if (textViewSoundOnOff.getText().toString().toLowerCase().equals(getString(R.string.textViewSoundOn).toLowerCase())) {
            textViewSoundOnOff.setText(getString(R.string.textViewSoundOff));

            //pause playing sound
            MusicService.mediaPlayer.pause();

            //save the sound setting
            MainActivity.isSoundOn = "no";

        }
        //if not - turn it on
        else {
            textViewSoundOnOff.setText(getString(R.string.textViewSoundOn));

            //resume or start playing music
            //if the service has yet to be started - start it
            if (!new isMyServiceRunning(this).isRunning(MusicService.class)) {
                startService(new Intent(this, MusicService.class));
            }
            //if the service is already running - just resume playing the sound track
            else {
                MusicService.mediaPlayer.start();
            }

            //save the sound setting
            MainActivity.isSoundOn = "yes";

        }
    }//end soundOnOff

}
