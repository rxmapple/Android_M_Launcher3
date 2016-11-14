package com.sprd.launcher3.ext.mainmenubg;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.View;

import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherProviderChangeListener;
import com.android.launcher3.R;
import com.android.launcher3.Utilities;
import com.sprd.launcher3.ext.LogUtils;
import com.sprd.launcher3.ext.UtilitiesExt;

/**
 * Created by SPREADTRUM\pichao.gao on 11/8/16.
 */

public class MainMenuBgDB {

    private static String TAG = "SPRD_MAIN_MENU_BG_SUPPORT MainMenuBgDB";

    public static String MAIN_MENU_BG_KEY = "pref_mainmenu_transparent";
    private static MainMenuBgDB instance = null;
    private Context sContext = null;
    private boolean needToUpdateAllApps = false;
    private String mCurrentBg = MainMenuBgUtils.MAIN_MENU_BG_DEFAULT;
    private boolean wallPaperDark = true;

    public static MainMenuBgDB getInstance(Context context) {
        if (instance == null) {
            synchronized (MainMenuBgDB.class) {
                if (instance == null) {
                    instance = new MainMenuBgDB(context);
                }
            }
        }
        return instance;
    }

    public MainMenuBgDB(Context context) {
        this.sContext = context;
        mCurrentBg = UtilitiesExt.getLauncherSettingsString(sContext, MAIN_MENU_BG_KEY, MainMenuBgUtils.MAIN_MENU_BG_DEFAULT);

        new AsyncTask<Void, Void, Boolean>(){
            @Override
            protected Boolean doInBackground(Void... params) {
                boolean bool;
                bool = isWallpaperDark();
                return bool;
            }

            @Override
            protected void onPostExecute(Boolean b) {
                wallPaperDark = b;
                super.onPostExecute(b);
            }
        }.execute();
    }

    public void setNeedToUpdateAllApps(boolean bool){
        needToUpdateAllApps = bool;
    }

    public boolean getNeedToUpdateAllApps(){
        return needToUpdateAllApps;
    }


    public String getMainMenuBg(){
        LogUtils.d(TAG,"getMainMenuBg:"+mCurrentBg);
        return  mCurrentBg;
    }

    public void setMainMenuBg(String bg){
        LogUtils.d(TAG,"setMainMenuBg:"+bg);
        mCurrentBg = bg;
        setNeedToUpdateAllApps(true);
    }

    public boolean isMainMenuBgWhite(){
        return mCurrentBg.equals(MainMenuBgUtils.MAIN_MENU_BG_WHITE);
    }

    public boolean isMainMenuBgBlack(){
        return mCurrentBg.equals(MainMenuBgUtils.MAIN_MENU_BG_BLACK);
    }

    public boolean isMainMenuBgTransparent(){
        return mCurrentBg.equals(MainMenuBgUtils.MAIN_MENU_BG_TRANSPARENT);
    }

    public boolean getTextColorWhite(){
        boolean res = true;

        if(isMainMenuBgWhite()){
            res = false;
        }else if(isMainMenuBgTransparent()){
//            res = wallPaperDark ? true : false;
        }
        return res;
    }

    public Rect updateBackgourndAndPaddings(View containerView, View revealView, View searchBarView, Rect padding){
        InsetDrawable background = null ;
        background = new InsetDrawable(sContext.getResources().getDrawable(R.drawable.quantum_panel_shape), padding.left, 0,padding.right, 0);
        if(isMainMenuBgBlack()){
            background.setDrawable(sContext.getResources().getDrawable(R.drawable.quantum_panel_shape_dark));
        }
        Rect bgPadding = new Rect();
        if((isMainMenuBgWhite()
                || isMainMenuBgBlack()
                && ( background != null ))){
          background.getPadding(bgPadding);
            containerView.setBackground(background);
            revealView.setBackground(background.getConstantState().newDrawable());
        }else{
            containerView.setBackground(null);
            revealView.setBackground(null);
        }

        InsetDrawable searchBg = null ;
        searchBg = new InsetDrawable(sContext.getResources().getDrawable(R.drawable.all_apps_search_bg), 10, 0,10, 0);

        if(!isMainMenuBgWhite()){
            searchBg.setDrawable(sContext.getResources().getDrawable(R.drawable.quantum_panel_dark_bitmap));
        }

        if(searchBarView!= null && searchBg != null ) {
            searchBarView.setBackground(searchBg);
        }
        return  bgPadding;
    }

    public boolean isWallpaperDark() {

        WallpaperManager wallpaperManager = WallpaperManager
                .getInstance(sContext);
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        Bitmap bitmap = ((BitmapDrawable) wallpaperDrawable).getBitmap();

        int sum_hue = 0;
        for (int h = 0; h < bitmap.getHeight(); h += 10) {
            for (int w = 0; w < bitmap.getWidth(); w += 10) {
                int hue = bitmap.getPixel(w, h);
                sum_hue += hue;
            }
        }
        return isColorDark( sum_hue*100 / bitmap.getHeight() * bitmap.getWidth());
    }


    public static boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return darkness >= 0.4;
    }

}
