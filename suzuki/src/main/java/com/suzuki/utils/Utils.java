package com.suzuki.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


import com.mappls.sdk.geojson.Point;
import com.mappls.sdk.geojson.utils.PolylineUtils;
import com.mappls.sdk.services.api.directions.models.LegStep;
import com.mappls.sdk.services.utils.Constants;
import com.mappls.sdk.turf.TurfMeasurement;

import java.util.List;

/**
 * Created by akram on 30/05/17
 */
public class Utils {
    public static Bitmap getBitmapFromDrawable(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        }
    }

    //    connect(bleDevice);
    public static float roundaboutAngleForNAN(@NonNull LegStep currentLegStep, @Nullable LegStep nextLegStep) {
        if (nextLegStep == null)
            return 0;
        Point currentPoint = currentLegStep.maneuver().location();
        System.out.println(currentPoint.toJson());
        Point nextPoint = nextLegStep.maneuver().location();
        System.out.println(nextPoint.toJson());


        double angleBetweenPoint = TurfMeasurement.bearing(currentPoint, nextPoint);
        if (angleBetweenPoint < 0)
            angleBetweenPoint = angleBetweenPoint + 360;
        double bearingBefore = currentLegStep.maneuver().bearingBefore();
        if (bearingBefore < 0)
            bearingBefore = bearingBefore + 360;
        double angle = (angleBetweenPoint - bearingBefore) + 180;
        if (angle < 0) {
            angle = angle + 360;
        }
        if (angle > 360) {
            angle = angle - 360;
        }
        return (float) angle;

    }

    public static float roundaboutAngle(@NonNull LegStep currentLegStep, @Nullable LegStep nextLegStep) {
        List<Point> point = PolylineUtils.decode(currentLegStep.geometry(), Constants.PRECISION_6);
        Point startPoint = point.get(0);
        Point endPoint = point.get(point.size() - 1);
        Point midPoint;
        if (point.size() % 2 == 0) {
            midPoint = point.get(point.size() / 2);
        } else {
            midPoint = point.get((point.size() / 2) + 1);
        }

        double ax = startPoint.latitude();
        double ay = startPoint.longitude();
        double bx = midPoint.latitude();
        double by = midPoint.longitude();
        double cx = endPoint.latitude();
        double cy = endPoint.longitude();

        double d = 2 * (ax * (by - cy) + bx * (cy - ay) + cx * (ay - by));
        double circum_x = ((ax * ax + ay * ay) * (by - cy) + (bx * bx + by * by) * (cy - ay) + (cx * cx + cy * cy) * (ay - by)) / d;
        double circum_y = ((ax * ax + ay * ay) * (cx - bx) + (bx * bx + by * by) * (ax - cx) + (cx * cx + cy * cy) * (bx - ax)) / d;

        double sLat = Math.toRadians(startPoint.latitude());
        double sLon = Math.toRadians(startPoint.longitude());
        double eLat = Math.toRadians(midPoint.latitude());
        double eLon = Math.toRadians(midPoint.longitude());

        double AB = 6371.01 * Math.acos(Math.sin(sLat) * Math.sin(eLat) + Math.cos(sLat) * Math.cos(eLat) * Math.cos(sLon - eLon));

        sLat = Math.toRadians(midPoint.latitude());
        sLon = Math.toRadians(midPoint.longitude());
        eLat = Math.toRadians(endPoint.latitude());
        eLon = Math.toRadians(endPoint.longitude());

        double BC = 6371.01 * Math.acos(Math.sin(sLat) * Math.sin(eLat) + Math.cos(sLat) * Math.cos(eLat) * Math.cos(sLon - eLon));

        sLat = Math.toRadians(startPoint.latitude());
        sLon = Math.toRadians(startPoint.longitude());
        eLat = Math.toRadians(endPoint.latitude());
        eLon = Math.toRadians(endPoint.longitude());

        double AC = 6371.01 * Math.acos(Math.sin(sLat) * Math.sin(eLat) + Math.cos(sLat) * Math.cos(eLat) * Math.cos(sLon - eLon));

        double AD = AC / 2;

        sLat = Math.toRadians(circum_x);
        sLon = Math.toRadians(circum_y);
        eLat = Math.toRadians(startPoint.latitude());
        eLon = Math.toRadians(startPoint.longitude());

        double AO = 6371.01 * Math.acos(Math.sin(sLat) * Math.sin(eLat) + Math.cos(sLat) * Math.cos(eLat) * Math.cos(sLon - eLon));

        System.out.println("AO = " + AO);

        sLat = Math.toRadians(circum_x);
        sLon = Math.toRadians(circum_y);
        eLat = Math.toRadians(midPoint.latitude());
        eLon = Math.toRadians(midPoint.longitude());

        double BO = 6371.01 * Math.acos(Math.sin(sLat) * Math.sin(eLat) + Math.cos(sLat) * Math.cos(eLat) * Math.cos(sLon - eLon));

        double m = AD / AO;

        System.out.println("M = " + m);
        if (m > 1)
            m = 1;
        else
            m = AD / AO;

        double angle_A = Math.acos(m);
        angle_A = angle_A * 180 / Math.PI;

        Point O = midPointEuclidean(startPoint, endPoint);
        sLat = Math.toRadians(midPoint.latitude());
        sLon = Math.toRadians(midPoint.longitude());
        eLat = Math.toRadians(O.latitude());
        eLon = Math.toRadians(O.longitude());
        double BD = 6371.01 * Math.acos(Math.sin(sLat) * Math.sin(eLat) + Math.cos(sLat) * Math.cos(eLat) * Math.cos(sLon - eLon));
        System.out.println("length of BD  :::  " + BD);

        double angle_O = 180 - 2 * angle_A;
        System.out.println("Angle O = " + angle_O);
        if (Double.isNaN(angle_O)) {
            return roundaboutAngleForNAN(currentLegStep, nextLegStep);
        }
        if (BD >= BO)
            return getRoundAngle((float) (360 - angle_O));
        else
            return getRoundAngle((float) angle_O);


    }

    private static Point midPointEuclidean(Point startPoint, Point endPoint) {
        double dist_x = Math.abs(startPoint.latitude() - endPoint.latitude()) / 2;
        double dist_y = Math.abs(startPoint.longitude() - endPoint.longitude()) / 2;

        double res_x;
        if (startPoint.latitude() > endPoint.latitude()) {
            res_x = startPoint.latitude() - dist_x;
        } else {
            res_x = endPoint.latitude() - dist_x;
        }

        double res_y;
        if (startPoint.longitude() > endPoint.longitude()) {
            res_y = startPoint.longitude() - dist_y;
        } else {
            res_y = endPoint.longitude() - dist_y;
        }

        return Point.fromLngLat(res_y, res_x);
    }
