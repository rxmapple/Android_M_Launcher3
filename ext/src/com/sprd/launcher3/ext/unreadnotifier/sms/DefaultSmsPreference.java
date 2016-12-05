package com.sprd.launcher3.ext.unreadnotifier.sms;

import android.content.ComponentName;
import android.content.Context;
import android.util.AttributeSet;

import com.sprd.launcher3.ext.AppListPreference;
import com.sprd.launcher3.ext.unreadnotifier.MMSAppUtils;
import com.sprd.launcher3.ext.unreadnotifier.UnreadBaseItem;
import com.sprd.launcher3.ext.unreadnotifier.UnreadInfoManager;

import java.util.Collection;

/**
 * Created by SPRD on 10/20/16.
 */
public class DefaultSmsPreference extends AppListPreference {
    public DefaultSmsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadSmsApps();
        UnreadBaseItem item = UnreadInfoManager.getInstance(context).getItemByType(UnreadMessageItem.TYPE_SMS);
        isPermissionGranted = item.checkPermission(item.mPermission);
        initValue = item.getCurrentComponentName().flattenToShortString();
    }

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

        setListValues(listValues, getDefaultPackageName());

    }

    private String getDefaultPackageName() {
        return UnreadMessageItem.DEFAULT_CNAME.getPackageName();
    }
}