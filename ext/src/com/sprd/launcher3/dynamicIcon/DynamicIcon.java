package com.sprd.launcher3.dynamicIcon;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

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
    public boolean mIsChecked;

    private int mType;
    private WeakReference<DynamicAppChangedCallbacks> mCallbacks;

    protected abstract void init();
    protected abstract boolean hasChanged();
    public abstract DynamicIconDrawCallback getDynamicIconDrawCallback();
    public abstract ComponentName getComponentName();
    public abstract Drawable getStableBackground();

    public class DynamicIconDrawCallback {
        public void drawDynamicIcon(Canvas canvas, View icon, float scale, int[] center) {}
    }

    public DynamicIcon(Context context, int type) {
        mContext = context;
        mType = type;
        init();
    }

    public void setDynamicIconDrawCallback(WeakReference<DynamicAppChangedCallbacks> callbacks) {
        mCallbacks = callbacks;
        if (LogUtils.DEBUG_DYNAMIC_ICON) {
            LogUtils.d(TAG, "setDynamicIconDrawCallback: callbacks = " + callbacks
                    + ", mCallbacks = " + mCallbacks);
        }
    }

    public void setCheckedSeate(boolean isChecked) {
        mIsChecked = isChecked;
    }

    public int getType() {
        return mType;
    }

    public boolean isCheckedState() {
        return mIsChecked;
    }

    public boolean isAppInstalled() {
        return isAppInstalled(getComponentName());
    }

    protected boolean isAppInstalled(ComponentName component) {
        if (component != null) {
            PackageManager pm = mContext.getPackageManager();
            String packageName = component.getPackageName();
            if (packageName != null) {
                try {
                    PackageInfo info = pm.getPackageInfo(packageName, 0);
                    return info != null;
                } catch (PackageManager.NameNotFoundException e) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return false;
    }

    public void forceUpdateView(boolean force) {
        if (mCallbacks != null) {
            final DynamicAppChangedCallbacks callbacks = mCallbacks.get();
            if (callbacks != null) {
                if (hasChanged() || force) {
                callbacks.bindComponentDynamicIconChanged(mComponent);
                }
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        if (LogUtils.DEBUG_DYNAMIC_ICON) {
            LogUtils.d(TAG, "Receive broadcast: " + action);
        }

        if (mIsChecked) {
            forceUpdateView(false);
        }
    }
}
