package com.sprd.launcher3.ext.unreadnotifier;

/**
 * Created by SPREADTRUM\jin.xie on 10/21/16.
 */

import android.Manifest.permission;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.android.launcher3.R;
import com.sprd.launcher3.ext.AppListPreference;
import com.sprd.launcher3.ext.LogUtils;
import com.sprd.launcher3.ext.UnreadLoaderUtils;


/**
 * This fragment shows the unread settings preferences.
 */
public class UnreadSettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener,
        SharedPreferences.OnSharedPreferenceChangeListener, AppListPreference.OnPreferenceCheckBoxClickListener{
    private static final String TAG = "UnreadSettingsFragment";
    private SharedPreferences mSharedPrefs;
    private PackageManager mPm;

    public static final String PREF_KEY_MISS_CALL = "pref_missed_call_count";
    public static final String PREF_KEY_UNREAD_SMS = "pref_unread_sms_count";

    private DefaultPhonePreference mDefaultPhonePref;
    private DefaultSmsPreference   mDefaultSmsPref;

    private Intent mServiceIntent;
    private ServiceHandler mServiceHandler;

    private static final int MSG_PHONE_OBSERVER_CHANGE = 1001;
    private static final int MSG_SMS_OBSERVER_CHANGE = 1002;

    String mPhoneString = "";
    String mSMSString = "";
    String mOldPhoneString = "";
    String mOldSMSString = "";

    public static final int FLAG_PHONE_CHECKED = 0x1;
    public static final int FLAG_SMS_CHECKED = 0x2;
    public static final int FLAG_ALL_CHECKED = 0x3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.unread_settings_preferences);

        HandlerThread ht = new HandlerThread(TAG);
        ht.start();
        mServiceHandler = new ServiceHandler(ht.getLooper());

        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mPm = getActivity().getPackageManager();

        initPreferences();
        loadSettings();
        initCheckboxState();
    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        if (mServiceHandler != null) {
            mServiceHandler.getLooper().quit();
            LogUtils.d(TAG, "HandlerThread has quit");
        }
        super.onDestroy();
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
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(PREF_KEY_MISS_CALL)){
            mOldPhoneString = mPhoneString;
            mPhoneString = sharedPreferences.getString(key, "");

            updateComponentUnreadInfo(mPhoneString, mOldPhoneString, UnreadUtils.TYPE_CALL_LOG);
        } else if(key.equals(PREF_KEY_UNREAD_SMS)) {
            mOldSMSString = mSMSString;
            mSMSString = sharedPreferences.getString(key, "");

            updateComponentUnreadInfo(mSMSString, mOldSMSString, UnreadUtils.TYPE_SMS);
        }

    }

    @Override
    public void onPreferenceCheckboxClick(Preference preference) {
        String key = preference.getKey();
        switch (key) {
            case PREF_KEY_MISS_CALL:
                if(mDefaultPhonePref.isPreferenceChecked()) {
                    if(!UnreadUtils.selfPermissionGranted(getActivity(), UnreadUtils.TypeAndPermission.getType(permission.READ_CALL_LOG))) {
                        requestPermissions(new String[] { permission.READ_CALL_LOG}, UnreadUtils.TYPE_CALL_LOG);
                        return;
                    }
                }
                sendMsgToStartService(MSG_PHONE_OBSERVER_CHANGE, UnreadUtils.TYPE_CALL_LOG);
                break;
            case PREF_KEY_UNREAD_SMS:
                if(mDefaultSmsPref.isPreferenceChecked()) {
                    if(!UnreadUtils.selfPermissionGranted(getActivity(), UnreadUtils.TypeAndPermission.getType(permission.READ_SMS))) {
                        requestPermissions(new String[] { permission.READ_SMS}, UnreadUtils.TYPE_SMS);
                        return;
                    }
                }

                sendMsgToStartService(MSG_SMS_OBSERVER_CHANGE, UnreadUtils.TYPE_SMS);
                break;
        }
    }

    private void updateComponentUnreadInfo(String current, String old, int type) {
        if (LogUtils.DEBUG_UNREAD) {
            LogUtils.d(TAG, "updateComponentUnreadInfo, old String: " + old
                    + ", current String: "+current);
        }
        switch(type) {
            case UnreadUtils.TYPE_CALL_LOG:
                if(mDefaultPhonePref.isPreferenceChecked()) {
                    int unreadPhoneNum = UnreadService.getUnreadNumByType(UnreadUtils.TYPE_CALL_LOG);
                    LogUtils.d(TAG, "unreadPhoneNum = "+unreadPhoneNum);
                    if(unreadPhoneNum != 0) {
                        updateUnreadInfo(unreadPhoneNum, ComponentName.unflattenFromString(current));
                        if(!TextUtils.isEmpty(old)) {
                            updateUnreadInfo(0, ComponentName.unflattenFromString(old));
                        }
                    }
                }
                break;
            case UnreadUtils.TYPE_SMS:
                if(mDefaultSmsPref.isPreferenceChecked()) {
                    int unreadSMSNum = UnreadService.getUnreadNumByType(UnreadUtils.TYPE_SMS);
                    LogUtils.d(TAG, "unreadSMSNum = "+unreadSMSNum);
                    if(unreadSMSNum != 0) {
                        updateUnreadInfo(unreadSMSNum, ComponentName.unflattenFromString(current));
                        if(!TextUtils.isEmpty(old))
                            updateUnreadInfo(0, ComponentName.unflattenFromString(old));
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode) {
            case UnreadUtils.TYPE_CALL_LOG:
                if(grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // permission denied
                    mDefaultPhonePref.setPreferenceChecked(false);
                } else {
                    // permission allowed
                    mDefaultPhonePref.setPreferenceChecked(true);
                }

                sendMsgToStartService(MSG_PHONE_OBSERVER_CHANGE, UnreadUtils.TYPE_CALL_LOG);
                break;
            case UnreadUtils.TYPE_SMS:
                if(grantResults.length != 1 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    // permission denied
                    mDefaultSmsPref.setPreferenceChecked(false);
                } else {
                    // permission allowed
                    mDefaultSmsPref.setPreferenceChecked(true);
                }

                sendMsgToStartService(MSG_SMS_OBSERVER_CHANGE, UnreadUtils.TYPE_SMS);
                break;
        }
    }

    private void sendMsgToStartService(int msg, int type) {
        int flag = 0x0;
        if(mDefaultPhonePref.isPreferenceChecked()) {
            flag |= FLAG_PHONE_CHECKED;
        }
        if(mDefaultSmsPref.isPreferenceChecked()) {
            flag |= FLAG_SMS_CHECKED;
        }
        if (LogUtils.DEBUG_UNREAD) {
            LogUtils.d(TAG, "sendMsgToStartService: flag = "+flag);
        }
        mServiceHandler.sendMessage(mServiceHandler.obtainMessage(msg, type, flag));
    }

    class ServiceHandler extends Handler {
        public ServiceHandler (Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PHONE_OBSERVER_CHANGE:
                case MSG_SMS_OBSERVER_CHANGE:
                    mServiceIntent = new Intent();
                    mServiceIntent.setClass(getActivity(), UnreadService.class);
                    mServiceIntent.putExtra(UnreadUtils.EXTRA_UNREAD_TYPE, msg.arg1);
                    mServiceIntent.putExtra(UnreadUtils.EXTRA_UNREAD_INFO, msg.arg2);
                    getActivity().startService(mServiceIntent);
                    break;
                default:
                    break;

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

    private void initCheckboxState() {
        initPrefCheckboxState(mDefaultPhonePref, UnreadUtils.TYPE_CALL_LOG);
        initPrefCheckboxState(mDefaultSmsPref, UnreadUtils.TYPE_SMS);
    }

    private void initPrefCheckboxState(Preference preference, int type) {
        if(preference == null || mSharedPrefs == null) {
            return;
        }
        if(preference instanceof AppListPreference) {
            if (LogUtils.DEBUG_UNREAD) {
                LogUtils.d(TAG, "initPrefCheckboxState, preference: "+preference
                        + ", permission: " +UnreadUtils.selfPermissionGranted(getActivity(), type));
            }
            if(!UnreadUtils.selfPermissionGranted(getActivity(), type)
                    && ((AppListPreference) preference).isPreferenceChecked()) {
                ((AppListPreference) preference).setPreferenceChecked(false);
            }
        }
    }

    private void loadPrefsSetting(Preference preference, int failSummaryID) {
        if (mSharedPrefs == null || preference == null) {
            return;
        }
        if (preference instanceof AppListPreference) {
            boolean ret = false;
            ApplicationInfo info = null;
            String pkgName = "";
            String listValue = mSharedPrefs.getString(preference.getKey(), null);

            if(preference == mDefaultPhonePref) {
                mPhoneString = listValue;
                mOldPhoneString = mPhoneString;
            } else if( preference == mDefaultSmsPref) {
                mSMSString = listValue;
                mOldSMSString = mSMSString;
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

    public static void initUnreadInfoIfNeeded(Context context) {
        int flag = 0x0;
        if(UnreadUtils.isPersistChecked(context, PREF_KEY_MISS_CALL)) {
            flag |= FLAG_PHONE_CHECKED;
        }
        if(UnreadUtils.isPersistChecked(context, PREF_KEY_UNREAD_SMS)) {
            flag |= FLAG_SMS_CHECKED;
        }
        if (LogUtils.DEBUG_UNREAD) {
            LogUtils.d(TAG, "initUnreadInfoIfNeeded, flag = "+flag);
        }

        boolean isPhonePermissionGranted = UnreadUtils.selfPermissionGranted(context, UnreadUtils.TYPE_CALL_LOG);
        boolean isSMSPermissionGranted = UnreadUtils.selfPermissionGranted(context, UnreadUtils.TYPE_SMS);

        switch(flag) {
            case FLAG_PHONE_CHECKED:
                if(!isPhonePermissionGranted ) {
                    saveUnreadLoaderUtilsNum(context, PREF_KEY_MISS_CALL);
                } else {
                    startServiceIfNeeded(context, flag);
                }
                break;
            case FLAG_SMS_CHECKED:
                if(!isSMSPermissionGranted) {
                    saveUnreadLoaderUtilsNum(context, PREF_KEY_UNREAD_SMS);
                } else {
                    startServiceIfNeeded(context, flag);
                }
                break;
            case FLAG_ALL_CHECKED:
                if(!isPhonePermissionGranted && !isSMSPermissionGranted) {
                    saveUnreadLoaderUtilsNum(context, PREF_KEY_MISS_CALL);
                    saveUnreadLoaderUtilsNum(context, PREF_KEY_UNREAD_SMS);
                } else if(isPhonePermissionGranted && !isSMSPermissionGranted) {
                    saveUnreadLoaderUtilsNum(context, PREF_KEY_UNREAD_SMS);

                    startServiceIfNeeded(context, FLAG_PHONE_CHECKED);

                } else if(isSMSPermissionGranted && !isPhonePermissionGranted) {
                    saveUnreadLoaderUtilsNum(context, PREF_KEY_MISS_CALL);

                    startServiceIfNeeded(context, FLAG_SMS_CHECKED);

                } else {
                    startServiceIfNeeded(context, flag);
                }
                break;
            default:
                break;
        }
    }

    private static void saveUnreadLoaderUtilsNum(Context context, String key){
        //DefaultSharedPreference
        SharedPreferences defaultSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String value = defaultSharedPref.getString(key, "");

        if(!TextUtils.isEmpty(value)) {
            //SharedPreference of UnreadLoaderUtils
            SharedPreferences sharedPref = context.getSharedPreferences(UnreadLoaderUtils.PREFS_FILE_NAME, context.MODE_PRIVATE);
            int num = sharedPref.getInt(value, -1);
            if(num != 0 && num != -1) {
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt(value, 0);
                editor.commit();
            }
        }
    }

    private static void startServiceIfNeeded(Context context, int flag){
        if (LogUtils.DEBUG_UNREAD) {
            LogUtils.d(TAG, "startServiceIfNeeded, flag = "+flag);
        }
        Intent intent = new Intent();
        intent.setClass(context,UnreadService.class);
        intent.putExtra(UnreadUtils.EXTRA_UNREAD_TYPE, UnreadUtils.TYPE_ALL);
        intent.putExtra(UnreadUtils.EXTRA_UNREAD_INFO, flag);
        context.startService(intent);
    }

    private void updateUnreadInfo(int unReadNum, ComponentName componentName) {
        if(LogUtils.DEBUG_UNREAD) {
            LogUtils.d(TAG, "updateUnreadInfo, componentName :   "+componentName
                    + ", unReadNum :  " +unReadNum);
        }
        if(componentName != null) {
            UnreadUtils.sendUnreadBroadcast(getActivity(), unReadNum, componentName);
        }
    }

}