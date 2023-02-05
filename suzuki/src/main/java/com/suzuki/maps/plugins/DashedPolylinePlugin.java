package com.suzuki.maps.plugins;

import static com.mappls.sdk.maps.style.layers.PropertyFactory.lineCap;
import static com.mappls.sdk.maps.style.layers.PropertyFactory.lineDasharray;
import static com.mappls.sdk.maps.style.layers.PropertyFactory.lineJoin;
import static com.mappls.sdk.maps.style.layers.PropertyFactory.lineWidth;

import android.graphics.Color;

import androidx.annotation.NonNull;

import com.mappls.sdk.geojson.Feature;
import com.mappls.sdk.geojson.FeatureCollection;
import com.mappls.sdk.geojson.LineString;
import com.mappls.sdk.geojson.Point;
import com.mappls.sdk.maps.MapView;
import com.mappls.sdk.maps.MapplsMap;
import com.mappls.sdk.maps.Style;
import com.mappls.sdk.maps.geometry.LatLng;
import com.mappls.sdk.maps.style.layers.LineLayer;
import com.mappls.sdk.maps.style.layers.Property;
import com.mappls.sdk.maps.style.sources.GeoJsonOptions;
import com.mappls.sdk.maps.style.sources.GeoJsonSource;
import com.mappls.sdk.services.api.directions.DirectionsCriteria;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Saksham on 18/9/19.
 */
public class DashedPolylinePlugin implements MapView.OnDidFinishLoadingStyleListener {
    private static final String UPPER_SOURCE_ID = "line-source-upper-id";

    private MapplsMap mapboxMap;

    private FeatureCollection featureCollection;
    private Style mStyle;
    private static final String LAYER_ID = "line-layer-upper-id";
    private List<LatLng> latLngs;

    private float widthDash = 4f;
    private float gapDash = 6f;

    private GeoJsonSource polylineSource;

    public DashedPolylinePlugin(MapplsMap mapboxMap, MapView mapView) {
        this.mapboxMap = mapboxMap;

        //updateSource();
        mapView.addOnDidFinishLoadingStyleListener(this);
    }


    /**
     * Add list of positions on Feature
     *
     * @param latLngs list of points
     */
//    public void createPolyline(List<LatLng> latLngs) {
//        this.latLngs = latLngs;
//        List<Point> points = new ArrayList<>();
//        for (LatLng latLng : latLngs) {
//            points.add(Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude()));
//        }
//        featureCollection = FeatureCollection.fromFeature(Feature.fromGeometry(LineString.fromLngLats(points)));
//
//        initSources(featureCollection);
//    }

    /**
     * Add various sources to the map.
     */
//    private void initSources(@NonNull FeatureCollection featureCollection) {
//        if (mapboxMap.getSource(UPPER_SOURCE_ID) == null) {
//            mapboxMap.addSource(polylineSource = new GeoJsonSource(UPPER_SOURCE_ID, featureCollection,
//                    new GeoJsonOptions().withLineMetrics(true).withBuffer(2)));
//        } else {
//            updateSource();
//        }
//    }
//
//    /**
//     * Update Source of the Polyline
//     */
//    private void updateSource() {
//        GeoJsonSource source = (GeoJsonSource) mapboxMap.getSource(UPPER_SOURCE_ID);
//        if (source == null) {
//            create();
//            return;
//        }
//        if (featureCollection != null) {
//            polylineSource.setGeoJson(featureCollection);
//        }
//    }
//
//    /**
//     * Add Layer on map
//     */
//    private void create() {
//        if (style.getLayer(LAYER_ID) == null) {
//            style.addLayer(lineLayer = new LineLayer(LAYER_ID, UPPER_SOURCE_ID).withProperties(
//                    lineCap(Property.LINE_CAP_ROUND),
//                    lineJoin(Property.LINE_JOIN_ROUND),
//                    lineWidth(5f)));
//
//
//            if (directionsCriteria.equalsIgnoreCase(DirectionsCriteria.PROFILE_WALKING)) {
//                lineLayer.setProperties(lineDasharray(new Float[]{gapDash, widthDash}));
//            }
//        }
//
//
//        if (mapboxMap.getLayer(LAYER_ID) == null) {
//            mapboxMap.addLayer(new LineLayer(LAYER_ID, UPPER_SOURCE_ID).withProperties(
//                    lineColor(Color.BLUE),
//                    lineDasharray(new Float[]{widthDash, gapDash}),
//                    lineCap(Property.LINE_CAP_ROUND),
//                    lineJoin(Property.LINE_JOIN_BEVEL),
//                    lineWidth(4f)));
//        }
//    }

//
//    @Override
//    public void onMapChanged(int i) {
//        if (i == MapView.DID_FINISH_LOADING_STYLE) {
//            updateSource();
//            createPolyline(latLngs);
//        }
//    }

    @Override
    public void onDidFinishLoadingStyle() {
//        updateSource();
//        createPolyline(latLngs);
    }
}