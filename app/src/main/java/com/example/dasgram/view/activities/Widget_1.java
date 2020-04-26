package com.example.dasgram.view.activities;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import androidx.lifecycle.LifecycleOwner;
import androidx.preference.PreferenceManager;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.example.dasgram.R;
import com.example.dasgram.controller.Tarea;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Clase utilizada para la gestión del Widget de la aplicación
 * Mostrará el último 'like', la última foto subida y la canción que se reproducirá en el menú
 * @author Iker Nafarrate Bilbao
 */
public class Widget_1 extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_1);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String song = sharedPreferences.getString("songName","Default");
        String likeA = sharedPreferences.getString("likeA","");
        String likeDe = sharedPreferences.getString("likeDe","");
        String subirDe = sharedPreferences.getString("subirDe","");

        //Construimos el mensaje de like
        String msgLike;
        if(likeA.isEmpty() || likeDe.isEmpty()){
            msgLike = "";
        }else{
            msgLike = context.getString(R.string.msgWidgetLike,likeA,likeDe);
        }

        //Construimos el mensaje de foto subida
        String msgSubir;
        if(subirDe.isEmpty()){
            msgSubir = "";
        }else{
            msgSubir = context.getString(R.string.msgSubir,subirDe);
        }

        //Asignamos cada mensaje a su TextView
        views.setTextViewText(R.id.txt_1, context.getString(R.string.cancion) + ": " + song);
        views.setTextViewText(R.id.txt_2,msgLike);
        views.setTextViewText(R.id.txt_3,msgSubir);


        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

}

