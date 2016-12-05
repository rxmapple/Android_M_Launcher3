package com.sprd.launcher3.ext.unreadnotifier.sms;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.BaseColumns;

import com.android.launcher3.R;
import com.android.launcher3.Launcher;
import com.sprd.launcher3.ext.LogUtils;
import com.sprd.launcher3.ext.unreadnotifier.BaseContentObserver;
import com.sprd.launcher3.ext.unreadnotifier.UnreadBaseItem;
import com.sprd.launcher3.ext.unreadnotifier.UnreadSettingsFragment;

/**
 * Created by SPRD on 2016/11/22.
 */

public class UnreadMessageItem extends UnreadBaseItem {
    private static final String TAG = "MessageUnreadItem";
    private static final Uri MMS_CONTENT_URI = Uri.parse("content://mms");
    private static final Uri SMS_CONTENT_URI = Uri.parse("content://sms");
    private static final Uri MMSSMS_CONTENT_URI = Uri.parse("content://mms-sms");
    public static final int TYPE_SMS = 102;

    public static final ComponentName DEFAULT_CNAME = new ComponentName("com.android.messaging",
            "com.android.messaging.ui.conversationlist.ConversationListActivity");

    public UnreadMessageItem(Launcher launcher) {
        super(launcher);
        mContentObserver = new BaseContentObserver(new Handler(), launcher, MMSSMS_CONTENT_URI, this);
        mPermission = Manifest.permission.READ_SMS;
        mPrefKey = UnreadSettingsFragment.PREF_KEY_UNREAD_SMS;
        mType = TYPE_SMS;
        mDefaultCn = DEFAULT_CNAME;
        mUnreadHintString = mLauncher.getString(R.string.unread_misssms_hint);
    }

    @Override
    public int getUnreadCount() {
        int unreadSms = 0;
        int unreadMms = 0;
        ContentResolver resolver = mLauncher.getContentResolver();

        boolean result = checkPermission(mPermission);
        LogUtils.d(TAG, "getUnreadSmsCount, result = "+result);
        if (!result) {
            LogUtils.d(TAG, "no READ_SMS Permission");
            return 0;
        }

        final Cursor cursor1 = resolver.query(SMS_CONTENT_URI, new String[]{BaseColumns._ID},
                "type =1 AND read = 0", null, null);
        if (cursor1 != null) {
            try {
                unreadSms = cursor1.getCount();
            } finally {
                cursor1.close();
            }
        }

        final Cursor cursor2 = resolver.query(MMS_CONTENT_URI, new String[]{BaseColumns._ID},
                "msg_box = 1 AND read = 0 AND ( m_type =130 OR m_type = 132 ) AND thread_id > 0",
                null, null);
        if (cursor2 != null) {
            try {
                unreadMms = cursor2.getCount();
            } finally {
                cursor2.close();
            }
        }
        mUnreadCount = unreadMms + unreadSms;
        return mUnreadCount;
    }

}



