//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.suzuki.maps.traffic;

import androidx.annotation.NonNull;

import com.mappls.sdk.maps.MapView;
import com.mappls.sdk.maps.MapView.OnDidFinishLoadingStyleListener;
import com.mappls.sdk.maps.MapplsMap;
import com.mappls.sdk.maps.Style;
import com.mappls.sdk.maps.Style.OnStyleLoaded;
import com.mappls.sdk.maps.style.layers.Layer;
import com.mappls.sdk.maps.style.layers.Property;
import com.mappls.sdk.maps.style.layers.PropertyFactory;
import com.mappls.sdk.maps.style.layers.PropertyValue;
import java.util.Iterator;
import java.util.List;

public class TrafficPlugin implements OnDidFinishLoadingStyleListener {
    private MapplsMap mapplsMap;
    private boolean enable = false;
    private boolean enableNonFreeFlow = true;
    private boolean enableClosure = true;
    private boolean enableStopIcon = true;
    private boolean enableFreeFlow = true;
    private boolean enableOth1 = false;
    private boolean enableOth2 = false;
    private boolean enableOth3 = false;
    private boolean enableOth4 = false;
    private boolean enableOth5 = false;
    private static final String NON_FREE_FLOW_LAYER = "Traffic_nonfreeflow";
    private static final String CLOSURE_LAYER = "Traffic_closure";
    private static final String STOP_ICON_LAYER = "Traffic_stopicon";
    private static final String TRAFFIC_FREEFLOW_LAYER = "Traffic_freeflow";
    private static final String TRAFFIC_OTH_1_LAYER = "Traffic_oth1";
    private static final String TRAFFIC_OTH_2_LAYER = "Traffic_oth2";
    private static final String TRAFFIC_OTH_3_LAYER = "Traffic_oth3";
    private static final String TRAFFIC_OTH_4_LAYER = "Traffic_oth4";
    private static final String TRAFFIC_OTH_5_LAYER = "Traffic_oth5";

    public TrafficPlugin(@NonNull MapView mapView, @NonNull MapplsMap mapplsMap) {
        this.mapplsMap = mapplsMap;
        mapView.addOnDidFinishLoadingStyleListener(this);
    }

    public boolean isEnableNonFreeFlow() {
        return this.enableNonFreeFlow;
    }

    public void enableNonFreeFlow(boolean enableNonFreeFlow) {
        this.enableNonFreeFlow = enableNonFreeFlow;
        this.updateState();
    }

    public boolean isEnableClosure() {
        return this.enableClosure;
    }

    public void enableClosure(boolean enableClosure) {
        this.enableClosure = enableClosure;
        this.updateState();
    }

    public boolean isEnableStopIcon() {
        return this.enableStopIcon;
    }

    public void enableStopIcon(boolean enableStopIcon) {
        this.enableStopIcon = enableStopIcon;
        this.updateState();
    }

    public boolean isEnableFreeFlow() {
        return this.enableFreeFlow;
    }

    public void enableFreeFlow(boolean enableFreeFlow) {
        this.enableFreeFlow = enableFreeFlow;
        this.updateState();
    }

    public boolean isEnable() {
        return this.enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
        this.updateState();
    }

    public boolean isEnableOth1() {
        return this.enableOth1;
    }

    public void enableOth1(boolean enableOth1) {
        this.enableOth1 = enableOth1;
        this.updateState();
    }

    public boolean isEnableOth2() {
        return this.enableOth2;
    }

    public void enableOth2(boolean enableOth2) {
        this.enableOth2 = enableOth2;
        this.updateState();
    }

    public boolean isEnableOth3() {
        return this.enableOth3;
    }

    public void enableOth3(boolean enableOth3) {
        this.enableOth3 = enableOth3;
        this.updateState();
    }

    public boolean isEnableOth4() {
        return this.enableOth4;
    }

    public void enableOth4(boolean enableOth4) {
        this.enableOth4 = enableOth4;
        this.updateState();
    }

    public boolean isEnableOth5() {
        return this.enableOth5;
    }

