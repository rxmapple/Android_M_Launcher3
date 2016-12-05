package com.sprd.launcher3.ext;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.NinePatchDrawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;

import com.android.launcher3.AppInfo;
import com.android.launcher3.FolderInfo;
import com.android.launcher3.ItemInfo;
import com.android.launcher3.LauncherSettings;
import com.android.launcher3.R;
import com.android.launcher3.ShortcutInfo;
import com.sprd.launcher3.ext.unreadnotifier.UnreadInfoManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rxmapple on 2016/9/27.
 */
class UnreadSupportShortcut {
    public UnreadSupportShortcut(String pkgName, String clsName, String keyString, int type) {
        mComponent = new ComponentName(pkgName, clsName);
        mKey = keyString;
        mShortcutType = type;
        mUnreadNum = 0;
    }

    ComponentName mComponent;
    String mKey;
    int mShortcutType;
    int mUnreadNum;

    @Override
    public String toString() {
        return "{UnreadSupportShortcut[" + mComponent + "], key = " + mKey + ",type = "
                + mShortcutType + ",unreadNum = " + mUnreadNum + "}";
    }
}

/**
 * This class is a util class, implemented to do the following two things,:
 *
 * 1.Read config xml to get the shortcuts which support displaying unread number,
 * then get the initial value of the unread number of each component and update
 * shortcuts and folders through callbacks implemented in Launcher.
 *
 * 2. Receive unread broadcast sent by application, update shortcuts and folders in
 * workspace, hot seat and update application icons in app customize paged view.
 */
public class UnreadLoaderUtils extends BroadcastReceiver {
    private static final String TAG = "UnreadLoaderUtils";

    public static final String PREFS_FILE_NAME = TAG + "_Pref";

    private static final int UNREAD_TYPE_INTERNAL = 0;
    private static final int UNREAD_TYPE_EXTERNAL = 1;

    private static final int INVALID_NUM = -1;

    public static final String ACTION_UNREAD_CHANGED = "com.sprd.action.UNREAD_CHANGED";
    private static final String EXTRA_UNREAD_COMPONENT = "com.sprd.intent.extra.UNREAD_COMPONENT";
    private static final String EXTRA_UNREAD_NUMBER = "com.sprd.intent.extra.UNREAD_NUMBER";

    private static final int MAX_UNREAD_COUNT = 999;

    private static final ArrayList<UnreadSupportShortcut> UNREAD_SUPPORT_SHORTCUTS =
            new ArrayList<>();

    private static int sUnreadSupportShortcutsNum = 0;
    private static final Object LOG_LOCK = new Object();

    private Context mContext;

    private SharedPreferences mSharePrefs;

    private static UnreadLoaderUtils INSTANCE;

    private WeakReference<UnreadCallbacks> mCallbacks;

    private UnreadLoaderUtils(Context context) {
        mContext = context;
        mSharePrefs = mContext.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
    }

