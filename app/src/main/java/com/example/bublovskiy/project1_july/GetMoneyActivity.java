package com.example.bublovskiy.project1_july;


import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

public class GetMoneyActivity extends AppCompatActivity {

    TextView textViewCancel, textViewDone;
    EditText editTextGetMoney;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_money_activity);

        //prevent from going into landscape mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //init all views
        initTextViewEditTextEtc();
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


    //init text views and edit views
    private void initTextViewEditTextEtc() {
        textViewCancel = (TextView) findViewById(R.id.textViewCancel);
        textViewDone = (TextView) findViewById(R.id.textViewDone);
        editTextGetMoney = (EditText) findViewById(R.id.editTextGetMoney);

        //set custom font to all
        textViewCancel.setTypeface(MainActivity.customFont);
        textViewDone.setTypeface(MainActivity.customFont);
        editTextGetMoney.setTypeface(MainActivity.customFont);
    }

    //set balance and exit activity
    public void done(View view) {
        //get input
        int currentInput;
        //check if number entered
        //if not - set current input to 0
        if (!editTextGetMoney.getText().toString().equals("")) {
            currentInput = Integer.parseInt(editTextGetMoney.getText().toString());
        }
        //if number is empty
        else {
            currentInput = 0;
        }

        //set balance not less than min bid from MainActivity
        if (currentInput>=MainActivity.minBid) {
        MainActivity.textViewBalanceScore.setText(currentInput+"");
        //finish activity and return to the main screen
        this.finish();
        }
        //if the user did't put a number
        else {
            //Message for the toast: Minimum is ...
            Toast.makeText(this, getString(R.string.textForToast) + " " + MainActivity.minBid,Toast.LENGTH_SHORT).show();
        }
    }

    //cancel money input and return to the main screen
    public void cancel(View view) {
        this.finish();
    }

}
