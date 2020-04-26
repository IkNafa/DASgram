package com.example.dasgram.model;
import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

/**
 * Objeto Publicación para guargar los datos
 * @author Iker Nafarrate Bilbao
 */
public class Publicacion {

    private String idFoto;
    private String username;
    private String nombreCompleto;
    private double latitud;
    private double longitud;
    private Uri foto;
    private String token;

    public Publicacion(String pIdFoto, String pUsername, String pNombre, double pLatitud, double pLongitud, Uri pFoto, String pToken){
        idFoto = pIdFoto;
        username = pUsername;
        nombreCompleto = pNombre;
        latitud = pLatitud;
        longitud = pLongitud;
        foto = pFoto;
        token = pToken;
    }

    public Publicacion(String pIdFoto, String pUsername, String pNombre, Uri pFoto,String pToken){
        this(pIdFoto, pUsername, pNombre, 0,0, pFoto, pToken);
    }

    public String getUsername() {
        return username;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public LatLng getLocalizacion() {
        return new LatLng(latitud,longitud);
    }

    public Uri getFoto() {
        return foto;
    }

    public String getToken(){return token;}

    public String getIdFoto() {
        return idFoto;
    }
}
