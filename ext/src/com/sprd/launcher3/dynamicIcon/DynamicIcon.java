package com.sprd.launcher3.dynamicIcon;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.view.View;

import com.android.launcher3.BubbleTextView;
import com.android.launcher3.DeviceProfile;
import com.sprd.launcher3.ext.DynamicIconUtils.DynamicAppChangedCallbacks;
import com.sprd.launcher3.ext.LogUtils;

import java.lang.ref.WeakReference;

/**
 * Created by SPRD on 10/18/16.
 */
public abstract class DynamicIcon extends BroadcastReceiver {

    private static final String TAG = "DynamicIcon";

    protected Context mContext;
    protected ComponentName mComponent;
    protected int mOffsetY;
    protected boolean mInitOffsetY = true;

    private WeakReference<DynamicAppChangedCallbacks> mCallbacks;

    protected abstract void init();
    protected abstract boolean hasChanged();
    public abstract DynamicIconDrawCallback getDynamicIconDrawCallback();

    public class DynamicIconDrawCallback {
        public void drawDynamicIcon(Canvas canvas, View icon, float scale, boolean createBitmap) {}
    }

    public DynamicIcon(Context context) {
        mContext = context;
        init();
    }

    protected int getOffsetY(View icon) {
        if (mInitOffsetY && (icon instanceof BubbleTextView)) {
            DeviceProfile grid = ((BubbleTextView)icon).getDeviceProfile();
            if (grid != null) {
                mOffsetY = grid.iconSizePx;
                mInitOffsetY = false;
            }
        }
        return mOffsetY;
    }

    public void setDynamicIconDrawCallback(WeakReference<DynamicAppChangedCallbacks> callbacks) {
        mCallbacks = callbacks;
        if (LogUtils.DEBUG_DYNAMIC_ICON) {
            LogUtils.d(TAG, "setDynamicIconDrawCallback: callbacks = " + callbacks
                    + ", mCallbacks = " + mCallbacks);
        }
    }

    public void forceUpdateView() {
        if (mCallbacks != null) {
            final DynamicAppChangedCallbacks callbacks = mCallbacks.get();
            if (callbacks != null) {
                callbacks.bindComponentDynamicIconChanged(mComponent);
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (LogUtils.DEBUG_DYNAMIC_ICON) {
            LogUtils.d(TAG, "Receive broadcast: " + action);
        }

        if (hasChanged()) {
            forceUpdateView();
        }
    }
}
