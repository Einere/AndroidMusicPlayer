package com.example.hj.homework03;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
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
    public final int STATUS_STOP = 0;
    public final int STATUS_RUNNING = 1;
    public final int STATUS_PAUSE = 2;
    public final int STATUS_COMPLETE = 3;
    private ImageButton ibPlay = null;
    private TextView tvName = null;
    private ServiceConnection con = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("MusicPlayerClient", "onServiceConnected()");
            binder = IMusicService.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("MusicPlayerClient", "onServiceDisconnected()");
        }
    };
    private IMusicService binder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // get view
        ibPlay = (ImageButton) findViewById(R.id.ib_play);
        tvName = (TextView) findViewById(R.id.tv_music_name);

        // set text view
        tvName.setText("Way Back Home");

        // bind service
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.example.hj.homework03", "com.example.hj.homework03.MusicService"));
        bindService(intent, con, BIND_AUTO_CREATE);

        // check & request read external file permission
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Log.i("MusicPlayerClient", "permission request");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }
        else {
            Log.i("MusicPlayerClient", "permission already requested");
        }
    }

    public void onClick(View v) {
        // check which button is clicked
        switch (v.getId()) {
            case R.id.ib_play: {
                try {
                    if (binder != null){
                        switch(binder.getStatus()){
                            case STATUS_STOP:{
                                // play music
                                Intent intent = new Intent();
                                intent.setComponent(new ComponentName("com.example.hj.homework03", "com.example.hj.homework03.MusicService"));
                                startService(intent);

                                // change image button's image to pause
                                ibPlay.setImageResource(R.drawable.noun_pause_343918);
                                break;
                            }
                            case STATUS_RUNNING:{
                                // pause music
                                binder.pause();

                                // change image button's image to play
                                ibPlay.setImageResource(R.drawable.noun_play_343514);
                                break;
                            }
                            case STATUS_PAUSE:{
                                // resume music
                                binder.resume();

                                // change image button's image to play
                                ibPlay.setImageResource(R.drawable.noun_pause_343918);
                                break;
                            }
                        }
                    }
                    else{
                        Toast.makeText(this, "binder is null... please restart this app", Toast.LENGTH_SHORT).show();
                    }
                }
                catch (RemoteException e) {
                    e.printStackTrace();
                }

                break;
            }
            case R.id.ib_rewind: {
                try {
                    binder.rewind();
                }
                catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            }
            case R.id.ib_stop: {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.example.hj.homework03", "com.example.hj.homework03.MusicService"));
                stopService(intent);
                break;
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(con);
    }
}
