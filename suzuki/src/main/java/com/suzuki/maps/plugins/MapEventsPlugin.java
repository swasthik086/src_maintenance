package com.suzuki.maps.plugins;

import static com.mappls.sdk.maps.style.layers.PropertyFactory.iconImage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.mappls.sdk.geojson.Feature;
import com.mappls.sdk.geojson.FeatureCollection;
import com.mappls.sdk.geojson.Point;
import com.mappls.sdk.maps.MapView;
import com.mappls.sdk.maps.MapplsMap;
import com.mappls.sdk.maps.Style;
import com.mappls.sdk.maps.style.expressions.Expression;
import com.mappls.sdk.maps.style.layers.SymbolLayer;
import com.mappls.sdk.maps.style.sources.GeoJsonSource;
import com.mappls.sdk.services.api.event.route.ReportCriteria;
import com.mappls.sdk.services.api.event.route.model.ReportDetails;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class MapEventsPlugin implements MapView.OnDidFinishLoadingStyleListener {

    private static final String SOURCE_ID = "mappls_events_source_id";
    private static final String LAYER_ID = "mappls_events_layer_id";
    private static final String IMAGE_NAME = "mappls_events_image_name";
    private MapplsMap mapplsMap;
    private MapView mMapView;
    private Handler handler = new Handler(Looper.getMainLooper());
    private FeatureCollection featureCollection;
    private Map<String, String> imagesMap;
    private Runnable updateMapEvents = new Runnable() {
        @Override
        public void run() {
            updateState();
            addImagesOnMap(imagesMap);
        }
    };

    public MapEventsPlugin(MapView mapView, MapplsMap mapplsMap) {
        this.mapplsMap = mapplsMap;
        this.mMapView = mapView;
        mapView.addOnDidFinishLoadingStyleListener(this);
        updateState();
    }

    private void updateState() {
        mapplsMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                GeoJsonSource source = (GeoJsonSource) style.getSource(SOURCE_ID);
                if (source == null) {
                    initialize("directions-marker-layer", style);
                    return;
                }
                if (featureCollection != null) {
                    source.setGeoJson(featureCollection);
                }
            }
        });
    }

    private void initialize(String aboveLayerId, Style style) {
        addSource(style);
        addLayer(aboveLayerId, style);
    }

    private void addLayer(String aboveLayerId, Style style) {
        SymbolLayer symbolLayer = new SymbolLayer(LAYER_ID, SOURCE_ID)
                .withProperties(iconImage(Expression.get(IMAGE_NAME)));
        style.addLayerAbove(symbolLayer, aboveLayerId);
    }

    private void addSource(Style style) {
        GeoJsonSource geoJsonSource;
        if (featureCollection != null) {
            geoJsonSource = new GeoJsonSource(SOURCE_ID, featureCollection);
        } else {
            geoJsonSource = new GeoJsonSource(SOURCE_ID);
        }
        style.addSource(geoJsonSource);
    }

    public void setNavigationEvents(List<ReportDetails> navigationEvents) {
        imagesMap = new HashMap();
        List<Feature> featureList = new ArrayList<>();
        for (ReportDetails reportDetails : navigationEvents) {
            Feature feature = Feature.fromGeometry(Point.fromLngLat(reportDetails.getLongitude(), reportDetails.getLatitude()));
            if (reportDetails.getChildCategory() != null && reportDetails.getParentCategory() != null) {
                feature.addStringProperty(IMAGE_NAME, reportDetails.getParentCategory() + "_" + reportDetails.getChildCategory());
                imagesMap.put(reportDetails.getParentCategory() + "_" + reportDetails.getChildCategory(), reportDetails.getReportIcon(ReportCriteria.ICON_24_PX));
            } else {
                feature.addStringProperty(IMAGE_NAME, reportDetails.getId());
                imagesMap.put(reportDetails.getId(), reportDetails.getReportIcon(ReportCriteria.ICON_24_PX));
            }
            featureList.add(feature);

        }
        featureCollection = FeatureCollection.fromFeatures(featureList);
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(updateMapEvents, 100);
    }

    private void addImagesOnMap(Map<String, String> imagesMap) {
        mapplsMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

                mMapView.post(new Runnable() {
                    @Override
                    public void run() {

                        for (String key : imagesMap.keySet()) {
                            if (style.getImage(key) == null) {
                                getBitmap(imagesMap.get(key), bitmap -> {
                                    style.addImageAsync(key, bitmap);
                                });
                            }
                        }
                    }
                });
            }
        });
    }

    private void getBitmap(String urlString, OnImageAdded onImageAdded) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                URL url = null;
                try {
                    url = new URL(urlString);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                Bitmap image = null;
                try {
                    image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                onImageAdded.getBitmap(image);
            }
        });
    }

    @Override
    public void onDidFinishLoadingStyle() {
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(updateMapEvents, 100);
    }

    private interface OnImageAdded {
        void getBitmap(Bitmap bitmap);
    }
}
