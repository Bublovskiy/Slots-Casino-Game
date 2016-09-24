package com.example.bublovskiy.project1_july;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;


public class SignInActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    //code for Google Api intent
    private static final int RES_CODE_SIGN_IN = 777;

    //Google API client
    static GoogleApiClient mGoogleApiClient;

    //visual element of the sign in screen
    private TextView textViewContinueAsGuest;
    private ImageView imageViewLeftArrow;
    private SignInButton buttonGooglePlusSignIn;

    //type fave of the entire app
    static Typeface appTypeFace;

    //user's info
    //by default ""Guest"
    static String userName;
    static String userEmail;
    static Uri userPhoto;

    //progress bar
    ProgressBar progressBar;


    // *************************************************
    // -------- ANDROID ACTIVITY LIFECYCLE METHODS
    // *************************************************
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_activity);


        //find the views we need on the screen
        textViewContinueAsGuest = (TextView) findViewById(R.id.textViewContinueAsGuest);
        imageViewLeftArrow = (ImageView) findViewById(R.id.imageViewLeftArrow);
        buttonGooglePlusSignIn = (SignInButton) findViewById(R.id.buttonGooglePlusSignIn);
        progressBar = (ProgressBar) findViewById(R.id.progressBarSignInWindow);

        //type fave of the entire app
        appTypeFace = Typeface.createFromAsset(getAssets(), getString(R.string.customTypeFace));

        //set listener to Google button and arrow button
        imageViewLeftArrow.setOnClickListener(this);
        buttonGooglePlusSignIn.setOnClickListener(this);

        //set app type face to the continue sign
        textViewContinueAsGuest.setTypeface(appTypeFace);


        //create sign-in options object
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                .build();

        // Build the GoogleApiClient object
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        //customize Google Play sign in button
        buttonGooglePlusSignIn.setScopes(gso.getScopeArray());
        buttonGooglePlusSignIn.setSize(SignInButton.SIZE_WIDE);
        buttonGooglePlusSignIn.setColorScheme(SignInButton.COLOR_DARK);



    }//end onCreate

    @Override
    protected void onResume() {
        super.onResume();

        //start playing music if flag allows us to do so
        //if service is already running - start the music
        if (MainActivity.isSoundOn.equals("yes")) {
            if (!new isMyServiceRunning(this).isRunning(MusicService.class)) {

                startService(new Intent(this, MusicService.class));
            }
            else {
                MusicService.mediaPlayer.start();
            }
        }
    }//end onResume

    @Override
    protected void onPause() {
        super.onPause();

        //if the service is running - pause the music
        if (new isMyServiceRunning(this).isRunning(MusicService.class) ) {
            MusicService.mediaPlayer.pause();
        }

    }//end onPause






    //initiate Google API to sign in
    private void startSignIn() {
        progressBar.setVisibility(View.VISIBLE);
        //Create sign-in intent and begin auth flow
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RES_CODE_SIGN_IN);
    } //end startSignIn


    //process result from  startActivityForResult(signInIntent, RES_CODE_SIGN_IN);
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode,resultCode,data);

        //check if the intent code is what we passed
        if (requestCode == RES_CODE_SIGN_IN) {
            progressBar.setVisibility(View.INVISIBLE);
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            //retrieve user info from result data
            signInResultHandler(result);
        }
    } //end onActivityResult


    //retrieve user info from result data
        private void signInResultHandler(GoogleSignInResult result) {

        //if retrieving information was successful
        if (result.isSuccess()) {

            progressBar.setVisibility(View.VISIBLE);
            //get package with details from data
            GoogleSignInAccount acct = result.getSignInAccount();

            try {

                //get user's details
                userName = acct.getDisplayName();
                userEmail = acct.getEmail();
                userPhoto = acct.getPhotoUrl();

                progressBar.setVisibility(View.INVISIBLE);
                //go to main activity
                Intent mainActivityIntent = new Intent(this, MainActivity.class);
                startActivity(mainActivityIntent);

            }
            catch (Exception e) {
                Toast.makeText(this, getString(R.string.cannotRetrieveInfo),Toast.LENGTH_SHORT).show();
            }
        }

        //if retrieving information was not successful
        //catch the code and explain it to the user
        else {
            Status status = result.getStatus();
            int statusCode = status.getStatusCode();

            if (statusCode == GoogleSignInStatusCodes.SIGN_IN_CANCELLED) {
                Toast.makeText(this,getString(R.string.signinCanceled), Toast.LENGTH_SHORT).show();
            }
            else if (statusCode == GoogleSignInStatusCodes.SIGN_IN_FAILED) {
                Toast.makeText(this,getString(R.string.signinUnsuccessful), Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this,getString(R.string.emptyData), Toast.LENGTH_SHORT).show();
            }
        }

    }//end signInResultHandler


    // *************************************************
    // -------- GOOGLE PLAY SERVICES METHODS
    // *************************************************
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this,getString(R.string.connectionFailed),Toast.LENGTH_SHORT).show();
    }

    // *************************************************
    // -------- CLICK LISTENER FOR THE ACTIVITY
    // *************************************************
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //sign with Google Plus
            case R.id.buttonGooglePlusSignIn:
                startSignIn();
                break;
            //continue as a guest
            case R.id.imageViewLeftArrow:
                //go to main activity with default user name = "Guest"
                userName = getString(R.string.userNameByDefault);
                Intent mainActivityIntent = new Intent(this, MainActivity.class);
                startActivity(mainActivityIntent);
                break;
        }
    } //end onClick


    //to load user's photo asynchronously
//    private class UserPhotoLoader extends AsyncTask<GoogleSignInAccount, Void, Uri> {
//
//
//        @Override
//        protected Uri doInBackground(GoogleSignInAccount... uris) {
//
//            //get user's photo
//            Uri photo = uris[0].getPhotoUrl();
//            return photo;
//        }
//
//        @Override
//        protected void onPostExecute(Uri photo) {
//
//            //load signed in user's phto into an image view
//            Picasso.with(getApplicationContext())
//                    .load(photo)
//                    .into(userImage);
//        }


   // } //end UserPhotoLoader


}//end SignInActivity