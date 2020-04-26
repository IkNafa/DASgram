package com.example.dasgram.view.activities;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;

import com.example.dasgram.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Actividad que muestra el Mapa de Google Maps con el que se establecerá la localización de una publicación.
 * @author Iker Nafarrate Bilbao
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FloatingActionButton floatingActionButton;
    private boolean mark;
    private LatLng selected;
    private String ciudad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        floatingActionButton = findViewById(R.id.floatingActionButton);
        floatingActionButton.setVisibility(View.INVISIBLE);

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("latitud",selected.latitude);
                intent.putExtra("longitud", selected.longitude);
                intent.putExtra("ciudad",ciudad);
                setResult(RESULT_OK,intent);
                finish();
            }
        });

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void onBackPressed() {
        if(mark){
            mark = false;
            mMap.clear();
            floatingActionButton.setVisibility(View.INVISIBLE);
        }else{
            finish();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng bilbao = new LatLng(43.25, -2.92);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(bilbao));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mark = true;
                mMap.clear();

                selected = latLng;

                Geocoder gcd = new Geocoder(MapsActivity.this, Locale.getDefault());
                List<Address> addresses = null;
                ciudad = "";
                try {
                    addresses = gcd.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    if(addresses.size() > 0){
                        ciudad = getLocalizacionName(addresses.get(0));
                    }
                } catch (IOException e) {}


                mMap.addMarker(new MarkerOptions().position(latLng).title(ciudad)).showInfoWindow();

                floatingActionButton.setVisibility(View.VISIBLE);

            }
        });



    }

    /**
     * Metodo que dado un Address se encarga de crear un nombre que se mostrará en el mapa.
     * Bilbao/Bizkaia o Bizkaia/Euskadi o Euskadi/España
     * @param address
     * @return
     */
    private String getLocalizacionName(Address address){
        String ciudad = address.getLocality(); //BILBAO
        if(ciudad == null){

            ciudad = address.getSubAdminArea(); //BIZKAIA

            if(ciudad == null){
                ciudad = address.getAdminArea();  //EUSKADI

                if(ciudad == null){

                    ciudad = address.getCountryName(); //ESPAÑA


                }else{
                    ciudad += ("/" + address.getCountryName());
                }
            }else{
                ciudad+= ("/" + address.getAdminArea());
            }

        }else{
            ciudad += ("/" + address.getSubAdminArea());
        }

        return ciudad;
    }
}
