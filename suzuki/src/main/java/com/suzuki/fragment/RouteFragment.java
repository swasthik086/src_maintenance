package com.suzuki.fragment;

import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.gson.Gson;



import com.mappls.sdk.geojson.LineString;
import com.mappls.sdk.geojson.Point;
import com.mappls.sdk.maps.MapplsMap;
import com.mappls.sdk.maps.OnMapReadyCallback;
import com.mappls.sdk.maps.camera.CameraUpdateFactory;
import com.mappls.sdk.maps.geometry.LatLng;
import com.mappls.sdk.maps.geometry.LatLngBounds;
import com.mappls.sdk.navigation.MapplsNavigationHelper;
import com.mappls.sdk.navigation.NavLocation;
import com.mappls.sdk.navigation.NavigationFormatter;
import com.mappls.sdk.navigation.data.WayPoint;
import com.mappls.sdk.navigation.iface.IStopSession;
import com.mappls.sdk.navigation.model.NavigationResponse;
import com.mappls.sdk.navigation.util.ErrorType;
import com.mappls.sdk.services.api.OnResponseCallback;
import com.mappls.sdk.services.api.autosuggest.model.ELocation;

import com.mappls.sdk.services.api.directions.DirectionsCriteria;
import com.mappls.sdk.services.api.directions.MapplsDirectionManager;
import com.mappls.sdk.services.api.directions.MapplsDirections;
import com.mappls.sdk.services.api.directions.models.DirectionsResponse;
import com.mappls.sdk.services.api.directions.models.DirectionsRoute;
import com.mappls.sdk.services.api.directions.models.DirectionsWaypoint;
import com.mappls.sdk.services.api.directions.models.LegStep;
import com.mappls.sdk.services.utils.Constants;
import com.suzuki.R;

import com.suzuki.activity.NavigationActivity;
import com.suzuki.application.SuzukiApplication;
import com.suzuki.maps.plugins.DirectionPolylinePlugin;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;


import static com.mappls.sdk.maps.Mappls.getApplicationContext;
import static com.suzuki.fragment.DashboardFragment.prev_cluster_name;
import static com.suzuki.utils.Common.BikeBleName;

public class RouteFragment extends Fragment implements View.OnClickListener, OnMapReadyCallback {
    private static final String KEY_STATE_MODEL = "state_model";
    private static final String ARG_E_LOCATION = "e_location";
    private static final String ARG_E_FROM_LOCATION = "from_location";
    private TextView textViewTime;
    private TextView textViewDistance;
    private TextView textFrom;
    private TextView textTo;
    private MapplsMap mapboxMap;
    private DirectionPolylinePlugin directionPolylinePlugin;
    private StateModel mStateModel;
    private SuzukiApplication app;
    private LinearLayout moveToNavigationFragment;
    private String fromLocation;
    private ImageView backImageView;


    MapMainFragment mapMainFragment;

    public RouteFragment() {

    }

