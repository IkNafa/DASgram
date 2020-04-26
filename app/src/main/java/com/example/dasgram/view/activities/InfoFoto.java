package com.example.dasgram.view.activities;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dasgram.R;
import com.example.dasgram.view.fragments.MiToolBarFoto;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class InfoFoto extends FragmentActivity implements OnMapReadyCallback {

    private double latitud;
    private double longitud;
    private String ciudad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infofoto);

        TextView autor = findViewById(R.id.txtPerfilFoto);
        TextView localizacion = findViewById(R.id.txtLocation);
        ImageView foto = findViewById(R.id.imgFoto);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){

            Geocoder gcd = new Geocoder(this, Locale.getDefault());

            latitud = bundle.getDouble("latitud");
            longitud = bundle.getDouble("longitud");

            if(latitud == 0 || longitud == 0){
                localizacion.setText("--");
                localizacion.setCompoundDrawablesWithIntrinsicBounds(null,null,null,null);
                localizacion.setTextColor(Color.BLACK);

                View v = findViewById(R.id.map);
                v.setVisibility(View.INVISIBLE);

            }else{
                ciudad = latitud + "," + longitud;
                try {
                    List<Address> addresses = gcd.getFromLocation(latitud, longitud, 1);
                    if(addresses.size() > 0){
                        Address address = addresses.get(0);
                        ciudad = getLocalizacionName(address);

                    }
                } catch (IOException ignored) {}

                localizacion.setText(ciudad);


                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);

            }

            foto.setImageURI((Uri) bundle.get("foto"));
            autor.setText(bundle.getString("autor"));
            String username = bundle.getString("username");

            Fragment frg = getSupportFragmentManager().findFragmentById(R.id.frgToolBar);
            if(frg instanceof MiToolBarFoto){
                MiToolBarFoto miToolBarFoto = (MiToolBarFoto) frg;

                miToolBarFoto.setDatos(bundle.getString("username"), bundle.getString("idFoto"), bundle.getString("token"));

                if(username.equals(bundle.getString("usernameFoto"))){
                    miToolBarFoto.enableRemove(true);
                }else{
                    miToolBarFoto.enableRemove(false);
                }
            }



        }

    }


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

    @Override
    public void onMapReady(GoogleMap googleMap) {

        LatLng localizacion = new LatLng(latitud, longitud);

        googleMap.addMarker(new MarkerOptions().position(localizacion).title(ciudad));
        CameraPosition Poscam = new CameraPosition.Builder()
                .target(localizacion)
                .zoom(10)
                .build();
        CameraUpdate otravista = CameraUpdateFactory.newCameraPosition(Poscam);
        googleMap.moveCamera(otravista);
    }
}
