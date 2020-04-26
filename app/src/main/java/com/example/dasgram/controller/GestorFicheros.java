package com.example.dasgram.controller;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Esta clase se encarga de guardar y eliminar los ficheros que se muestran en la aplicación
 * @author Iker Nafarrate Bilbao
 */
public class GestorFicheros {

    private static GestorFicheros mGestorFicheros;
    private ArrayList<File> ficheros;
    
    private GestorFicheros(){
        ficheros = new ArrayList<>();
    }
    
    public static GestorFicheros getGestorFicheros(){
        if(mGestorFicheros == null)
            mGestorFicheros = new GestorFicheros();
        return mGestorFicheros;
    }

    public Uri guardarArchivo(File file, Bitmap bitmap){

        boolean exist = file.exists();

        try {
            OutputStream os = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();

            if(!exist)
                ficheros.add(file);
            return Uri.fromFile(file);
        } catch (IOException e) {
            return null;
        }

    }

    public void borrarFicheros(){
        Iterator<File> itr = ficheros.iterator();

        while (itr.hasNext()) {
            File f = itr.next();
            f.delete();
            itr.remove();
        }

        ficheros.clear();
    }
    
}
