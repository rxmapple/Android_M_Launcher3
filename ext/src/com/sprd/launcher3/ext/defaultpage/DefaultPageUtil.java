package com.sprd.launcher3.ext.defaultpage;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.android.launcher3.CellLayout;
import com.android.launcher3.DeviceProfile;
import com.android.launcher3.Launcher;
import com.android.launcher3.LauncherAppState;
import com.sprd.launcher3.ext.LogUtils;

/**
 * Created by SPREADTRUM\hualan.su on 10/20/16.
 */

public class DefaultPageUtil {

    public static final String DEFAULT_PAGE_INDEX = "defaultPageIndex";
    private static String TAG = "Launcher3.DefaultPageUtil";
    private static boolean H_LAYOUT_OF_LEFT = true;

    private DefaultPageUtil() {

    }

    // CellLayout add util method-------------------------start

    public static HomeImageView createHomeImageView(Launcher launcher,
            CellLayout CellLayout) {
        return new HomeImageView(launcher, CellLayout);
    }

    public static boolean isTouch(HomeImageView homeImageView, MotionEvent ev) {
        if (isHomeShow(homeImageView)){
            Rect homeRect = new Rect();
            homeImageView.getHitRect(homeRect);
            if (homeRect.contains((int) ev.getX(), (int) ev.getY())) {
                return true;
            }
        }
        return false;
    }

    public static int getCalculateCellWidth(HomeImageView homeImageView, int childWidthSize,
              int countX, int cw){
        if (H_LAYOUT_OF_LEFT && isHomeShow(homeImageView)) {
            cw = DeviceProfile.calculateCellWidth(childWidthSize * 2, countX *2 +1);
        }
        return cw;
    }

    public static int getHomeHeightOnMeasure(HomeImageView homeImageView,
            int cellWidth, int cellHeight) {
        int homeImageHeight = 0;
        if (isHomeShow(homeImageView)) {
            homeImageView.measure(View.MeasureSpec.makeMeasureSpec(cellWidth / 2,
                    View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(
                    cellHeight / 2, View.MeasureSpec.EXACTLY));
            if(!H_LAYOUT_OF_LEFT || !isHorizontalMode(cellWidth,cellHeight) ) {
                homeImageHeight = homeImageView.getMeasuredHeight();
            }
        }
        return homeImageHeight;
    }

    public static int getHomeWidth(HomeImageView homeImageView) {
        int homeImageWidth = 0;
        if (H_LAYOUT_OF_LEFT && isHomeShow(homeImageView)) {
            int homeWidth = homeImageView.getMeasuredWidth();
            int homeHeight = homeImageView.getMeasuredHeight();
            if (isHorizontalMode(homeWidth,homeHeight)) {
                homeImageWidth = homeWidth;
            }
        }
        return homeImageWidth;
    }

    public static int getHomeHeightOnLayout(HomeImageView homeImageView,
             int left, int top, int width, int height) {
        int homeImageHeight = 0;
        if (isHomeShow(homeImageView)) {
            int homeWidth = homeImageView.getMeasuredWidth();
            int homeHeight = homeImageView.getMeasuredHeight();
            if (H_LAYOUT_OF_LEFT && isHorizontalMode(homeWidth,homeHeight)) {
                int homeImageViewTop = top + (height - homeHeight) / 2;
                homeImageView.layout(left, homeImageViewTop, left
                        + homeWidth, homeImageViewTop + homeHeight);
            }else{
                homeImageHeight = homeHeight;
                homeImageView.setLayout(left, top,  width);
            }
        }
        return homeImageHeight;
    }

    // CellLayout add util method-------------------------end

    // Workspace add util method-------------------------start
    public static void setupDefaultPage(Launcher launcher, int defaultPage) {
        launcher.getWorkspace().setDefaultPage(
                DefaultPageUtil.getSharedDefaultPage(launcher, defaultPage));
    }

    public static void removePageChange(Launcher launcher,
            CellLayout removeCellLayout) {
        int removeIndex = launcher.getWorkspace()
                .indexOfChild(removeCellLayout);
        int defaultIndex = launcher.getWorkspace().getDefaultPage();
        if (removeIndex <= defaultIndex && defaultIndex > 0) {
            int childCount = launcher.getWorkspace().getChildCount();
            if (LogUtils.DEBUG) {
                Log.d(TAG, "removePage removeIndex = " + removeIndex
                        + " defaultIndex = " + defaultIndex + " childCount = "
                        + childCount);
            }
            boolean isChangeDefaultIndex = false;
            if (removeIndex < defaultIndex) {
                defaultIndex--;
                isChangeDefaultIndex = true;
            } else if (defaultIndex >= childCount - 1) {
                defaultIndex = childCount - 2;
                isChangeDefaultIndex = true;
            }
            if (isChangeDefaultIndex) {
                saveSharedDefaultPage(launcher, defaultIndex);
            }
        }
    }

    public static void addPageChange(Launcher launcher, int addIndex) {
        int defaultIndex = launcher.getWorkspace().getDefaultPage();
        int childCount = launcher.getWorkspace().getChildCount();
        if (addIndex <= defaultIndex && childCount - 1 > defaultIndex) {
            if (LogUtils.DEBUG) {
                Log.d(TAG, "addPage addIndex = " + addIndex
                        + " defaultIndex = " + defaultIndex + " childCount = "
                        + childCount);
            }
            defaultIndex++;
            saveSharedDefaultPage(launcher, defaultIndex);
        }
    }

    public static void dragPageChange(Launcher launcher, int dragIndex,
            CellLayout dragCellLayout) {
        HomeImageView homeImageView = dragCellLayout.getHomeImageView();
        if (isHomeShow(homeImageView) && homeImageView.isDefaultHome()) {
            if (LogUtils.DEBUG) {
                Log.d(TAG, "dragPage  = " + dragIndex);
            }
            saveSharedDefaultPage(launcher, dragIndex);
        }
    }

    public static void setHomeVisiAndDefault(Launcher launcher,
            CellLayout updateCellLayout, int pageNo) {
        HomeImageView homeImageView = updateCellLayout.getHomeImageView();
        if (homeImageView != null) {
            int defaultHomeIndex = launcher.getWorkspace().getDefaultPage();
            homeImageView.updateVisibility();
            homeImageView.updateImageTintList(pageNo == defaultHomeIndex);
        }
    }

    // Workspace add util method-------------------------end

    public static void saveSharedDefaultPage(Launcher launcher, int delault) {
        String spKey = LauncherAppState.getSharedPreferencesKey();
        launcher.getWorkspace().setDefaultPage(delault);
        SharedPreferences sp = launcher.getSharedPreferences(spKey,
                Context.MODE_PRIVATE);
        sp.edit().putInt(DEFAULT_PAGE_INDEX, delault).commit();
    }


    public static boolean isHorizontalMode(int width, int height) {
        return  width > height;
    }

    private static boolean isHomeShow(HomeImageView homeImageView) {
        return homeImageView != null && homeImageView.isHomeShow();
    }

    private static int getSharedDefaultPage(Launcher launcher, int delault) {
        String spKey = LauncherAppState.getSharedPreferencesKey();
        SharedPreferences sp = launcher.getSharedPreferences(spKey,
                Context.MODE_PRIVATE);
        return sp.getInt(DEFAULT_PAGE_INDEX, delault);
    }
}
