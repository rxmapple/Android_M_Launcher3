package com.sprd.launcher3.ext.unreadnotifier.call;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.BaseColumns;
import android.provider.CallLog;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.sprd.launcher3.ext.LogUtils;
import com.sprd.launcher3.ext.unreadnotifier.BaseContentObserver;
import com.sprd.launcher3.ext.unreadnotifier.UnreadBaseItem;
import com.sprd.launcher3.ext.unreadnotifier.UnreadSettingsFragment;

/**
 * Created by SPRD on 2016/11/22.
 */

public class MissCallItem extends UnreadBaseItem {
    private static final String TAG = "MissCallItem";
    private static final Uri CALLS_CONTENT_URI = CallLog.Calls.CONTENT_URI;
    private static final String MISSED_CALLS_SELECTION =
            CallLog.Calls.TYPE + " = " + CallLog.Calls.MISSED_TYPE + " AND " + CallLog.Calls.NEW + " = 1";
    public static final int TYPE_CALL_LOG = 101;

    public static final ComponentName DEFAULT_CNAME = new ComponentName("com.android.dialer",
            "com.android.dialer.DialtactsActivity");

    public MissCallItem(Launcher launcher) {
        super(launcher);
        mContentObserver = new BaseContentObserver(new Handler(), launcher, CALLS_CONTENT_URI, this);
        mPermission = Manifest.permission.READ_CALL_LOG;
        mPrefKey = UnreadSettingsFragment.PREF_KEY_MISS_CALL;
        mType = TYPE_CALL_LOG;
        mDefaultCn = DEFAULT_CNAME;
        mUnreadHintString = mLauncher.getString(R.string.unread_misscall_hint);
    }

    @Override
    public int getUnreadCount() {
        int missedCalls = 0;
        ContentResolver resolver = mLauncher.getContentResolver();

        boolean result = checkPermission(mPermission);
        LogUtils.d(TAG, "getMissCallCount, result = "+result);
        if (!result) {
            LogUtils.d(TAG, "no READ_CALL_LOG Permission");
            return 0;
        }

        final Cursor cursor = resolver.query(CALLS_CONTENT_URI, new String[]{BaseColumns._ID},
                MISSED_CALLS_SELECTION, null, null);
        if (cursor != null) {
            try {
                missedCalls = cursor.getCount();
            } finally {
                cursor.close();
            }
        }
        mUnreadCount = missedCalls;
        return mUnreadCount;
    }

}