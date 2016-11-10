/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * SPRD_SETTINGS_ACTIVITY_SUPPORT
 */

package com.sprd.launcher3.ext;

import android.app.Activity;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

import com.android.launcher3.LauncherSettings;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;

/**
 * Created by SPREADTRUM\pichao.gao on 11/8/16.
 *
 * SPRD Settings activity for Launcher. Currently implements the following setting: Allow rotation
 */
public class SprdSettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SprdLauncherSettingsFragment())
                .commit();
    }

    /**
     * This fragment shows the launcher preferences.
     */
    public static class SprdLauncherSettingsFragment extends PreferenceFragment
            implements OnPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.sprdlauncher_settings_preferences);

            SwitchPreference pref = (SwitchPreference) findPreference(
                    Utilities.ALLOW_ROTATION_PREFERENCE_KEY);
            pref.setPersistent(false);

            boolean rotation_value = Utilities.getLauncherSettingsBoolean(getContext(),
                    Utilities.ALLOW_ROTATION_PREFERENCE_KEY,
                    false);
            pref.setChecked(rotation_value);

            pref.setOnPreferenceChangeListener(this);


        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            //You can add CheckboxPreference here if need,I think boolean and String type is enough!
            if(preference instanceof SwitchPreference) {
                Bundle extras = new Bundle();
                extras.putBoolean(LauncherSettings.Settings.EXTRA_VALUE, (Boolean) newValue);
                getActivity().getContentResolver().call(
                        LauncherSettings.Settings.CONTENT_URI,
                        LauncherSettings.Settings.METHOD_SET_BOOLEAN,
                        preference.getKey(), extras);
            }else if(preference instanceof ListPreference){
                Bundle extras = new Bundle();
                extras.putString(LauncherSettings.Settings.EXTRA_VALUE, (String) newValue);
                getActivity().getContentResolver().call(
                        LauncherSettings.Settings.CONTENT_URI,
                        LauncherSettings.Settings.METHOD_SET_STRING,
                        preference.getKey(), extras);
            }
            return true;
        }
    }
}
