package com.sprd.launcher3.ext.unreadnotifier.call;

import android.content.ComponentName;
import android.content.Context;
import android.util.AttributeSet;

import com.sprd.launcher3.ext.AppListPreference;
import com.sprd.launcher3.ext.unreadnotifier.CallAppUtils;
import com.sprd.launcher3.ext.unreadnotifier.UnreadBaseItem;
import com.sprd.launcher3.ext.unreadnotifier.UnreadInfoManager;

import java.util.List;

/**
 * Created by SPRD on 10/20/16.
 */
public class DefaultPhonePreference extends AppListPreference {
    public DefaultPhonePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadDialerApps();
        UnreadBaseItem item = UnreadInfoManager.getInstance(context).getItemByType(MissCallItem.TYPE_CALL_LOG);
        isPermissionGranted = item.checkPermission(item.mPermission);
        initValue = item.getCurrentComponentName().flattenToShortString();
    }

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

        setListValues(listValues, getDefaultPackageName());

    }

    private String getDefaultPackageName() {
        return MissCallItem.DEFAULT_CNAME.getPackageName();
    }

}