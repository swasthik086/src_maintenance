package com.suzuki.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.mappls.sdk.maps.geometry.LatLng;
import com.suzuki.R;
import com.suzuki.activity.TripDetailsActivity;
import com.suzuki.adapter.RealmRecentAdapter;
//import com.suzuki.interfaces.OnButtonPressListener;
import com.suzuki.interfaces.IOnclickFromAdapterToActivityAndFragment;
import com.suzuki.pojo.FavouriteTripRealmModule;
import com.suzuki.pojo.RecentTripRealmModule;
import com.suzuki.pojo.ViaPointLocationRealmModel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.suzuki.activity.TripActivity.ivDelete;

public class RecentFragment extends Fragment implements IOnclickFromAdapterToActivityAndFragment,Serializable {

    View v;
    private RealmRecentAdapter realmRecentAdapter;
    private RecyclerView rvRecentTrip;
    Realm realm;
    LinearLayout llNoRecords;
    RealmResults<RecentTripRealmModule> recentTrip;
    RealmResults<FavouriteTripRealmModule> favTrip;
//    public static ArrayList<Integer> deleteIdList = new ArrayList<Integer>();
    public Set<Integer> deleteIdList = new HashSet<Integer>();
    public Set<Integer> favIdList = new HashSet<Integer>();

    public RecentFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        v = inflater.inflate(R.layout.recent_fragment, container, false);

        llNoRecords = v.findViewById(R.id.llNoRecords);
        rvRecentTrip = v.findViewById(R.id.recyclerViewData);
        ivDelete = getActivity().findViewById(R.id.ivDelete);
        realm = Realm.getDefaultInstance();

        readTripRecords();

