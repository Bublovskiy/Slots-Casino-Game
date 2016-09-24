package com.example.bublovskiy.project1_july;

import android.content.Intent;
import android.os.AsyncTask;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.content.Context;

import java.net.URL;
import java.util.HashMap;
import java.util.Random;
import java.io.InputStream;
import android.util.DisplayMetrics;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;


public class MainActivity extends AppCompatActivity {

    //three flippers for numbers
    ViewFlipper viewFlipper1,viewFlipper2,viewFlipper3;
    //three threads for running numbers animations
    Thread numberThread1,numberThread2,numberThread3, animationInProgress;
    Runnable number1, number2,number3,enableButtonsBidAndGo,startRotationInProgress, checkWin;
    TextView textViewBid, textViewBidSign, textViewBalance
             ,popupTextViewNumber, textViewOops, textViewLowBalanceMessage,
             textViewMoney, textViewExit;

    //to get access from another activity
    static TextView textViewBalanceScore;

    Button buttonBidIncrease, buttonBidDecrease;
    ImageView imageViewInProgress;
    Animation animationOfProgress;
    PopupWindow popupWinWindow, popupWindowLowBalance, popupScoreChart;
    HashMap<String,Integer> rewordCombinations = new HashMap<>();
    static Typeface customFont;

    //min & max textViewBid
    static int minBid = 1;
    static int maxBid = 100;

    //by default all threads are running with no actions unless these property are true
    boolean startNumber1 = false, startNumber2 = false, startNumber3 = false, finishGame = false, startRotation = false;

    //Notification ON / OFF flag
    //String - because we want to write save it to the file (can't save boolean type)
    String isNotificationsAllowed = "yes";
    boolean shallSingOut = false;

    //Sound ON / OFF flag
    static String isSoundOn = "yes";

    //keep reference to app option menu
    Menu appMenu;

    //code for Google Api intent
    private static final int RES_CODE_SIGN_IN_MAIN_ACTIVITY = 999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        //prevent from going into landscape mode
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        initFlippers();
        initAllRunnablesAndThreds();
        initTextViewsButtonsImageViewsEtc();

