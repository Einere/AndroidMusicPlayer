package com.example.hj.homework03;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.security.Permission;
import java.security.Permissions;

public class MainActivity extends AppCompatActivity {
    final int STATUS_STOP = 0;
    final int STATUS_RUNNING = 1;
    final int STATUS_PAUSE = 2;
    final int STATUS_COMPLETE = 3;
    final String receiverAction = "com.example.hj.homework03_receiver";

    private ImageButton ibPlay = null;
    private TextView tvName = null;

    private ServiceConnection con = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("MusicPlayerClient", "onServiceConnected()");
            binder = IMusicService.Stub.asInterface(service);

            // set play button
            try {
                switch (binder.getStatus()) {
                    case STATUS_STOP: {
                        ibPlay.setImageResource(R.drawable.noun_play_343514);
                        Log.i("MusicPlayerClient", "onServiceConnected(), status is stop");
                        break;
                    }
                    case STATUS_RUNNING: {
                        ibPlay.setImageResource(R.drawable.noun_pause_343918);
                        Log.i("MusicPlayerClient", "onServiceConnected(), status is running");
                        break;
                    }
                    case STATUS_PAUSE: {
                        ibPlay.setImageResource(R.drawable.noun_play_343514);
                        Log.i("MusicPlayerClient", "onServiceConnected(), status is pause");
                        break;
                    }
                    case STATUS_COMPLETE: {
                        ibPlay.setImageResource(R.drawable.noun_play_343514);
                        Log.i("MusicPlayerClient", "onServiceConnected(), status is complete");
                        break;
                    }
                }
            } catch (RemoteException e) {
                Log.i("MusicPlayerClient", "onServiceConnected(), failed to set play button image");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("MusicPlayerClient", "onServiceDisconnected()");
        }
    };
    private IMusicService binder = null;
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getStringExtra("ACTION")) {
                case "PLAY": {
                    updateIbPlaySrc();

                    Log.i("MusicPlayerClient", "onReceive(), PLAY");
                    break;
                }
                case "REWIND": {

                    Log.i("MusicPlayerClient", "onReceive(), REWIND");
                    break;
                }
                case "STOP": {
                    ibPlay.setImageResource(R.drawable.noun_play_343514);
                    Log.i("MusicPlayerClient", "onReceive(), STOP");
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("MusicPlayerClient", "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get view
        ibPlay = findViewById(R.id.ib_play);
        tvName = findViewById(R.id.tv_music_name);

        // set text view
        tvName.setText("Sample.mp3");

        // bind & start service (assure service alive)
        // assure service alive, must start music when play button clicked
        // 스타티드 서비스로 시작을 제어하는 경우, 액티비티 실행과 동시에 서비스가 실행되며, 자동으로 음악이 재생됩니다.
        // 이를 방지하기 위해 아래와 같이 코딩하였습니다.
        Intent intent = new Intent(this, MusicService.class);
        intent.setPackage(getPackageName());
        bindService(intent, con, BIND_AUTO_CREATE);

        // check & request read external file permission
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.i("MusicPlayerClient", "permission request");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        } else {
            Log.i("MusicPlayerClient", "permission already requested");
        }

        // register broadcast receiver
        registerReceiver(receiver, new IntentFilter(receiverAction));
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void onClick(View v) {
        // check which button is clicked
        switch (v.getId()) {
            case R.id.ib_play: {
                try {
                    if (binder != null) {
                        switch (binder.getStatus()) {
                            case STATUS_STOP: {
                                Log.i("MusicServiceClient", "onClick(), status is stop");
                                // to bind service if start after stop
                                Intent intent = new Intent(this, MusicService.class);
                                intent.setPackage(getPackageName());
                                bindService(intent, con, BIND_ABOVE_CLIENT);

                                // play music
                                startService(intent);

                                // change image button's image to pause
                                ibPlay.setImageResource(R.drawable.noun_pause_343918);
                                break;
                            }
                            case STATUS_RUNNING: {
                                Log.i("MusicServiceClient", "onClick(), status is running");
                                // pause music
                                binder.pause();

                                // change image button's image to play
                                ibPlay.setImageResource(R.drawable.noun_play_343514);
                                break;
                            }
                            case STATUS_PAUSE: {
                                Log.i("MusicServiceClient", "onClick(), status is pause");
                                // resume music
                                binder.resume();

                                // change image button's image to play
                                ibPlay.setImageResource(R.drawable.noun_pause_343918);
                                break;
                            }
                        }
                    } else {
                        Toast.makeText(this, "binder is null...", Toast.LENGTH_SHORT).show();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                break;
            }
            case R.id.ib_rewind: {
                try {
                    binder.rewind();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            }
            case R.id.ib_stop: {
                // if use broadcast
                // 아래의 방식을 사용하면, 의도한 대로 잘 작동함.
                /*Intent broadcastStop = new Intent("com.example.hj.homework03_receiver");
                broadcastStop.putExtra("ACTION", "STOP");
                sendBroadcast(broadcastStop);*/

                // if use started service
                // 만약 아래의 방식을 사용하면 의도한 대로 작동하지 않음.
                // 예를 들어, 정지하면 unbind는 되지만, 알람이 죽지 않음.
                try{
                    unbindService(con);
                    Intent intent = new Intent(this, MusicService.class);
                    intent.setPackage(getPackageName());
                    stopService(intent);
                    ibPlay.setImageResource(R.drawable.noun_play_343514);
                }
                catch (IllegalArgumentException e){
                    Toast.makeText(this, "already unregistered...", Toast.LENGTH_SHORT).show();
                }

                break;
            }
        }
    }

    @Override
    protected void onDestroy() {
        Log.i("MusicPlayerClient", "onDestroy()");
        unbindService(con);
        super.onDestroy();
    }

    private void updateIbPlaySrc(){
        try {
            if (binder != null) {
                switch (binder.getStatus()) {
                    case STATUS_STOP: {
                        // change image button's image to pause
                        ibPlay.setImageResource(R.drawable.noun_pause_343918);
                        break;
                    }
                    case STATUS_RUNNING: {
                        // change image button's image to play
                        ibPlay.setImageResource(R.drawable.noun_play_343514);
                        break;
                    }
                    case STATUS_PAUSE: {
                        // change image button's image to play
                        ibPlay.setImageResource(R.drawable.noun_pause_343918);
                        break;
                    }
                }
            } else {
                Toast.makeText(this, "binder is null... please restart this app", Toast.LENGTH_SHORT).show();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

}
