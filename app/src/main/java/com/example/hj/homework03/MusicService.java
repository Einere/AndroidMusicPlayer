package com.example.hj.homework03;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.io.IOException;
import java.net.URI;

public class MusicService extends Service {
    public static final int STATUS_STOP = 0;
    public static final int STATUS_RUNNING = 1;
    public static final int STATUS_PAUSE = 2;
    public static final int STATUS_COMPLETE = 3;
    int status = STATUS_STOP;
    int position = 0;
    MediaPlayer player = null;
    String filePath = null;

    public MusicService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("MusicService", "onCreate()");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("MusicService", "onStartCommand()");
        player = new MediaPlayer();
        try {
            filePath = Environment.getExternalStorageDirectory().getPath() + "/Music/1-01 Way Back Home.mp3";
            Log.i("MusicService", "file path : " + filePath);
            player.setDataSource(filePath);
            player.prepare();
            player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                status = STATUS_COMPLETE;
            }
        });
        status = STATUS_RUNNING;
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i("MusicService", "onDestory()");
        player.release();
        status = STATUS_STOP;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.i("MusicService", "onBind()");
        filePath = intent.getStringExtra("filePath");
        return new IMusicService.Stub() {
            @Override
            public int currentPosition() throws RemoteException {
                return player.getCurrentPosition();
            }

            @Override
            public int getMaxDuration() throws RemoteException {
                return player.getDuration();
            }

            @Override
            public void pause() throws RemoteException {
                if (status == STATUS_RUNNING && player.isPlaying()){
                    player.pause();
                    status = STATUS_PAUSE;
                    position = player.getCurrentPosition();
                }
            }

            @Override
            public void rewind() throws RemoteException {
                if (status == STATUS_RUNNING){
                    player.pause();
                }
                position = player.getCurrentPosition();
                if (position >= 10000){
                    position -= 10000;
                }
                else{
                    position = 0;
                }
                player.seekTo(position);
                if (status == STATUS_RUNNING){
                    player.start();
                }
            }

            @Override
            public int getStatus() throws RemoteException {
                return status;
            }

            @Override
            public void resume() throws RemoteException {
                if (status == STATUS_PAUSE && !player.isPlaying()){
                    player.seekTo(position);
                    player.start();
                    status = STATUS_RUNNING;
                }
            }
        };
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("MusicService", "onUnbind()");
        return super.onUnbind(intent);
    }
}