    public void enableOth5(boolean enableOth5) {
        this.enableOth5 = enableOth5;
        this.updateState();
    }

    private void updateState() {
        this.mapplsMap.getStyle(new OnStyleLoaded() {
            public void onStyleLoaded(@NonNull Style style) {
                TrafficPlugin.this.setVisibility(style);
            }
        });
    }

    public void onStartLoadingMap() {
    }

    private void onFinishLoadingStyle() {
        this.updateState();
    }

    private void setVisibility(@NonNull Style style) {
        List<Layer> layers = style.getLayers();
        if (layers.size() > 0) {
            Iterator var3 = layers.iterator();

            while(true) {
                while(var3.hasNext()) {
                    Layer layer = (Layer)var3.next();
                    String var5 = layer.getId();
                    byte var6 = -1;
                    switch(var5.hashCode()) {
                        case -1418677347:
                            if (var5.equals("Traffic_stopicon")) {
                                var6 = 2;
                            }
                            break;
                        case -376946244:
                            if (var5.equals("Traffic_freeflow")) {
                                var6 = 3;
                            }
                            break;
                        case 488357337:
                            if (var5.equals("Traffic_closure")) {
                                var6 = 1;
                            }
                            break;
                        case 1002362512:
                            if (var5.equals("Traffic_oth1")) {
                                var6 = 4;
                            }
                            break;
                        case 1002362513:
                            if (var5.equals("Traffic_oth2")) {
                                var6 = 5;
                            }
                            break;
                        case 1002362514:
                            if (var5.equals("Traffic_oth3")) {
                                var6 = 6;
                            }
                            break;
                        case 1002362515:
                            if (var5.equals("Traffic_oth4")) {
                                var6 = 7;
                            }
                            break;
                        case 1002362516:
                            if (var5.equals("Traffic_oth5")) {
                                var6 = 8;
                            }
                            break;
                        case 1043448837:
                            if (var5.equals("Traffic_nonfreeflow")) {
                                var6 = 0;
                            }
                    }

                    switch(var6) {
                        case 0:
                            layer.setProperties(new PropertyValue[]{PropertyFactory.visibility(this.isEnableNonFreeFlow() && this.isEnable() ? Property.VISIBLE : Property.NONE)});
                            break;
                        case 1:
                            layer.setProperties(new PropertyValue[]{PropertyFactory.visibility(this.isEnableClosure() && this.isEnable() ? Property.VISIBLE : Property.NONE)});
                            break;
                        case 2:
                            layer.setProperties(new PropertyValue[]{PropertyFactory.visibility(this.isEnableStopIcon() && this.isEnable() ? Property.VISIBLE : Property.NONE)});
                            break;
                        case 3:
                            layer.setProperties(new PropertyValue[]{PropertyFactory.visibility(this.isEnableFreeFlow() && this.isEnable() ? Property.VISIBLE : Property.NONE)});
                            break;
                        case 4:
                            layer.setProperties(new PropertyValue[]{PropertyFactory.visibility(this.isEnableOth1() && this.isEnable() ? Property.VISIBLE : Property.NONE)});
                            break;
                        case 5:
                            layer.setProperties(new PropertyValue[]{PropertyFactory.visibility(this.isEnableOth2() && this.isEnable() ? Property.VISIBLE : Property.NONE)});
                            break;
                        case 6:
                            layer.setProperties(new PropertyValue[]{PropertyFactory.visibility(this.isEnableOth3() && this.isEnable() ? Property.VISIBLE : Property.NONE)});
                            break;
                        case 7:
                            layer.setProperties(new PropertyValue[]{PropertyFactory.visibility(this.isEnableOth4() && this.isEnable() ? Property.VISIBLE : Property.NONE)});
                            break;
                        case 8:
                            layer.setProperties(new PropertyValue[]{PropertyFactory.visibility(this.isEnableOth5() && this.isEnable() ? Property.VISIBLE : Property.NONE)});
                    }
                }

                return;
            }
        }
    }

    public void onDidFinishLoadingStyle() {
        this.onFinishLoadingStyle();
    }
}
