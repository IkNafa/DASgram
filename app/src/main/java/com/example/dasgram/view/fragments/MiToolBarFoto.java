package com.example.dasgram.view.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.dasgram.R;
import com.example.dasgram.controller.Tarea;


public class MiToolBarFoto extends Fragment {

    public MiToolBarFoto() {
        // Required empty public constructor
    }

    private ImageButton btnRemove;
    private boolean borrar;
    private boolean like;
    private String username;
    private String idFoto;
    private String token;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_mitoolbarfoto, container, false);

        btnRemove = view.findViewById(R.id.btnRemove);
        btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(borrar){
                    AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                            .setTitle("Borrar")
                            .setMessage("¿Deseas borrar esta publicación?")
                            .setPositiveButton("Sí", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    borrarFoto();
                                }
                            })
                            .setNegativeButton("No", null).show();

                }else{
                    actualizarLike(!like);
                }
            }
        });



        return view;
    }

    public void enableRemove(Boolean pEnable){
        borrar = pEnable;

        if(borrar)
            btnRemove.setImageResource(R.drawable.ic_trash);
        else
            getLike();
    }

    public void enableLike(boolean pLike){
        borrar = false;
        like = pLike;
        btnRemove.setImageResource(pLike?R.drawable.like_si:R.drawable.like_no);
    }

    public void setDatos(String pUsername, String pIdFoto, String pToken){
        username = pUsername;
        idFoto = pIdFoto;
        token = pToken;
    }


    public void actualizarLike(boolean pLike){
        Data.Builder dataBuilder = new Data.Builder()
                .putString("accion","actualizarLike")
                .putString("username", username)
                .putString("idFoto",idFoto)
                .putBoolean("like", pLike)
                .putString("token",token);


        Data data = dataBuilder.build();

        Constraints restricciones = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest trabajo = new OneTimeWorkRequest.Builder(Tarea.class)
                .setConstraints(restricciones)
                .setInputData(data)
                .build();


        WorkManager.getInstance(getContext()).getWorkInfoByIdLiveData(trabajo.getId()).observe(
                this, status -> {
                    if(status != null && status.getState().isFinished()) {
                        enableLike(pLike);
                    }
                }


        );

        WorkManager.getInstance(getContext()).enqueue(trabajo);
    }

    public void borrarFoto(){
        Data.Builder dataBuilder = new Data.Builder()
                .putString("accion","borrarPublicacion")
                .putString("idFoto",idFoto);


        Data data = dataBuilder.build();

        Constraints restricciones = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest trabajo = new OneTimeWorkRequest.Builder(Tarea.class)
                .setConstraints(restricciones)
                .setInputData(data)
                .build();


        WorkManager.getInstance(getContext()).getWorkInfoByIdLiveData(trabajo.getId()).observe(
                this, status -> {
                    if(status != null && status.getState().isFinished()) {
                        getActivity().finish();
                    }
                }


        );

        WorkManager.getInstance(getContext()).enqueue(trabajo);
    }

    public void getLike(){
        Data.Builder dataBuilder = new Data.Builder()
                .putString("accion","getGustoFoto")
                .putString("username", username)
                .putString("idFoto",idFoto);


        Data data = dataBuilder.build();

        Constraints restricciones = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest trabajo = new OneTimeWorkRequest.Builder(Tarea.class)
                .setConstraints(restricciones)
                .setInputData(data)
                .build();

        WorkManager.getInstance(getContext()).getWorkInfoByIdLiveData(trabajo.getId()).observe(
                this, status -> {
                    if(status != null && status.getState().isFinished()) {
                        enableLike(status.getOutputData().getBoolean("gusta",false));
                    }
                }


        );

        WorkManager.getInstance(getContext()).enqueue(trabajo);
    }

}
