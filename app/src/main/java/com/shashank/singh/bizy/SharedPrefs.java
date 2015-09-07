package com.shashank.singh.bizy;

import android.content.Context;
import android.content.SharedPreferences;


public class SharedPrefs {

    // region #variables
    public static final String PREFS_NAME = "com.example.shashank.bizy.BizyPrefs";
    public static SharedPreferences settings;
    public static SharedPreferences.Editor editor;
    // endregion #variables

    // Constructor
    public SharedPrefs(Context ctx) {
        if(settings == null) {
            settings = ctx.getSharedPreferences(PREFS_NAME,
                    Context.MODE_PRIVATE);
        }
        editor = settings.edit();
    }
}