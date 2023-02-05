package com.suzuki.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mappls.sdk.maps.geometry.LatLng;
import com.suzuki.R;
import com.suzuki.activity.TripDetailsActivity;
import com.suzuki.adapter.RealmFavouriteAdapter;
import com.suzuki.interfaces.IOnclickFromAdapterToActivityAndFragment;
import com.suzuki.pojo.FavouriteTripRealmModule;
import com.suzuki.pojo.RecentTripRealmModule;
import com.suzuki.pojo.ViaPointLocationRealmModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.suzuki.utils.Common.EXCEPTION;

//import com.suzuki.interfaces.OnButtonPressListener;

public class FavouriteTripFragment extends Fragment implements IOnclickFromAdapterToActivityAndFragment {

    View v;
    private RealmFavouriteAdapter realmRecentAdapter;
    private RecyclerView rvRecentTrip;
    Realm realm;
    LinearLayout llNoRecords;
    RealmResults<FavouriteTripRealmModule> recentTrip;
//    public Set<Integer> deleteIdList = new HashSet<Integer>();
//    public Set<Integer> favIdList = new HashSet<Integer>();
    private String ClassName;

    public FavouriteTripFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        v = inflater.inflate(R.layout.recent_fragment, container, false);
        llNoRecords = v.findViewById(R.id.llNoRecords);
        rvRecentTrip = v.findViewById(R.id.recyclerViewData);
        realm = Realm.getDefaultInstance();

        readTripRecords(realm);

        recentTrip.addChangeListener(new RealmChangeListener<RealmResults<FavouriteTripRealmModule>>() {
            @Override
            public void onChange(RealmResults<FavouriteTripRealmModule> tripRealmModules) {

                readTripRecords(realm);
            }
        });