    public static UnreadLoaderUtils getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new UnreadLoaderUtils(context);
        }
        return INSTANCE;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String action = intent.getAction();
        if (ACTION_UNREAD_CHANGED.equals(action)) {
            final ComponentName componentName = intent.getParcelableExtra(EXTRA_UNREAD_COMPONENT);
            final int unreadNum = intent.getIntExtra(EXTRA_UNREAD_NUMBER, INVALID_NUM);
            if (LogUtils.DEBUG_UNREAD) {
                LogUtils.d(TAG, "Receive unread broadcast: componentName = " + componentName
                        + ", unreadNum = " + unreadNum + ", mCallbacks = " + mCallbacks
                        + getUnreadSupportShortcutInfo());
            }

            updateComponentUnreadInfos(unreadNum , componentName);

        }
    }

    public void updateComponentUnreadInfos(int unreadNum, ComponentName componentName) {
        if (mCallbacks != null && componentName != null && unreadNum != INVALID_NUM) {
            final int index = supportUnreadFeature(componentName);
            String key = componentName.flattenToShortString();
            if (index >= 0) {
                boolean ret = setUnreadNumberAt(index, unreadNum, key);
                if (ret) {
                    final UnreadCallbacks callbacks = mCallbacks.get();
                    if (callbacks != null) {
                        callbacks.bindComponentUnreadChanged(componentName, unreadNum);
                    }
                }
            } else {
                addComponentToSupportList(componentName, unreadNum);
            }
        }
    }

    /**
     * Set this as the current Launcher activity object for the loader.
     */
    public void initialize(UnreadCallbacks callbacks) {
        mCallbacks = new WeakReference<>( callbacks );
        if (LogUtils.DEBUG_UNREAD) {
            LogUtils.d(TAG, "initialize: callbacks = " + callbacks
                    + ", mCallbacks = " + mCallbacks);
        }

        UnreadInfoManager.getInstance(mContext).init();

        loadAndInitUnreadShortcuts();
    }

    /**
     * Load and initialize unread shortcuts.
     */
    public void loadAndInitUnreadShortcuts() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... unused) {
                loadUnreadSupportShortcuts();
                return null;
            }

            @Override
            protected void onPostExecute(final Void result) {
                if (mCallbacks != null) {
                    UnreadCallbacks callbacks = mCallbacks.get();
                    if (callbacks != null) {
                        callbacks.bindUnreadInfoIfNeeded();
                    }
                }
            }
        }.execute();
    }

    private void addComponentToSupportList(final ComponentName component, final int unReadNum) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... unused) {
                String pkgName = component.getPackageName();
                String clsName = component.getClassName();
                String key = component.flattenToShortString();

                UnreadSupportShortcut usShortcut = new UnreadSupportShortcut( pkgName,clsName,
                        key,UNREAD_TYPE_EXTERNAL);
                if (!UNREAD_SUPPORT_SHORTCUTS.contains( usShortcut )) {
                    usShortcut.mUnreadNum = unReadNum;

                    if (saveUnreadNum(key, unReadNum)) {
                        UNREAD_SUPPORT_SHORTCUTS.add( usShortcut );
                        sUnreadSupportShortcutsNum = UNREAD_SUPPORT_SHORTCUTS.size();
                        if (LogUtils.DEBUG_UNREAD) {
                            LogUtils.d(TAG, "addComponentToSupportList, key:" + key + " success.");
                        }
                        return true;
                    } else {
                        LogUtils.e(TAG, "save key:" + key + " error!");
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                if (aBoolean) {
                    final UnreadCallbacks callbacks = mCallbacks.get();
                    if (callbacks != null) {
                        callbacks.bindComponentUnreadChanged(component, unReadNum);
                    }
                }
                super.onPostExecute( aBoolean );
            }
        }.execute();
    }

    private int loadUnreadNum(final String key) {
        return mSharePrefs.getInt(key, INVALID_NUM);
    }

    private boolean saveUnreadNum(final String key, final int unReadNum) {
        SharedPreferences.Editor editor = mSharePrefs.edit();
        editor.putInt(key, unReadNum);
        return editor.commit();
    }

    private static List<ResolveInfo> getLauncherActivities(PackageManager pm, String pkgName) {
        final Intent mainIntent = new Intent(Intent.ACTION_MAIN);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        if (!TextUtils.isEmpty(pkgName)) {
            mainIntent.setPackage(pkgName);
        }
        return pm.queryIntentActivities(mainIntent, 0);
    }

    private void loadUnreadSupportShortcuts() {
        long start = System.currentTimeMillis();
        if (LogUtils.DEBUG_UNREAD) {
            LogUtils.d(TAG, "loadUnreadSupportShortcuts begin: start = " + start);
        }

        // Clear all previous parsed unread shortcuts.
        UNREAD_SUPPORT_SHORTCUTS.clear();

        List<ResolveInfo> list = getLauncherActivities(mContext.getPackageManager(), null);
        final int count = list.size();
        for (int i=0; i<count; i++) {
            ResolveInfo info = list.get(i);
            if (info.activityInfo == null) {
                continue;
            }

            ComponentName cpName = new ComponentName(info.activityInfo.packageName,
                    info.activityInfo.name);
            String key = cpName.flattenToShortString();
            int loadNum = loadUnreadNum(key);

            if (loadNum != INVALID_NUM) {
                UnreadSupportShortcut usShortcut = new UnreadSupportShortcut(
                        info.activityInfo.packageName, info.activityInfo.name,
                        key, UNREAD_TYPE_INTERNAL);
                usShortcut.mUnreadNum = loadNum;
                if (!UNREAD_SUPPORT_SHORTCUTS.contains( usShortcut )) {
                    UNREAD_SUPPORT_SHORTCUTS.add( usShortcut );
                }
            }
        }
        sUnreadSupportShortcutsNum = UNREAD_SUPPORT_SHORTCUTS.size();

        if (LogUtils.DEBUG_UNREAD) {
            LogUtils.d(TAG, "loadUnreadSupportShortcuts end: time used = "
                    + (System.currentTimeMillis() - start) + ",sUnreadSupportShortcutsNum = "
                    + sUnreadSupportShortcutsNum + getUnreadSupportShortcutInfo());
        }
    }

    /**
     * Get unread support shortcut information, since the information are stored
     * in an array list, we may query it and modify it at the same time, a lock
     * is needed.
     *
     * @return SupportShortString
     */
    private static String getUnreadSupportShortcutInfo() {
        String info = " Unread support shortcuts are ";
        synchronized (LOG_LOCK) {
            info += UNREAD_SUPPORT_SHORTCUTS.toString();
        }
        return info;
    }

    /**
     * Whether the given component support unread feature.
     *
     * @param component component
     * @return array index, find fail return INVALID_NUM
     */
    static int supportUnreadFeature(ComponentName component) {
        if (component == null) {
            return INVALID_NUM;
        }

        final int size = UNREAD_SUPPORT_SHORTCUTS.size();
        for (int i = 0; i < size; i++) {
            if (UNREAD_SUPPORT_SHORTCUTS.get(i).mComponent.equals(component)) {
                return i;
            }
        }

        return INVALID_NUM;
    }

    /**
     * Set the unread number of the item in the list with the given unread number.
     *
     * @param index
     * @param unreadNum
     * @return
     */
    synchronized boolean setUnreadNumberAt(int index, int unreadNum, String key) {
        if (index >= 0 || index < sUnreadSupportShortcutsNum) {
            if (LogUtils.DEBUG_UNREAD) {
                LogUtils.d(TAG, "setUnreadNumberAt: index = " + index
                        + ",unreadNum = " + unreadNum + getUnreadSupportShortcutInfo());
            }
            if (UNREAD_SUPPORT_SHORTCUTS.get(index).mUnreadNum != unreadNum) {
                UNREAD_SUPPORT_SHORTCUTS.get(index).mUnreadNum = unreadNum;
                saveUnreadNum(key, unreadNum);
                return true;
            }
        }
        return false;
    }

    /**
     * Get unread number of application at the given position in the supported
     * shortcut list.
     *
     * @param index
     * @return
     */
    static synchronized int getUnreadNumberAt(int index) {
        if (index < 0 || index >= sUnreadSupportShortcutsNum) {
            return 0;
        }
        if (LogUtils.DEBUG_UNREAD) {
            LogUtils.d(TAG, "getUnreadNumberAt: index = " + index
                    + getUnreadSupportShortcutInfo());
        }
        return UNREAD_SUPPORT_SHORTCUTS.get(index).mUnreadNum;
    }

    /**
     * Get unread number for the given component.
     *
     * @param component
     * @return
     */
    public static int getUnreadNumberOfComponent(ComponentName component) {
        final int index = supportUnreadFeature(component);
        return getUnreadNumberAt(index);
    }

    /**
     * Draw unread number for the given icon.
     *
     * @param canvas
     * @param icon
     * @return
     */
    public static void drawUnreadEventIfNeed(Canvas canvas, View icon) {
        ItemInfo info = (ItemInfo) icon.getTag();

        if (info != null && info.unreadNum > 0) {
            Resources res = icon.getContext().getResources();

            /// SPRD: Meature sufficent width for unread text and background image
            Paint unreadTextNumberPaint = new Paint();
            unreadTextNumberPaint.setTextSize(res.getDimension(R.dimen.unread_text_number_size));
            unreadTextNumberPaint.setTypeface(Typeface.DEFAULT_BOLD);
            unreadTextNumberPaint.setColor(0xffffffff);
            unreadTextNumberPaint.setTextAlign(Paint.Align.CENTER);

            Paint unreadTextPlusPaint = new Paint(unreadTextNumberPaint);
            unreadTextPlusPaint.setTextSize(res.getDimension(R.dimen.unread_text_plus_size));

            String unreadTextNumber;
            String unreadTextPlus = "+";
            Rect unreadTextNumberBounds = new Rect(0, 0, 0, 0);
            Rect unreadTextPlusBounds = new Rect(0, 0, 0, 0);
            if (info.unreadNum > MAX_UNREAD_COUNT) {
                unreadTextNumber = String.valueOf(MAX_UNREAD_COUNT);
                unreadTextPlusPaint.getTextBounds(unreadTextPlus, 0,
                        unreadTextPlus.length(), unreadTextPlusBounds);
            } else {
                unreadTextNumber = String.valueOf(info.unreadNum);
            }
            unreadTextNumberPaint.getTextBounds(unreadTextNumber, 0,
                    unreadTextNumber.length(), unreadTextNumberBounds);
            int textHeight = unreadTextNumberBounds.height();
            int textWidth = unreadTextNumberBounds.width() + unreadTextPlusBounds.width();

            /// SPRD: Draw unread background image.
            NinePatchDrawable unreadBgNinePatchDrawable =
                    (NinePatchDrawable) ContextCompat.getDrawable(icon.getContext(),
                            R.drawable.ic_newevent_bg );
            int unreadBgWidth = unreadBgNinePatchDrawable.getIntrinsicWidth();
            int unreadBgHeight = unreadBgNinePatchDrawable.getIntrinsicHeight();

            int unreadMinWidth = (int) res.getDimension(R.dimen.unread_minWidth);
            if (unreadBgWidth < unreadMinWidth) {
                unreadBgWidth = unreadMinWidth;
            }
            int unreadTextMargin = (int) res.getDimension(R.dimen.unread_text_margin);
            if (unreadBgWidth < textWidth + unreadTextMargin) {
                unreadBgWidth = textWidth + unreadTextMargin;
            }
            if (unreadBgHeight < textHeight) {
                unreadBgHeight = textHeight;
            }
            Rect unreadBgBounds = new Rect(0, 0, unreadBgWidth, unreadBgHeight);
            unreadBgNinePatchDrawable.setBounds(unreadBgBounds);

            int unreadMarginTop = 0;
            int unreadMarginRight = 0;
            if (info instanceof ShortcutInfo) {
                if (info.container == (long) LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                    unreadMarginTop = (int) res.getDimension(R.dimen.hotseat_unread_margin_top);
                    unreadMarginRight = (int) res.getDimension(R.dimen.hotseat_unread_margin_right);
                } else if (info.container == (long) LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                    unreadMarginTop = (int) res.getDimension(R.dimen.workspace_unread_margin_top);
                    unreadMarginRight = (int) res.getDimension(
                            R.dimen.workspace_unread_margin_right);
                } else {
                    unreadMarginTop = (int) res.getDimension(R.dimen.folder_unread_margin_top);
                    unreadMarginRight = (int) res.getDimension(R.dimen.folder_unread_margin_right);
                }
            } else if (info instanceof FolderInfo) {
                if (info.container == (long) LauncherSettings.Favorites.CONTAINER_HOTSEAT) {
                    unreadMarginTop = (int) res.getDimension(R.dimen.hotseat_unread_margin_top);
                    unreadMarginRight = (int) res.getDimension(R.dimen.hotseat_unread_margin_right);
                } else if (info.container == (long) LauncherSettings.Favorites.CONTAINER_DESKTOP) {
                    unreadMarginTop = (int) res.getDimension(R.dimen.workspace_unread_margin_top);
                    unreadMarginRight = (int) res.getDimension(
                            R.dimen.workspace_unread_margin_right);
                }
            } else if (info instanceof AppInfo) {
                unreadMarginTop = (int) res.getDimension(R.dimen.app_list_unread_margin_top);
                unreadMarginRight = (int) res.getDimension(R.dimen.app_list_unread_margin_right);
            }

            int unreadBgPosX = icon.getScrollX() + icon.getWidth()
                    - unreadBgWidth - unreadMarginRight;
            int unreadBgPosY = icon.getScrollY() + unreadMarginTop;

            canvas.save();
            canvas.translate(unreadBgPosX, unreadBgPosY);

            unreadBgNinePatchDrawable.draw(canvas);

            /// SPRD: Draw unread text.
            Paint.FontMetrics fontMetrics = unreadTextNumberPaint.getFontMetrics();
            if (info.unreadNum > MAX_UNREAD_COUNT) {
                canvas.drawText(unreadTextNumber,
                        (unreadBgWidth - unreadTextPlusBounds.width()) / 2,
                        (unreadBgHeight + textHeight) / 2,
                        unreadTextNumberPaint);
                canvas.drawText(unreadTextPlus,
                        (unreadBgWidth + unreadTextNumberBounds.width()) / 2,
                        (unreadBgHeight + textHeight) / 2 + fontMetrics.ascent / 2,
                        unreadTextPlusPaint);
            } else {
                canvas.drawText(unreadTextNumber,
                        unreadBgWidth / 2,
                        (unreadBgHeight + textHeight) / 2,
                        unreadTextNumberPaint);
            }

            canvas.restore();
        }
    }

    public interface UnreadCallbacks {
        /**
         * Bind shortcuts and application icons with the given component, and
         * update folders unread which contains the given component.
         *
         * @param component
         * @param unreadNum
         */
        void bindComponentUnreadChanged(ComponentName component, int unreadNum);

        /**
         * Bind unread shortcut information if needed, this call back is used to
         * update shortcuts and folders when launcher first created.
         */
        void bindUnreadInfoIfNeeded();
    }
}

