package com.example.hj.homework03;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

class MusicNotiManager {
    private final int STATUS_STOP = 0;
    private final int STATUS_RUNNING = 1;
    private final int STATUS_PAUSE = 2;
    private final int STATUS_COMPLETE = 3;
    private final String receiverAction = "com.example.hj.homework03_receiver";

    private Context context = null;
    private NotificationManager manager = null;
    private NotificationCompat.Builder builder = null;
    private RemoteViews notiView = null;
    private int status = 0;
    private Notification notification;
    private String filePath = null;

    MusicNotiManager(Context context, int status, String filePath) {
        this.context = context;
        this.status = status;
        this.notiView = new RemoteViews(context.getPackageName(), R.layout.layout_noti);
        this.filePath = filePath;
        setManager();
    }

    void setManager() {
        // set notification
        String channelId = "MusicChannel";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // get notification manager
            manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // create channel
            NotificationChannel channel = new NotificationChannel(channelId, "Music Player", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
            builder = new NotificationCompat.Builder(context, channelId);

            // make large icon
            Bitmap largeIcon = BitmapFactory.decodeResource(context.getResources(), R.drawable.noun_play_343514);

            // set thumbnail
            Bitmap thumbnail = getThumbnail(filePath);
            notiView.setImageViewBitmap(R.id.ib_noti_thumbnail, thumbnail);

            // make pending intent
            Intent myIntent = new Intent();
            myIntent.setComponent(new ComponentName("com.example.hj.homework03", "com.example.hj.homework03.MainActivity"));
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, myIntent, 0);

            // set RemoteViews's initial image button
            switch (status) {
                case STATUS_STOP: {
                    notiView.setImageViewResource(R.id.ib_noti_play, R.drawable.noun_play_343514);
                    break;
                }
                case STATUS_RUNNING: {
                    notiView.setImageViewResource(R.id.ib_noti_play, R.drawable.noun_pause_343918);
                    break;
                }
                case STATUS_PAUSE: {
                    notiView.setImageViewResource(R.id.ib_noti_play, R.drawable.noun_play_343514);
                    break;
                }
                case STATUS_COMPLETE: {
                    notiView.setImageViewResource(R.id.ib_noti_play, R.drawable.noun_play_343514);
                    break;
                }
            }

            // set RemoteViews image button event handler
            Intent broadcastPlay = new Intent(receiverAction);
            broadcastPlay.putExtra("ACTION", "PLAY");
            PendingIntent pendingBroadcastPlay = PendingIntent.getBroadcast(context, 1, broadcastPlay, PendingIntent.FLAG_UPDATE_CURRENT);
            notiView.setOnClickPendingIntent(R.id.ib_noti_play, pendingBroadcastPlay);

            Intent broadcastRewind = new Intent(receiverAction);
            broadcastRewind.putExtra("ACTION", "REWIND");
            PendingIntent pendingBroadcastRewind = PendingIntent.getBroadcast(context, 2, broadcastRewind, PendingIntent.FLAG_UPDATE_CURRENT);
            notiView.setOnClickPendingIntent(R.id.ib_noti_rewind, pendingBroadcastRewind);

            Intent broadcastStop = new Intent(receiverAction);
            broadcastStop.putExtra("ACTION", "STOP");
            PendingIntent pendingBroadcastStop = PendingIntent.getBroadcast(context, 3, broadcastStop, PendingIntent.FLAG_UPDATE_CURRENT);
            notiView.setOnClickPendingIntent(R.id.ib_noti_stop, pendingBroadcastStop);

            // set notification
            notification = builder.setLargeIcon(largeIcon)
                    .setContentTitle("Music Player")
                    .setTicker("ticker")
                    .setContentText("content text")
                    .setSmallIcon(R.drawable.noun_stop_343504)
                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .setCustomContentView(notiView)
                    .setCustomBigContentView(notiView)
                    .build();
        }
    }

    Notification getNotification() {
        return notification;
    }

    void setProgress(int max, int cur) {
        notiView.setProgressBar(R.id.pb_noti_position, max, cur, false);
        notification = builder.setCustomContentView(notiView)
                .build();
    }

    void cancel() {
        manager.cancel(222);
    }

    void updateIbPlaySrc(){
        switch (status) {
            case STATUS_STOP: {
                notiView.setImageViewResource(R.id.ib_noti_play, R.drawable.noun_play_343514);
                builder.setSmallIcon(R.drawable.noun_stop_343504);
                Log.i("MusicNotiManager", "updateIbPlaySrc(), stop");
                break;
            }
            case STATUS_RUNNING: {
                notiView.setImageViewResource(R.id.ib_noti_play, R.drawable.noun_pause_343918);
                builder.setSmallIcon(R.drawable.noun_play_343514);
                Log.i("MusicNotiManager", "updateIbPlaySrc(), running");
                break;
            }
            case STATUS_PAUSE: {
                notiView.setImageViewResource(R.id.ib_noti_play, R.drawable.noun_play_343514);
                builder.setSmallIcon(R.drawable.noun_pause_343918);
                Log.i("MusicNotiManager", "updateIbPlaySrc(), pause");
                break;
            }
            case STATUS_COMPLETE: {
                notiView.setImageViewResource(R.id.ib_noti_play, R.drawable.noun_play_343514);
                builder.setSmallIcon(R.drawable.noun_stop_343504);
                Log.i("MusicNotiManager", "updateIbPlaySrc(), complete");
                break;
            }
        }
        notification = builder.setCustomContentView(notiView)
                .build();
    }

    void setStatus(int status){
        this.status = status;
    }

    Bitmap getThumbnail(String filePath){
        MediaMetadataRetriever mr = new MediaMetadataRetriever();
        mr.setDataSource(filePath);
        byte[] bytes = mr.getEmbeddedPicture();
        mr.release();

        if(bytes != null){
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
        else{
            return null;
        }
    }


}
