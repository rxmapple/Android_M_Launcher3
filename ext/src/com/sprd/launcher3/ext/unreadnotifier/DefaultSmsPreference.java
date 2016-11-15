package com.sprd.launcher3.ext.unreadnotifier;

import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;

import com.sprd.launcher3.ext.AppListPreference;

import java.util.Collection;

/**
 * Created by SPREADTRUM\jin.xie on 10/20/16.
 */
public class DefaultSmsPreference extends AppListPreference {
    private static final String TAG = "SmsPreference";
    private final Context mContext;
    public DefaultSmsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        loadSmsApps();
    }

//    @Override
//    protected boolean persistString(String value) {
//        setSummary(getEntry());
//        return true;
//    }

    private void loadSmsApps() {
        Collection<MMSAppUtils.SmsApplicationData> smsApplications =
                        MMSAppUtils.getApplicationCollection(getContext());

        int count = smsApplications.size();
        String[] listValues = new String[count];
        int i = 0;
        for (MMSAppUtils.SmsApplicationData smsApplicationData : smsApplications) {
            ComponentName componentName = new ComponentName(smsApplicationData.mPackageName, smsApplicationData.smsClassName);
            listValues[i++] = componentName.flattenToShortString();
        }

        setListValues(listValues, null);

    }

    private String getDefaultPackage() {
        return Settings.Secure.getString(mContext.getContentResolver(),
                MMSAppUtils.SMS_DEFAULT_APPLICATION);
    }
}