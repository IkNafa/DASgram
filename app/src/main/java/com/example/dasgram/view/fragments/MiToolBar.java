package com.example.dasgram.view.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.dasgram.R;
import com.example.dasgram.view.activities.Preferencias;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MiToolBar extends Fragment {

    private CircleImageView imgView;
    private ImageView btnTools;

    public MiToolBar() {
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
        View v =  inflater.inflate(R.layout.fragment_mitoolbar, container, false);

        imgView = v.findViewById(R.id.imgPerfil);
        btnTools = v.findViewById(R.id.btnTools);
        btnTools.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toPreferencias();
            }
        });

        return v;
    }

    public void cambiarFotoPerfil(Uri pUri){
        Picasso.with(getContext()).load(pUri).into(imgView);
    }

    private void toPreferencias(){
        Intent i = new Intent(getActivity(), Preferencias.class);
        startActivity(i);
    }
}