    public static RouteFragment newInstance(ELocation eLocation, String fromString) {

        RouteFragment fragment = new RouteFragment();
        Bundle args = new Bundle();
        args.putString(ARG_E_LOCATION, new Gson().toJson(eLocation));
        args.putString(ARG_E_FROM_LOCATION, fromString);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = getMyApplication();


        if (savedInstanceState == null) {

            app.setTrip(null);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_route, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView(view);

        if (getActivity() != null && MapMainFragment.getMapView() != null)
            MapMainFragment.getMapView().getMapAsync(this);


        if (savedInstanceState != null) {
            mStateModel = savedInstanceState.getParcelable(KEY_STATE_MODEL);
        } else {
            mStateModel = new StateModel();
            if (getArguments() != null) {
                if (getArguments().containsKey(ARG_E_LOCATION))
                    mStateModel.eLocation = new Gson().fromJson(getArguments().getString(ARG_E_LOCATION), ELocation.class);
                fromLocation = getArguments().getString(ARG_E_FROM_LOCATION);
            }
        }
        try {
            if (mStateModel != null && mStateModel.trip != null) {
                app.setTrip(mStateModel.trip);
                update();
                addPolyLine(new LatLng(app.getCurrentLocation().getLatitude(),
                                app.getCurrentLocation().getLongitude()),
                        new LatLng(Double.parseDouble(String.valueOf(mStateModel.eLocation.latitude)),
                                Double.parseDouble(String.valueOf(mStateModel.eLocation.longitude))),
                        mStateModel.trip.routes().get(0));
            } else {
                try {
                    if (app.getCurrentLocation() != null) {
                        ArrayList<LatLng> geoPoints = new ArrayList<>();
                        geoPoints.add(new LatLng(app.getCurrentLocation().getLatitude(), app.getCurrentLocation().getLongitude()));
                        geoPoints.add(getPoint(mStateModel.eLocation));
                        getRoute(geoPoints, mStateModel.eLocation);
                    } else {
                        Toast.makeText(getActivity(), R.string.current_location_not_available, Toast.LENGTH_SHORT).show();
                        onFragmentBackPressed();
                    }
                } catch (Exception e) {
                    Timber.e(e);
                    Toast.makeText(getActivity(), R.string.something_went_wrong, Toast.LENGTH_SHORT).show();
                    onFragmentBackPressed();
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }


        moveToNavigationFragment.setOnClickListener(v -> startNavigation());

        textFrom.setText(fromLocation);
        textTo.setText(mStateModel.eLocation.placeName);

        backImageView.setOnClickListener(v -> getActivity().onBackPressed());
    }


    private void initView(View view) {
        textViewTime = view.findViewById(R.id.text_view_time);
        textViewDistance = view.findViewById(R.id.text_view_distance_header);
        textFrom = view.findViewById(R.id.text_view_from);
        textTo = view.findViewById(R.id.text_view_to);
        moveToNavigationFragment = view.findViewById(R.id.linear_layout_navigation);
        backImageView = view.findViewById(R.id.image_view_back);

    }

    private void update() {
        if (getActivity() == null)
            return;
        try {
            textViewDistance.setText(String.format("%s", NavigationFormatter.getFormattedDistance(mStateModel.trip.routes().get(0).distance().floatValue(), getMyApplication())));
            textViewTime.setText(String.format("%s ", NavigationFormatter.getFormattedDuration(mStateModel.trip.routes().get(0).duration().intValue(), getMyApplication())));
        } catch (Exception e) {
            Timber.e(e);
        }
    }


    public void startNavigation() {
        if (getActivity() == null)
            return;
        NavLocation location = MapMainFragment.getUserLocation();
        if (location == null)
            return;

        LongOperation operation = new LongOperation();
        operation.execute();
    }


    public SuzukiApplication getMyApplication() {
        return ((SuzukiApplication) getActivity().getApplication());
    }


    @Override
    public void onDestroyView() {
        try {
            if (getActivity() != null)
                mapMainFragment = new MapMainFragment();

            mapMainFragment.clearPOIs();
        } catch (Exception e) {
            //ignore
        }
        super.onDestroyView();
    }


    private void showProgress() {
        try {
            if (getActivity() == null)
                return;
//            ((MainHomeMapFragment) getActivity()).showProgress();
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    private void hideProgress() {
        try {
            if (getActivity() == null)
                return;
//            ((MainHomeMapFragment) getActivity()).hideProgress();
        } catch (Exception e) {
            Timber.e(e);
        }
    }


    public void addPolyLine(LatLng start, LatLng stop, final DirectionsRoute directionsResponse) {
        if (getActivity() == null || mapboxMap == null || directionsResponse == null || directionsResponse.geometry() == null)
            return;

        ArrayList<LatLng> latLngs = new ArrayList<>();
        if (directionPolylinePlugin != null) {
            directionPolylinePlugin.setEnabled(true);
            directionPolylinePlugin.onDidFinishLoadingStyle();
            ArrayList<LineString> lineStrings = new ArrayList<>();
            LineString lineString = LineString.fromPolyline(directionsResponse.geometry(), Constants.PRECISION_6);
            lineStrings.add(lineString);

            directionPolylinePlugin.setTrips(lineStrings, start, stop, null,null);
        }
        if (directionsResponse.legs() != null &&
                directionsResponse.legs().get(0) != null &&
                directionsResponse.legs().get(0).steps() != null &&
                directionsResponse.legs().get(0).steps().size() > 0) {
            for (LegStep legStep : directionsResponse.legs().get(0).steps()) {
                latLngs.add(new LatLng(legStep.maneuver().location().latitude(), legStep.maneuver().location().longitude()));
            }
        }
        if (latLngs.size() == 1) {
            LatLng _point = new LatLng(latLngs.get(0).getLatitude(), latLngs.get(0).getLongitude());
            mapboxMap.animateCamera(CameraUpdateFactory.newLatLng(_point));


        } else {
            try {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.includes(latLngs);
                LatLngBounds bounds = builder.build();
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0),
                        300);
            } catch (Exception e) {
                e.printStackTrace();
                LatLng _point = new LatLng(latLngs.get(0).getLatitude(), latLngs.get(0).getLongitude());
                mapboxMap.animateCamera(CameraUpdateFactory.newLatLng(_point));
            }
        }
    }

    private void getRoute(final ArrayList<LatLng> wayPoints, final ELocation eLocation) {
        if (getActivity() == null) {
            return;
        }
        if (directionPolylinePlugin != null)
            directionPolylinePlugin.removeAllData();
        if (wayPoints == null || wayPoints.size() < 2)
            return;
        Point origin = Point.fromLngLat(wayPoints.get(0).getLongitude(), wayPoints.get(0).getLatitude());
        Point destination = Point.fromLngLat(wayPoints.get(wayPoints.size() - 1).getLongitude(), wayPoints.get(wayPoints.size() - 1).getLatitude());


//        MapmyIndiaDirections.Builder builder = MapmyIndiaDirections.builder()
//                .origin(origin)
//                .destination(destination)
//                .annotations(DirectionsCriteria.ANNOTATION_CONGESTION)
//                .resource(DirectionsCriteria.RESOURCE_ROUTE_ETA)
//                .profile(DirectionsCriteria.PROFILE_BIKING)
//                .steps(true)
//                .alternatives(false)
//                .overview(DirectionsCriteria.OVERVIEW_FULL);
        MapplsDirections directions = MapplsDirections.builder()
                .origin(origin)
                .steps(true)
                .resource(DirectionsCriteria.RESOURCE_ROUTE_ETA)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .destination(destination)
                .build();

        showProgress();


        MapplsDirectionManager.newInstance(directions).call(new OnResponseCallback<DirectionsResponse>() {
            @Override
            public void onSuccess(DirectionsResponse directionsResponse) {
// You can get generic HTTP info about the response

                try {
                    if (directionsResponse.code().equals(200)) {
                        if (directionsResponse == null) {
                            showErrorMessage(R.string.something_went_wrong);
                            onFragmentBackPressed();
                            return;
                        }
//                        DirectionsResponse directionsResponses = response.body();
//                        mStateModel.trip = directionsResponse;
//                        app.setTrip(mStateModel.trip);

                        List<DirectionsWaypoint> waypointsList = directionsResponse.waypoints();

                        update();
//                        mapboxMap.addPolyline(new PolylineOptions().addAll(listOfLatlang).color(Color.parseColor("#3bb2d0")).width(4));

                        addPolyLine(new LatLng(app.getCurrentLocation().getLatitude(),
                                        app.getCurrentLocation().getLongitude()),
                                getPoint(mStateModel.eLocation), mStateModel.trip.routes().get(0));
                    } else {
                        showErrorMessage(R.string.something_went_wrong);
                        onFragmentBackPressed();
                    }
                } catch (Exception e) {
                    Timber.e(e);
                    showErrorMessage(R.string.something_went_wrong);
                    onFragmentBackPressed();
                }hideProgress();
            }

            @Override
            public void onError(int i, String s) {
                if (getActivity() == null)
                    return;
//                if (!s.isCanceled()) {
//                    throwable.printStackTrace();
//                    showErrorMessage(R.string.something_went_wrong);
//                }
                hideProgress();
                onFragmentBackPressed();
            }
        });
//        MapmyIndiaDirections mapmyIndiaDirections = builder.build();
//        mapmyIndiaDirections.enqueueCall(new Callback<DirectionsResponse>() {
//            @Override
//            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
//                // You can get generic HTTP info about the response
//                Timber.d("Response code: %d", response.code());
//                Timber.d("Response url: %s", call.request().url().toString());
//                try {
//                    if (response.code() == 200) {
//                        if (response.body() == null) {
//                            showErrorMessage(R.string.something_went_wrong);
//                            onFragmentBackPressed();
//                            return;
//                        }
//                        DirectionsResponse directionsResponse = response.body();
//                        mStateModel.trip = directionsResponse;
//                        app.setTrip(mStateModel.trip);
//
//                        List<DirectionsWaypoint> waypointsList = directionsResponse.waypoints();
//
//                        update();
////                        mapboxMap.addPolyline(new PolylineOptions().addAll(listOfLatlang).color(Color.parseColor("#3bb2d0")).width(4));
//
//                        addPolyLine(new LatLng(app.getCurrentLocation().getLatitude(),
//                                        app.getCurrentLocation().getLongitude()),
//                                getPoint(mStateModel.eLocation), mStateModel.trip.routes().get(0));
//                    } else {
//                        showErrorMessage(R.string.something_went_wrong);
//                        onFragmentBackPressed();
//                    }
//                } catch (Exception e) {
//                    Timber.e(e);
//                    showErrorMessage(R.string.something_went_wrong);
//                    onFragmentBackPressed();
//                }
//                hideProgress();
//            }
//
//            @Override
//            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
//                if (getActivity() == null)
//                    return;
//                Timber.d("onFailure url: %s", call.request().url().toString());
//                if (!call.isCanceled()) {
//                    throwable.printStackTrace();
//                    showErrorMessage(R.string.something_went_wrong);
//                }
//                hideProgress();
//                onFragmentBackPressed();
//            }
//        });
    }


    private void showErrorMessage(int resId) {
        try {
            if (getActivity() != null)
                Toast.makeText(getActivity(), resId, Toast.LENGTH_SHORT).show();
        } catch (Resources.NotFoundException e) {
            Timber.e(e);
        }
    }

    @Override
    public void onMapReady(MapplsMap mapboxMap) {
        if (getActivity() == null)
            return;
        try {
            this.mapboxMap = mapboxMap;
            directionPolylinePlugin = MapMainFragment.getDirectionPolylinePlugin();
            if (mStateModel != null && mStateModel.trip != null) {
                addPolyLine(new LatLng(app.getCurrentLocation().getLatitude(),
                                app.getCurrentLocation().getLongitude()),
                        getPoint(mStateModel.eLocation), mStateModel.trip.routes().get(0));
            }
        } catch (Exception e) {
            Timber.e(e);
        }
    }



    @Override
    public void onMapError(int i, String s) {

    }

    public void onFragmentBackPressed() {
        if (getActivity() != null)
            getActivity().onBackPressed();
    }

//    @Override
//    public void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
////        if (mStateModel != null)
////            outState.putString(KEY_STATE_MODEL, new Gson().toJson(mStateModel));
//    }

    public LatLng getPoint(ELocation eLocation) {
        try {
            return new LatLng(Double.parseDouble(String.valueOf(eLocation.latitude)), Double.parseDouble(String.valueOf(eLocation.longitude)));
        } catch (Exception e) {
            return new LatLng(0, 0);
        }
    }

    public WayPoint getNavigationGeoPoint(ELocation eLocation) {
        try {
            if (eLocation.entryLatitude > 0 && eLocation.entryLongitude > 0)
                return new WayPoint(eLocation.entryLatitude, eLocation.entryLongitude, eLocation.placeName);
            else
                return new WayPoint(Double.parseDouble(String.valueOf(eLocation.latitude)), Double.parseDouble(String.valueOf(eLocation.longitude)), eLocation.placeName);
        } catch (Exception e) {
            return new WayPoint(0, 0, null);
        }
    }

    @Override
    public void onClick(View v) {

    }

    private static class StateModel {
        private ELocation eLocation;
        private DirectionsResponse trip;

    }


    private class LongOperation extends AsyncTask<Void, Void, NavigationResponse> {


        @Override
        protected NavigationResponse doInBackground(Void... params) {

            try {
                Log.d("dddd", "---" + params);

                LatLng currentLocation = null;
                NavLocation location = MapMainFragment.getUserLocation();
                if (location != null)
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                NavLocation navLocation = new NavLocation("navigation");
                Point position = mStateModel.trip.routes().get(0).legs().get(0).steps().get(0).maneuver().location();
                LatLng point = new LatLng(position.latitude(), position.longitude());
                navLocation.setLongitude(point.getLongitude());
                navLocation.setLatitude(point.getLatitude());
                app = getMyApplication();
                app.setStartNavigationLocation(navLocation);
                if (currentLocation == null)
                    return new NavigationResponse(ErrorType.UNKNOWN_ERROR, null);


                Log.d("kkkjhhj", "--" + app.getTrip());


                return MapplsNavigationHelper.getInstance().startNavigation(app.getTrip(), 0, currentLocation,
                        getNavigationGeoPoint(mStateModel.eLocation), null, prev_cluster_name);
                //TODo device id will be passed here


            } catch (Exception e) {
                Timber.e(e);
                Log.d("jkkkkhkhkh", "--" + e.getMessage());
                return new NavigationResponse(ErrorType.UNKNOWN_ERROR, e);
            }

        }

        @Override
        protected void onPostExecute(NavigationResponse result) {

            if (this == null)
                return;
//            dismissProgress();
            hideProgress();

            if (result != null && result.getError() != null) {
                if(result.getError().errorCode == 409) {
                    MapplsNavigationHelper.getInstance().deleteSession(BikeBleName.getValue(), new IStopSession() {
                        @Override
                        public void onSuccess() {
                            LongOperation operation = new LongOperation();
                            operation.execute();
                        }

                        @Override
                        public void onFailure() {

                        }
                    });
                    /*show dialog to user*/
                    /*new AlertDialog.Builder(getContext())
                            .setMessage(getContext().getResources().getString(R.string.Session_Message))
                            .setTitle("Navigation Alert")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    MapmyIndiaNavigationHelper.getInstance().deleteSession(MACID, new IStopSession() {
                                        @Override
                                        public void onSuccess() {
                                            Toast.makeText(getContext(), "Start Navigation Now", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onFailure() {

                                        }
                                    });
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                    dialog.dismiss();
                                }
                            })
                            .create()
                            .show();*/
                }

                return;
            }


//            this.startActivity(new Intent(this, NavigationActivity.class));
            Intent ittt = new Intent(getApplicationContext(), NavigationActivity.class);
            startActivity(ittt);


//            getApplication().startActivity(new Intent(getApplication(), NavigationActivity.class));

        }

        @Override
        protected void onPreExecute() {

            showProgress();
//            showProgress(getApplicationContext());


        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }


    /*private class LongOperation extends AsyncTask<Void, Void, Boolean> {


        @Override
        protected Boolean doInBackground(Void... params) {

            try {

                LatLng currentLocation = null;
                NavLocation location = MapMainFragment.getUserLocation();
                if (location != null)
                    currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                NavLocation navLocation = new NavLocation("navigation");
                Point position = app.getTrip().legs().get(0).steps().get(0).maneuver().location();
                LatLng point = new LatLng(position.latitude(), position.longitude());
                navLocation.setLongitude(point.getLongitude());
                navLocation.setLatitude(point.getLatitude());
                app.setStartNavigationLocation(navLocation);
                if (currentLocation == null)
                    return false;


                return MapmyIndiaNavigationHelper.getInstance().startNavigation(app, app.getTrip(), currentLocation,
                        getNavigationGeoPoint(mStateModel.eLocation),null);


            } catch (Exception e) {
                Timber.e(e);
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean result) {

            if (getActivity() == null)
                return;
            hideProgress();
            if (result != null && !result) {

                //TODO show message to the user , we are not able to start navigation
                return;
            }


            getActivity().startActivity(new Intent(getActivity(), NavigationActivity.class));
        }

        @Override
        protected void onPreExecute() {

            showProgress();

        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }*/


}



