package com.example.dasgram.controller;

import android.Manifest;
import android.content.ContentResolver;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import com.example.dasgram.R;

import java.util.HashMap;

/**
 * Se encarga de guardar todas las preferencias que utilizaremos a lo largo de nuestro paso por la app.
 */
public class GestorPreferencias extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener{

    private EditTextPreference editText;
    private SwitchPreference switchPreference;
    private ListPreference listPreference;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferencias);

        editText = (EditTextPreference) findPreference("nombrePref");
        switchPreference = (SwitchPreference) findPreference("switchMusic");
        listPreference = (ListPreference) findPreference("listMusic");

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());

        editText.setSummary(sharedPreferences.getString("username",""));

        listPreference.setSummary(sharedPreferences.getString("song",""));



        boolean music = sharedPreferences.getBoolean("music",false);
        switchPreference.setChecked(music);

        HashMap<String,String> listaCanciones = getListaCanciones();
        listPreference.setEntries(listaCanciones.values().toArray(new String[listaCanciones.size()]));
        listPreference.setEntryValues(listaCanciones.keySet().toArray(new String[listaCanciones.size()]));
        listPreference.setEnabled(music);
        listPreference.setSummary(music?sharedPreferences.getString("songName",""):"");



    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key){
            case "listMusic":
                listPreference.setSummary(listPreference.getEntry());
                sharedPreferences.edit().putString("song", (String) listPreference.getValue()).apply();
                sharedPreferences.edit().putString("songName", (String) listPreference.getEntry()).apply();
                break;
            case "switchMusic":

                if(!pedirPermiso()){
                    switchPreference.setChecked(false);
                    sharedPreferences.edit().putBoolean("music",switchPreference.isChecked()).apply();
                    listPreference.setEnabled(switchPreference.isChecked());
                    return;
                }

                sharedPreferences.edit().putBoolean("music",switchPreference.isChecked()).apply();
                listPreference.setEnabled(switchPreference.isChecked());
                listPreference.setSummary(switchPreference.isChecked()?
                        sharedPreferences.getString("songName",""):
                        ""
                        );
                break;
        }
    }

    /**
     * Metodo que recoge todas las canciones que hay en nuestro dispositivo, para que podamos elegir la que queramos
     * que suene en el menú principal.
     * @return
     */
    private HashMap<String,String> getListaCanciones(){

        HashMap<String,String> listaCanciones = new HashMap<>();

        ContentResolver cr = getActivity().getContentResolver();
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";
        Cursor cur = cr.query(uri, null, selection, null, sortOrder);
        int count = 0;

        if(cur != null)
        {
            count = cur.getCount();

            if(count > 0)
            {
                while(cur.moveToNext())
                {
                    String title = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.TITLE));
                    String id = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media._ID));

                    listaCanciones.put(id,title);
                }

            }

            cur.close();
        }

        return listaCanciones;
    }

    private boolean pedirPermiso(){
        if(ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);

            return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

}
