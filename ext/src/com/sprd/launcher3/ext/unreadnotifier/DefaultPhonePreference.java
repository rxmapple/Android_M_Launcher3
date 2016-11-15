package com.sprd.launcher3.ext.unreadnotifier;

import android.content.ComponentName;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import com.sprd.launcher3.ext.AppListPreference;

import java.util.List;

/**
 * Created by SPREADTRUM\jin.xie on 10/20/16.
 */
public class DefaultPhonePreference extends AppListPreference {
    private static final String TAG = "PhonePreference";

    public DefaultPhonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadDialerApps();
    }

//    @Override
//    protected boolean persistString(String value) {
//        setSummary(getEntry());
//        return true;
//    }

    private void loadDialerApps() {
        List<CallAppUtils.CallApplicationData> callApplications =
                        CallAppUtils.getInstalledDialerApplications(getContext());

        int count = callApplications.size();
        String[] listValues = new String[count];
        int i = 0;
        for (CallAppUtils.CallApplicationData callApplication : callApplications) {
            ComponentName componentName = new ComponentName(callApplication.mPackageName, callApplication.callClassName);
            listValues[i++] = componentName.flattenToShortString();
        }

        setListValues(listValues, null);

    }

    private String getDefaultPackage() {
        return CallAppUtils.getDefaultDialerApplication(getContext());
    }

}