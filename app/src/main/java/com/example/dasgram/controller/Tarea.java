package com.example.dasgram.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.dasgram.R;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
/**
 * Clase que se encarga de todas las tareas en segundo plano, dependiendo del atributo accion
 * hará una cosa u otra, aunque todas las tareas tienen que ver con operaciones en la BD.
 */
public class Tarea extends Worker {


    public Tarea(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Data data = getInputData();

        try {
            String accion = data.getString("accion");

            if(accion == null){
                return Result.failure();
            }

            switch (accion) {
                case "loginGoogle": {

                    String username = data.getString("username");
                    String pass = data.getString("pass");
                    String token = data.getString("token");

                    Bitmap imagen = GestorDB.getGestorDB(getApplicationContext()).comprobarUsuario(username,pass,token);

                    if(imagen == null) {
                        Log.i("Tarea", "Registrando usuario");
                        String email = data.getString("email");
                        String nombreCompleto = data.getString("nombreCompleto");
                        String foto = data.getString("foto");


                        imagen = GestorDB.getGestorDB(getApplicationContext()).registrarUsuarioGoogle(username, nombreCompleto, email, pass, foto,token);
                    }

                    if (imagen != null) {
                        Uri uri = bitmaptoUri(username, imagen);

                        Data result = new Data.Builder().putString("foto", uri.toString()).putString("username", username).build();
                        return Result.success(result);
                    } else {
                        return Result.failure();
                    }

                }
                case "comprobarUsuario": {

                    String username = data.getString("username");
                    String pass = data.getString("pass");
                    String token = data.getString("token");

                    Bitmap foto = GestorDB.getGestorDB(getApplicationContext()).comprobarUsuario(username, pass, token);

                    return Result.success(new Data.Builder()
                            .putBoolean("DatosCorrectos", foto!=null)
                            .putString("username",username)
                            .putString("foto", bitmaptoUri(username,foto).toString())
                            .build());

                }
                case "registrarUsuario": {

                    String username = data.getString("username");
                    String pass = data.getString("pass");

                    if(GestorDB.getGestorDB(getApplicationContext()).getFotoUsuario(username) != null)
                        return Result.success(new Data.Builder().putBoolean("existe",true).build());

                    String email = data.getString("email");
                    String nombreCompleto = data.getString("nombreCompleto");
                    String foto = data.getString("foto");
                    String token = data.getString("token");

                    Bitmap imagen = GestorDB.getGestorDB(getApplicationContext()).registrarUsuarioDatos(username, nombreCompleto, email, pass, foto, token);

                    if (imagen != null) {
                        Uri uri = bitmaptoUri(username, imagen);

                        Data result = new Data.Builder().putBoolean("existe",false).putString("foto", uri.toString()).putString("username", username).build();
                        return Result.success(result);
                    } else {
                        return Result.failure();
                    }


                }

                case "getPublicaciones": {
                    String result = GestorDB.getGestorDB(getApplicationContext()).getPublicaciones();

                    if(result == null || result.isEmpty()){
                        return Result.failure();
                    }else{
                        JSONParser parser = new JSONParser();
                        JSONArray json= (JSONArray) parser.parse(result);
                        for(int i = 0; i<json.size();i++) {
                            JSONObject jsonObject = (JSONObject) json.get(i);
                            String base64 = (String) jsonObject.get("foto");
                            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                            Uri uri = bitmaptoUri((String)jsonObject.get("idFoto"), bitmap);
                            jsonObject.put("foto",uri.toString());

                        }
                        Log.i("JSON_RESULT", "Resultado:" + json.toString());
                        return Result.success(new Data.Builder().putString("result",json.toString()).build());
                    }
                }

                case "subirPublicacion":{

                    String username = data.getString("username");
                    String uri = data.getString("foto");
                    Double latitud = data.getDouble("latitud",0);
                    Double longitud = data.getDouble("longitud",0);
                    if(GestorDB.getGestorDB(getApplicationContext()).subirPublicacion(username, uri, latitud, longitud)){
                        return Result.success();
                    }else{
                        return Result.failure();
                    }
                }

                case "getGustoFoto":{

                    String username = data.getString("username");
                    String idFoto = data.getString("idFoto");

                    boolean gusta = GestorDB.getGestorDB(getApplicationContext()).getGustoPublicacion(username,idFoto);

                    return Result.success(new Data.Builder().putBoolean("gusta",gusta).build());

                }

                case "actualizarLike":{

                    String username = data.getString("username");
                    String idFoto = data.getString("idFoto");
                    boolean like = data.getBoolean("like",false);
                    String token = data.getString("token");

                    GestorDB.getGestorDB(getApplicationContext()).actualizarLike(username,idFoto,like);

                    if(like){
                        GestorDB.getGestorDB(getApplicationContext()).notificarLike(getApplicationContext().getString(R.string.msgLike,username),token);
                    }

                    return Result.success();

                }

                case "borrarPublicacion":{
                    String idFoto = data.getString("idFoto");

                    boolean exito = GestorDB.getGestorDB(getApplicationContext()).borrarPublicacion(idFoto);
                    if(exito)
                        return Result.success();
                    return Result.failure();
                }

                case "publicacionReciente":{

                    String result = GestorDB.getGestorDB(getApplicationContext()).publicacionesRecientes();

                    if(result != null && !result.isEmpty()){
                        return Result.success(new Data.Builder().putString("result",result).build());
                    }else{
                        return Result.failure();
                    }

                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Uri bitmaptoUri(String id, Bitmap pBitmap){
        File eldirectorio= getApplicationContext().getFilesDir();
        String nombrefichero = id;
        File imagenFich= new File(eldirectorio, nombrefichero+ ".jpg");
        return GestorFicheros.getGestorFicheros().guardarArchivo(imagenFich,pBitmap);
    }
}
