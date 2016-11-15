package com.sprd.launcher3.ext.unreadnotifier;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.CallLog;
import android.text.TextUtils;

import com.sprd.launcher3.ext.LogUtils;


/**
 * Created by SPREADTRUM\jin.xie on 10/11/16.
 */

public class UnreadService extends Service{
    private static final String TAG = "UnreadService";
    private final Uri MMSSMS_CONTENT_URI = Uri.parse("content://mms-sms");
    private final Uri CALLS_CONTENT_URI = CallLog.Calls.CONTENT_URI;
    private ContentObserver mUnreadCallContentObserver;
    private ContentObserver mUnreadMMSContentObserver;

    public static int mUnreadPhoneNum;
    public static int mUnreadSMSNum;
    private SharedPreferences mSharedPrefs;
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate() {
        LogUtils.d(TAG, "service onCreate");
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null ) {
            int unreadFlag = intent.getIntExtra(UnreadUtils.EXTRA_UNREAD_INFO, 0);
            int type = intent.getIntExtra(UnreadUtils.EXTRA_UNREAD_TYPE, 0);
            boolean stopServiceIfNeeded = unreadFlag == 0;

            boolean isPhoneChecked = (unreadFlag & UnreadSettingsFragment.FLAG_PHONE_CHECKED) != 0;
            boolean isSMSChecked = (unreadFlag & UnreadSettingsFragment.FLAG_SMS_CHECKED) != 0;

            if(LogUtils.DEBUG_UNREAD) {
                LogUtils.d(TAG, "onStartCommand:  unreadFlag = "+unreadFlag
                        + ", type = "+type+", stopServiceIfNeeded = "+stopServiceIfNeeded
                        + ", isPhoneChecked = "+isPhoneChecked+", isSMSChecked = "+isSMSChecked);
            }

            if(type == UnreadUtils.TYPE_ALL) {
                if(isPhoneChecked) {
                    registerCallObserverAndPrepareUnreadInfo(isPhoneChecked);
                }
                if(isSMSChecked) {
                    registerSMSObserverAndPrepareUnreadInfo(isSMSChecked);
                }

            } else if(type == UnreadUtils.TYPE_CALL_LOG) {
                registerCallObserverAndPrepareUnreadInfo(isPhoneChecked);
            } else if(type == UnreadUtils.TYPE_SMS){
                registerSMSObserverAndPrepareUnreadInfo(isSMSChecked);
            }

            if(stopServiceIfNeeded) {
                stopSelf();
            }
        }

