package com.example.bublovskiy.project1_july;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;


public class MusicService extends Service {

    //create mediaPlayer instance
    static MediaPlayer mediaPlayer;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //set loop in media player
        mediaPlayer.setLooping(true);

        //start play music
        mediaPlayer.start();

        //restart the service AND redeliver the same intent to onStartCommand()
        // of the service, because, again, of the flag.
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //init media player with sound track
        mediaPlayer = MediaPlayer.create(getApplicationContext(),R.raw.main_music_theme);

    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        //release mediaPlayer resource
        mediaPlayer.release();

    }



}
