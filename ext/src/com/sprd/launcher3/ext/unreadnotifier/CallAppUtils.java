package com.sprd.launcher3.ext.unreadnotifier;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.telecom.PhoneAccount;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SPRD on 10/16/16.
 */
public class CallAppUtils {
    public static class CallApplicationData {
        /**
         * Name of this SMS app for display.
         */
        public String mApplicationName;

        /**
         * Package name for this SMS app.
         */
        public String mPackageName;

        /**
         * Activity class name for this SMS app.
         */
        public String callClassName;

        public CallApplicationData(String applicationName, String packageName) {
            mApplicationName = applicationName;
            mPackageName = packageName;
        }
    }

    /**
     * Returns a list of installed and available dialer applications.
     **/
    @TargetApi(Build.VERSION_CODES.M)
    public static List<CallApplicationData> getInstalledDialerApplications(Context context) {
        PackageManager packageManager = context.getPackageManager();

        // Get the list of apps registered for the DIAL intent with empty scheme
        Intent intent = new Intent(Intent.ACTION_DIAL);
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(intent, 0);

        List<String> packageNames = new ArrayList<>();

        for (ResolveInfo resolveInfo : resolveInfoList) {
            final ActivityInfo activityInfo = resolveInfo.activityInfo;
            if (activityInfo != null && !packageNames.contains(activityInfo.packageName)) {
                packageNames.add(activityInfo.packageName);
            }
        }

        final Intent dialIntentWithTelScheme = new Intent(Intent.ACTION_DIAL);
        dialIntentWithTelScheme.setData( Uri.fromParts( PhoneAccount.SCHEME_TEL, "", null));
        return filterByIntent(context, packageNames, dialIntentWithTelScheme);
    }

    /**
     * Filter a given list of package names for those packages that contain an activity that has
     * an intent filter for a given intent.
     *
     * @param context A valid context
     * @param packageNames List of package names to filter.
     * @return The filtered list.
     */
    private static List<CallApplicationData> filterByIntent(Context context, List<String> packageNames,
                                               Intent intent) {
        PackageManager packageManager = context.getPackageManager();
        if (packageNames == null || packageNames.isEmpty()) {
            return new ArrayList<>();
        }

        final List<CallApplicationData> result = new ArrayList<>();
        final List<ResolveInfo> resolveInfoList =
                context.getPackageManager().queryIntentActivities(intent, 0);

        for (ResolveInfo resolveInfo : resolveInfoList) {
            final ActivityInfo info = resolveInfo.activityInfo;
            if (info != null && packageNames.contains(info.packageName)
                    && !result.contains(info.packageName)) {
                final String packageName = info.packageName;
                final String applicationName = resolveInfo.loadLabel(packageManager).toString();
                final CallApplicationData callApplicationData = new CallApplicationData(
                        applicationName, packageName);
                callApplicationData.callClassName = info.name;
                result.add(callApplicationData);
            }
        }
        return result;
    }

}
