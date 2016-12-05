package com.sprd.launcher3.ext.unreadnotifier;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.text.TextUtils;
import android.widget.Toast;

import com.android.launcher3.R;
import com.sprd.launcher3.ext.AppListPreference;
import com.sprd.launcher3.ext.LogUtils;
import com.sprd.launcher3.ext.unreadnotifier.call.DefaultPhonePreference;
import com.sprd.launcher3.ext.unreadnotifier.call.MissCallItem;
import com.sprd.launcher3.ext.unreadnotifier.sms.DefaultSmsPreference;
import com.sprd.launcher3.ext.unreadnotifier.sms.UnreadMessageItem;

/**
 * Created by SPRD on 10/21/16.
 */

/**
 * This fragment shows the unread settings preferences.
 */
public class UnreadSettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener,
        AppListPreference.OnPreferenceCheckBoxClickListener{
    private static final String TAG = "UnreadSettingsFragment";
    private Context mContext;
    private PackageManager mPm;

    public static final String PREF_KEY_MISS_CALL = "pref_missed_call_count";
    public static final String PREF_KEY_UNREAD_SMS = "pref_unread_sms_count";

    private DefaultPhonePreference mDefaultPhonePref;
    private DefaultSmsPreference mDefaultSmsPref;

    private MissCallItem mMissCallItem;
    private UnreadMessageItem mUnreadSMSItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.unread_settings_preferences);

        mContext = getActivity();
        mPm = mContext.getPackageManager();

        mMissCallItem = (MissCallItem) UnreadInfoManager.getInstance(mContext).getItemByKey(PREF_KEY_MISS_CALL);
        mUnreadSMSItem = (UnreadMessageItem) UnreadInfoManager.getInstance(mContext).getItemByKey(PREF_KEY_UNREAD_SMS);

        initPreferences();
        loadSettings();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(LogUtils.DEBUG_UNREAD) {
            LogUtils.d(TAG, "onStop");
        }

        updateUnreadItemInfo(mMissCallItem, mDefaultPhonePref);
        updateUnreadItemInfo(mUnreadSMSItem, mDefaultSmsPref);

    }

    public void updateUnreadItemInfo(UnreadBaseItem item, AppListPreference preference) {
        //get the initial value and state
        boolean oldState = preference.isChecked;
        String oldValue = preference.initValue;
        boolean isGranted = preference.isPermissionGranted;

        //get the current value and state
        boolean currentState = item.isPersistChecked();
        String currentValue = item.getCurrentComponentName().flattenToShortString();
        boolean isCurrentGranted = item.checkPermission(item.mPermission);

        if(LogUtils.DEBUG_UNREAD) {
            LogUtils.d(TAG, "item = "+item
                    + ", oldSate = "+oldValue + ", oldValue = "+oldValue + ", isGranted = "+isGranted
                    + ",currentState = "+ currentState + "currentValue = "+ currentValue + ", isCurrentGranted = "+ isCurrentGranted);
        }

        if(!isCurrentGranted) {
            return;
        }

        if((currentState != oldState)) {
            //checkbox state changed
            if(currentState ) {
                item.mContentObserver.registerContentObserver();
                item.updateUnreadInfo();
            } else {
                item.mContentObserver.unregisterContentObserver();
                item.updateInfo(0, ComponentName.unflattenFromString(oldValue));
            }
        } else {
            if(currentState) {
                //clear the unread info on the old icon.
                if(!oldValue.equals(currentValue)) {
                    item.updateInfo(0, ComponentName.unflattenFromString(oldValue));
                }

                if(!isGranted) {
                    item.mContentObserver.registerContentObserver();
                    item.updateUnreadInfo();
                } else {
                    if(!oldValue.equals(currentValue)) {
                        item.updateInfo(item.mUnreadCount, ComponentName.unflattenFromString(currentValue));
                    }
                }
            }
        }

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        boolean result = true;
        final String key = preference.getKey();
        if(PREF_KEY_MISS_CALL.equals(key)) {
            String listValue = (String) newValue;
            if (TextUtils.isEmpty(listValue)) {
                result = false;
            }
            mDefaultPhonePref.setValue(listValue);
            mDefaultPhonePref.setSummary(((ListPreference) preference).getEntry());
        } else if(PREF_KEY_UNREAD_SMS.equals(key)) {
            String listValue = (String) newValue;
            if (TextUtils.isEmpty(listValue)) {
                result = false;
            }
            mDefaultSmsPref.setValue(listValue);
            mDefaultSmsPref.setSummary(((ListPreference) preference).getEntry());
        }

        return result;
    }

    @Override
    public void onPreferenceCheckboxClick(Preference preference) {
        String key = preference.getKey();
        if(LogUtils.DEBUG_UNREAD) {
            LogUtils.d(TAG, "onPreferenceCheckboxClick, key is: "+ key);
        }

        UnreadBaseItem item = UnreadInfoManager.getInstance(mContext).getItemByKey(key);

        if (item != null) {
            if (item.isPersistChecked() && !item.checkPermission(item.mPermission)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[] {item.mPermission}, item.mType);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(LogUtils.DEBUG_UNREAD) {
            LogUtils.d(TAG, "onRequestPermissionsResult, requestCode: " + requestCode + ", permissions:" + permissions+ "grantResults: "+grantResults.length);
        }

        UnreadBaseItem item = UnreadInfoManager.getInstance(mContext).getItemByType(requestCode);
        if(item != null) {
            if (grantResults.length == 1) {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(mContext, item.mUnreadHintString, Toast.LENGTH_LONG).show();
                }
            } else {
                LogUtils.e(TAG, "grantResult length error.");
            }
        }

    }

    private void initPreferences() {
        mDefaultPhonePref = (DefaultPhonePreference) findPreference(PREF_KEY_MISS_CALL);
        setPreferenceListener(mDefaultPhonePref);

        mDefaultSmsPref = (DefaultSmsPreference) findPreference(PREF_KEY_UNREAD_SMS);
        setPreferenceListener(mDefaultSmsPref);

        mDefaultPhonePref.setOnPreferenceCheckBoxClickListener(this);
        mDefaultSmsPref.setOnPreferenceCheckBoxClickListener(this);
    }

    private void setPreferenceListener(Preference preference) {
        if (preference != null) {
            preference.setOnPreferenceChangeListener(this);
        }
    }

    private boolean loadSettings() {
        loadPrefsSetting(mDefaultPhonePref, R.string.pref_missed_call_count_summary);
        loadPrefsSetting(mDefaultSmsPref, R.string.pref_unread_sms_count_summary);
        return true;
    }

    private void loadPrefsSetting(Preference preference, int failSummaryID) {
        if (preference == null) {
            return;
        }

        boolean ret = false;
        ApplicationInfo info = null;
        String pkgName = "";
        String listValue = "";

        if(preference == mDefaultPhonePref) {
            listValue = mMissCallItem.getCurrentComponentName().flattenToShortString();
        } else if( preference == mDefaultSmsPref) {
            listValue = mUnreadSMSItem.getCurrentComponentName().flattenToShortString();
        }

        if (!TextUtils.isEmpty(listValue)) {
            try {
                pkgName = listValue.substring(0, listValue.indexOf("/"));
                info = mPm.getApplicationInfo(pkgName, 0);
                ret = info != null;
            } catch (PackageManager.NameNotFoundException e) {
                LogUtils.w(TAG, "loadPrefsSetting, get app failed, e:" + e);
            }
        }
        preference.setSummary(ret ? info.loadLabel(mPm) : getString(failSummaryID));
    }

}