package com.example.hj.homework03;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MusicService extends Service {
    final int STATUS_STOP = 0;
    final int STATUS_RUNNING = 1;
    final int STATUS_PAUSE = 2;
    final int STATUS_COMPLETE = 3;

    final String receiverAction = "com.example.hj.homework03_receiver";
    int status = STATUS_STOP;
    int position = 0;
    MediaPlayer player = null;
    String filePath = null;
    MusicNotiManager notiManager = null;
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getStringExtra("ACTION")) {
                case "PLAY": {
                    switch (status) {
                        case STATUS_STOP: {
                            myPlay();
                            break;
                        }
                        case STATUS_RUNNING: {
                            myPause();
                            break;
                        }
                        case STATUS_PAUSE: {
                            myResume();
                        }
                    }

                    Toast.makeText(context, "receive broadcast play", Toast.LENGTH_SHORT).show();
                    break;
                }
                case "REWIND": {
                    myRewind();

                    Toast.makeText(context, "receive broadcast rewind", Toast.LENGTH_SHORT).show();
                    break;
                }
                case "STOP": {
                    myStop();

                    Toast.makeText(context, "receive broadcast stop", Toast.LENGTH_SHORT).show();
                    break;
                }
            }
        }
    };
    private Thread progressThread = null;

    public MusicService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("MusicService", "onCreate()");

        // register broadcast receiver
        registerReceiver(receiver, new IntentFilter(receiverAction));

        // set player
        setPlayer();

        // make notification manager
        notiManager = new MusicNotiManager(this, status);
        notiManager.setProgress(player.getDuration(), player.getCurrentPosition());

        // set work thread
        Runnable runnable = new Runnable() {
            @SuppressWarnings("LoopConditionNotUpdatedInsideLoop")
            @Override
            public void run() {
                while (status != STATUS_COMPLETE) {
                    if (status == STATUS_RUNNING) {
                        // update progress bar
                        notiManager.setProgress(player.getDuration(), player.getCurrentPosition());
                        startForeground(222, notiManager.getNotification());
                        Log.i("MusicService", "progress thread, current postion : " + player.getCurrentPosition());
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
            }
        };
        ProgressManager.setRunnable(runnable);
        progressThread = ProgressManager.getThread();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("MusicService", "onStartCommand()");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i("MusicService", "onDestory()");
        if(player != null){
            player.release();
        }
        status = STATUS_STOP;

        // unregister broadcast receiver
        unregisterReceiver(receiver);
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
            public void play() throws RemoteException {
                myPlay();
            }

            @Override
            public void pause() throws RemoteException {
                myPause();
            }

            @Override
            public void rewind() throws RemoteException {
                myRewind();
            }

            @Override
            public int getStatus() throws RemoteException {
                return status;
            }

            @Override
            public void resume() throws RemoteException {
                myResume();
            }
        };
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i("MusicService", "onUnbind()");
        return super.onUnbind(intent);
    }

    public void setPlayer() {
        filePath = Environment.getExternalStorageDirectory().getPath() + "/Music/1-01 Way Back Home.mp3";
        player = MediaPlayer.create(getApplicationContext(), Uri.fromFile(new File(filePath)));
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                status = STATUS_COMPLETE;
            }
        });
    }

    public void myPlay() {
        if (player == null) {
            setPlayer();
        }
        player.start();
        status = STATUS_RUNNING;
        notiManager.setStatus(status);
        notiManager.updateIbPlaySrc();
        startForeground(222, notiManager.getNotification());
        progressThread.start();
    }

    public void myPause() {
        if (status == STATUS_RUNNING && player.isPlaying()) {
            player.pause();
            position = player.getCurrentPosition();
            status = STATUS_PAUSE;
            notiManager.setStatus(status);
            notiManager.updateIbPlaySrc();
            startForeground(222, notiManager.getNotification());
        }
    }

    public void myResume() {
        if (status == STATUS_PAUSE && !player.isPlaying()) {
            player.seekTo(position);
            player.start();
            status = STATUS_RUNNING;
            notiManager.setStatus(status);
            notiManager.updateIbPlaySrc();
            startForeground(222, notiManager.getNotification());
        }
    }

    public void myRewind() {
        if (status == STATUS_RUNNING) {
            player.pause();
        }
        position = player.getCurrentPosition();
        if (position >= 10000) {
            position -= 10000;
        } else {
            position = 0;
        }
        player.seekTo(position);
        if (status == STATUS_RUNNING) {
            player.start();
        }
    }

    public void myStop() {
        player.stop();
        player.release();
        player = null;
        stopForeground(true);
        notiManager.cancel();
        status = STATUS_STOP;
        progressThread.interrupt();
    }

}
