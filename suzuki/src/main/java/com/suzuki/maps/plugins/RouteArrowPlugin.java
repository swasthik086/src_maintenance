package com.suzuki.maps.plugins;

import static com.mappls.sdk.maps.style.expressions.Expression.color;
import static com.mappls.sdk.maps.style.expressions.Expression.get;
import static com.mappls.sdk.maps.style.expressions.Expression.interpolate;
import static com.mappls.sdk.maps.style.expressions.Expression.linear;
import static com.mappls.sdk.maps.style.expressions.Expression.step;
import static com.mappls.sdk.maps.style.expressions.Expression.stop;
import static com.mappls.sdk.maps.style.expressions.Expression.zoom;
import static com.mappls.sdk.maps.style.layers.Property.ICON_ROTATION_ALIGNMENT_MAP;
import static com.mappls.sdk.maps.style.layers.Property.NONE;
import static com.mappls.sdk.maps.style.layers.Property.VISIBLE;
import static com.mappls.sdk.maps.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mappls.sdk.maps.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mappls.sdk.maps.style.layers.PropertyFactory.visibility;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.mappls.sdk.geojson.Feature;
import com.mappls.sdk.geojson.FeatureCollection;
import com.mappls.sdk.geojson.LineString;
import com.mappls.sdk.geojson.Point;
import com.mappls.sdk.geojson.utils.PolylineUtils;
import com.mappls.sdk.maps.MapView;
import com.mappls.sdk.maps.MapplsMap;
import com.mappls.sdk.maps.Style;
import com.mappls.sdk.maps.style.layers.Layer;
import com.mappls.sdk.maps.style.layers.LineLayer;
import com.mappls.sdk.maps.style.layers.Property;
import com.mappls.sdk.maps.style.layers.PropertyFactory;
import com.mappls.sdk.maps.style.layers.SymbolLayer;
import com.mappls.sdk.maps.style.sources.GeoJsonOptions;
import com.mappls.sdk.maps.style.sources.GeoJsonSource;
import com.mappls.sdk.maps.utils.BitmapUtils;
import com.mappls.sdk.maps.utils.MathUtils;
import com.mappls.sdk.services.api.directions.models.LegStep;
import com.mappls.sdk.services.utils.Constants;
import com.mappls.sdk.turf.TurfConstants;
import com.mappls.sdk.turf.TurfMeasurement;
import com.mappls.sdk.turf.TurfMisc;
import com.suzuki.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Saksham on 23-08-2021
 */

public class RouteArrowPlugin implements MapView.OnDidFinishLoadingStyleListener {

    static final String ARROW_BEARING = "mappls-navigation-arrow-bearing";
    static final String ARROW_SHAFT_SOURCE_ID = "mappls-navigation-arrow-shaft-source";
    static final String ARROW_HEAD_SOURCE_ID = "mappls-navigation-arrow-head-source";
    static final String ARROW_SHAFT_CASING_LINE_LAYER_ID = "mappls-navigation-arrow-shaft-casing-layer";
    static final String ARROW_SHAFT_LINE_LAYER_ID = "mappls-navigation-arrow-shaft-layer";
    static final String ARROW_HEAD_ICON = "mappls-navigation-arrow-head-icon";
    static final String ARROW_HEAD_ICON_CASING = "mappls-navigation-arrow-head-icon-casing";
    static final int MAX_DEGREES = 360;
    static final String ARROW_HEAD_CASING_LAYER_ID = "mappls-navigation-arrow-head-casing-layer";
    static final Float[] ARROW_HEAD_CASING_OFFSET = {0f, -7f};
    static final String ARROW_HEAD_LAYER_ID = "mappls-navigation-arrow-head-layer";
    static final Float[] ARROW_HEAD_OFFSET = {0f, -7f};
    private MapView mapView;
    private MapplsMap mapplsMap;
    private GeoJsonSource arrowShaftGeoJsonSource;
    private GeoJsonSource arrowHeadGeoJsonSource;
    private List<Point> maneuverPoints;

