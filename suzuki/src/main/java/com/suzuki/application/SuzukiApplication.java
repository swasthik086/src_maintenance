package com.suzuki.application;

import static com.suzuki.utils.Common.PRICOL_CONNECTED;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.location.Location;
import android.os.Environment;

import com.clj.fastble.data.BleDevice;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import com.mappls.sdk.maps.Mappls;
import com.mappls.sdk.maps.geometry.LatLng;
import com.mappls.sdk.navigation.MapplsNavigationHelper;
import com.mappls.sdk.navigation.NavLocation;
import com.mappls.sdk.navigation.NavigationApplication;
import com.mappls.sdk.services.account.MapplsAccountManager;
import com.mappls.sdk.services.account.Region;
import com.mappls.sdk.services.api.OnResponseCallback;
import com.mappls.sdk.services.api.Place;
import com.mappls.sdk.services.api.PlaceResponse;
import com.mappls.sdk.services.api.autosuggest.model.ELocation;
import com.mappls.sdk.services.api.directions.models.DirectionsResponse;
import com.mappls.sdk.services.api.reversegeocode.MapplsReverseGeoCode;
import com.mappls.sdk.services.api.reversegeocode.MapplsReverseGeoCodeManager;
import com.suzuki.R;
import com.suzuki.activity.NavigationActivity;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;


public class SuzukiApplication extends NavigationApplication {

    private static final String NEPAL = "NEPAL";
    private static final String BHUTAN = "BHUTAN";
    private static final String SRILANKA = "SRILANKA";
    private static final String BANGLADESH = "BANGLADESH";
    private static final String MYANMAR = "MYANMAR";

    ELocation eLocation = null;
    ArrayList<LatLng> viaPoints;
    private Location currentLocation;
    private NavLocation startNavigationLocation;
    private DirectionsResponse trip;

    private BluetoothGattService bluetoothGattService;
    private BluetoothGattCharacteristic characteristic;
    private int charaProp;

    public static BleDevice bleDevice;
    public static boolean isRegionFixed = false;
    public static ArrayList<String> oldConnectedDeviceList = new ArrayList<>();
    public static String lastConnectedVehicleName="";

    public static String getLastConnectedVehicleName() {
        return lastConnectedVehicleName;
    }

    public static void setLastConnectedVehicleName(String lastConnectedVehicleName) {
        SuzukiApplication.lastConnectedVehicleName = lastConnectedVehicleName;
    }

