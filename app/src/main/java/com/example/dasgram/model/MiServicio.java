package com.example.dasgram.model;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import com.example.dasgram.R;

/**
 * Servicio que reproduce la música que se le pase como parámetro.
 * @author Iker Nafarrate Bilbao
 */
public class MiServicio extends Service {

    private final IBinder elBinder = new MiBinder();
    private MediaPlayer mediaPlayer;
    private String musicId;

    public MiServicio() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return elBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationManager elManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel canal = new NotificationChannel("music","Musica", NotificationManager.IMPORTANCE_DEFAULT);
            elManager.createNotificationChannel(canal);
            Notification.Builder builder = new Notification.Builder(this,"music");
            builder.setContentTitle(getString(R.string.musica));

            builder.setSmallIcon(R.drawable.ic_music);
            builder.setAutoCancel(true);
            Notification notification = builder.build();
            startForeground(1,notification);
        }
    }

    public void playMusic(String pMusica){

        musicId = pMusica;

        if(mediaPlayer != null){
            mediaPlayer.stop();
        }

        if(pMusica.isEmpty()){
            mediaPlayer = MediaPlayer.create(this, R.raw.prueba);
        }else{
            Uri uri = getMusicUri(pMusica);
            mediaPlayer = MediaPlayer.create(this, uri);

            if(mediaPlayer == null){
                mediaPlayer = MediaPlayer.create(this, R.raw.prueba);
            }
        }

        mediaPlayer.start();
        mediaPlayer.setLooping(true);
    }

    public void stop(){
        if(mediaPlayer != null && mediaPlayer.isPlaying())
            mediaPlayer.stop();
    }


    private Uri getMusicUri(String pMusica){

        long id = Long.parseLong(pMusica);
        Uri uri= ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);

        return uri;
    }


    public class MiBinder extends Binder {
        public MiServicio obtenServicio(){
            return MiServicio.this;
        }
    }

    public boolean esIgual(String pId){
        if(pId.equals(musicId)){
            return mediaPlayer.isPlaying();
        }

        return false;
    }
}


