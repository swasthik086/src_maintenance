package com.suzuki.preferences;

public interface KeyValueStore {
    boolean put(String key, String value);

    boolean put(String key, int value);

    String get(String key);



    int getInteger(String key);

    boolean putBoolean(String key, boolean value);

    boolean getBoolean(String key);

    boolean remove(String key);

    boolean put(String key, Enum value);

    Enum get(String key, Enum defaultValue);

    boolean clearSharePreference();


}