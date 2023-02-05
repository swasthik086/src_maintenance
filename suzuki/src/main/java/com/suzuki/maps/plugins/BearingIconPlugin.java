package com.suzuki.maps.plugins;

import static com.mappls.sdk.maps.style.expressions.Expression.get;
import static com.mappls.sdk.maps.style.expressions.Expression.literal;
import static com.mappls.sdk.maps.style.expressions.Expression.match;
import static com.mappls.sdk.maps.style.expressions.Expression.stop;
import static com.mappls.sdk.maps.style.layers.PropertyFactory.visibility;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.mappls.sdk.geojson.Feature;
import com.mappls.sdk.geojson.FeatureCollection;
import com.mappls.sdk.geojson.Point;
import com.mappls.sdk.maps.MapView;
import com.mappls.sdk.maps.MapplsMap;
import com.mappls.sdk.maps.Style;
import com.mappls.sdk.maps.geometry.LatLng;
import com.mappls.sdk.maps.style.layers.Layer;
import com.mappls.sdk.maps.style.layers.Property;
import com.mappls.sdk.maps.style.layers.PropertyFactory;
import com.mappls.sdk.maps.style.layers.SymbolLayer;
import com.mappls.sdk.maps.style.sources.GeoJsonSource;
import com.mappls.sdk.maps.style.sources.Source;
import com.suzuki.R;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;


public final class BearingIconPlugin implements MapView.OnDidFinishLoadingStyleListener {


    private static final String FILTER_TEXT = "direction_type";
    private static final String POSITION_TEXT = "position_text";
    private Handler handler = new Handler();
    private boolean bearingVisibility = false;
    private float bearing = 0f;
    private LatLng position;
    private MapplsMap mapplsMap;
    private MapView mMapView;
    private List<String> layerIds;
    private Runnable updatePolylineStateRunnable = this::updateSelectedPolylineStates;
    private boolean enabled = false;


    public BearingIconPlugin(@NonNull MapView mapView, @NonNull MapplsMap mapplsMap) {
        this.mapplsMap = mapplsMap;
        this.mMapView = mapView;
        updateState();
        mapView.addOnDidFinishLoadingStyleListener(this);

    }

