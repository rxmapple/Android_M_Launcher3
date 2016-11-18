package com.sprd.launcher3.ext;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;

import com.android.launcher3.BubbleTextView;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.Folder;
import com.android.launcher3.FolderIcon;
import com.android.launcher3.FolderInfo;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.Launcher;
import com.android.launcher3.ShortcutAndWidgetContainer;
import com.android.launcher3.ShortcutInfo;
import com.android.launcher3.Workspace;
import com.android.launcher3.allapps.AllAppsContainerView;
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

    public static final float STABLE_SCALE = 1.0f;

    private static DynamicIconUtils INSTANCE;
    private static ArrayList<DynamicSupportInfo> DYNAMIC_SUPPORT_INFOS =
            new ArrayList<>();
    private static final int INVALID_NUM = -1;
    private static int sDynamicIconInfoNum = 0;

    private Launcher mLauncher;
    private Workspace mWorkspace;
    private AllAppsContainerView mAppsView;
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
    public void initialize(Launcher launcher) {
        mLauncher = launcher;
        mWorkspace = launcher.getWorkspace();
        mAppsView = launcher.getAppsView();
        if (launcher instanceof DynamicAppChangedCallbacks) {
            mCallbacks = new WeakReference<>((DynamicAppChangedCallbacks)launcher);
            if (LogUtils.DEBUG_DYNAMIC_ICON) {
                LogUtils.d(TAG, "initialize: launcher = " + launcher
                        + ", mCallbacks = " + mCallbacks);
            }
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
    public static DynamicIconDrawCallback getDIDCForComponent(ComponentName componentName) {
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
            calendar.setOffsetY(mLauncher.getDeviceProfile().iconSizePx);
            DYNAMIC_SUPPORT_INFOS.add(dynamicCalendar);
        }

        if (FeatureOption.SPRD_DYNAMIC_CLOCK_SUPPORT) {
            DynamicDeskclock deskclock = new DynamicDeskclock(mContext);
            DynamicSupportInfo dynamicClock =
                    new DynamicSupportInfo(deskclock.getComponentName(),
                            deskclock.getStableBackground(), deskclock.getDynamicIconDrawCallback());
            deskclock.setDynamicIconDrawCallback(mCallbacks);
            deskclock.setOffsetY(mLauncher.getDeviceProfile().iconSizePx);
            DYNAMIC_SUPPORT_INFOS.add(dynamicClock);
        }

        sDynamicIconInfoNum = DYNAMIC_SUPPORT_INFOS.size();

        if (LogUtils.DEBUG_DYNAMIC_ICON) {
            LogUtils.d(TAG, "loadDynamicIconInfo: sDynamicIconInfoNum = " + sDynamicIconInfoNum
                + ", DYNAMIC_SUPPORT_INFOS = " + DYNAMIC_SUPPORT_INFOS.toString());
        }
    }

    public void bindComponentDynamicIconChanged(final ComponentName component) {
        if (mWorkspace != null) {
            updateComponentDynamicIconChanged(component);
        }

        if (mAppsView != null && mLauncher.isAppsViewVisible()) {
            mAppsView.updateAppsUnreadAndDynamicIconChanged(component, INVALID_NUM);
        }
    }

    /**
     * SPRD: Update dynamic icon of shortcuts and folders in workspace and hotseat
     * with the given component.
     * @param component the component name of the app.
     */
    public void updateComponentDynamicIconChanged(ComponentName component) {
        if (LogUtils.DEBUG_DYNAMIC_ICON) {
            LogUtils.d(TAG, "updateComponentDynamicIconChanged: component = " + component);
        }
        final ArrayList<ShortcutAndWidgetContainer> childrenLayouts =
                mWorkspace.getAllShortcutAndWidgetContainers();
        int childCount = 0;
        View view = null;
        Object tag = null;
        for (ShortcutAndWidgetContainer layout : childrenLayouts) {
            childCount = layout.getChildCount();
            for (int j = 0; j < childCount; j++) {
                view = layout.getChildAt(j);

                if (view != null) {
                    tag = view.getTag();
                } else {
                    if (LogUtils.DEBUG_DYNAMIC_ICON) {
                        LogUtils.d(TAG, "updateComponentDynamicIconChanged: view is null pointer");
                    }
                    continue;
                }
                /// SPRD.
                if (LogUtils.DEBUG_DYNAMIC_ICON) {
                    LogUtils.d(TAG, "updateComponentDynamicIconChanged: component = " + component
                            + ",tag = " + tag + ",j = " + j + ",view = " + view);
                }
                if (tag instanceof ShortcutInfo) {
                    final ShortcutInfo info = (ShortcutInfo) tag;
                    final Intent intent = info.intent;
                    final ComponentName componentName = intent.getComponent();
                    if (LogUtils.DEBUG_DYNAMIC_ICON) {
                        LogUtils.d(TAG, "updateComponentDynamicIconChanged 1: find component = "
                                + component + ",intent = " + intent + ",componentName = "
                                + componentName);
                    }
                    if (componentName != null && componentName.equals(component)) {
                        LogUtils.d(TAG, "updateComponentDynamicIconChanged 2: find component = "
                                + component + ",tag = " + tag + ",j = " + j + ",cellX = "
                                + info.cellX + ",cellY = " + info.cellY);
                        ((BubbleTextView) view).invalidate();
                    }
                }
            }
        }

        /// SPRD: Update shortcut within folder if open folder exists.
        Folder openFolder = mWorkspace.getOpenFolder();
        updateContentDynamicIcon(openFolder, component);
    }

    /**
     * SPRD: Update dynamic icon of the content shortcut.
     */
    private void updateContentDynamicIcon(Folder folder, ComponentName component) {
        if (folder == null) {
            return;
        }
        if (LogUtils.DEBUG_DYNAMIC_ICON) {
            LogUtils.d(TAG, "Folder updateContentDynamicIcon: folder.getInfo() = " + folder.getInfo());
        }
        final ArrayList<ShortcutAndWidgetContainer> childrenLayouts =
                folder.getAllShortcutContainersInFolder();
        int childCount = 0;
        View view = null;
        Object tag = null;

        for (ShortcutAndWidgetContainer layout : childrenLayouts) {
            childCount = layout.getChildCount();
            for (int j = 0; j < childCount; j++) {
                view = layout.getChildAt(j);
                tag = view.getTag();
                if (LogUtils.DEBUG_DYNAMIC_ICON) {
                    LogUtils.d(TAG, "updateShortcutsAndFoldersUnread: tag = " + tag + ", j = "
                            + j + ", view = " + view);
                }
                if (tag instanceof ShortcutInfo) {
                    final ShortcutInfo info = (ShortcutInfo) tag;
                    final Intent intent = info.intent;
                    final ComponentName componentName = intent.getComponent();
                    if (LogUtils.DEBUG_DYNAMIC_ICON) {
                        LogUtils.d(TAG, "updateShortcutsAndFoldersUnread:find component = " + component
                                + ", ,intent = " + intent + ", componentName = " + componentName);
                    }
                    if (componentName != null && componentName.equals(component)) {
                        ((BubbleTextView) view).invalidate();
                    }
                }
            }
        }
    }

    /**
     * SPRD: Update dynamic icon  of shortcuts in workspace and hotseat.
     */
    public void updateShortcutsAndFoldersDynamicIcon() {
        if (mWorkspace == null) {
            return;
        }
        if (LogUtils.DEBUG_DYNAMIC_ICON) {
            LogUtils.d(TAG, "updateShortcutsAndFoldersDynamicIcon: this = " + this);
        }
        final ArrayList<ShortcutAndWidgetContainer> childrenLayouts =
                mWorkspace.getAllShortcutAndWidgetContainers();
        int childCount = 0;
        View view = null;
        Object tag = null;
        for (ShortcutAndWidgetContainer layout : childrenLayouts) {
            childCount = layout.getChildCount();
            for (int j = 0; j < childCount; j++) {
                view = layout.getChildAt(j);
                tag = view.getTag();
                if (LogUtils.DEBUG_DYNAMIC_ICON) {
                    LogUtils.d(TAG, "updateShortcutsAndFoldersDynamicIcon: tag = " + tag + ", j = "
                            + j + ", view = " + view);
                }
                if (tag instanceof ShortcutInfo) {
                    final ShortcutInfo info = (ShortcutInfo) tag;
                    final Intent intent = info.intent;
                    final ComponentName componentName = intent.getComponent();
                    info.dynamicIconDrawCallback = getDIDCForComponent(componentName);
                    ((BubbleTextView) view).invalidate();
                } else if (tag instanceof FolderInfo) {
                    updateFolderDynamicIcon((FolderIcon) view);
                }
            }
        }
    }

    /**
     * SPRD: Update dynamic icon of the items in the folder.
     */
    private void updateFolderDynamicIcon(FolderIcon folderIcon) {
        final ArrayList<ShortcutInfo> contents = folderIcon.getFolderInfo().contents;
        final int contentsCount = contents.size();
        ShortcutInfo shortcutInfo = null;
        ComponentName componentName = null;
        DynamicIconDrawCallback drawCallback = null;
        for (int i = 0; i < contentsCount; i++) {
            shortcutInfo = contents.get(i);
            componentName = shortcutInfo.intent.getComponent();
            drawCallback = getDIDCForComponent(componentName);
            if (drawCallback != null) {
                shortcutInfo.dynamicIconDrawCallback = drawCallback;
            }
        }
        folderIcon.invalidate();
        if (LogUtils.DEBUG_DYNAMIC_ICON) {
            LogUtils.d(TAG, "updateFolderDynamicIcon end");
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