        recentTrip.addChangeListener(new RealmChangeListener<RealmResults<RecentTripRealmModule>>() {
            @Override
            public void onChange(RealmResults<RecentTripRealmModule> tripRealmModules) {
                readTripRecords();
            }
        });

        ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (deleteIdList.size() > 0) {

                    Dialog dialog = new Dialog(getActivity(), R.style.custom_dialog);
                    dialog.setContentView(R.layout.custom_dialog);

                    ImageView ivCross = dialog.findViewById(R.id.ivCross);
                    ivCross.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.cancel();
                        }
                    });

                    ImageView ivCheck = dialog.findViewById(R.id.ivCheck);

                    ivCheck.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (deleteIdList.size() > 0) {
                                deleteSelectedData(realm, deleteIdList);
                                dialog.cancel();

                            } else dialog.cancel();
                        }
                    });
                    dialog.show();

                } else Toast.makeText(getActivity(), "Please select data to delete", Toast.LENGTH_SHORT).show();
            }
        });

        favTrip = realm.where(FavouriteTripRealmModule.class).findAll();

        favTrip.addChangeListener(new RealmChangeListener<RealmResults<FavouriteTripRealmModule>>() {
            @Override
            public void onChange(RealmResults<FavouriteTripRealmModule> tripRealmModules) {

                realm.executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {

                        favTrip = realm.where(FavouriteTripRealmModule.class).findAll();
                        realmRecentAdapter.notifyDataSetChanged();
                    }
                });
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        recentTrip.addChangeListener(new RealmChangeListener<RealmResults<RecentTripRealmModule>>() {
            @Override
            public void onChange(RealmResults<RecentTripRealmModule> tripRealmModules) {
                readTripRecords();
            }
        });
    }

    private List<RecentTripRealmModule> readTripRecords() {

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                recentTrip = realm.where(RecentTripRealmModule.class).sort("dateTime", Sort.DESCENDING).findAll();
            }
        });

        if (recentTrip.size() <= 0) {
            llNoRecords.setVisibility(View.VISIBLE);
            rvRecentTrip.setVisibility(View.GONE);

        } else {
            rvRecentTrip.setVisibility(View.VISIBLE);
            llNoRecords.setVisibility(View.GONE);
        }

        realmRecentAdapter = new RealmRecentAdapter(recentTrip, getContext(), this);
        rvRecentTrip.setAdapter(realmRecentAdapter);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        rvRecentTrip.setLayoutManager(new LinearLayoutManager(getContext()));
        realmRecentAdapter.notifyDataSetChanged();
        return recentTrip;

    }

    public void deleteSelectedData(Realm realm, Set<Integer> idList) {

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                RealmQuery<RecentTripRealmModule> query = realm.where(RecentTripRealmModule.class);

                boolean first = true;
                for (int id : idList) {
                    if (!first) query.or();
                    else first = false;

                    query.equalTo("id", id);
                }
                RealmResults<RecentTripRealmModule> results = query.findAll();

//                Log.d("delett", "ss--" + results.size());

                /*for (int i = 0; i < results.size(); i++) {
                    Log.d("delett", "ssff--" + results.get(i).getId());
                }*/

                if (results != null) results.deleteAllFromRealm();

                deleteIdList.clear();
                Toast.makeText(getActivity(), "Deleted successfully", Toast.LENGTH_SHORT).show();
            }
        });
        readTripRecords();
    }

    public void updateRecentData(Realm realm, int id, boolean clicked) {

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                RealmResults<FavouriteTripRealmModule> results = realm.where(FavouriteTripRealmModule.class).findAll();

                RecentTripRealmModule receFavDataResult = realm.where(RecentTripRealmModule.class).equalTo("id", id).findFirst();

                int tripSize = results.size();

                if (clicked) {

                    if (favTrip.size() < 10) {

                        if (receFavDataResult != null) {

                            receFavDataResult.setFavorite(true);
                            realm.insertOrUpdate(receFavDataResult);
                        }
                    } else {
                        if (receFavDataResult != null) {

                            receFavDataResult.setFavorite(false);
                            realm.insertOrUpdate(receFavDataResult);
                        }
                    }
                } else {
                    if (receFavDataResult != null) {

                        receFavDataResult.setFavorite(false);
                        realm.insertOrUpdate(receFavDataResult);
                    }
                }
            }
        });

        readTripRecords();
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
                                favouriteTripRealmModule.setTrip_name(tripName);
                                favouriteTripRealmModule.setRideTime(rideTime);
                                favouriteTripRealmModule.setTotalDistance(totalDistance);
                                favouriteTripRealmModule.setTopSpeed(Integer.parseInt(topspeed));
                                favouriteTripRealmModule.setRideTimeLt10(Integer.parseInt(timelt10));
                                realm.insert(favouriteTripRealmModule);
                                RealmResults<FavouriteTripRealmModule> resultsafte = realm.where(FavouriteTripRealmModule.class).findAll();

                                for (int i = 0; i < resultsafte.size(); i++) {
                                    Log.d("hjjghj", "ss-dd-" + resultsafte.get(i).getEndlocation() + resultsafte.get(i).getId());

                                }

                            }
                            else if (favtripUpdateModel != null) {

                                Log.d("daiaia", "-ssff" + date + dateTime + endLoc);

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
                                favtripUpdateModel.setTrip_name(tripName);
                                favtripUpdateModel.setRideTime(rideTime);
                                favtripUpdateModel.setTotalDistance(totalDistance);
                                favouriteTripRealmModule.setTopSpeed(Integer.parseInt(topspeed));
                                favouriteTripRealmModule.setRideTimeLt10(Integer.parseInt(timelt10));
                                realm.insertOrUpdate(favtripUpdateModel);

                                RealmResults<FavouriteTripRealmModule> resultsafte = realm.where(FavouriteTripRealmModule.class).findAll();

                                for (int i = 0; i < resultsafte.size(); i++) {
                                    Log.d("hjjghj", "ss--ssdd" + resultsafte.get(i).getEndlocation() + resultsafte.get(i).getId());

                                }

                            }
                        } else {
                            Toast.makeText(getActivity(), "Favourite list is full, Please delete some of your favourites.", Toast.LENGTH_SHORT).show();
                        }

                    } else {
//                        FavouriteTripRealmModule receFavDataResult = realm.where(FavouriteTripRealmModule.class).equalTo("id", id).findFirst();

                        Log.d("jdjdjdj", "s=" + favtripUpdateModel.getId() + id);

                        if (favtripUpdateModel != null) {
//
                            Log.d("jdjdjdj", "s=" + favtripUpdateModel.getId() + id);
                            FavouriteTripRealmModule favtripUpdateModel1 = realm.where(FavouriteTripRealmModule.class).equalTo("id", id).findFirst();

                            favtripUpdateModel1.deleteFromRealm();
                        }
//                        if (favouriteTripRealmModule != null) {
//                            favouriteTripRealmModule.deleteFromRealm();
//
//                            RealmResults<FavouriteTripRealmModule> resultsafte = realm.where(FavouriteTripRealmModule.class).findAll();
//
//                            Log.d("hjjghj","--"+resultsafte.size());
//                        }
                    }

                    RealmResults<FavouriteTripRealmModule> result = realm.where(FavouriteTripRealmModule.class).equalTo("id", 0).findAll();
                    result.deleteAllFromRealm();
                }
            });

        } catch (Exception e) {
            Log.d("realmex", "--" + e.getMessage());
        }
    }

    @Override
    public void adapterItemIsClicked(int clickedPositon, String actionToPerform) { }

    @Override
    public void adapterItemIsClicked(double lat, double lng, String placeAddress, String placeName) { }

    @Override
    public void onPause() {
        super.onPause();
//        readTripRecords(realm);
    }

    @Override
    public void adapterItemIsClicked(int clickedPositon, String actionToPerform, boolean clicked, String date, Date dateTime, String time, String startLoc, String endLoc, String cuurent_lat, String current_long, String destiny_lat,
                                     String destiny_long, String tripName, String rideTime, String totalDistance, String topspeed, String timelt10, RealmList<ViaPointLocationRealmModel> viaPointRealmList) {

        if (actionToPerform.contentEquals("delete")) {

            if (deleteIdList.contains(clickedPositon)) deleteIdList.remove(clickedPositon);
            else if (!deleteIdList.contains(clickedPositon)) deleteIdList.add(clickedPositon);

        } else if (actionToPerform.contentEquals("fav")) {

            Log.d("RecentFragment", "adapterItemIsClicked() called with: clickedPositon = [" + clickedPositon + "], actionToPerform = [" + actionToPerform + "], clicked = [" + clicked + "], date = [" + date + "], dateTime = [" + dateTime + "], time = [" + time + "], startLoc = [" + startLoc + "], endLoc = [" + endLoc + "], cuurent_lat = [" + cuurent_lat + "], current_long = [" + current_long + "], destiny_lat = [" + destiny_lat + "], destiny_long = [" + destiny_long + "], tripName = [" + tripName + "], rideTime = [" + rideTime + "], totalDistance = [" + totalDistance + "], topspeed = [" + topspeed + "], timelt10 = [" + timelt10 + "], viaPointRealmList = [" + viaPointRealmList + "]");

            addFavouriteTripDataToRealm(date, dateTime, clickedPositon, time, startLoc, endLoc, clicked, cuurent_lat, current_long, destiny_lat, destiny_long, tripName, rideTime, totalDistance, topspeed, timelt10, viaPointRealmList);

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

            Log.d("RecentFragment", "adapterItemIsClicked: "+viaPointList);
            Intent in = new Intent(getActivity(), TripDetailsActivity.class);
            Log.d("gghhtreee", "" + tripName);
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
       //  in.putExtra("viaPointRealmList", String.valueOf(viaPointRealmList));

            startActivity(in);
        }
    }
}