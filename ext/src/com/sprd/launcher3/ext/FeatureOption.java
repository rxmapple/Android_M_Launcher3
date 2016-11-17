package com.sprd.launcher3.ext;

import android.os.SystemProperties;
import android.util.Log;

/**
 * Created by rxmapple on 2016/9/27.
 */
public class FeatureOption {
    public static final String TAG = "Launcher3.FeatureOption";

    //SPRD add for SPRD_SETTINGS_ACTIVITY_SUPPORT start {
    public static final boolean SPRD_SETTINGS_ACTIVITY_SUPPORT = getProp("ro.sprd_settings_activity", true);
    //end }
    public static final boolean SPRD_UNREAD_INFO_SUPPORT = getProp("ro.sprd_unread_info", true);

    //SPRD Add for Desktop cycle start {
    public static final boolean SPRD_CIRCULAR_SLIDING_SUPPORT = getProp("ro.sprd_circular_sliding", true);
    // end }

    private static boolean getProp(String prop, boolean devalues) {
        boolean ret = false;

        try {
            ret = SystemProperties.getBoolean(prop, devalues);
        } catch (Exception e) {
            Log.e(TAG, "getProp:" + prop + " error." + e);
        }

        return ret;
    }
}