        //start threads for three rotating numbers
        numberThread1.start();
        numberThread2.start();
        numberThread3.start();
        //start thread of rotating button
        animationInProgress.start();

    }//end onCreate


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
    
    //**********************************************************
    //************OVERRIDE TOOL BAR MENU METHODS****************
    //**********************************************************
    //show item in the menu by inflating the main layout with the menu layout
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    //Check if we need to change Notification ON -> Notification OFF
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        //find the items
        MenuItem itemPush = menu.findItem(R.id.push);
        MenuItem itmeUserProfile = menu.findItem(R.id.userProfile);
        MenuItem itemLogInOut = menu.findItem(R.id.itemLogInOut);

        //if notifications allowed - set it to Notifications ON else Notifications OFF
        if (isNotificationsAllowed.equals("yes")) {
            //Sign: Notifications ON
            itemPush.setTitle(getString(R.string.notificationON));
        }
        else {
            //Sign: Notifications OFF
            itemPush.setTitle(getString(R.string.notificationOFF));
        }

        //set log in / log out sign
        if (SignInActivity.userName.toLowerCase().equals(getString(R.string.userNameByDefault).toLowerCase())) {
            //set user name (name of signed in user ot Guest)
            itmeUserProfile.setTitle(SignInActivity.userName);
            //set sign inside the menu
            itemLogInOut.setTitle(getString(R.string.signedInWithGoogle));
        }
        else {
            itemLogInOut.setTitle(getString(R.string.signOut));
            //set user's picture (custom or default one)
            new UserPhotoLoader().execute(SignInActivity.userPhoto);
        }

        //save option menu in app instance
        appMenu = menu;

        return super.onPrepareOptionsMenu(menu);
    }


    //get user's photo asynchronously
    private class UserPhotoLoader extends AsyncTask<Uri, Void, Drawable> {
        @Override
        protected Drawable doInBackground(Uri... uris) {
            try {
                InputStream in = (InputStream) new URL(uris[0].toString()).getContent();
                Drawable userPic = Drawable.createFromStream(in,uris[0].toString());
                in.close();
                return userPic;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Drawable userPic) {
            if (userPic != null) {
                //load picture and user's name to the top menu item
                appMenu.findItem(R.id.userProfile).setIcon(userPic);
            }
            //in case the picture is null for amy reason - load the builtin picture.
            else {
                appMenu.findItem(R.id.userProfile).setIcon(getDrawable(R.drawable.default_user_pic));
            }
        }
    } //end UserPhotoLoader

    //navigate the toolbar menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            //sign in - out
            case R.id.itemLogInOut:
                //sign out
                if (item.getTitle().toString().toLowerCase().equals(getString(R.string.signOut).toLowerCase())) {
                  //set flag that before exit this activity we need to sign out the player
                  shallSingOut = true;
                  this.finish();
                }
                //sign in
                else {

                    //Start Google+ API
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(SignInActivity.mGoogleApiClient);
                    startActivityForResult(signInIntent, RES_CODE_SIGN_IN_MAIN_ACTIVITY);

                }
                return true;
            //show settings window
            case R.id.settings:
                //create new intent with SettingActivity class
                Intent settingIntent = new Intent(this,SettingsActivity.class);
                startActivity(settingIntent);
                return true;

            //show score chart
            case R.id.chart:
                //create new intent with SettingActivity class
                showPopupChart();
                return true;

            //show about info
            case R.id.about:
                //Message:  The Casino game was created by
                //          Maxim Bublovskiy
                //          bublowskiy@gmail.com
                //          Vancouver 2016
                Toast.makeText(this,getString(R.string.textAbout),Toast.LENGTH_LONG).show();
                return true;

            //change notification permission
            case R.id.push:
                //set isNotificationsAllowed to opposite value
                if (isNotificationsAllowed.equals("on")) {
                    isNotificationsAllowed = "off";
                    //Message:Pushup notifications OFF
                    Toast.makeText(this,getString(R.string.toastPushupNotificationsOFF), Toast.LENGTH_SHORT).show();
                }
                else {
                    isNotificationsAllowed = "on";
                    //Message:Pushup notifications ON
                    Toast.makeText(this,getString(R.string.toastPushupNotificationsON), Toast.LENGTH_SHORT).show();
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }//end onOptionsItemSelected


    //**********************************************************
    //************END OF OVERRIDING TOOL BAR MENU METHODS*******
    //**********************************************************


    //process result from  startActivityForResult(signInIntent, RES_CODE_SIGN_IN_MAIN_ACTIVITY);
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        //check if the intent code is what we passed
        if (requestCode == RES_CODE_SIGN_IN_MAIN_ACTIVITY) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            if (result.isSuccess()) {
                //retrieve user info from result data
                GoogleSignInAccount info = result.getSignInAccount();

                //update top menu
                try {
                    SignInActivity.userName = info.getDisplayName();
                    appMenu.findItem(R.id.itemLogInOut).setTitle(R.string.signOut);
                    //update photo and name in the menu
                    new UserPhotoLoader().execute(info.getPhotoUrl());
                }
                catch (Exception e) {
                    Toast.makeText(this, getString(R.string.cannotRetrieveInfo),Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(this,getString(R.string.signinUnsuccessful),Toast.LENGTH_SHORT).show();
            }
        }
    } //end onActivityResult


    //start animations to turn three numbers
    public void go(View view) {

        //get current bid and balance
        int currentBid = Integer.parseInt(textViewBid.getText().toString());
        int currentBalance = Integer.parseInt(textViewBalanceScore.getText().toString());

        //proceed only if current balance > current bid
        if (currentBalance>=currentBid) {

            //check if rotation is not ongoing
            if (!startRotation) {
                //set all start keys to true to run all threads
                startNumber1 = true;
                startNumber2 = true;
                startNumber3 = true;
                //disable textViewBid buttons and Go button
                buttonBidIncrease.setEnabled(false);
                buttonBidDecrease.setEnabled(false);
                //start spinning the picture in progress
                startRotation = true;
            }

            //hide oooopps message if visible
            if (textViewOops.isShown()) {
                textViewOops.setVisibility(View.INVISIBLE);
            }

        }
        //balance is less than current bid
        else {
            //show low balance pop up window
            showPopupLowBalance();
        }

    }//end go

    //exit from main activity
    public void exit(View view) {
        this.finish();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //if flag set to true - sign out the player
        if (shallSingOut) {
            Auth.GoogleSignInApi.signOut(SignInActivity.mGoogleApiClient);
        }
    }

    //init all flippers and define animations layouts for them
    private void initFlippers() {
        viewFlipper1 = (ViewFlipper) findViewById(R.id.viewFlipper1);
        viewFlipper2 = (ViewFlipper) findViewById(R.id.viewFlipper2);
        viewFlipper3 = (ViewFlipper) findViewById(R.id.viewFlipper3);

        viewFlipper1.setInAnimation(this,R.anim.slide_in_from_top);
        viewFlipper1.setOutAnimation(this,R.anim.slide_out_to_bottom);

        viewFlipper2.setInAnimation(this,R.anim.slide_in_from_top);
        viewFlipper2.setOutAnimation(this,R.anim.slide_out_to_bottom);

        viewFlipper3.setInAnimation(this,R.anim.slide_in_from_top);
        viewFlipper3.setOutAnimation(this,R.anim.slide_out_to_bottom);
    } //end initFlippers


    //implements logic of numbers animations
    private void initAllRunnablesAndThreds() {

        number1 = new Runnable() {
            @Override
            public void run() {
                //show next funny_guy in the flipper1
                viewFlipper1.showNext();
            }
        };//end number1
        number2 = new Runnable() {
            @Override
            public void run() {
                //show next funny_guy in the flipper2
                viewFlipper2.showNext();
            }
        };//end number2
        number3 = new Runnable() {
            @Override
            public void run() {
                //show next funny_guy in the flipper3
                viewFlipper3.showNext();
            }
        };//end number3

        //enable buttons for Bid and Go
        enableButtonsBidAndGo = new Runnable() {
            @Override
            public void run() {
                //if all numbers have come to full stop
                if (!startNumber1&!startNumber2&!startNumber3) {
                    //enable textViewBid buttons and Go button
                    buttonBidIncrease.setEnabled(true);
                    buttonBidDecrease.setEnabled(true);
                }
            }
        };//end enableButtonsBidAndGo

        //one circle of rotation of the picture when the game in progress
        startRotationInProgress = new Runnable() {
            @Override
            public void run() {
                imageViewInProgress.startAnimation(animationOfProgress);
            }
        };//end startRotationInProgress


        //check the win
        checkWin = new Runnable() {
            @Override
            public void run() {

                //if all numbers have come to full stop
                if (!startNumber1&!startNumber2&!startNumber3) {
                    //get current bid and balance
                    int currentBid = Integer.parseInt(textViewBid.getText().toString());
                    //get current balance
                    int currentBalance = Integer.parseInt(textViewBalanceScore.getText().toString());

                    //get numbers of current number in each column
                    int number1 = viewFlipper1.getDisplayedChild();
                    int number2 = viewFlipper2.getDisplayedChild();
                    int number3 = viewFlipper3.getDisplayedChild();

                    //****TO FAKE RESULT****************************
                    //number1 = 0;number2=1;number3=2;
                    //**********************************************

                    //produce a string with combination of pictures' number
                    String currentCombination = number1+"" + number2 + number3 ;

                    //fetch reword if the combination exists in the collection
                    Integer multiplier = rewordCombinations.get(currentCombination);
                    if (multiplier != null) {

                        //calculate reword based on the bid
                        int reword = currentBid * rewordCombinations.get(currentCombination);

                        //update balance
                        currentBalance +=reword;
                        textViewBalanceScore.setText(currentBalance+"");

                        //get text in pop up window
                        TextView tempTextView = (TextView) popupWinWindow.getContentView().findViewById(R.id.textViewNumber);
                        tempTextView.setText(reword+" (x"+multiplier+")");

                        //show the pop up window
                        showPopupWinWindow();
                    }
                    else {
                        //show oooopps message
                        if (!textViewOops.isShown()) {textViewOops.setVisibility(View.VISIBLE);}
                        //update balance
                        currentBalance -=currentBid;
                        textViewBalanceScore.setText(currentBalance+"");
                    }

                }
            }
        };//end checkWin

        //thread for number main
        numberThread1 = new Thread() {
            Random r = new Random();
            int speed1,speed2,speed3;
            int i;

            @Override
            public void run() {

                //keep the thread alive until finishing the game
                while (!finishGame) {

                    //do one wheel turn
                    while (startNumber1) {

                        //set three random stage for the turning number
                        speed1 = r.nextInt(30);
                        speed2 = r.nextInt(30);
                        speed3 = r.nextInt(30);

                        try {
                            for (i = 0; i < speed1; ++i) {
                                runOnUiThread(number1);
                                sleep(100);
                            }
                            for (i = 0; i < speed2; ++i) {
                                runOnUiThread(number1);
                                sleep(200);
                            }
                            for (i = 0; i < speed3; ++i) {
                                runOnUiThread(number1);
                                sleep(300);
                            }
                        } catch (Exception c) {
                            //do nothing
                        }
                        //stop turning the number
                        startNumber1 = false;
                        //enable buttons + - bid and go
                        runOnUiThread(enableButtonsBidAndGo);
                        //stop rotating picture in progress
                        stopRotationInProgress();
                        //check the win
                        runOnUiThread(checkWin);
                    }
                }
            }
        };//end numberThread1

        //thread for number 2
        numberThread2 = new Thread() {
            Random r = new Random();
            int speed1, speed2,speed3;
            int i;

            @Override
            public void run() {

                //keep the thread alive until finishing the game
                while (!finishGame) {

                    //do one wheel turn
                    while (startNumber2) {

                        //set three random stage for the turning number
                        speed1 = r.nextInt(30);
                        speed2 = r.nextInt(30);
                        speed3 = r.nextInt(30);

                        try {
                            for (i = 0; i < speed1; ++i) {
                                runOnUiThread(number2);
                                sleep(100);
                            }
                            for (i = 0; i < speed2; ++i) {
                                runOnUiThread(number2);
                                sleep(200);
                            }
                            for (i = 0; i < speed3; ++i) {
                                runOnUiThread(number2);
                                sleep(300);
                            }
                        } catch (Exception c) {
                            //do nothing
                        }
                        //stop turning the number
                        startNumber2 = false;
                        //enable buttons + - bid and go
                        runOnUiThread(enableButtonsBidAndGo);
                        //stop rotating picture in progress
                        stopRotationInProgress();
                        //check the win
                        runOnUiThread(checkWin);
                    }
                }
            }

        };//end numberThread1

        //thread for number 3
        numberThread3 = new Thread() {
            Random r = new Random();
            int speed1, speed2,speed3;
            int i;

            @Override
            public void run() {

                //keep the thread alive until finishing the game
                while (!finishGame) {

                    //do one wheel turn
                    while (startNumber3) {

                        //set three random stage for the turning number
                        speed1 = r.nextInt(30);
                        speed2 = r.nextInt(30);
                        speed3 = r.nextInt(30);

                        try {
                            for (i = 0; i < speed1; ++i) {
                                runOnUiThread(number3);
                                sleep(100);
                            }
                            for (i = 0; i < speed2; ++i) {
                                runOnUiThread(number3);
                                sleep(200);
                            }
                            for (i = 0; i < speed3; ++i) {
                                runOnUiThread(number3);
                                sleep(300);
                            }
                        } catch (Exception c) {
                            //do nothing
                        }
                        //stop turning the number
                        startNumber3 = false;
                        //enable buttons + - bid and go
                        runOnUiThread(enableButtonsBidAndGo);
                        //stop rotating picture in progress
                        stopRotationInProgress();
                        //check the win
                        runOnUiThread(checkWin);
                    }
                }
            }
        };//end numberThread1

        //start animationOfProgress while all three pictures are turning
        animationInProgress = new Thread() {
            @Override
            public void run() {
                //keep the thread alive until finishing the game
                while (!finishGame) {
                    //keep rotation the picture while turning in progress
                    while (startRotation) {
                        try {
                            runOnUiThread(startRotationInProgress);
                            sleep(500);
                        } catch (Exception c) {
                            //do nothing
                        }
                    }
                }
            }
        };//end animationInProgress
    }//end initAllRunnables

    //init and set up text view for textViewBid and score
    private void initTextViewsButtonsImageViewsEtc() {

        textViewBalance = (TextView) findViewById(R.id.textViewBalance);
        textViewBalanceScore = (TextView) findViewById(R.id.textViewBallanceScore);
        textViewOops = (TextView) findViewById(R.id.textViewOops);
        textViewBidSign = (TextView) findViewById(R.id.textViewPlaceBid);
        textViewBid = (TextView) findViewById(R.id.textViewBid);
        textViewExit = (TextView) findViewById(R.id.textViewExit);
        textViewMoney = (TextView) findViewById(R.id.textViewMoney);

        textViewBid.setText(minBid+"");

        //set custom font to the sign "Place your bid", bid number, balance sign and balance number
        //fetch custom font: fonts/CALVINN.TTF
        customFont = Typeface.createFromAsset(getAssets(),getString(R.string.customTypeFace));
        //set fonts
        textViewBidSign.setTypeface(customFont);
        textViewBid.setTypeface(customFont);
        textViewBalance.setTypeface(customFont);
        textViewBalanceScore.setTypeface(customFont);
        textViewOops.setTypeface(customFont);
        textViewMoney.setTypeface(customFont);
        textViewExit.setTypeface(customFont);

        buttonBidIncrease = (Button)findViewById(R.id.buttonBidIncrease);
        buttonBidDecrease = (Button)findViewById(R.id.buttonBidDecrease);
        imageViewInProgress = (ImageView) findViewById(R.id.imageViewInProgress);

        //set instructions for animationOfProgress
        animationOfProgress = AnimationUtils.loadAnimation(this,R.anim.rotation);

        //init popup_win windows with layouts
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //********DISPLAY CURRENT dpi *********************************
        //*************************************************************

        //get current display dpi
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int densityDpi = (int)(metrics.density * 160f);

        //calculate an appropriate seze for popup window in px
        //catching dpi 560 as it is still considered to be xxhdpi yet typically devices with
        //this dpi have much higher resolution than devices with dpi 480
        int w,h;
        if (densityDpi >=560 && densityDpi <640) {
            w = 1300; h = 1500;
        }
        else {
            w = getResources().getInteger(R.integer.popupWidth);
            h =  getResources().getInteger(R.integer.popupHeight);
        }

        //********END DISPLAY CURRENT dpi *********************************
        //*************************************************************


        popupWinWindow = new PopupWindow(inflater.inflate(R.layout.popup_win,null,false),
                w,
                h,
                false);

        popupWindowLowBalance = new PopupWindow(inflater.inflate(R.layout.popup_low_balance,null,false),
                w,
                h,
                false);

        popupScoreChart = new PopupWindow(inflater.inflate(R.layout.popup_score_chart,null,false),
                w,
                h,
                false);

        //if touch outside of the window - dismiss the pop up window
        popupWinWindow.setOutsideTouchable(true);
        popupWindowLowBalance.setOutsideTouchable(true);
        popupScoreChart.setOutsideTouchable(true);
        //set font for pop up windows' texts
        popupTextViewNumber =(TextView) popupWinWindow.getContentView().findViewById(R.id.textViewNumber);
        popupTextViewNumber.setTypeface(customFont);
        textViewLowBalanceMessage=(TextView) popupWindowLowBalance.getContentView().findViewById(R.id.textViewLowBalanceMessage);
        textViewLowBalanceMessage.setTypeface(customFont);

        //some work to set up custom font to the chart texts...-((
        //we will use temp TextView
        //x5
        TextView tempeTextView = (TextView) popupScoreChart.getContentView().findViewById(R.id.textViewx5);
        tempeTextView.setTypeface(customFont);
        //x4
        tempeTextView = (TextView) popupScoreChart.getContentView().findViewById(R.id.textViewx4);
        tempeTextView.setTypeface(customFont);
        //x3
        tempeTextView = (TextView) popupScoreChart.getContentView().findViewById(R.id.textViewx3);
        tempeTextView.setTypeface(customFont);
        //x2
        tempeTextView = (TextView) popupScoreChart.getContentView().findViewById(R.id.textViewx2);
        tempeTextView.setTypeface(customFont);
        //x2
        tempeTextView = (TextView) popupScoreChart.getContentView().findViewById(R.id.textViewx22);
        tempeTextView.setTypeface(customFont);
        //x2
        tempeTextView = (TextView) popupScoreChart.getContentView().findViewById(R.id.textViewx222);
        tempeTextView.setTypeface(customFont);
        //x1
        tempeTextView = (TextView) popupScoreChart.getContentView().findViewById(R.id.textViewx1);
        tempeTextView.setTypeface(customFont);

        //populate key-value collection with reword combinations;
        //reword calculation: current bid * value of the collection
        //winning combination can be adjust in R.string recourse file in MainActivity section
        rewordCombinations.put(getString(R.string.win1),getResources().getInteger(R.integer.win1));
        rewordCombinations.put(getString(R.string.win2),getResources().getInteger(R.integer.win2));
        rewordCombinations.put(getString(R.string.win3),getResources().getInteger(R.integer.win3));
        rewordCombinations.put(getString(R.string.win4),getResources().getInteger(R.integer.win4));
        rewordCombinations.put(getString(R.string.win5),getResources().getInteger(R.integer.win5));
        rewordCombinations.put(getString(R.string.win6),getResources().getInteger(R.integer.win6));
        rewordCombinations.put(getString(R.string.win7),getResources().getInteger(R.integer.win7));

    }//end initTextViewsButtonsImageViewsEtc

    //increase textViewBid
    public void increaseBid(View view) {
        //get current balance
        int currentBalance = Integer.parseInt(textViewBalanceScore.getText().toString());
        //get current textViewBid
        int currentBid = Integer.parseInt(textViewBid.getText().toString());

        if (((currentBid+minBid)<=maxBid)&&(currentBalance >= (currentBid+minBid))) {
            textViewBid.setText((currentBid+minBid)+"");}

        else if (currentBalance < (currentBid+minBid)) {
            textViewBid.setText(currentBalance+"");
        }
        else {
            textViewBid.setText(maxBid+"");
        }
    }

    //decrease textViewBid
    public void decreaseBid(View view) {
        //get current balance
        int currentBalance = Integer.parseInt(textViewBalanceScore.getText().toString());
        //get current textViewBid
        int currentBid = Integer.parseInt(textViewBid.getText().toString());

        if (((currentBid-minBid)>=minBid)&&(currentBalance >= (currentBid-minBid))) {
            textViewBid.setText((currentBid-minBid)+"");}

        else if (currentBalance<(currentBid-minBid)) {
            textViewBid.setText(currentBalance+"");
        }
        else {
            textViewBid.setText(minBid+"");
        }
    }

    //stop rotation of the picture in progress
    private void stopRotationInProgress() {
        //if all numbers have come to full stop
        if (!startNumber1&!startNumber2&!startNumber3) {
                //stop rotation in progress
                startRotation = false;
            }
    };//end stopRotationInProgress

    //show popup_win WIN window
    private void showPopupWinWindow() {
        popupWinWindow.showAtLocation(findViewById(R.id.mainLayout), Gravity.CENTER,0,0);
    }

    //show popup low balance window
    private void showPopupLowBalance() {
        popupWindowLowBalance.showAtLocation(findViewById(R.id.mainLayout), Gravity.CENTER,0,0);
    }

    //show score chart
    private void showPopupChart() {
        popupScoreChart.showAtLocation(findViewById(R.id.mainLayout), Gravity.CENTER,0,0);
    }
    
    //close popup_win windows (Win or Low Balance
    public void dismissPopup(View view)
    {
      //check if popup_win Win window is being displayed
        if (popupWinWindow.isShowing()) {
            popupWinWindow.dismiss();}

        //check if popup_win Low Balance window is being displayed
        if (popupWindowLowBalance.isShowing()) {
            popupWindowLowBalance.dismiss();}

        //check if popup score chart window is being displayed
        if (popupScoreChart.isShowing()) {
            popupScoreChart.dismiss();}
    }

    //evoke the activity GetMoney
    public void getMoney(View view) {
        //only if balance 0 we go to the input dialog
        if (textViewBalanceScore.getText().toString().equals("0")) {
            //init intent for the second activity
            Intent intent = new Intent(this, GetMoneyActivity.class);
            startActivity(intent);
        }
        else {
            //Message: Only when balance is 0
            Toast.makeText(this,getString(R.string.toastMessageEmptyBalance),Toast.LENGTH_SHORT).show();
        }
    }

}//end MainActivity
