package com.example.dasgram.view.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.dasgram.R;
import com.example.dasgram.controller.GestorFicheros;
import com.example.dasgram.view.activities.MenuPrincipal;
import com.getbase.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.sql.Date;
import java.util.Locale;


//https://www.youtube.com/watch?v=XsdG_6i5YIk
public class MiFloatingButton extends Fragment {

    private FloatingActionButton btnUpload;
    private FloatingActionButton btnCamera;

    public MiFloatingButton() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_mifloatingbutton, container, false);

        btnUpload = v.findViewById(R.id.btnUpload);
        btnCamera = v.findViewById(R.id.btnCamera);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirGaleria(v);
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirCamara(v);
            }
        });

        return v;

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if((requestCode == 1 || requestCode == 2) && resultCode == Activity.RESULT_OK){

            Uri uri;
            if(requestCode == 1){
                uri = data.getData();
            }else{
                File eldirectorio= getContext().getFilesDir();

                String nombrefichero = "camara";
                File imagenFich= new File(eldirectorio, nombrefichero+ ".jpg");
                uri = GestorFicheros.getGestorFicheros().guardarArchivo(imagenFich,(Bitmap)data.getExtras().get("data"));
            }

            if(getActivity() instanceof MenuPrincipal){
                MenuPrincipal mp = (MenuPrincipal) getActivity();
                mp.subirPublicacion(uri);
            }
        }
    }

    private void abrirGaleria(View v){
        Intent elIntentGal= new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(elIntentGal, 1);
    }

    private void abrirCamara(View v){

        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA},1);

            if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                return;
            }
        }
        Intent elIntent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (elIntent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(elIntent, 2);
        }

    }
}
