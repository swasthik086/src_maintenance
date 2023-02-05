package com.suzuki.preferences;

import android.content.Context;
import android.content.SharedPreferences;


import com.suzuki.BuildConfig;

import static android.content.Context.MODE_PRIVATE;

class SharedPreferencesStore implements KeyValueStore {
    private static final String PREF_NAME = BuildConfig.APPLICATION_ID + ".SharedPrefs";
    private Context context;

    SharedPreferencesStore(Context context) {
        this.context = context;
    }

    @Override
    public boolean put(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences().edit();
        editor.putString(key, value);
        return editor.commit();
    }

    @Override
    public boolean put(String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences().edit();
        editor.putInt(key, value);
        return editor.commit();
    }

    @Override
    public String get(String key) {
        return sharedPreferences().getString(key, null);
    }

    @Override
    public int getInteger(String key) {
        return sharedPreferences().getInt(key, 0);
    }

    @Override
    public boolean put(String key, Enum value) {
        SharedPreferences.Editor editor = sharedPreferences().edit();
        editor.putString(key, value.name());
        return editor.commit();
    }

    @Override
    public Enum get(String key, Enum defaultValue) {
        String value = sharedPreferences().getString(key, defaultValue.name());
        return Enum.valueOf(defaultValue.getClass(), value);
    }

    @Override
    public boolean putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = sharedPreferences().edit();
        editor.putBoolean(key, value);
        return editor.commit();
    }

    @Override
    public boolean getBoolean(String key) {
        return sharedPreferences().getBoolean(key, false);
    }

    @Override
    public boolean remove(String key) {
        SharedPreferences.Editor editor = sharedPreferences().edit();
        editor.remove(key);
        return editor.commit();
    }

    private SharedPreferences sharedPreferences() {
        return context.getSharedPreferences(PREF_NAME, MODE_PRIVATE);
    }

    public boolean clearSharePreference() {
        SharedPreferences.Editor editor = sharedPreferences().edit();
        editor.clear();
        if (editor.commit()) {
            return true;
        }
        return false;
    }


}