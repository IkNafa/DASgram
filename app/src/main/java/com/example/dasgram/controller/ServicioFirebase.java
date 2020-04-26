package com.example.dasgram.controller;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Servicio que se encarga de las notificaciones que vienen de Firebase
 * @author Iker Nafarrate Bilbao
 */
public class ServicioFirebase extends FirebaseMessagingService {


    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.i("Token", "Nuevo Token: " + s);
    }

    //En el caso de que tengamos la app abierta, en vez de una notificación se nos mostrará el mensaje
    //en un toast
    @Override
    public void onMessageReceived(@NonNull final RemoteMessage remoteMessage) {
        if(remoteMessage.getNotification() != null){
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {

                @Override
                public void run() {
                    Toast.makeText(ServicioFirebase.this.getApplicationContext(),remoteMessage.getNotification().getTitle(),Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
