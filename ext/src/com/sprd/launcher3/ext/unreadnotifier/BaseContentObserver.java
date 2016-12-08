package com.sprd.launcher3.ext.unreadnotifier;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

import com.sprd.launcher3.ext.LogUtils;

/**
 * Created by SPRD on 2016/11/23.
 */

public class BaseContentObserver extends ContentObserver {
    private static final String TAG = "BaseContentObserver";
    private Uri mUri;
    private Context mContext;
    protected UnreadBaseItem mItem;

    public BaseContentObserver(Handler handler,Context context,Uri uri, UnreadBaseItem item) {
        super(handler);
        mContext = context;
        mUri = uri;
        mItem = item;
    }

    public void registerContentObserver() {
        mContext.getContentResolver().registerContentObserver(mUri, true, this);
    }

    public void unregisterContentObserver() {
        mContext.getContentResolver().unregisterContentObserver(this);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        if(LogUtils.DEBUG_UNREAD) {
            LogUtils.d(TAG, String.format("onChange: uri=%s selfChange=%b", uri.toString(), selfChange));
        }
        if (mItem != null) {
            mItem.updateUnreadInfo();
        }
    }

}
