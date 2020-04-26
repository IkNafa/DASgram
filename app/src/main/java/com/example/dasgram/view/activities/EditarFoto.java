package com.example.dasgram.view.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.dasgram.R;
import com.example.dasgram.controller.Tarea;

/**
 * Actividad que se encarga de modificar los datos de la foto justo antes de subirla.
 * @author Iker Nafarrate Bilbao
 */
public class EditarFoto extends AppCompatActivity {

    private String username;
    private String uri;
    private double lat;
    private double lon;
    private EditText edtLocalizacion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editarfoto);

        EditText edtUsername = findViewById(R.id.edtUsername);
        edtUsername.setEnabled(false);

        edtLocalizacion = findViewById(R.id.edtLocalizacion);
        edtLocalizacion.setEnabled(false);

        ImageView imgFoto = findViewById(R.id.imgFoto);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            username = bundle.getString("username");
            uri = bundle.getString("uri");

            edtUsername.setText(username);
            imgFoto.setImageURI(Uri.parse(uri));
        }

    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putDouble("latitud", lat);
        outState.putDouble("longitud",lon);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        lat = savedInstanceState.getDouble("latitud");
        lon = savedInstanceState.getDouble("longitud");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK){


            if (data != null) {
                lat = data.getDoubleExtra("latitud",0);
                lon = data.getDoubleExtra("longitud",0);
                String ciudad = data.getStringExtra("ciudad");

                edtLocalizacion.setText(ciudad);
            }


        }
    }

    public void abrirMapa(View v){

        Intent intent = new Intent(this, MapsActivity.class);
        startActivityForResult(intent,1);

    }

    public void back(View v){
        finish();
    }


    /**
     * Cuando se pulse el botón 'PUBLICAR' llamará a la tarea y éste se encargará de subir la foto
     * con sus datos a la BD
     */

    public void subirFoto(View v){
        Data.Builder dataBuilder = new Data.Builder()
                .putString("accion","subirPublicacion")
                .putString("username", username)
                .putString("foto",uri)
                .putDouble("latitud", lat)
                .putDouble("longitud",lon);


        Data data = dataBuilder.build();

        Constraints restricciones = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest trabajo = new OneTimeWorkRequest.Builder(Tarea.class)
                .setConstraints(restricciones)
                .setInputData(data)
                .build();

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(trabajo.getId()).observe(
                this, status -> {
                    if(status != null && status.getState().isFinished()) {
                        finish();
                    }
                }


        );

        WorkManager.getInstance(this).enqueue(trabajo);
    }
}
