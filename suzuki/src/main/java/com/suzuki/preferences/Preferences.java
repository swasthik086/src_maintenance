package com.suzuki.preferences;

import android.content.Context;

public class Preferences {
    private KeyValueStore store;


    public Preferences(Context context) {
        this(new SharedPreferencesStore(context));
    }

    public Preferences(KeyValueStore store) {
        this.store = store;
    }


    public void setSyncCLock(Boolean inputType) {
        this.store.putBoolean(Keys.SYNC_CLOCK, inputType);
    }


    public Boolean getSyncCLock() {
        return this.store.getBoolean(Keys.SYNC_CLOCK);
    }


    public boolean setBleName(String blename) {
        return store.put(Keys.BleName, blename);
    }


    public String getMacAddress() {
        return store.get(Keys.MacAddr);
    }


    public boolean setMacAddress(String macaddr) {
        return store.put(Keys.MacAddr, macaddr);
    }


    public String getBleName() {
        return store.get(Keys.BleName);
    }

    public void setReadSMS(int SANA_STOPPING) {
        this.store.put(Keys.READSMS, SANA_STOPPING);
    }


    public int getReadSMS() {
        return this.store.getInteger(Keys.READSMS);
    }


    public interface Keys {

        String SYNC_CLOCK = "SYNC_CLOCK";

        String BleName = "";
        String MacAddr = "MacAddr";

        String READSMS = "SANA_STOPPING";


    }

    public boolean clearSharePreference() {
        if (store.clearSharePreference()) {
            return true;
        }

        return false;
    }

}