        ClassName = getClass().getName();
        return v;
    }

    private List<FavouriteTripRealmModule> readTripRecords(Realm realm) {

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                recentTrip = realm.where(FavouriteTripRealmModule.class).equalTo("favorite", true)
                        .sort("dateTime", Sort.DESCENDING)
                        .findAll();
            }
        });

        if (recentTrip.size() <= 0) {
            llNoRecords.setVisibility(View.VISIBLE);
            rvRecentTrip.setVisibility(View.GONE);

        } else {
            rvRecentTrip.setVisibility(View.VISIBLE);
            llNoRecords.setVisibility(View.GONE);
        }

        realmRecentAdapter = new RealmFavouriteAdapter(recentTrip, getContext(), this);
        rvRecentTrip.setAdapter(realmRecentAdapter);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rvRecentTrip.setLayoutManager(new LinearLayoutManager(getContext()));
        realmRecentAdapter.notifyDataSetChanged();

        return recentTrip;
    }


    @Override
    public void onResume() {
        super.onResume();
        readTripRecords(realm);
        recentTrip.addChangeListener(tripRealmModules -> readTripRecords(realm));
    }

    @Override
    public void onPause() {
        super.onPause();
        readTripRecords(realm);
        recentTrip.addChangeListener(tripRealmModules -> readTripRecords(realm));
    }

    /*public void addFavTripData(Realm realm, int id, boolean clicked) {

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                RecentTripRealmModule receFavDataResult = realm.where(RecentTripRealmModule.class).equalTo("id", id).findFirst();

                if (clicked) {

                    if (receFavDataResult != null) {

                        receFavDataResult.setFavorite(true);
                        realm.insertOrUpdate(receFavDataResult);
                    }
                } else if (receFavDataResult != null) {

                        receFavDataResult.setFavorite(false);
                        realm.insertOrUpdate(receFavDataResult);
                }
            }
        });

        readTripRecords(realm);
    }*/

    @Override
    public void adapterItemIsClicked(int clickedPositon, String actionToPerform) { }

    @Override
    public void adapterItemIsClicked(double lat, double lng, String placeAddress, String placeName) { }


    @Override
    public void adapterItemIsClicked(int clickedPositon, String actionToPerform, boolean clicked, String date, Date dateTime, String time, String startLoc, String endLoc, String cuurent_lat, String current_long, String destiny_lat, String destiny_long, String tripName, String rideTime, String totalDistance, String topspeed, String timelt10, RealmList<ViaPointLocationRealmModel> viaPointRealmList) {

        if (actionToPerform.contentEquals("fav")) {


            addFavouriteTripDataToRealm(date, dateTime, clickedPositon, time, startLoc, endLoc, clicked, cuurent_lat, current_long, destiny_lat, destiny_long, tripName, rideTime, totalDistance, topspeed, timelt10,viaPointRealmList);

            updateRecentData(realm, clickedPositon, clicked);

        }
        else if (actionToPerform.contentEquals("details")) {

            ArrayList<LatLng> viaPointList = new ArrayList<>();
            for (ViaPointLocationRealmModel model : viaPointRealmList){
                LatLng latLng = new LatLng();
                latLng.setLatitude(model.getLatitude());
                latLng.setLongitude(model.getLongitude());
                viaPointList.add(latLng);
            }

            Intent in = new Intent(getActivity(), TripDetailsActivity.class);
            in.putExtra("clickedPositon", clickedPositon);
            in.putExtra("date", date);
            in.putExtra("dateTime", dateTime.toString());
            in.putExtra("time", time);
            in.putExtra("startLoc", startLoc);
            in.putExtra("endLoc", endLoc);
            in.putExtra("cuurent_lat", cuurent_lat);
            in.putExtra("current_long", current_long);
            in.putExtra("destiny_lat", destiny_lat);
            in.putExtra("destiny_long", destiny_long);
            in.putExtra("clicked", clicked);
            in.putExtra("tripName", tripName);
            in.putExtra("rideTime", rideTime);
            in.putExtra("totalDistance", totalDistance);
            in.putExtra("topspeed", topspeed);
            in.putExtra("timelt10", timelt10);
            in.putExtra("viaPointList",viaPointList);
            startActivity(in);
        }
    }

    public void updateRecentData(Realm realm, int id, boolean clicked) {

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                RealmResults<FavouriteTripRealmModule> results = realm.where(FavouriteTripRealmModule.class).findAll();

                RecentTripRealmModule receFavDataResult = realm.where(RecentTripRealmModule.class).equalTo("id", id).findFirst();

                int tripSize = results.size();

                if (clicked) {

                    if (tripSize < 11) {

                        if (receFavDataResult != null) {

                            receFavDataResult.setFavorite(true);
                            realm.insertOrUpdate(receFavDataResult);
                        }
                    } else if (receFavDataResult != null) {

                            receFavDataResult.setFavorite(false);
                            realm.insertOrUpdate(receFavDataResult);

                    }
                }
                else if (receFavDataResult != null) {

                        receFavDataResult.setFavorite(false);
                        realm.insertOrUpdate(receFavDataResult);
                }
            }
        });

        readTripRecords(realm);

    }

    private void addFavouriteTripDataToRealm(String date, Date dateTime, int id, String time, String startLoc, String endLoc, boolean clicked, String current_lat, String current_long, String destiny_lat, String destiny_long, String tripName, String rideTime, String totalDistance, String topspeed, String timelt10, RealmList<ViaPointLocationRealmModel> viaPointRealmList) {
        Realm realm = Realm.getDefaultInstance();
        try {

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {

                    RealmResults<FavouriteTripRealmModule> results = realm.where(FavouriteTripRealmModule.class).findAll();
                    FavouriteTripRealmModule favouriteTripRealmModule = realm.createObject(FavouriteTripRealmModule.class);
                    FavouriteTripRealmModule favtripUpdateModel = realm.where(FavouriteTripRealmModule.class).equalTo("id", id).findFirst();

                    int tripSize = results.size();

                    if (clicked) {

                        if (tripSize < 11) {

                            if (favtripUpdateModel == null) {
                                for (ViaPointLocationRealmModel model : viaPointRealmList){
                                    favouriteTripRealmModule.setPointLocationRealmModels(model);
                                }

                                favouriteTripRealmModule.setDate(date);
                                favouriteTripRealmModule.setId(id);
                                favouriteTripRealmModule.setTime(time);
                                favouriteTripRealmModule.setStartlocation(startLoc);
                                favouriteTripRealmModule.setEndlocation(endLoc);
                                favouriteTripRealmModule.setFavorite(clicked);
                                favouriteTripRealmModule.setDateTime(dateTime);
                                favouriteTripRealmModule.setCurrent_lat(current_lat);
                                favouriteTripRealmModule.setCurrent_long(current_long);
                                favouriteTripRealmModule.setDestination_lat(destiny_lat);
                                favouriteTripRealmModule.setDestination_long(destiny_long);
                                favouriteTripRealmModule.setDestination_long(tripName);
                                favouriteTripRealmModule.setRideTime(rideTime);
                                favouriteTripRealmModule.setTotalDistance(totalDistance);
                                favouriteTripRealmModule.setTopSpeed(Integer.parseInt(topspeed));
                                favouriteTripRealmModule.setRideTimeLt10(Integer.parseInt(timelt10));
                                realm.insert(favouriteTripRealmModule);

                                RealmResults<FavouriteTripRealmModule> resultsafte = realm.where(FavouriteTripRealmModule.class).findAll();

                            } else if (favtripUpdateModel != null) {

                                for (ViaPointLocationRealmModel model : viaPointRealmList){
                                    favtripUpdateModel.setPointLocationRealmModels(model);
                                }

                                favtripUpdateModel.setDate(date);
                                favtripUpdateModel.setId(id);
                                favtripUpdateModel.setTime(time);
                                favtripUpdateModel.setStartlocation(startLoc);
                                favtripUpdateModel.setEndlocation(endLoc);
                                favtripUpdateModel.setFavorite(clicked);
                                favtripUpdateModel.setDateTime(dateTime);
                                favtripUpdateModel.setCurrent_lat(current_lat);
                                favtripUpdateModel.setCurrent_long(current_long);
                                favtripUpdateModel.setDestination_lat(destiny_lat);
                                favtripUpdateModel.setDestination_long(destiny_long);
                                favtripUpdateModel.setDestination_long(tripName);
                                favtripUpdateModel.setRideTime(rideTime);
                                favtripUpdateModel.setTotalDistance(totalDistance);
                                favouriteTripRealmModule.setTopSpeed(Integer.parseInt(topspeed));
                                favouriteTripRealmModule.setRideTimeLt10(Integer.parseInt(timelt10));
                                realm.insertOrUpdate(favtripUpdateModel);
                                RealmResults<FavouriteTripRealmModule> resultsafte = realm.where(FavouriteTripRealmModule.class).findAll();

                            }
                        }
                        else Toast.makeText(getActivity(), "Favourite list is full, Please delete some of your favourites.", Toast.LENGTH_SHORT).show();

                    }
                    else if (favtripUpdateModel != null) {

                        FavouriteTripRealmModule favtripUpdateModel1 = realm.where(FavouriteTripRealmModule.class).equalTo("id", id).findFirst();
                        favtripUpdateModel1.deleteFromRealm();

                    }

                    RealmResults<FavouriteTripRealmModule> result = realm.where(FavouriteTripRealmModule.class).equalTo("id", 0).findAll();
                    result.deleteAllFromRealm();
                }
            });
        } catch (Exception e) {
            Log.e(EXCEPTION, ClassName+" addFavouriteTripDataToRealm "+e);
        }
    }
}














