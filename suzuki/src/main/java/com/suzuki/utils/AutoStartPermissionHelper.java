package com.suzuki.utils;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;


import java.util.List;
public class AutoStartPermissionHelper {
    private static AutoStartPermissionHelper helper = null;
    /***
     * Letv
     */
    private String BRAND_LETV = "letv";
    private String PACKAGE_LETV_MAIN = "com.letv.android.letvsafe";
    private String PACKAGE_LETV_COMPONENT = "com.letv.android.letvsafe.AutobootManageActivity";
    /***
     * ASUS ROG
     */
    //connect(bleDevice);
    private String BRAND_ASUS = "asus";
    private String PACKAGE_ASUS_MAIN = "com.asus.mobilemanager";
    private String PACKAGE_ASUS_COMPONENT = "com.asus.mobilemanager.powersaver.PowerSaverSettings";
    /***
     * Honor
     */
    private String BRAND_HONOR = "honor";
    private String PACKAGE_HONOR_MAIN = "com.huawei.systemmanager";
    private String PACKAGE_HONOR_COMPONENT = "com.huawei.systemmanager.optimize.process.ProtectActivity";
    /**
     * Oppo
     */
    private String BRAND_OPPO = "oppo";
    private String PACKAGE_OPPO_MAIN = "com.coloros.safecenter";
    private String PACKAGE_OPPO_FALLBACK = "com.oppo.safe";
    private String PACKAGE_OPPO_COMPONENT = "com.coloros.safecenter.permission.startup.StartupAppListActivity";
    private String PACKAGE_OPPO_COMPONENT_FALLBACK = "com.oppo.safe.permission.startup.StartupAppListActivity";
    private String PACKAGE_OPPO_COMPONENT_FALLBACK_A = "com.coloros.safecenter.startupapp.StartupAppListActivity";
    /**
     * Vivo
     */
    private String BRAND_VIVO = "vivo";
    private String PACKAGE_VIVO_MAIN = "com.iqoo.secure";
    private String PACKAGE_VIVO_FALLBACK = "com.vivo.permissionmanager";
    private String PACKAGE_VIVO_COMPONENT = "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity";
    private String PACKAGE_VIVO_COMPONENT_FALLBACK = "com.vivo.permissionmanager.activity.BgStartUpManagerActivity";
    private String PACKAGE_VIVO_COMPONENT_FALLBACK_A = "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager";
    /**
     * Nokia
     */
    private String BRAND_NOKIA = "nokia";
    private String PACKAGE_NOKIA_MAIN = "com.evenwell.powersaving.g3";
    private String PACKAGE_NOKIA_COMPONENT = "com.evenwell.powersaving.g3.exception.PowerSaverExceptionActivity";
    /**
     * Xiaomi
     */
    private String BRAND_XIAOMI = "xiaomi";
    private String PACKAGE_XIAOMI_MAIN = "com.miui.securitycenter";
    private String PACKAGE_XIAOMI_COMPONENT = "com.miui.permcenter.autostart.AutoStartManagementActivity";

    public static AutoStartPermissionHelper getPermissionHelper() {
        if (helper == null)
            helper = new AutoStartPermissionHelper();
        return helper;
    }

    private static String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    public void getAutoStartPermission(Context context) {
        String build_info = Build.BRAND.toLowerCase();
        if (BRAND_ASUS.equalsIgnoreCase(build_info)) {
            autoStartAsus(context);
        } else if (BRAND_XIAOMI.equalsIgnoreCase(build_info)) {
            autoStartXiaomi(context);
        } else if (BRAND_LETV.equalsIgnoreCase(build_info)) {
            autoStartLetv(context);
        } else if (BRAND_HONOR.equalsIgnoreCase(build_info)) {
            autoStartHonor(context);
        } else if (BRAND_OPPO.equalsIgnoreCase(build_info)) {
            autoStartOppo(context);
        } else if (BRAND_VIVO.equalsIgnoreCase(build_info)) {
            autoStartVivo(context);
        } else if (BRAND_NOKIA.equalsIgnoreCase(build_info)) {
            autoStartNokia(context);
        }

    }

