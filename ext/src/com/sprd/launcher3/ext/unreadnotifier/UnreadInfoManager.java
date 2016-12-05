package com.sprd.launcher3.ext.unreadnotifier;

import android.content.Context;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.android.launcher3.Launcher;
import com.sprd.launcher3.ext.LogUtils;
import com.sprd.launcher3.ext.unreadnotifier.call.MissCallItem;
import com.sprd.launcher3.ext.unreadnotifier.sms.UnreadMessageItem;

import java.util.ArrayList;

/**
 * Created by SPRD on 11/15/16.
 */

public class UnreadInfoManager {
    private static final String TAG = "UnreadInfoManager";

    public static final int PERMISSIONS_REQUEST_CODE = 103;

    private static UnreadInfoManager INSTANCE;
    public Context mContext;

    public UnreadMessageItem mMessageUnreadItem;
    public MissCallItem mMissCallItem;

    private static final ArrayList<UnreadBaseItem> ALL_ITEMS =
            new ArrayList<>();
    private static final ArrayList<UnreadBaseItem> ALL_GRANTEDPERMISSION_ITEMS =
            new ArrayList<>();
    private static final ArrayList<UnreadBaseItem> ALL_DENIEDPERMISSION_ITEMS =
            new ArrayList<>();

    private UnreadInfoManager(Context context) {
        mContext = context;
    }

    public static UnreadInfoManager getInstance(Context context) {
        synchronized (UnreadInfoManager.class) {
            if (INSTANCE == null) {
                INSTANCE = new UnreadInfoManager(context);
            }
        }
        return INSTANCE;
    }

    public void init() {
        if(mMessageUnreadItem != null || mMissCallItem != null && !(mContext instanceof Launcher)) {
            LogUtils.d(TAG, "Unread call or Message item not null, return directly.");
            return;
        }

        //init
        createItems();

        initPermissionList();

        int N = ALL_DENIEDPERMISSION_ITEMS.size();

        String[] deniedString = new String[N];
        for (int i = 0 ; i < N; i++) {
            deniedString[i] = ALL_DENIEDPERMISSION_ITEMS.get(i).mPermission;
            UnreadBaseItem item = ALL_DENIEDPERMISSION_ITEMS.get(i);
            if(item != null) {
                item.resetUnreadLoaderUtilsNum();
            }
        }
        if(N > 0) {
            ((Launcher) mContext).requestPermissions(deniedString, PERMISSIONS_REQUEST_CODE);
        }

        N = ALL_GRANTEDPERMISSION_ITEMS.size();
        for (int i = 0 ; i < N; i++) {
            UnreadBaseItem item = ALL_GRANTEDPERMISSION_ITEMS.get(i);
            if(item != null) {
                item.mContentObserver.registerContentObserver();
                item.updateUnreadInfo();
            }
        }
    }

    private void createItems() {
        if(mMessageUnreadItem == null) {
            mMessageUnreadItem = new UnreadMessageItem((Launcher) mContext);
            ALL_ITEMS.add(mMessageUnreadItem);
        }

        if(mMissCallItem == null) {
            mMissCallItem = new MissCallItem((Launcher) mContext);
            ALL_ITEMS.add(mMissCallItem);
        }
        if(LogUtils.DEBUG_UNREAD) {
            LogUtils.d(TAG, "createItems(), size of ALL_ITEMS is "+ALL_ITEMS.size());
        }
    }

    private void initPermissionList() {
        int N = ALL_ITEMS.size();
        for (int i = 0; i < N; i ++) {
            UnreadBaseItem item = ALL_ITEMS.get(i);
            if (item.isPersistChecked()) {
                if (item.checkPermission(item.mPermission)) {
                    ALL_GRANTEDPERMISSION_ITEMS.add(item);
                } else {
                    ALL_DENIEDPERMISSION_ITEMS.add(item);
                }
            }
        }
    }

    public UnreadBaseItem getItemByType(int type) {
        for (int i = 0; i < ALL_ITEMS.size(); i++) {
            UnreadBaseItem item = ALL_ITEMS.get(i);
            if (item.mType == type) {
                return item;
            }
        }
        return null;
    }

    public UnreadBaseItem getItemByKey(String key) {
        for (int i = 0; i < ALL_ITEMS.size(); i++) {
            UnreadBaseItem item = ALL_ITEMS.get(i);
            if (item.mPrefKey.equals(key)) {
                return item;
            }
        }
        return null;
    }

    public void handleRequestPermissionResult(int requestCode, String[] permissions,
                                              int[] grantResults) {
        if(LogUtils.DEBUG_UNREAD) {
            LogUtils.d(TAG, "handleRequestPermissionResult, onPermissionsResult counts: " + permissions.length + ":" + grantResults.length);
        }

        for (int i = 0; i < ALL_DENIEDPERMISSION_ITEMS.size(); i++) {
            UnreadBaseItem item = ALL_DENIEDPERMISSION_ITEMS.get(i);
            if(grantResults.length > 0 && permissions.length >0 && item.mPermission.equals(permissions[i])) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    if(LogUtils.DEBUG_UNREAD) {
                        LogUtils.d(TAG, "handleRequestPermissionResult, permission granted:" + item.mPermission);
                    }
                    item.mContentObserver.registerContentObserver();
                    item.updateUnreadInfo();
                } else if (grantResults[i] == PackageManager.PERMISSION_DENIED){
                    Toast.makeText(mContext, item.mUnreadHintString, Toast.LENGTH_LONG).show();
                    if(LogUtils.DEBUG_UNREAD) {
                        LogUtils.d(TAG, "handleRequestPermissionResult, permission denied:" + item.mPermission);
                    }
                }
            }
        }

    }

}
