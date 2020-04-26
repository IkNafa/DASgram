package com.example.dasgram.view.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.dasgram.controller.GestorFicheros;
import com.example.dasgram.model.MiServicio;
import com.example.dasgram.model.Publicacion;
import com.example.dasgram.R;
import com.example.dasgram.controller.Tarea;
import com.example.dasgram.view.fragments.MiToolBar;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * Actividad del menú principal, donde se mostrarán todas las publicaciones subidad.
 */
public class MenuPrincipal extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Fragment toolbar;

    private String username;
    private Uri foto;
    private ArrayList<Publicacion> publicacions;
    private ElAdaptadorRecycler elAdaptadorRecycler;


    MiServicio miServicio;
    private ServiceConnection laConexion = new ServiceConnection() {
        //Cuando el servicio esté creado, si la musica está activada se empezará a reproducir.
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("Servicio","creado");
            miServicio = ((MiServicio.MiBinder) service).obtenServicio();

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MenuPrincipal.this);
            boolean music = sharedPreferences.getBoolean("music",false);
            if(music){
                String id = sharedPreferences.getString("song","");
                miServicio.playMusic(id);
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("Servicio","destruido");
            miServicio = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menuprincipal);
        recyclerView = findViewById(R.id.recycler_view);

        toolbar = getSupportFragmentManager().findFragmentById(R.id.frgToolBar);

        cargarDatosIntent();


        publicacions = new ArrayList<>();


        elAdaptadorRecycler = new ElAdaptadorRecycler(publicacions);
        recyclerView.setAdapter(elAdaptadorRecycler);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(gridLayoutManager);

        int spanCount = 2; // 2 columns
        int spacing = 50; // 50px
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));

        startService();
    }


    private void cargarDatosIntent() {

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("username");
            foto = (Uri) extras.get("foto");
            setFotoPerfil(foto);
        }

    }

    //Iniciamos el servicio
    private void startService(){
        Log.i("Servicio","Creando servicio");
        Intent intentServicio = new Intent(this, MiServicio.class);
        bindService(intentServicio,laConexion, Context.BIND_AUTO_CREATE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            startForegroundService(intentServicio);
        }else{
            startService(intentServicio);
        }

    }

    @Override
    public void onBackPressed() {
        AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.salir)
                .setMessage(R.string.msgSalir)
                .setPositiveButton("Si", (dialog1, which) -> cerrarSesion()).setNegativeButton("No", null).show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MenuPrincipal.this);
        String id = sharedPreferences.getString("song","");
        boolean music = sharedPreferences.getBoolean("music",false);
        if(miServicio != null){

            if(!music){
                miServicio.stop();
            }else if(!miServicio.esIgual(id)){
                miServicio.playMusic(id);
            }


        }
        cargarPublicaciones();
    }

    private void setFotoPerfil(Uri pUri) {
        if (toolbar instanceof MiToolBar) {
            MiToolBar miToolBar = (MiToolBar) toolbar;
            miToolBar.cambiarFotoPerfil(pUri);
        }
    }

    //Borra los datos de google y las fotos de la memoria.
    public void cerrarSesion() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        miServicio.stop();
                        unbindService(laConexion);
                        miServicio.stopSelf();

                        Intent intent = new Intent(MenuPrincipal.this, MainActivity.class);
                        startActivity(intent);

                        GestorFicheros.getGestorFicheros().borrarFicheros();

                        finish();
                    }
                });


    }

    //Carga todas las fotos de la BD
    private void cargarPublicaciones(){
        Data data = new Data.Builder()
                .putString("accion","getPublicaciones")
                .build();

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

                        Data mdata = status.getOutputData();
                        String result = mdata.getString("result");
                        if(result == null)
                            return;

                        JSONParser parser= new JSONParser();
                        try {

                            JSONArray json= (JSONArray) parser.parse(result);
                            Publicacion[] publicaciones = new Publicacion[json.size()];
                            for(int i = 0; i<json.size();i++){
                                JSONObject jsonObject = (JSONObject) json.get(i);

                                if((String)jsonObject.get("latitud") == null || (String)jsonObject.get("longitud") == null){
                                    publicaciones[i] = new Publicacion((String)jsonObject.get("idFoto"),
                                            (String)jsonObject.get("username"),
                                            (String)jsonObject.get("nombreCompleto"),
                                            Uri.parse((String)jsonObject.get("foto")),
                                            (String)jsonObject.get("token"));
                                }else{
                                    publicaciones[i] = new Publicacion((String)jsonObject.get("idFoto"),
                                            (String)jsonObject.get("username"),
                                            (String)jsonObject.get("nombreCompleto"),
                                            Double.parseDouble((String)jsonObject.get("latitud")),
                                            Double.parseDouble((String)jsonObject.get("longitud")),
                                            Uri.parse((String)jsonObject.get("foto")),
                                            (String)jsonObject.get("token"));
                                }




                            }



                            actualizarLista(publicaciones);


                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }


        );

        WorkManager.getInstance(this).enqueue(trabajo);

    }

    private void actualizarLista(Publicacion[] pPublicaciones){
        publicacions.clear();
        publicacions.addAll(Arrays.asList(pPublicaciones));

        elAdaptadorRecycler.notifyDataSetChanged();

    }


    public void subirPublicacion(Uri uri) {

        Intent intent = new Intent(this, EditarFoto.class);
        intent.putExtra("uri", uri.toString());
        intent.putExtra("username", username);
        startActivity(intent);
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

    class ElAdaptadorRecycler extends RecyclerView.Adapter<ElViewHolder> {

        private ArrayList<Publicacion> publicaciones;

        public ElAdaptadorRecycler(ArrayList<Publicacion> pPublicaciones) {
            publicaciones = pPublicaciones;
        }

        @NonNull
        @Override
        public ElViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View elLayout = LayoutInflater.from(parent.getContext()).inflate(R.layout.micardview, null);
            ElViewHolder evh = new ElViewHolder(elLayout);
            return evh;
        }

        @Override
        public void onBindViewHolder(@NonNull final ElViewHolder holder, final int position) {

            final Publicacion p = publicaciones.get(position);

            holder.txtNombre.setText(p.getNombreCompleto());
            holder.imgFoto.setImageURI(p.getFoto());



            if (p.getLocalizacion().latitude == 0 && p.getLocalizacion().longitude == 0) {
                holder.txtLocation.setVisibility(View.INVISIBLE);
            } else {

                Geocoder gcd = new Geocoder(MenuPrincipal.this, Locale.getDefault());
                List<Address> addresses = null;
                String ciudad = "Lat: " + p.getLocalizacion().latitude + "\nLong: " + p.getLocalizacion().longitude;
                try {
                    addresses = gcd.getFromLocation(p.getLocalizacion().latitude, p.getLocalizacion().longitude, 1);
                    if(addresses.size() > 0){
                        String c = getLocalizacionName(addresses.get(0));
                        if(c != null){
                            ciudad = c;
                        }
                    }
                } catch (IOException e) {}

                holder.txtLocation.setText(ciudad);

                holder.txtLocation.setVisibility(View.VISIBLE);

            }

            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(MenuPrincipal.this, InfoFoto.class);
                    i.putExtra("username",username);

                    i.putExtra("usernameFoto", p.getUsername());
                    i.putExtra("foto", p.getFoto());
                    i.putExtra("autor", p.getNombreCompleto());
                    i.putExtra("latitud",p.getLocalizacion().latitude);
                    i.putExtra("longitud",p.getLocalizacion().longitude);
                    i.putExtra("idFoto",p.getIdFoto());
                    i.putExtra("token", p.getToken());

                    startActivity(i);
                }
            });
        }

        @Override
        public int getItemCount() {
            return publicaciones.size();
        }
    }

    class ElViewHolder extends RecyclerView.ViewHolder {

        public CardView cardView;
        public TextView txtNombre;
        public ImageView imgFoto;
        public TextView txtLocation;

        public ElViewHolder(View v) {
            super(v);
            cardView = v.findViewById(R.id.card_view);
            txtNombre = v.findViewById(R.id.txtPerfilFoto);
            imgFoto = v.findViewById(R.id.imgFoto);
            txtLocation = v.findViewById(R.id.txtLocation);
        }
    }


    /**
     * Clase utilizada para el espaciado de elementos en el RecyclerView
     * Pregunta: https://stackoverflow.com/questions/28531996/android-recyclerview-gridlayoutmanager-column-spacing
     * Autor: https://stackoverflow.com/users/1676363/ianhanniballake
     */
    class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                if (position < spanCount) { // top edge
                    outRect.top = spacing;
                }
                outRect.bottom = spacing; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

}