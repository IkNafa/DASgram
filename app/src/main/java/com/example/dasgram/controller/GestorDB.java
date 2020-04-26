package com.example.dasgram.controller;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Clase Sigleton correspondiente con todas las acciones que se realizan con la BD, por cada operación hay un método
 * a esta clase se le llama únicamente desde Tarea.
 * @author Iker Nafarrate Bilbao
 */
public class GestorDB {

    private static GestorDB mGestorDB;
    private static Context contextActual;

    private GestorDB(Context pContexto){
        contextActual = pContexto;
    }

    public static GestorDB getGestorDB(Context pContexto){
        if(mGestorDB == null || pContexto != contextActual)
            mGestorDB = new GestorDB(pContexto);
        return mGestorDB;
    }

    public Bitmap comprobarUsuario(String pUsername, String pPass, String token) throws IOException, ParseException {
        HttpsURLConnection conexion = GeneradorConexionesSeguras.getInstance().crearConexionSegura(contextActual, "https://134.209.235.115/inafarrate002/WEB/pruebaConexion.php");

        JSONObject parametrosJSON = new JSONObject();
        parametrosJSON.put("accion", "getDatosUsuario");
        parametrosJSON.put("username", pUsername.toLowerCase());
        parametrosJSON.put("pass", pPass);
        parametrosJSON.put("token",token);
        String parametros = parametrosJSON.toString();
        conexion.setRequestMethod("POST");
        conexion.setDoOutput(true);
        conexion.setRequestProperty("Content-Type", "application/json");

        PrintWriter out = new PrintWriter(conexion.getOutputStream());
        out.print(parametros);
        out.close();

        int statusCode = conexion.getResponseCode();

        Log.i("StatusCode", statusCode + "");
        if (statusCode == 200) {
            BufferedInputStream inputStream = new BufferedInputStream(conexion.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line, result = "";
            return BitmapFactory.decodeStream(conexion.getInputStream());
        }
        return null;

    }

    public Bitmap registrarUsuarioGoogle(String pUsuario, String pNombreCompleto, String pEmail, String pPass, String pUri, String token) throws IOException {

        URL url = new URL(pUri);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoInput(true);
        connection.connect();
        InputStream input = connection.getInputStream();
        Bitmap imagen = BitmapFactory.decodeStream(input);

        return registrarUsuario(pUsuario, pNombreCompleto, pEmail,pPass, imagen, token);
    }

    public Bitmap registrarUsuarioDatos(String pUsuario, String pNombreCompleto, String pEmail, String pPass, String pUri, String token) throws IOException {
        Uri uri = Uri.parse(pUri);
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(contextActual.getContentResolver(), uri);

        return registrarUsuario(pUsuario, pNombreCompleto, pEmail, pPass, bitmap, token);
    }


    private Bitmap registrarUsuario(String pUsuario, String pNombreCompleto, String pEmail, String pPass, Bitmap pImagen, String token) throws IOException {

        HttpsURLConnection conexion = GeneradorConexionesSeguras.getInstance().crearConexionSegura(contextActual,"https://134.209.235.115/inafarrate002/WEB/pruebaConexion.php");

        Bitmap imagen = reescalarImagen(pImagen);

        String base64 = bitmapToBase64(imagen);



        JSONObject parametrosJSON= new JSONObject();
        parametrosJSON.put("accion","registrarUsuario");
        parametrosJSON.put("username", pUsuario.toLowerCase());
        parametrosJSON.put("nombrecompleto", pNombreCompleto);
        parametrosJSON.put("pass", pPass);
        parametrosJSON.put("email", pEmail);
        parametrosJSON.put("foto", base64);
        parametrosJSON.put("token", token);
        String parametros= parametrosJSON.toString();
        conexion.setRequestMethod("POST");
        conexion.setDoOutput(true);
        conexion.setRequestProperty("Content-Type", "application/json");

        PrintWriter out= new PrintWriter(conexion.getOutputStream());
        out.print(parametros);
        out.close();

        int statusCode= conexion.getResponseCode();

        if(statusCode == 200){
            return imagen;
        }else{
            Log.i("resultado","error " + statusCode);
        }

        return null;
    }

    public Bitmap getFotoUsuario(String username) throws IOException{

        HttpsURLConnection conexion = GeneradorConexionesSeguras.getInstance().crearConexionSegura(contextActual,"https://134.209.235.115/inafarrate002/WEB/pruebaConexion.php");


        JSONObject parametrosJSON= new JSONObject();
        parametrosJSON.put("accion","getFotoUsername");
        parametrosJSON.put("username", username.toLowerCase());
        String parametros= parametrosJSON.toString();
        conexion.setRequestMethod("POST");
        conexion.setDoOutput(true);
        conexion.setRequestProperty("Content-Type", "application/json");

        PrintWriter out= new PrintWriter(conexion.getOutputStream());
        out.print(parametros);
        out.close();

        int statusCode= conexion.getResponseCode();

        if(statusCode == 200){
            return BitmapFactory.decodeStream(conexion.getInputStream());
        }

        return null;
    }

    public String getPublicaciones() throws IOException {
        HttpsURLConnection conexion = GeneradorConexionesSeguras.getInstance().crearConexionSegura(contextActual,"https://134.209.235.115/inafarrate002/WEB/publicacionesLikes.php");
        JSONObject parametrosJSON= new JSONObject();
        parametrosJSON.put("accion","getPublicaciones");
        String parametros= parametrosJSON.toString();
        conexion.setRequestMethod("POST");
        conexion.setDoOutput(true);
        conexion.setRequestProperty("Content-Type", "application/json");

        PrintWriter out= new PrintWriter(conexion.getOutputStream());
        out.print(parametros);
        out.close();

        int statusCode= conexion.getResponseCode();

        if(statusCode == 200){
            BufferedInputStream inputStream= new BufferedInputStream(conexion.getInputStream());
            BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line, result="";
            while((line = bufferedReader.readLine()) != null){
                result += line;
            }
            inputStream.close();

            return result;

        }

        return null;
    }

    public boolean getGustoPublicacion(String pUsername, String pIdFoto) throws IOException {
        HttpsURLConnection conexion = GeneradorConexionesSeguras.getInstance().crearConexionSegura(contextActual,"https://134.209.235.115/inafarrate002/WEB/publicacionesLikes.php");
        JSONObject parametrosJSON= new JSONObject();
        parametrosJSON.put("accion","getGustoFoto");
        parametrosJSON.put("username", pUsername);
        parametrosJSON.put("idFoto", pIdFoto);
        String parametros= parametrosJSON.toString();
        conexion.setRequestMethod("POST");
        conexion.setDoOutput(true);
        conexion.setRequestProperty("Content-Type", "application/json");

        PrintWriter out= new PrintWriter(conexion.getOutputStream());
        out.print(parametros);
        out.close();

        int statusCode= conexion.getResponseCode();

        if(statusCode == 200){
            BufferedInputStream inputStream= new BufferedInputStream(conexion.getInputStream());
            BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line, result="";
            while((line = bufferedReader.readLine()) != null){
                result += line;
            }
            inputStream.close();

            return Integer.parseInt(result) == 1;

        }

        Log.i("Gusta","Error");

        return false;
    }

    public void actualizarLike(String pUsername, String pIdFoto, boolean like) throws IOException {

        HttpsURLConnection conexion = GeneradorConexionesSeguras.getInstance().crearConexionSegura(contextActual,"https://134.209.235.115/inafarrate002/WEB/publicacionesLikes.php");
        JSONObject parametrosJSON= new JSONObject();
        parametrosJSON.put("accion","actualizarLike");
        parametrosJSON.put("username", pUsername);
        parametrosJSON.put("idFoto", pIdFoto);
        parametrosJSON.put("like",like);
        String parametros= parametrosJSON.toString();
        conexion.setRequestMethod("POST");
        conexion.setDoOutput(true);
        conexion.setRequestProperty("Content-Type", "application/json");

        PrintWriter out= new PrintWriter(conexion.getOutputStream());
        out.print(parametros);
        out.close();

        int statusCode= conexion.getResponseCode();



    }

    public boolean subirPublicacion(String username, String pUri, Double latitud, Double longitud) throws IOException {
        HttpsURLConnection conexion = GeneradorConexionesSeguras.getInstance().crearConexionSegura(contextActual,"https://134.209.235.115/inafarrate002/WEB/publicacionesLikes.php");
        JSONObject parametrosJSON= new JSONObject();
        parametrosJSON.put("accion","publicar");
        parametrosJSON.put("username",username.toLowerCase());
        parametrosJSON.put("latitud",latitud);
        parametrosJSON.put("longitud",longitud);

        Uri uri = Uri.parse(pUri);
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(contextActual.getContentResolver(), uri);
        bitmap = reescalarImagen(bitmap);

        parametrosJSON.put("foto",bitmapToBase64(bitmap));
        String parametros= parametrosJSON.toString();
        conexion.setRequestMethod("POST");
        conexion.setDoOutput(true);
        conexion.setRequestProperty("Content-Type", "application/json");

        PrintWriter out= new PrintWriter(conexion.getOutputStream());
        out.print(parametros);
        out.close();

        int statusCode= conexion.getResponseCode();

        return statusCode==200;
    }

    public boolean borrarPublicacion(String pIdFoto) throws IOException {
        HttpsURLConnection conexion = GeneradorConexionesSeguras.getInstance().crearConexionSegura(contextActual,"https://134.209.235.115/inafarrate002/WEB/publicacionesLikes.php");
        JSONObject parametrosJSON= new JSONObject();
        parametrosJSON.put("accion","borrarPublicacion");
        parametrosJSON.put("idFoto", pIdFoto);
        String parametros= parametrosJSON.toString();
        conexion.setRequestMethod("POST");
        conexion.setDoOutput(true);
        conexion.setRequestProperty("Content-Type", "application/json");

        PrintWriter out= new PrintWriter(conexion.getOutputStream());
        out.print(parametros);
        out.close();

        int statusCode= conexion.getResponseCode();

        return statusCode == 200;
    }

    public void notificarLike(String username, String token) throws IOException {

        HttpsURLConnection conexion = GeneradorConexionesSeguras.getInstance().crearConexionSegura(contextActual,"https://134.209.235.115/inafarrate002/WEB/notificaciones.php");
        JSONObject parametrosJSON= new JSONObject();
        parametrosJSON.put("username",username.toLowerCase());
        parametrosJSON.put("token", token);
        String parametros= parametrosJSON.toString();
        conexion.setRequestMethod("POST");
        conexion.setDoOutput(true);
        conexion.setRequestProperty("Content-Type", "application/json");

        PrintWriter out= new PrintWriter(conexion.getOutputStream());
        out.print(parametros);
        out.close();

        int statusCode= conexion.getResponseCode();
        if(statusCode == 200){
            BufferedInputStream inputStream= new BufferedInputStream(conexion.getInputStream());
            BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line, result="";
            while((line = bufferedReader.readLine()) != null){
                result += line;
            }
            inputStream.close();

            Log.i("Notificacion", "To-> " + token);
        }else{

        }

    }

    private String bitmapToBase64(Bitmap pFoto){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        pFoto.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] fototransformada = stream.toByteArray();
        return Base64.encodeToString(fototransformada, Base64.DEFAULT);
    }