    private void autoStartXiaomi(Context context) {
        //      if (isPackageExists(context, PACKAGE_XIAOMI_MAIN)) {
        try {
            startIntent(context, PACKAGE_XIAOMI_MAIN, PACKAGE_XIAOMI_COMPONENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //    }
    }

    private void autoStartAsus(Context context) {
        //  if (isPackageExists(context, PACKAGE_ASUS_MAIN)) {
        try {
            startIntent(context, PACKAGE_ASUS_MAIN, PACKAGE_ASUS_COMPONENT);
        } catch (Exception e) {

            e.printStackTrace();

        }


        //   }

    }

    private void autoStartLetv(Context context) {
        // if (isPackageExists(context, PACKAGE_LETV_MAIN)) {
        try {
            startIntent(context, PACKAGE_LETV_MAIN, PACKAGE_LETV_COMPONENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //}
    }

    private void autoStartHonor(Context context) {
        //if (isPackageExists(context, PACKAGE_HONOR_MAIN)) {
        try {
            startIntent(context, PACKAGE_HONOR_MAIN, PACKAGE_HONOR_COMPONENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //}
    }

    private void autoStartOppo(Context context) {
        //if (isPackageExists(context, PACKAGE_OPPO_MAIN) || isPackageExists(context, PACKAGE_OPPO_FALLBACK)) {
        try {
            startIntent(context, PACKAGE_OPPO_MAIN, PACKAGE_OPPO_COMPONENT);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                startIntent(context, PACKAGE_OPPO_FALLBACK, PACKAGE_OPPO_COMPONENT_FALLBACK);
            } catch (Exception ex) {
                ex.printStackTrace();
                try {
                    startIntent(context, PACKAGE_OPPO_MAIN, PACKAGE_OPPO_COMPONENT_FALLBACK_A);
                } catch (Exception exx) {
                    exx.printStackTrace();
                }
            }
        }
        //}
    }

    private void autoStartVivo(Context context) {
        //      if (isPackageExists(context, PACKAGE_VIVO_MAIN) || isPackageExists(context, PACKAGE_VIVO_FALLBACK)) {
        try {
            startIntent(context, PACKAGE_VIVO_MAIN, PACKAGE_VIVO_COMPONENT);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                startIntent(context, PACKAGE_VIVO_FALLBACK, PACKAGE_VIVO_COMPONENT_FALLBACK);
            } catch (Exception ex) {
                ex.printStackTrace();
                try {
                    startIntent(context, PACKAGE_VIVO_MAIN, PACKAGE_VIVO_COMPONENT_FALLBACK_A);
                } catch (Exception exx) {
                    exx.printStackTrace();
                }
            }
        }
        //   }
    }

    private void autoStartNokia(Context context) {
        //if (isPackageExists(context, PACKAGE_NOKIA_MAIN)) {
        try {
            startIntent(context, PACKAGE_NOKIA_MAIN, PACKAGE_NOKIA_COMPONENT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //}
    }

    private void startIntent(Context context, String packageName, String componentName) {
        try {

            Intent intent = new Intent();

            intent.setComponent(new ComponentName(packageName, componentName));

            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            if (list.size() > 0) {
                context.startActivity(intent);
                /*new AlertDialog.Builder(context)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("App Permissions")
                        .setMessage(String.format("%s requires background permissions or autostart permission to function properly.%n", getApplicationName(context)))
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                context.startActivity(intent);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .show();*/
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private Boolean isPackageExists(Context context, String targetPackage) {
        List<ApplicationInfo> packages = null;
        PackageManager pm = context.getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName == targetPackage) {
                return true;
            }
        }
        return false;
    }
}