//    public static float roundaboutAngle(@NonNull LegStep currentLegStep, @Nullable LegStep nextLegStep){
//        float angle=0f;
//        if(nextLegStep==null) {
//            return (float) (180 - currentLegStep.maneuver().bearingBefore());
//        }else {
//            float bearBefore = currentLegStep.maneuver().bearingBefore().floatValue();
//            float bearAfter = nextLegStep.maneuver().bearingAfter().floatValue();
//            if (bearBefore < 180) {
//                bearBefore = bearBefore + 180;
//            } else {
//                bearBefore = bearBefore - 180;
//            }
//            angle = bearAfter - bearBefore;
//            if (angle < 0) {
//                angle = angle + 360;
//            }
//        }
//        return getRoundAngle(angle);
//    }

    public static int getManeuverID(float degree) {
        if (degree <= 45) {
            return 65;
        } else if (degree <= 90) {
            return 66;
        } else if (degree <= 135) {
            return 67;
        } else if (degree <= 180) {
            return 68;
        } else if (degree <= 225) {
            return 69;
        } else if (degree <= 270) {
            return 70;
        } else {
            return 71;
        }
    }

    public static int getRoundAngle(float degree) {
        if (degree <= 45) {
            return 45;
        } else if (degree <= 90) {
            return 90;
        } else if (degree <= 135) {
            return 135;
        } else if (degree <= 180) {
            return 180;
        } else if (degree <= 225) {
            return 225;
        } else if (degree <= 270) {
            return 270;
        } else {
            return 315;
        }
    }
}
