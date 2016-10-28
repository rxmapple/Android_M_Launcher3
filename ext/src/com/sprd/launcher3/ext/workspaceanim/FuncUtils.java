package com.sprd.launcher3.ext.workspaceanim;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.Transformation;

import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.sprd.launcher3.ext.FeatureOption;
import com.sprd.launcher3.ext.workspaceanim.effect.EffectFactory;
import com.sprd.launcher3.ext.workspaceanim.effect.EffectInfo;

/**
 * Created by SPREADTRUM\Steven.God on 10/24/16.
 */

public final class FuncUtils {
    private static String TAG = "WSPA FuncUtils";

    protected static float mLayoutScale = 1.0f;
    private static int mMinimumWidth;

    /*Animation Preview Parameter*/
//    private final float OFFSET = 0.0f;
//    private final float SCALE_OFFSET = 1.0f;
//    private final float INVISIBLE = 0.0f;
//    private final float VISIBLE = 1.0f;
    public static float CAMERA_DISTANCE = 5000;


    public static final int ANIMATION_DEFAULT = 0;// 0 is no animation. 4 is
    // for SharedPreference XML NAME
    public static final String WORKSPACE_STYLE = "workspace_style";
    // the workspace_setting.xml key and SharePreference key are same
    public static final String KEY_ANIMATION_STYLE = "workspace_pref_key_animation";


    /**
     * Single instance
     */
/*    private static FuncUtils instance = null;
    private static Context sContext = null;

    public static FuncUtils getInstance(Context context) {
        if (instance == null) {
            synchronized (FuncUtils.class) {
                if (instance == null) {
                    sContext = context;
                    instance = new FuncUtils();
                }
            }
        }
        return instance;
    }*/


    public static void addWorkspaceAnimationPref(final Activity activity, PreferenceScreen prefScreen){
        Preference pref = new Preference(activity);
        pref.setTitle(R.string.effect_button_text);
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                WorkspaceSettings mDialog = new WorkspaceSettings();
                mDialog.show(activity.getFragmentManager(),"WorkspaceSettings");
                return false;
            }
        });
        prefScreen.addPreference(pref);
    }
    /*
    * Used for Launcher.java
    */
    public static void onClickEffectButton(Context context, View v) {
        context.startActivity(new Intent(context, com.sprd.launcher3.ext.workspaceanim.WorkspaceSettings.class));
    }


    public static int getScaledMeasuredWidth(View child) {
        // This functions are called enough times that it actually makes a difference in the
        // profiler -- so just inline the max() here
        if (child != null) {
            final int measuredWidth = child.getMeasuredWidth();
            final int minWidth = mMinimumWidth;
            final int maxWidth = (minWidth > measuredWidth) ? minWidth
                    : measuredWidth;
            return (int) (maxWidth * mLayoutScale + 0.5f);
        } else {
            return (int) (mMinimumWidth * mLayoutScale + 0.5f);
        }
    }

    public static SharedPreferences getmAnimSharePref(Context context) {
        if(context == null) return null;
        SharedPreferences sharedPref = context.getApplicationContext().getSharedPreferences(WORKSPACE_STYLE, Context.MODE_WORLD_READABLE|Context.MODE_MULTI_PROCESS);
        return sharedPref;
    }


    public static EffectInfo getCurentAnimInfo(Context context){
        SharedPreferences sharedPref = getmAnimSharePref(context);
        if(sharedPref == null) return null;
        int type = sharedPref.getInt(KEY_ANIMATION_STYLE, FuncUtils.ANIMATION_DEFAULT);
        Log.d(TAG,"type:"+type);
        return EffectFactory.getEffect(type);
    }
}
