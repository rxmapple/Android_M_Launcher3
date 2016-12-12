package com.sprd.launcher3.dynamicIcon;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by SPREADTRUM\linlin.zhang on 12/2/16.
 */
public class DynamicIconSettings extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new DynamicIconSettingsFragment())
                .commit();
    }
}