    static Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
            // width and height are equal for all assets since they are ovals.
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }
    }


    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (enabled != this.enabled) {
            this.enabled = enabled;
            updateState();
        }

    }


    public void toggle() {
        enabled = !enabled;
        updateState();
    }

    @Override
    public void onDidFinishLoadingStyle() {
        updateState();
        if (isEnabled())
            updatePolylineStatus();
    }

    private void updateState() {
        mapplsMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                Source source = style.getSource(DirectionPolylineData.SOURCE_ID);
                if (source == null) {
                    initialise(style);
                    return;
                }
                setVisibility(enabled, style);
            }
        });

    }


    private void initialise(Style style) {
        layerIds = new ArrayList<>();
        addDirectionPolylineSource(style);
        addDirectionsLayer(style);
    }


    private void addDirectionPolylineSource(Style style) {
        GeoJsonSource trafficSource = new GeoJsonSource(DirectionPolylineData.SOURCE_ID);
        style.addSource(trafficSource);
    }


    private void addDirectionsLayer(Style style) {
        try {
            addBearingLayer(ContextCompat.getDrawable(mMapView.getContext(), R.drawable.user_puck_icon_demo), style);
        } catch (Exception exception) {
            Timber.e("Unable to attach Traffic Layers to current style.");
        }
    }


    private String getLastAddedLayerId() {
        return layerIds.get(layerIds.size() - 1);
    }


    private void setVisibility(boolean visible, Style style) {
        if (layerIds == null)
            return;
        List<Layer> layers = style.getLayers();
        if (layers != null && layers.size() > 0)
            for (Layer layer : layers) {
                if (layerIds.contains(layer.getId())) {
                    layer.setProperties(visibility(visible ? Property.VISIBLE : Property.NONE));
                }
            }

        setBearingLayerVisibility(bearingVisibility);
    }

    private synchronized void updateSelectedPolylineStates() {
        mapplsMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                setVisibility(true, style);

                List<Feature> features = new ArrayList<>();

                if (position != null) {
                    Feature bearingFeature = Feature.fromGeometry(
                            Point.fromLngLat(position.getLongitude(), position.getLatitude()));

                    bearingFeature.addStringProperty("icon", DirectionsSymbolLayer.ICON_BEARING_IMAGE);
                    bearingFeature.addStringProperty(FILTER_TEXT, "bearing");

                    features.add(bearingFeature);
                }


                Layer locationLayer = style.getLayer(DirectionsSymbolLayer.BASE_BEARING_LAYER_ID);
                if (locationLayer != null) {
                    locationLayer.setProperties(
                            PropertyFactory.iconRotate(bearing)
                    );
                }

                FeatureCollection featureCollection = FeatureCollection.fromFeatures(features);
                GeoJsonSource source = (GeoJsonSource) style.getSource(DirectionPolylineData.SOURCE_ID);


                if (source != null) {
                    source.setGeoJson(featureCollection);
                }

            }
        });

    }

    private void updatePolylineStatus() {
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(updatePolylineStateRunnable, 100);
    }

    public void removeAllData() {

        if (mapplsMap != null && mapplsMap.getStyle() != null && mapplsMap.getStyle().isFullyLoaded()) {
            GeoJsonSource source = (GeoJsonSource) mapplsMap.getStyle().getSource(DirectionPolylineData.SOURCE_ID);


            if (source != null) {
                source.setGeoJson(FeatureCollection.fromFeatures(new ArrayList<Feature>()));
            }
        }
        bearingVisibility = false;
        bearing = 0;
    }

    public void setBearingLayerVisibility(boolean visible) {

        if(mapplsMap.getStyle() != null && mapplsMap.getStyle().isFullyLoaded()) {
            Layer layer = mapplsMap.getStyle().getLayer(DirectionsSymbolLayer.BASE_BEARING_LAYER_ID);
            if (layer != null) {
                layer.setProperties(PropertyFactory.visibility(visible ? Property.VISIBLE : Property.NONE));
            }
        }

        this.bearingVisibility = visible;
    }

    public void setBearingIcon(float bearing, LatLng position) {
        this.bearing = bearing;
        this.position = position;
        updatePolylineStatus();
    }

    private void addBearingLayer(Drawable bearingDrawable, Style style) {
        Layer foregroundLayer = getLayer(
                DirectionsSymbolLayer.BASE_BEARING_LAYER_ID,
                DirectionsSymbolLayer.ICON_BEARING_IMAGE,
                bearingDrawable,
                style
        );
        addLocationLayerToMap(foregroundLayer, null, style);
    }

    private Layer getLayer(String layerId, String image, Drawable drawable, Style style) {
        Bitmap bitmap = getBitmapFromDrawable(drawable);
        style.addImage(image, bitmap);
        return new SymbolLayer(
                layerId, DirectionPolylineData.SOURCE_LAYER).withProperties(
                PropertyFactory.iconImage(image),
                PropertyFactory.iconAllowOverlap(true),
                PropertyFactory.iconIgnorePlacement(true),
                PropertyFactory.iconRotationAlignment(Property.ICON_ROTATION_ALIGNMENT_MAP)).withFilter(match(get(FILTER_TEXT), literal(false),
                stop("bearing", true)));
    }

    private void addLocationLayerToMap(Layer layer, @Nullable String idBelowLayer, Style style) {
        if (idBelowLayer == null) {
            style.addLayer(layer);
        } else {
            style.addLayerAbove(layer, idBelowLayer);
        }
        layerIds.add(layer.getId());
    }


    private static class DirectionPolylineData {
        private static final String SOURCE_ID = "directions_bearing";
        private static final String SOURCE_LAYER = "directions_bearing";

    }


    private static class DirectionsSymbolLayer {


        private static final String BASE_BEARING_LAYER_ID = "directions-marker-bearing-layer";

        private static final String ICON_BEARING_IMAGE = "directions-marker-bearing-image";
    }


}