    private Bitmap reescalarImagen(Bitmap bitmapFoto){
        int anchoDestino= 300;
        int altoDestino= 300;
        int anchoImagen= bitmapFoto.getWidth();
        int altoImagen= bitmapFoto.getHeight();
        float ratioImagen= (float) anchoImagen/ (float) altoImagen;
        float ratioDestino= (float) anchoDestino/ (float) altoDestino;
        int anchoFinal= anchoDestino;
        int altoFinal= altoDestino;

        if (ratioDestino> ratioImagen) {
            anchoFinal= (int) ((float)altoDestino* ratioImagen);
        } else{
            altoFinal= (int) ((float)anchoDestino/ ratioImagen);
        }
        return Bitmap.createScaledBitmap(bitmapFoto,anchoFinal,altoFinal,true);
    }


    public String publicacionesRecientes() throws IOException {

        HttpsURLConnection conexion = GeneradorConexionesSeguras.getInstance().crearConexionSegura(contextActual,"https://134.209.235.115/inafarrate002/WEB/publicacionesLikes.php");
        JSONObject parametrosJSON= new JSONObject();
        parametrosJSON.put("accion","publicacionReciente");
        String parametros= parametrosJSON.toString();
        conexion.setRequestMethod("POST");
        conexion.setDoOutput(true);
        conexion.setRequestProperty("Content-Type", "application/json");

        PrintWriter out= new PrintWriter(conexion.getOutputStream());
        out.print(parametros);
        out.close();

        int statusCode= conexion.getResponseCode();
        if(statusCode == 200){
            BufferedInputStream inputStream= new BufferedInputStream(conexion.getInputStream());
            BufferedReader bufferedReader= new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line, result="";
            while((line = bufferedReader.readLine()) != null){
                result += line;
            }
            inputStream.close();

            return result;

        }

        return null;
    }
}
