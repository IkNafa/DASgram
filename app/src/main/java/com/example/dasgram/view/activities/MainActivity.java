package com.example.dasgram.view.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.dasgram.R;
import com.example.dasgram.controller.Tarea;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Actividad que se encarga del inicio de sesión, es la actividad principal.
 * @author Iker Nafarrate Bilbao
 */
public class MainActivity extends AppCompatActivity {

    private TextView username;
    private TextView pass;
    private GoogleSignInClient mGoogleSignInClient;
    private String token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = findViewById(R.id.edtNombre);
        pass = findViewById(R.id.edtPass);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null) {
                        token= task.getResult().getToken();
                    }
                });
    }

    /**
     * Si ya habia iniciado sesión con Google anteriormente no será necesario que vuelva a
     * hacerlo. Le redigirá directamente.
     */
    @Override
    protected void onStart() {
        super.onStart();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null){
            irAMenuPrincipal(account.getEmail().split("@")[0],account.getPhotoUrl());
        }
    }


    /**
     * Metodo que se invoca cuando se pulse el botón asignado, te lleva a la pantalla del Registro.
     */
    public void toRegistro(View v){
        Intent i = new Intent(this,Registro.class);
        startActivity(i);
    }

    /**
     * Antes de nada comprueba si los campos están introducidos de manera correcta, luego realiza distintas operaciones
     * hasta redirigirte al menú principal
     */
    public void toMenuPrincipal(View v){

        if(!comprobarDatos())
            return;

        String strUsername = username.getText().toString();
        String strPass = pass.getText().toString();

        iniciarSesion(false,strUsername,strPass,null,null,null);

    }


    /**
     * Método que se ejecuta cuando se pulsa sobre 'Iniciar sesion con Google'
     */
    public void loginGoogle(View v){
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, 100);
    }

    /**
     * Gestiona cuando se selecciona una cuenta de Google.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 100) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);

                iniciarSesionORegistrarteGoogle(account);

            } catch (ApiException e) {
                Log.w("Error", "signInResult:failed code=" + e.getStatusCode());

                Toast.makeText(this, getString(R.string.error), Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Recoge los datos que necesitamos de la cuenta de Google
     * @param account
     */
    private void iniciarSesionORegistrarteGoogle(GoogleSignInAccount account){
        String username = account.getEmail().split("@")[0];
        String nombreCompleto = account.getDisplayName();
        String email = account.getEmail();
        Uri foto = account.getPhotoUrl();
        String pass = account.getId();

        iniciarSesion(true, username, pass, nombreCompleto, email, foto);

    }

    /**
     * Metodo común para los que inician sesión con Google o introduciendo los campos manualmente.
     * En el caso de iniciar sesión con Google y no esté registrado, tendrá que registrarle.
     * Llamará a la Tarea con su acción correspondiente: "loginGoogle" si quiere iniciar sesión con Google
     * y "comprobarUsuario" si no está registrado con una cuenta Google.
     * @param pGoogle
     * @param pUsername
     * @param pPass
     * @param pNombreCompleto
     * @param pEmail
     * @param pFoto
     */
    private void iniciarSesion(boolean pGoogle, String pUsername, String pPass, String pNombreCompleto, String pEmail, Uri pFoto){
        Data.Builder dataBuilder = new Data.Builder()
                .putString("accion", pGoogle?"loginGoogle":"comprobarUsuario")
                .putString("username", pUsername)
                .putString("pass",pPass)
                .putString("token",token);

        if(pGoogle){
            dataBuilder.putString("nombreCompleto", pNombreCompleto)
                    .putString("email", pEmail)
                    .putString("foto",pFoto.toString());
        }

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

                        Data nData = status.getOutputData();

                        if(!pGoogle){
                            Boolean correcto = nData.getBoolean("DatosCorrectos",false);
                            if(!correcto){
                                Toast.makeText(this, getString(R.string.usuarioyPassIncorrecto), Toast.LENGTH_SHORT).show();
                                return;
                            }
                        }


                        irAMenuPrincipal(nData.getString("username"),Uri.parse(nData.getString("foto")));
                    }
                }
        );

        WorkManager.getInstance(this).enqueue(trabajo);
    }

    private boolean comprobarDatos() {
        String strUsername = username.getText().toString();
        String strPass = pass.getText().toString();

        if(strUsername.isEmpty() || strPass.isEmpty()){
            if(strUsername.isEmpty()){
                username.setCompoundDrawablesWithIntrinsicBounds(null,null,getDrawable(R.drawable.ic_warning),null);
            }

            if(strPass.isEmpty()){
                pass.setCompoundDrawablesWithIntrinsicBounds(null,null,getDrawable(R.drawable.ic_warning),null);
            }

            return false;
        }

        return true;

    }

    /**
     * En el momento que el inicio de sesión ya haya sido correcto se llamará a este método que se encargará
     * de guardar algunos datos en las preferencias para después mostrarlos en el Widget.
     * El like más reciente, la última foto subida y la música que tiene configurado el usuario para
     * que se reproduzca en el menú principal.
     * @param username
     * @param pUri
     */
    private void irAMenuPrincipal(String username, Uri pUri){

        Data.Builder dataBuilder = new Data.Builder()
                .putString("accion", "publicacionReciente");

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
                    if (status != null && status.getState().isFinished()) {

                        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

                        Data nData = status.getOutputData();
                        String strResul = nData.getString("result");

                        JSONParser parser = new JSONParser();
                        JSONArray jsonArray = null;
                        try {
                            jsonArray = (JSONArray) parser.parse(strResul);
                            for(int i = 0; i<jsonArray.size();i++){
                                JSONObject object = (JSONObject) jsonArray.get(i);
                                String accion = (String) object.get("accion");
                                switch (accion){
                                    case "like": {
                                        String a = (String) object.get("a");
                                        String de = (String) object.get("de");
                                        sharedPreferences.edit().putString("likeA", a).putString("likeDe", de).apply();
                                        break;
                                    }
                                    case "subir": {
                                        String de = (String) object.get("de");
                                        sharedPreferences.edit().putString("subirDe", de).apply();
                                        break;
                                    }
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }




                        sharedPreferences.edit().putString("username",username).apply();

                        Intent intent = new Intent(MainActivity.this, MenuPrincipal.class);
                        intent.putExtra("username", username);
                        intent.putExtra("foto",pUri);

                        startActivity(intent);
                        finish();
                    }
                }
        );

        WorkManager.getInstance(this).enqueue(trabajo);



    }


}
