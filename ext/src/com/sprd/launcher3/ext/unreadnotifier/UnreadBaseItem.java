package com.sprd.launcher3.ext.unreadnotifier;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.sprd.launcher3.ext.AppListPreference;
import com.sprd.launcher3.ext.LogUtils;
import com.sprd.launcher3.ext.UnreadLoaderUtils;

/**
 * Created by SPRD on 2016/11/22.
 */

public abstract class UnreadBaseItem {
    private static final String TAG = "UnreadBaseItem";
    public int mType;
    public int mUnreadCount;
    public BaseContentObserver mContentObserver;
    public String mPermission;
    public String mPrefKey;
    public Launcher mLauncher;
    public ComponentName mDefaultCn;
    public String mUnreadHintString;

    public UnreadBaseItem(Launcher launcher) {
        mLauncher = launcher;
    }

    public boolean checkPermission(String permission) {
        boolean isChecked = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            isChecked = mLauncher.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }
        return isChecked;
    }

    public boolean isPersistChecked() {
        SharedPreferences sharePref = PreferenceManager.getDefaultSharedPreferences(mLauncher);
        return sharePref.getBoolean(mPrefKey + AppListPreference.CHECKBOX_KEY, mLauncher.getResources().getBoolean(R.bool.config_default_unread_enable));
    }

    public void resetUnreadLoaderUtilsNum() {
        String value = getCurrentComponentName().flattenToShortString();

        if (!TextUtils.isEmpty(value)) {
            //SharedPreference of UnreadLoaderUtils
            SharedPreferences sharedPref = mLauncher.getSharedPreferences(UnreadLoaderUtils.PREFS_FILE_NAME, Context.MODE_PRIVATE);
            int num = sharedPref.getInt(value, -1);
            if (num != 0 && num != -1) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(value, 0);
                editor.commit();
            }
        }
    }

    /**
     * Send broadcast to update the unread info.
     */
    public void updateUnreadInfo() {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                return getUnreadCount();
            }

            @Override
            protected void onPostExecute(Integer unReadNum) {
                ComponentName componentName = getCurrentComponentName();
                updateInfo(unReadNum, componentName);
            }
        }.execute();

    }

    public ComponentName getCurrentComponentName() {
        ComponentName componentName = null;
        String value = PreferenceManager.getDefaultSharedPreferences(mLauncher).getString(mPrefKey, "");
        if (!TextUtils.isEmpty(value)) {
            componentName = ComponentName.unflattenFromString(value);
        } else {
            componentName = mDefaultCn;
        }

        return componentName;
    }

    public void updateInfo(int unreadCount, ComponentName componentName) {
        if(LogUtils.DEBUG_UNREAD) {
            LogUtils.d(TAG, "updateInfo, unreadCount = "+unreadCount + ", componentName = "+componentName);
        }
        if (componentName != null) {
            UnreadLoaderUtils.getInstance(mLauncher).updateComponentUnreadInfos(unreadCount, componentName);
        }
    }

    public abstract int getUnreadCount();
}
