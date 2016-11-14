package com.sprd.launcher3.ext;

import android.content.Context;
import android.os.Bundle;

import com.android.launcher3.LauncherSettings;

/**
 * Created by SPREADTRUM\pichao.gao on 11/14/16.
 */

public class UtilitiesExt {

    //SPRD add for SPRD_SETTINGS_ACTIVITY_SUPPORT start {
    /**
     *
     * @param context  to getContentResolver
     * @param key  Preference key
     *  @param defaultVal if get value is null, return defaultVal
     */
    public static boolean getLauncherSettingsBoolean(Context context, String key, boolean defaultVal) {
        Bundle extras = new Bundle();
        extras.putBoolean(LauncherSettings.Settings.EXTRA_DEFAULT_VALUE, defaultVal);
        Bundle bundle = context.getContentResolver().call(
                LauncherSettings.Settings.CONTENT_URI,
                LauncherSettings.Settings.METHOD_GET_BOOLEAN,
                key, extras);

        if(bundle == null){
            return defaultVal;
        }
        return bundle.getBoolean(LauncherSettings.Settings.EXTRA_VALUE);
    }

    public static String getLauncherSettingsString(Context context, String key, String defaultVal) {
        Bundle extras = new Bundle();
        extras.putString(LauncherSettings.Settings.EXTRA_DEFAULT_VALUE, defaultVal);
        Bundle bundle = context.getContentResolver().call(
                LauncherSettings.Settings.CONTENT_URI,
                LauncherSettings.Settings.METHOD_GET_STRING,
                key, extras);

        if(bundle == null){
            return defaultVal;
        }
        return bundle.getString(LauncherSettings.Settings.EXTRA_VALUE);
    }
    //end }

}
