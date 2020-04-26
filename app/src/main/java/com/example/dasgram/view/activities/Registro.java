package com.example.dasgram.view.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.dasgram.R;
import com.example.dasgram.controller.Tarea;
import com.google.firebase.iid.FirebaseInstanceId;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Actividad que se encarga del registro de Usuarios.
 */
public class Registro extends AppCompatActivity {

    private CircleImageView imgView;
    private EditText username,
                     nombreCompleto,
                     email,
                     pass,
                     repPass;

    private CheckBox checkBox;


    private Uri foto;
    private String token = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        imgView = findViewById(R.id.imgPerfil);

        username = findViewById(R.id.edtUsername);
        username.setOnFocusChangeListener(new EdtFocusChangeListener());

        nombreCompleto = findViewById(R.id.edtNombreCompleto);
        nombreCompleto.setOnFocusChangeListener(new EdtFocusChangeListener());

        email = findViewById(R.id.email);
        email.setOnFocusChangeListener(new EdtFocusChangeListener());

        pass = findViewById(R.id.edtPass);
        pass.setOnFocusChangeListener(new EdtFocusChangeListener());

        repPass = findViewById(R.id.edtRepPass);
        repPass.setOnFocusChangeListener(new EdtFocusChangeListener());

        checkBox = findViewById(R.id.checkbox);
        checkBox.setOnFocusChangeListener(new EdtFocusChangeListener());

        //Obtenemos el token
        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null) {
                        token = task.getResult().getToken();
                    }

                });
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(requestCode == 1 && resultCode == RESULT_OK){
            if(data != null){
                foto= data.getData();
                imgView.setImageURI(foto);
            }
        }


        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(foto != null)
            outState.putString("foto",foto.toString());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String f = savedInstanceState.getString("foto");
        if(f != null && !f.isEmpty()){
            foto = Uri.parse(f);
            imgView.setImageURI(foto);
        }
    }

    public void abrirGaleria(View v){
        Intent elIntentGal= new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(elIntentGal, 1);
    }

    //Después de comprobar los datos, llama a la tarea con su acción correspondiente para registrar al usuario.
    public void registrarse(View v){
        if(!comprobarDatos())
            return;

        if(foto == null)
            foto = Uri.parse("android.resource://"+  getPackageName() + "/" + R.drawable.img_perfil);

        Data data = new Data.Builder()
                .putString("accion", "registrarUsuario")
                .putString("username", username.getText().toString().toLowerCase())
                .putString("pass",pass.getText().toString())
                .putString("nombreCompleto", nombreCompleto.getText().toString())
                .putString("email", email.getText().toString())
                .putString("foto",foto.toString())
                .putString("token",token)
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

                        Data nData = status.getOutputData();

                        boolean existe = nData.getBoolean("existe",true);
                        if(existe){
                            Toast.makeText(Registro.this, getString(R.string.yaExiste), Toast.LENGTH_SHORT).show();
                            username.setTextColor(Color.RED);
                        }else{
                            Intent intent = new Intent(Registro.this, MenuPrincipal.class);
                            intent.putExtra("username", nData.getString("username"));
                            intent.putExtra("foto",Uri.parse(nData.getString("foto")));

                            startActivity(intent);
                            finish();
                        }

                    }
                }
        );

        WorkManager.getInstance(this).enqueue(trabajo);

    }

    private boolean comprobarDatos(){
        String strUsername = username.getText().toString();
        String strNombreCompleto = nombreCompleto.getText().toString();
        String strEmail = email.getText().toString();
        String strPass = pass.getText().toString();
        String strRepPass = repPass.getText().toString();


        limpiarCampos();
        if(strUsername.isEmpty() || strNombreCompleto.isEmpty() || strEmail.isEmpty() || strPass.isEmpty() || !strPass.equals(strRepPass) || !checkBox.isChecked()){

            if(strUsername.isEmpty()){
                username.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, getDrawable(R.drawable.ic_warning), null);
            }

            if(strNombreCompleto.isEmpty()){
                nombreCompleto.setCompoundDrawablesWithIntrinsicBounds(null, null, getDrawable(R.drawable.ic_warning), null);
            }

            if(strEmail.isEmpty()){
                email.setCompoundDrawablesWithIntrinsicBounds(null, null, getDrawable(R.drawable.ic_warning), null);
            }

            if(strPass.isEmpty()){

                pass.setCompoundDrawablesWithIntrinsicBounds(null, null, getDrawable(R.drawable.ic_warning), null);

            }else if (!strPass.equals(strRepPass)){

                pass.setText("");
                repPass.setText("");

                pass.setCompoundDrawablesWithIntrinsicBounds(null, null, getDrawable(R.drawable.ic_warning), null);
                repPass.setCompoundDrawablesWithIntrinsicBounds(null, null, getDrawable(R.drawable.ic_warning), null);

            }

            if(!checkBox.isChecked()){
                checkBox.setCompoundDrawablesRelativeWithIntrinsicBounds(null,null, getDrawable(R.drawable.ic_warning),null);
            }

            return false;
        }

        return true;

    }

    private void limpiarCampos(){
        username.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
        username.setTextColor(Color.BLACK);
        nombreCompleto.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
        email.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
        pass.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
        repPass.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
        checkBox.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
    }


    class EdtFocusChangeListener implements View.OnFocusChangeListener{

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if(hasFocus)
                limpiarCampos();
        }
    }
}




