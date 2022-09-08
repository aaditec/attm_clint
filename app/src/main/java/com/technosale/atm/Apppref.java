package com.technosale.atm;

import android.content.Context;
import android.content.SharedPreferences;

public class Apppref {
    private static final String TAG = Apppref.class.getSimpleName();

    private SharedPreferences appPref;
    private SharedPreferences.Editor appPrefEditor;
    private Context mContext;

    // shared pref mode
    private static final int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "com.technosales.dvrvideorecording.appPref";


    public static final String GET_NAME = "get_device_name";


    public Apppref(Context context) {
        this.mContext = context;
        appPref = mContext.getSharedPreferences(PREF_NAME,
                PRIVATE_MODE);
        appPrefEditor = appPref.edit();
    }

    public void setDeviceName(String name) {
        appPrefEditor.putString(GET_NAME, name);
        appPrefEditor.apply();

    }

    public String getDeviceName() {
        return appPref.getString(GET_NAME, "");
    }

}
