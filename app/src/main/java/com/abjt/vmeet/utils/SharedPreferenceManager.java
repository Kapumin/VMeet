package com.abjt.vmeet.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceManager {
    private SharedPreferences sharedPreferences;

    public SharedPreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(Constants.KEY_SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
    }

    public void putBoolean(String key, Boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public Boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key) {
        return sharedPreferences.getString(key, null);
    }

    public void clearSharedPreference() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
