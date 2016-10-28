package com.sprd.launcher3.ext;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;

import com.android.launcher3.BubbleTextView;
import com.android.launcher3.ItemInfo;
import com.sprd.launcher3.dynamicIcon.DynamicCalendar;
import com.sprd.launcher3.dynamicIcon.DynamicDeskclock;
import com.sprd.launcher3.dynamicIcon.DynamicIcon.DynamicIconDrawCallback;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by SPRD on 10/19/16.
 */
public class DynamicIconUtils {

    private static final String TAG = "DynamicIconUtils";

    private static DynamicIconUtils INSTANCE;
    private static ArrayList<DynamicSupportInfo> DYNAMIC_SUPPORT_INFOS =
            new ArrayList<>();
    private static final int INVALID_NUM = -1;
    private static int sDynamicIconInfoNum = 0;

    private WeakReference<DynamicAppChangedCallbacks> mCallbacks;
    private Context mContext;

    class DynamicSupportInfo {

        ComponentName component;
        Drawable background;
        DynamicIconDrawCallback callback;

        public DynamicSupportInfo(ComponentName componentName, Drawable background,
                                  DynamicIconDrawCallback callback) {
            component = componentName;
            this.background = background;
            this.callback = callback;
        }

        @Override
        public String toString() {
            return "{DynamicSupportInfo[" + component + "], background = " + background
                    + ", callback = " + callback + "}";
        }
    }

    public DynamicIconUtils(Context context) {
        mContext = context;
    }

    public static DynamicIconUtils getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new DynamicIconUtils(context);
        }
        return INSTANCE;
    }

    /**
     * Set this as the current Launcher activity object for the loader.
     */
    public void initialize(DynamicAppChangedCallbacks callbacks) {
        mCallbacks = new WeakReference<>( callbacks );
        if (LogUtils.DEBUG_DYNAMIC_ICON) {
            LogUtils.d(TAG, "initialize: callbacks = " + callbacks
                    + ", mCallbacks = " + mCallbacks);
        }
    }

    /**
     * Get the stable part of the dynamic icon for the ComponentName
     */
    public static Drawable getStableBGForComponent(ComponentName componentName) {
        final int index = supportDynamicIcon(componentName);
        return getStableBGAt(index);
    }

    /**
     * Get DynamicIconDrawCallback according to componentName
     */
    public static DynamicIconDrawCallback getDICForComponent(ComponentName componentName) {
        final int index = supportDynamicIcon(componentName);
        return getDynamicIconCallbackAt(index);
    }

    private static int supportDynamicIcon(ComponentName componentName) {
        if (componentName == null) {
            return INVALID_NUM;
        }

        final int size = DYNAMIC_SUPPORT_INFOS.size();
        for(int i = 0; i < size; i++) {
            if (DYNAMIC_SUPPORT_INFOS.get(i).component.equals(componentName)) {
                return i;
            }
        }
        if (LogUtils.DEBUG_DYNAMIC_ICON) {
            LogUtils.d(TAG, " supportDynamicIcon: componentName = " + componentName
                    + ", INVALID_NUM = " + INVALID_NUM);
        }
        return INVALID_NUM;
    }

    private static DynamicIconDrawCallback getDynamicIconCallbackAt(int index) {
        if (index < 0 || index >= sDynamicIconInfoNum) {
            return null;
        }

        if (LogUtils.DEBUG_DYNAMIC_ICON) {
            LogUtils.d(TAG, "getDynamicIconCallbackAt: index = " + index);
        }

        return DYNAMIC_SUPPORT_INFOS.get(index).callback;
    }

    private static Drawable getStableBGAt(int index) {
        if (index < 0 || index >= sDynamicIconInfoNum) {
            return null;
        }

        if (LogUtils.DEBUG_DYNAMIC_ICON) {
            LogUtils.d(TAG, "getStableBGAt: index = " + index);
        }

        return DYNAMIC_SUPPORT_INFOS.get(index).background;
    }

    /**
     * Load and initialize dynamic icon.
     */
    public void loadAndInitDynamicIcon() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... unused) {
                loadDynamicIconInfo();
                return null;
            }

            @Override
            protected void onPostExecute(final Void result) {
                if (mCallbacks != null) {
                    DynamicAppChangedCallbacks callbacks = mCallbacks.get();
                    if (callbacks != null) {
                        callbacks.bindDynamicIconIfNeeded();
                    }
                }
            }
        }.execute();
    }

    private void loadDynamicIconInfo() {
        if (FeatureOption.SPRD_DYNAMIC_CALENDAR_SUPPORT) {
            DynamicCalendar calendar = new DynamicCalendar(mContext);
            DynamicSupportInfo dynamicCalendar =
                    new DynamicSupportInfo(calendar.getComponentName(),
                            calendar.getStableBackground(), calendar.getDynamicIconDrawCallback());
            calendar.setDynamicIconDrawCallback(mCallbacks);
            DYNAMIC_SUPPORT_INFOS.add(dynamicCalendar);
        }

        if (FeatureOption.SPRD_DYNAMIC_CLOCK_SUPPORT) {
            DynamicDeskclock deskclock = new DynamicDeskclock(mContext);
            DynamicSupportInfo dynamicClock =
                    new DynamicSupportInfo(deskclock.getComponentName(),
                            deskclock.getStableBackground(), deskclock.getDynamicIconDrawCallback());
            deskclock.setDynamicIconDrawCallback(mCallbacks);
            DYNAMIC_SUPPORT_INFOS.add(dynamicClock);
        }

        sDynamicIconInfoNum = DYNAMIC_SUPPORT_INFOS.size();

        if (LogUtils.DEBUG_DYNAMIC_ICON) {
            LogUtils.d(TAG, "loadDynamicIconInfo: sDynamicIconInfoNum = " + sDynamicIconInfoNum
                + ", DYNAMIC_SUPPORT_INFOS = " + DYNAMIC_SUPPORT_INFOS.toString());
        }
    }

    /**
     * SPRD: Draw dynamic part of the dynamic icon if needed.
     * @param canvas the canvas to draw the dynamic icon.
     * @param icon the view on which to draw the dynamic icon.
     * @param scale the scale of the dynamic icon.
     * @param createBitmap whether need create a bitmap or not.
     */
    public static void drawDynamicIconIfNeed(Canvas canvas, View icon, float scale, boolean createBitmap) {
        if (icon instanceof BubbleTextView) {
            ItemInfo info = (ItemInfo) icon.getTag();
            if (info != null) {
                DynamicIconDrawCallback callback = info.dynamicIconDrawCallback;
                if (callback != null) {
                    callback.drawDynamicIcon(canvas, icon, scale, createBitmap);
                }
            }
        }
    }

    public interface DynamicAppChangedCallbacks {
        void bindDynamicIconIfNeeded();
        void bindComponentDynamicIconChanged(ComponentName component);
    }

}
