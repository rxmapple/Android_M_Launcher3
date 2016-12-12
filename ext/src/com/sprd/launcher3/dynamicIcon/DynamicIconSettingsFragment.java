package com.sprd.launcher3.dynamicIcon;

import android.content.ComponentName;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

import com.android.launcher3.LauncherSettings;
import com.android.launcher3.R;
import com.sprd.launcher3.ext.DynamicIconUtils;
import com.sprd.launcher3.ext.FeatureOption;

/**
 * This fragment shows the dynamic icon setting preferences.
 */
public class DynamicIconSettingsFragment  extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "DynamicIconSettingsFragment";

    public static final String PRE_DYNAMIC_CALENDAR = "pref_calendar";
    public static final String PRE_DYNAMIC_CLOCK = "pref_clock";

    private SwitchPreference mCalendarPre;
    private SwitchPreference mClockPre;

    private DynamicCalendar  mDynamicCalendar;
    private DynamicDeskclock mDynamicDeskclock;

    private boolean mCalendarPreInitState;
    private boolean mClockPreInitState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.dynamic_icon_settings);

        DynamicIconUtils utils = DynamicIconUtils.getInstance(getActivity());

        mCalendarPre = (SwitchPreference) findPreference(PRE_DYNAMIC_CALENDAR);
        if (FeatureOption.SPRD_DYNAMIC_CALENDAR_SUPPORT) {
            mDynamicCalendar = (DynamicCalendar) utils.
                    getDynamicIconByType(DynamicIconUtils.DYNAMIC_CALENDAR_TYPE);
            if (mDynamicCalendar != null && mDynamicCalendar.isAppInstalled()) {
                mCalendarPreInitState = mDynamicCalendar.isCheckedState();
                mCalendarPre.setChecked(mCalendarPreInitState);
                mCalendarPre.setOnPreferenceChangeListener(this);
            } else {
                getPreferenceScreen().removePreference(mCalendarPre);
            }
        } else {
            getPreferenceScreen().removePreference(mCalendarPre);
        }

        mClockPre = (SwitchPreference) findPreference(PRE_DYNAMIC_CLOCK);
        if (FeatureOption.SPRD_DYNAMIC_CLOCK_SUPPORT) {
            mDynamicDeskclock = (DynamicDeskclock) utils.
                    getDynamicIconByType(DynamicIconUtils.DYNAMIC_CLOCK_TYPE);
            if (mDynamicDeskclock != null && mDynamicDeskclock.isAppInstalled()) {
                mClockPreInitState = mDynamicDeskclock.isCheckedState();
                mClockPre.setChecked(mClockPreInitState);
                mClockPre.setOnPreferenceChangeListener(this);
            } else {
                getPreferenceScreen().removePreference(mClockPre);
            }
        } else {
            getPreferenceScreen().removePreference(mClockPre);
        }
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        final String key = preference.getKey();
        Bundle extras = new Bundle();
        extras.putBoolean(LauncherSettings.Settings.EXTRA_VALUE, (Boolean) newValue);
        getActivity().getContentResolver().call(
                LauncherSettings.Settings.CONTENT_URI,
                LauncherSettings.Settings.METHOD_SET_BOOLEAN,
                preference.getKey(), extras);
        if (key.equals(PRE_DYNAMIC_CALENDAR)) {
                if (mDynamicCalendar != null) {
                    mDynamicCalendar.setCheckedSeate((Boolean) newValue);
                }
            } else if (key.equals(PRE_DYNAMIC_CLOCK)) {
                if (mDynamicDeskclock != null) {
                    mDynamicDeskclock.setCheckedSeate((Boolean) newValue);
                }
            }
        return true;
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mDynamicCalendar != null && mDynamicCalendar.mIsChecked != mCalendarPreInitState) {
            mDynamicCalendar.forceUpdateView(true);
        }

        if (mDynamicDeskclock != null && mDynamicDeskclock.mIsChecked != mClockPreInitState) {
            mDynamicDeskclock.startAutoUpdateView();
        }
    }

    private boolean isAppInstalled(ComponentName component) {
        if (component != null) {
            PackageManager pm = getActivity().getPackageManager();
            String packageName = component.getPackageName();
            if (packageName != null) {
                try {
                    PackageInfo info = pm.getPackageInfo(packageName, 0);
                    return info != null;
                } catch (PackageManager.NameNotFoundException e) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return false;
    }
}