    public RouteArrowPlugin(MapView mapView, MapplsMap mapplsMap) {
        this.mapView = mapView;
        this.mapplsMap = mapplsMap;
        mapView.addOnDidFinishLoadingStyleListener(this);
        mapplsMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

                initialize("directions-marker-layer", style);
            }
        });
    }


    private void initialize(String aboveLayer, Style style) {
        initializeArrowShaft(style);
        initializeArrowHead(style);

        addArrowHeadIcon(style);
        addArrowHeadIconCasing(style);

        LineLayer shaftLayer = createArrowShaftLayer(style);
        LineLayer shaftCasingLayer = createArrowShaftCasingLayer(style);
        SymbolLayer headLayer = createArrowHeadLayer(style);
        SymbolLayer headCasingLayer = createArrowHeadCasingLayer(style);

        style.addLayerAbove(shaftCasingLayer, aboveLayer);
        style.addLayerAbove(headCasingLayer, shaftCasingLayer.getId());

        style.addLayerAbove(shaftLayer, headCasingLayer.getId());
        style.addLayerAbove(headLayer, shaftLayer.getId());

    }

    public void addUpcomingManeuverArrow(LegStep currentLegStep,LegStep nextLegStep) {
        if(true)
            return;
        if (nextLegStep != null && currentLegStep != null) {
            List<Point> upcomingPoints = PolylineUtils.decode(nextLegStep.geometry(), Constants.PRECISION_6);
            List<Point> currentPoints = PolylineUtils.decode(currentLegStep.geometry(), Constants.PRECISION_6);
            boolean invalidUpcomingStepPoints = upcomingPoints == null
                    || upcomingPoints.size() < 2;
            boolean invalidCurrentStepPoints = currentPoints.size() < 2;
            if (invalidUpcomingStepPoints || invalidCurrentStepPoints) {
                updateVisibilityTo(false);
                return;
            }
            updateVisibilityTo(true);

            maneuverPoints = obtainArrowPointsFrom(currentPoints, upcomingPoints);
            updateArrowShaftWith(maneuverPoints);
            updateArrowHeadWith(maneuverPoints);
        }
    }


    void updateVisibilityTo(boolean visible) {

        mapplsMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                List<String> arrowLayerIds = new ArrayList<>();
                arrowLayerIds.add(ARROW_SHAFT_CASING_LINE_LAYER_ID);
                arrowLayerIds.add(ARROW_SHAFT_LINE_LAYER_ID);
                arrowLayerIds.add(ARROW_HEAD_CASING_LAYER_ID);
                arrowLayerIds.add(ARROW_HEAD_LAYER_ID);

                for (String layerId : arrowLayerIds) {
                    Layer layer = style.getLayer(layerId);
                    if (layer != null) {
                        String targetVisibility = visible ? VISIBLE : NONE;
                        if (!targetVisibility.equals(layer.getVisibility().getValue())) {
                            layer.setProperties(visibility(targetVisibility));
                        }

                    }
                }
            }
        });

    }

    private List<Point> obtainArrowPointsFrom(List<Point> reversedCurrent, List<Point> nextLegStepPoints) {
//    List<Point> reversedCurrent = PolylineUtils.decode(routeInformation.currentLegStep.geometry(), Constants.PRECISION_6);
        Collections.reverse(reversedCurrent);

        LineString arrowLineCurrent = LineString.fromLngLats(reversedCurrent);
        LineString arrowLineUpcoming = LineString.fromLngLats(nextLegStepPoints);

        LineString arrowCurrentSliced = TurfMisc.lineSliceAlong(arrowLineCurrent, 0, 30, TurfConstants.UNIT_METERS);
        LineString arrowUpcomingSliced = TurfMisc.lineSliceAlong(arrowLineUpcoming, 0, 30, TurfConstants.UNIT_METERS);

        Collections.reverse(arrowCurrentSliced.coordinates());

        List<Point> combined = new ArrayList<>();
        combined.addAll(arrowCurrentSliced.coordinates());
        combined.addAll(arrowUpcomingSliced.coordinates());
        return combined;
    }

    private void updateArrowShaftWith(List<Point> points) {
        if (points == null && mapplsMap.getStyle() != null && mapplsMap.getStyle().isFullyLoaded()) {
            return;
        }
        LineString shaft = LineString.fromLngLats(points);
        Feature arrowShaftGeoJsonFeature = Feature.fromGeometry(shaft);
        arrowShaftGeoJsonSource.setGeoJson(arrowShaftGeoJsonFeature);
    }

    private void updateArrowHeadWith(List<Point> points) {
        if (points == null && mapplsMap.getStyle() != null && mapplsMap.getStyle().isFullyLoaded()) {
            return;
        }
        double azimuth = TurfMeasurement.bearing(points.get(points.size() - 2), points.get(points.size() - 1));
        Feature arrowHeadGeoJsonFeature = Feature.fromGeometry(points.get(points.size() - 1));
        arrowHeadGeoJsonFeature.addNumberProperty(ARROW_BEARING, (float) MathUtils.wrap(azimuth, 0, MAX_DEGREES));
        arrowHeadGeoJsonSource.setGeoJson(arrowHeadGeoJsonFeature);
    }


    private void initializeArrowShaft(Style style) {
        if (style.getSource(ARROW_SHAFT_SOURCE_ID) == null) {
            arrowShaftGeoJsonSource = new GeoJsonSource(
                    ARROW_SHAFT_SOURCE_ID,
                    FeatureCollection.fromFeatures(new Feature[]{}),
                    new GeoJsonOptions().withMaxZoom(16)
            );
            style.addSource(arrowShaftGeoJsonSource);
        }

        if (maneuverPoints != null) {
            updateArrowShaftWith(maneuverPoints);
        }
    }

    private void initializeArrowHead(Style style) {
        if (style.getSource(ARROW_HEAD_SOURCE_ID) == null) {
            arrowHeadGeoJsonSource = new GeoJsonSource(
                    ARROW_HEAD_SOURCE_ID,
                    FeatureCollection.fromFeatures(new Feature[]{}),
                    new GeoJsonOptions().withMaxZoom(16)
            );
            style.addSource(arrowHeadGeoJsonSource);
        }

        if (maneuverPoints != null) {
            updateArrowHeadWith(maneuverPoints);
        }
    }

    private void addArrowHeadIcon(Style style) {
        Bitmap icon = BitmapUtils.getBitmapFromDrawable(ContextCompat.getDrawable(mapView.getContext(), R.drawable.ic_arrow_head));
        style.addImage(ARROW_HEAD_ICON, icon);
    }

    private void addArrowHeadIconCasing(Style style) {
        Bitmap icon = BitmapUtils.getBitmapFromDrawable(ContextCompat.getDrawable(mapView.getContext(), R.drawable.ic_arrow_head_casing));
        style.addImage(ARROW_HEAD_ICON_CASING, icon);
    }

    private LineLayer createArrowShaftLayer(Style style) {
        LineLayer shaftLayer = (LineLayer) style.getLayerAs(ARROW_SHAFT_LINE_LAYER_ID);
        if (shaftLayer != null) {
            style.removeLayer(shaftLayer);
        }
        return new LineLayer(ARROW_SHAFT_LINE_LAYER_ID, ARROW_SHAFT_SOURCE_ID).withProperties(
                PropertyFactory.lineColor(color(ContextCompat.getColor(mapView.getContext(), R.color.color_white))),
                PropertyFactory.lineWidth(
                        interpolate(linear(), zoom(),
                                stop(10, 2.6f),
                                stop(22, 13.0f)
                        )
                ),
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.visibility(NONE),
                PropertyFactory.lineOpacity(
                        step(zoom(), 0.0,
                                stop(
                                        14, 1.0
                                )
                        )
                )
        );
    }


    private LineLayer createArrowShaftCasingLayer(Style style) {
        LineLayer shaftCasingLayer = (LineLayer) style.getLayer(ARROW_SHAFT_CASING_LINE_LAYER_ID);
        if (shaftCasingLayer != null) {
            style.removeLayer(shaftCasingLayer);
        }
        return new LineLayer(ARROW_SHAFT_CASING_LINE_LAYER_ID, ARROW_SHAFT_SOURCE_ID).withProperties(
                PropertyFactory.lineColor(color(ContextCompat.getColor(mapView.getContext(), R.color.colorGray700))),
                PropertyFactory.lineWidth(
                        interpolate(linear(), zoom(),
                                stop(10, 3.4f),
                                stop(22, 17.0)
                        )
                ),
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.visibility(NONE),
                PropertyFactory.lineOpacity(
                        step(zoom(), 0.0,
                                stop(
                                        14, 1.0
                                )
                        )
                )
        );
    }


    private SymbolLayer createArrowHeadLayer(Style style) {
        SymbolLayer headLayer = (SymbolLayer) style.getLayer(ARROW_HEAD_LAYER_ID);
        if (headLayer != null) {
            style.removeLayer(headLayer);
        }
        return new SymbolLayer(ARROW_HEAD_LAYER_ID, ARROW_HEAD_SOURCE_ID)
                .withProperties(
                        PropertyFactory.iconImage(ARROW_HEAD_ICON),
                        iconAllowOverlap(true),
                        iconIgnorePlacement(true),
                        PropertyFactory.iconSize(interpolate(linear(), zoom(),
                                stop(10, 0.2),
                                stop(22, 0.8)
                                )
                        ),
                        PropertyFactory.iconOffset(ARROW_HEAD_OFFSET),
                        PropertyFactory.iconRotationAlignment(ICON_ROTATION_ALIGNMENT_MAP),
                        PropertyFactory.iconRotate(get(ARROW_BEARING)),
                        PropertyFactory.visibility(NONE),
                        PropertyFactory.iconOpacity(
                                step(zoom(), 0.0,
                                        stop(
                                                14, 1.0
                                        )
                                )
                        )
                );
    }

    private SymbolLayer createArrowHeadCasingLayer(Style style) {
        SymbolLayer headCasingLayer = (SymbolLayer) style.getLayer(ARROW_HEAD_CASING_LAYER_ID);
        if (headCasingLayer != null) {
            style.removeLayer(headCasingLayer);
        }
        return new SymbolLayer(ARROW_HEAD_CASING_LAYER_ID, ARROW_HEAD_SOURCE_ID).withProperties(
                PropertyFactory.iconImage(ARROW_HEAD_ICON_CASING),
                iconAllowOverlap(true),
                iconIgnorePlacement(true),
                PropertyFactory.iconSize(interpolate(
                        linear(), zoom(),
                        stop(10, 0.2),
                        stop(22, 0.8)
                )),
                PropertyFactory.iconOffset(ARROW_HEAD_CASING_OFFSET),
                PropertyFactory.iconRotationAlignment(ICON_ROTATION_ALIGNMENT_MAP),
                PropertyFactory.iconRotate(get(ARROW_BEARING)),
                PropertyFactory.visibility(NONE),
                PropertyFactory.iconOpacity(
                        step(zoom(), 0.0,
                                stop(
                                        14, 1.0
                                )
                        )
                )
        );
    }

    void redraw(String aboveLayer) {
        mapplsMap.getStyle(new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                if(style.getSource(ARROW_HEAD_SOURCE_ID) == null) {
                    initialize(aboveLayer, style);
                    return;
                }

                if(style.getSource(ARROW_SHAFT_SOURCE_ID) == null) {
                    initialize(aboveLayer, style);
                    return;
                }

                arrowHeadGeoJsonSource = (GeoJsonSource) style.getSource(ARROW_HEAD_SOURCE_ID);
                arrowShaftGeoJsonSource = (GeoJsonSource) style.getSource(ARROW_SHAFT_SOURCE_ID);

                if(arrowHeadGeoJsonSource != null && arrowShaftGeoJsonSource != null) {
                    updateArrowShaftWith(maneuverPoints);
                    updateArrowHeadWith(maneuverPoints);
                }
            }
        });

    }

    @Override
    public void onDidFinishLoadingStyle() {

        redraw("directions-marker-layer");
    }

}