    public void onCreate() {
        super.onCreate();

        MapplsNavigationHelper.getInstance().init(this);
        Realm.init(this);

        Realm.setDefaultConfiguration(new RealmConfiguration.Builder()
                .name("Suzuki.db")
                .schemaVersion(0)
                .deleteRealmIfMigrationNeeded()
                .build());

        MapplsAccountManager.getInstance().setRestAPIKey(getRestAPIKey());
        MapplsAccountManager.getInstance().setMapSDKKey(getMapSDKKey());
        MapplsAccountManager.getInstance().setAtlasClientId(getAtlasClientId());
        MapplsAccountManager.getInstance().setAtlasClientSecret(getAtlasClientSecret());
//        MapplsNavigationHelper.getInstance().setNavigationActivityClass(NavigationActivity.class);
//        MapplsNavigationHelper.getInstance().setJunctionViewEnabled(true);
//        MapplsNavigationHelper.getInstance().setNavigationEventEnabled(true);
        Mappls.getInstance(this);
//
//        MapmyIndiaAccountManager.getInstance().setRestAPIKey(getRestAPIKey());
//        MapmyIndiaAccountManager.getInstance().setMapSDKKey(getMapSDKKey());
//        MapmyIndiaAccountManager.getInstance().setAtlasGrantType(getAtlasGrantType());
//        MapmyIndiaAccountManager.getInstance().setAtlasClientId(getAtlasClientId());
//        MapmyIndiaAccountManager.getInstance().setAtlasClientSecret(getAtlasClientSecret());
//        MapmyIndiaAccountManager.getInstance().setAtlasGrantType("client_credentials");
//
//        MapmyIndia.getInstance(this);

        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();



        if (isExternalStorageWritable()) {

            File appDirectory = new File(Environment.getExternalStorageDirectory() + "/suzukilogss");
            File logDirectory = new File(appDirectory + "/log");
            File logFile = new File(logDirectory, "logcat" + System.currentTimeMillis() + ".txt");

            // create app folder
            if (!appDirectory.exists()) {
                appDirectory.mkdir();
            }

            // create log folder
            if (!logDirectory.exists()) {
                logDirectory.mkdir();
            }

            // clear the previous logcat and then write the new one to the file
            try {
                Process process = Runtime.getRuntime().exec("logcat -c");
                process = Runtime.getRuntime().exec("logcat -f " + logFile);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (isExternalStorageReadable()) {
            // only readable
        } else {
            // not accessible
        }
    }

    public void setRegion(double lat, double lng) {
        if (lat > 0 && lng > 0) {
            try {
                getCountryName(lat, lng);
            } catch (Exception ignored){}
        }
    }

    private void getCountryName(double lat, double lng) {
        isRegionFixed = true;

        MapplsReverseGeoCode mapplsReverseGeoCode = MapplsReverseGeoCode.builder()
                .setLocation(lat,lng)
                .build();
        MapplsReverseGeoCodeManager.newInstance(mapplsReverseGeoCode).call(new OnResponseCallback<PlaceResponse>() {
            @Override
            public void onSuccess(PlaceResponse response) {
                //Handle Response
                if (response.getResponseCode() == 200) {
                    if (response!= null) {
                        List<Place> placeList = response.getPlaces();
                        if (placeList.size() > 0) {
                            Place place = placeList.get(0);
                            MapplsAccountManager.getInstance().setRegion(getFormattedName(place.getArea()));
                        }
                    }
                }
            }

            @Override
            public void onError(int code, String message) {
                //Handle Error
            }
        });



//        MapmyIndiaReverseGeoCode.builder().setLocation(lat, lng).build().enqueueCall(
//                new Callback<PlaceResponse>() {
//                    @Override
//                    public void onResponse(@NotNull Call<PlaceResponse> call, @NotNull Response<PlaceResponse> response) {
//                        if (response.code() == 200) {
//                            if (response.body() != null) {
//                                List<Place> placeList = response.body().getPlaces();
//                                if (placeList.size() > 0) {
//                                    Place place = placeList.get(0);
//                                    MapmyIndiaAccountManager.getInstance().setRegion(getFormattedName(place.getArea()));
//                                }
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(@NotNull Call<PlaceResponse> call, @NotNull Throwable t) {
//                        isRegionFixed = false;
//                    }
//                }
       // );
    }

    private String getFormattedName(String country) {
        String name = Region.REGION_DEFAULT;
        if (country != null && country.length() > 0) {
            country = country.toUpperCase().replaceAll("\\s+", "");
            if (country.equalsIgnoreCase(NEPAL)) name = Region.REGION_NEPAL;
            else if (country.equalsIgnoreCase(BHUTAN)) name = Region.REGION_BHUTAN;
            else if (country.equalsIgnoreCase(MYANMAR)) name = Region.REGION_MYANMAR;
            else if (country.equalsIgnoreCase(SRILANKA)) name = Region.REGION_SRILANKA;
            else if (country.equalsIgnoreCase(BANGLADESH)) name = Region.REGION_BANGLADESH;
        }
        return name;
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) return true;

        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) return true;

        return false;
    }

    //first one is testing and next is production
    private String getAtlasClientId() {
     //   return getString(R.string.client_id_test);
        return getString(R.string.client_id_prod);

    }

    private String getAtlasClientSecret() {
       // return getString(R.string.client_secret_test);
        return getString(R.string.client_secret_prod);

    }

    private String getAtlasGrantType() {
        return "client_credentials";
    }

    String getMapSDKKey() {
      //  return getString(R.string.map_sdk_test);
        return getString(R.string.map_sdk_prod);
    }

    String getRestAPIKey() {
       // return getString(R.string.rest_api_test);
        return getString(R.string.rest_api_prod);
    }

    public void setELocation(ELocation eLocation) {
        this.eLocation = eLocation;
    }

    public ELocation getELocation() {
        return eLocation;
    }

    public void setViaPoints(ArrayList<LatLng> viaPoints) {
        this.viaPoints = viaPoints;
    }

    public ArrayList<LatLng> getViaPoints() {
        return this.viaPoints;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public NavLocation getStartNavigationLocation() {
        return startNavigationLocation;
    }

    public void setStartNavigationLocation(NavLocation startNavigationLocation) {
        this.startNavigationLocation = startNavigationLocation;
    }

    public DirectionsResponse getTrip() {
        return trip;
    }

    public void setTrip(DirectionsResponse trip) {
        this.trip = trip;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    /*public static byte calculateCheckSum(byte[] u1_buffer) {
        byte fl_u1_Sum = 0;
        byte u1_Ret;
        byte len = 0;
        for (len = 1; len <= 27; len++) {
            fl_u1_Sum += u1_buffer[len];

        }
//        u1_Ret = (byte) (0xFF - (fl_u1_Sum % 0x100));
        u1_Ret = (byte) (*//*0xFF - *//*(fl_u1_Sum % 0x100));
        return (u1_Ret);
    }*/

    public static byte calculateCheckSum_pricol(byte[] u1_buffer) {
        byte fl_u1_Sum = 0;
        byte u1_Ret;
        byte len = 0;
        for (len = 1; len <= 27; len++) {
            fl_u1_Sum += u1_buffer[len];
        }
        /*if(!cluster_a_connected) u1_Ret = (byte) (0xFF - (fl_u1_Sum % 0x100));
        else u1_Ret = (byte) (*//*0xFF - *//*(fl_u1_Sum % 0x100));*/
        u1_Ret = (byte) (0xFF - (fl_u1_Sum % 0x100));
        return (u1_Ret);
    }

    public static byte calculateCheckSum(byte[] u1_buffer) {
        byte fl_u1_Sum = 0;
        byte u1_Ret;
        byte len = 0;
        for (len = 1; len <= 27; len++) {
            fl_u1_Sum += u1_buffer[len];
        }

        if(PRICOL_CONNECTED) u1_Ret = (byte) (0xFF - (fl_u1_Sum % 0x100));
        else u1_Ret = (byte) ((fl_u1_Sum % 0x100));

        return (u1_Ret);
    }

    public BleDevice getBleDevice() {
        return bleDevice;
    }

    public void setBleDevice(BleDevice bleDevice) {
        SuzukiApplication.bleDevice = bleDevice;
    }

    public BluetoothGattService getBluetoothGattService() {
        return bluetoothGattService;
    }

    public void setBluetoothGattService(BluetoothGattService bluetoothGattService) {
        this.bluetoothGattService = bluetoothGattService;
    }

    public void setCharacteristic(BluetoothGattCharacteristic characteristic) {
        this.characteristic = characteristic;
    }

    public int getCharaProp() {
        return charaProp;
    }

    public void setCharaProp(int charaProp) {
        this.charaProp = charaProp;
    }

    public static ArrayList<String> getOldConnectedDeviceList() {
        return oldConnectedDeviceList;
    }

    //TEMP deactivated
    /*private String getCountryName() {
        String displayCountry, country;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            displayCountry = getResources().getConfiguration().getLocales().get(0).getDisplayCountry();
            country = getResources().getConfiguration().getLocales().get(0).getCountry();
        } else {
            displayCountry = getResources().getConfiguration().locale.getDisplayCountry();
            country = getResources().getConfiguration().locale.getCountry();
        }

        *//*TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            String countryCodeValue = tm.getNetworkCountryIso();
        }*//*


        return getFormattedName(country);
    }
    public BluetoothGattCharacteristic getCharacteristic() {
        return characteristic;
    }
    public static void setOldConnectedDeviceList(ArrayList<String> oldConnectedDeviceList) {
        SuzukiApplication.oldConnectedDeviceList = oldConnectedDeviceList;
    }
    public static boolean isDeviceMatched(String deviceName){
        if(oldConnectedDeviceList !=null && oldConnectedDeviceList.size()>0)
            return oldConnectedDeviceList.contains(deviceName);
        return false;
    }*/
}