        return START_STICKY;
    }


    private void registerCallObserverAndPrepareUnreadInfo(boolean isChecked) {
        ComponentName phoneComponentName = getSelectedComponentName(UnreadSettingsFragment.PREF_KEY_MISS_CALL);
        if(isChecked) {
            if(mUnreadCallContentObserver == null) {
                mUnreadCallContentObserver = new UnreadCallContentObserver(new Handler());
                getContentResolver().registerContentObserver(CALLS_CONTENT_URI, true, mUnreadCallContentObserver);
            }

            asyncUpdateUnreadPhoneInfo();
        } else {
            if(mUnreadCallContentObserver != null ) {
                getContentResolver().unregisterContentObserver(mUnreadCallContentObserver);
                mUnreadCallContentObserver = null;
            }
            if(LogUtils.DEBUG_UNREAD) {
                LogUtils.d(TAG, "phoneCheckbox unChecked, mUnreadPhoneNum = "+mUnreadPhoneNum);
            }
            if(mUnreadPhoneNum != 0) {
                updateUnreadInfo(0, phoneComponentName);
            }
        }
    }

    private void registerSMSObserverAndPrepareUnreadInfo(boolean isChecked) {
        ComponentName smsComponentName = getSelectedComponentName(UnreadSettingsFragment.PREF_KEY_UNREAD_SMS);
        if(isChecked) {
            if(mUnreadMMSContentObserver == null) {
                mUnreadMMSContentObserver = new UnreadMMSContentObserver(new Handler());
                getContentResolver().registerContentObserver(MMSSMS_CONTENT_URI, true, mUnreadMMSContentObserver);
            }

            asyncUpdateUnreadSMSInfo();
        } else {
            if(mUnreadMMSContentObserver != null ) {
                getContentResolver().unregisterContentObserver(mUnreadMMSContentObserver);
                mUnreadMMSContentObserver = null;
            }
            if(LogUtils.DEBUG_UNREAD) {
                LogUtils.d(TAG, "smsCheckbox unChecked, mUnreadSMSNum = "+mUnreadSMSNum);
            }
            if(mUnreadSMSNum != 0) {
                updateUnreadInfo(0, smsComponentName);
            }
        }
    }

    @Override
    public void onDestroy() {
        LogUtils.d(TAG, "service onDestroy");
        super.onDestroy();

    }

    private final class UnreadCallContentObserver extends ContentObserver {
        public UnreadCallContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if(LogUtils.DEBUG_UNREAD) {
                LogUtils.d(TAG, String.format("onChange: uri=%s selfChange=%b", uri.toString(), selfChange));
            }
            asyncUpdateUnreadPhoneInfo();
        }
    }


    private void asyncUpdateUnreadPhoneInfo() {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                mUnreadPhoneNum = UnreadUtils.getMissedCallCount(getApplicationContext());
                if(LogUtils.DEBUG_UNREAD) {
                    LogUtils.d(TAG, "asyncUpdateUnreadPhoneInfo, mUnreadPhoneNum = "+mUnreadPhoneNum);
                }
                return mUnreadPhoneNum;
            }

            @Override
            protected void onPostExecute(Integer unReadCallNum) {
                ComponentName phoneComponentName = getSelectedComponentName(UnreadSettingsFragment.PREF_KEY_MISS_CALL);
                if(phoneComponentName != null) {
                    updateUnreadInfo(unReadCallNum, phoneComponentName);
                }

            }
        }.execute();
    }


    private final class UnreadMMSContentObserver extends ContentObserver {
        public UnreadMMSContentObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if(LogUtils.DEBUG_UNREAD) {
                LogUtils.d(TAG, String.format("onChange: uri=%s selfChange=%b", uri.toString(), selfChange));
            }
            asyncUpdateUnreadSMSInfo();
        }
    }

    private void asyncUpdateUnreadSMSInfo() {
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                mUnreadSMSNum = UnreadUtils.getUnreadMessageCount(getApplicationContext());
                if(LogUtils.DEBUG_UNREAD) {
                    LogUtils.d(TAG, "asyncUpdateUnreadSMSInfo, mUnreadSMSNum = "+mUnreadSMSNum);
                }
                return mUnreadSMSNum;
            }

            @Override
            protected void onPostExecute(Integer unReadSMSNum) {
                ComponentName smsComponentName = getSelectedComponentName(UnreadSettingsFragment.PREF_KEY_UNREAD_SMS);
                if(smsComponentName != null) {
                    updateUnreadInfo(unReadSMSNum, smsComponentName);
                }
            }
        }.execute();
    }


    private ComponentName getSelectedComponentName(String key){
        String value = mSharedPrefs.getString(key, "");
        if(!TextUtils.isEmpty(value)) {
            return ComponentName.unflattenFromString(value);
        }
        return null;
    }

    public static int getUnreadNumByType(int type) {
        switch (type) {
            case UnreadUtils.TYPE_CALL_LOG:
                return mUnreadPhoneNum;
            case UnreadUtils.TYPE_SMS:
                return  mUnreadSMSNum;
            default:
                return 0;
        }
    }

    private void updateUnreadInfo(int unReadNum, ComponentName componentName) {
        if(LogUtils.DEBUG_UNREAD) {
            LogUtils.d(TAG, "updateUnreadInfo, componentName :   "+componentName
                       + ", unReadNum :  " +unReadNum);
        }
        if(componentName != null) {
            UnreadUtils.sendUnreadBroadcast(getApplicationContext(), unReadNum, componentName);
        }
    }

}
