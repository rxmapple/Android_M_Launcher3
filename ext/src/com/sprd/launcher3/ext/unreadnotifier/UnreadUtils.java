package com.sprd.launcher3.ext.unreadnotifier;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.provider.CallLog;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import com.sprd.launcher3.ext.AppListPreference;
import com.sprd.launcher3.ext.LogUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by SPREADTRUM\jin.xie on 11/4/16.
 */

public class UnreadUtils {
    private static final String TAG = "UnreadUtils";
    private static final Uri MMS_CONTENT_URI = Uri.parse("content://mms");
    private static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
    private static final Uri CALLS_CONTENT_URI = CallLog.Calls.CONTENT_URI;
    private static final String MISSED_CALLS_SELECTION =
            CallLog.Calls.TYPE + " = " + CallLog.Calls.MISSED_TYPE + " AND " + CallLog.Calls.NEW + " = 1";

    public static final int TYPE_ALL = 100;
    public static final int TYPE_CALL_LOG = 101;
    public static final int TYPE_SMS = 102;

    public static final String EXTRA_UNREAD_TYPE = "extra_unread_type";
    public static final String EXTRA_UNREAD_INFO = "extra_unread_info";

    private static final String ACTION_UNREAD_CHANGED = "com.sprd.action.UNREAD_CHANGED";
    private static final String EXTRA_UNREAD_COMPONENT = "com.sprd.intent.extra.UNREAD_COMPONENT";
    private static final String EXTRA_UNREAD_NUMBER = "com.sprd.intent.extra.UNREAD_NUMBER";

    public static final class TypeAndPermission{
        private static HashMap<String,Integer> mMap = new HashMap<String,Integer>(){
            {
                put(Manifest.permission.READ_CALL_LOG,TYPE_CALL_LOG);
                put(Manifest.permission.READ_SMS,TYPE_SMS);
            }
        };

        public static String getPermisson(int type){
            for (Map.Entry<String, Integer> entry : mMap.entrySet()) {
                if(entry.getValue() == type)
                    return entry.getKey();
                System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
            }
            return "";
        }

        public static int getType(String string){
            for (Map.Entry<String, Integer> entry : mMap.entrySet()) {
                if(entry.getKey().equals(string))
                    return entry.getValue();
                System.out.println("key= " + entry.getKey() + " and value= " + entry.getValue());
            }
            return -1;
        }
    }

    /**
     * Query Call/SMS database to get total unread number.
     */
    public static int getMissedCallCount(Context context) {
        int missedCalls = 0;
        ContentResolver resolver = context.getContentResolver();

        if (!selfPermissionGranted(context, TypeAndPermission.getType(Manifest.permission.READ_CALL_LOG))) {
            Log.d(TAG, "no READ_CALL_LOG Permission");
            return 0;
        }

        final Cursor cursor = resolver.query(CALLS_CONTENT_URI, new String[]{BaseColumns._ID},
                MISSED_CALLS_SELECTION, null, null);
        if (cursor != null) {
            try {
                missedCalls = cursor.getCount();
                Log.i(TAG, "Missed Call count = " + missedCalls);
            } finally {
                cursor.close();
            }
        }
        return missedCalls;
    }


    public static int getUnreadMessageCount(Context context) {
        int unreadSms = 0;
        int unreadMms = 0;
        ContentResolver resolver = context.getContentResolver();

        if (!selfPermissionGranted(context, TypeAndPermission.getType(Manifest.permission.READ_SMS))) {
            Log.d(TAG, "no READ_SMS Permission");
            return 0;
        }

        // get Unread SMS count
       final Cursor cursor1 = resolver.query(SMS_CONTENT_URI, new String[]{BaseColumns._ID},
                "type =1 AND read = 0", null, null);
        if (cursor1 != null) {
            try {
                unreadSms = cursor1.getCount();
                Log.i(TAG, "Missed sms count = " + unreadSms);
            } finally {
                cursor1.close();
            }
        }

        // get Unread MMS count
        final Cursor cursor2 = resolver.query(MMS_CONTENT_URI, new String[]{BaseColumns._ID},
                "msg_box = 1 AND read = 0 AND ( m_type =130 OR m_type = 132 ) AND thread_id > 0",
                null, null);
        if (cursor2 != null) {
            try {
                unreadMms = cursor2.getCount();
                Log.i(TAG, "Missed mms count = " + unreadMms);
            } finally {
                cursor2.close();
            }

        }

        return unreadMms + unreadSms;
    }


    /**
     * Whether call or SMS permission is granted.
     */
    public static boolean selfPermissionGranted(Context context, int type) {
        boolean result = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (context.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.M) {
                // targetSdkVersion >= Android M, we can
                // use Context#checkSelfPermission
                result = context.checkSelfPermission(TypeAndPermission.getPermisson(type))
                        == PackageManager.PERMISSION_GRANTED;
            } else {
                // targetSdkVersion < Android M, we have to use PermissionChecker
                result = PermissionChecker.checkSelfPermission(context, TypeAndPermission.getPermisson(type))
                        == PermissionChecker.PERMISSION_GRANTED;
            }
        }

        return result;
    }


    /**
     * Whether phone/SMS preference is checked.
     */
    public static boolean isPersistChecked(Context context, String string){
        SharedPreferences sharePref = PreferenceManager.getDefaultSharedPreferences(context);
        return sharePref.getBoolean(string+ AppListPreference.CHECKBOX_KEY, false);
    }

    /**
     * Whether a service is running or not.
     */
    public static boolean isServiceRunning(Context mContext, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(Integer.MAX_VALUE);

        if (!(serviceList.size() > 0)) {
            return false;
        }

        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(className)) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    /**
     * Send broadcast to Launcher to update the unread icon.
     */
    public static void sendUnreadBroadcast(Context context, int paramInt, ComponentName paramComponentName)
    {
        Intent localIntent = new Intent(ACTION_UNREAD_CHANGED);
        localIntent.putExtra(EXTRA_UNREAD_NUMBER, paramInt);
        localIntent.putExtra(EXTRA_UNREAD_COMPONENT, paramComponentName);
        context.sendBroadcast(localIntent);
        if(LogUtils.DEBUG_UNREAD) {
            LogUtils.d(TAG, "sendUnreadBroadcast(), cn:" + paramComponentName.toShortString() + " unReadNum:" + paramInt);
        }
    }
}
