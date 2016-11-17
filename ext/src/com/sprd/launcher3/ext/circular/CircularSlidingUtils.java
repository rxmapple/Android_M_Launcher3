package com.sprd.launcher3.ext;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.launcher3.LauncherFiles;
import com.android.launcher3.PagedView;
/**
 * Created by rxmapple on 2016/9/18.
 * You can search the key word "circular" to look all modifiy about this.
 */
public class CircularSlidingUtils {

    public static final String ALLOW_CIRCULAR_SLIDING_PREFERENCE_KEY = "pref_allowCircularSliding";
    public static final int OVER_FIRST_PAGE_INDEX = -2; // for -1 is occupied by INVALID_PAGE;
    public static final int OVER_FIRST_PAGE_SCROLL_INDEX = 0;
    public static final int OVER_LAST_PAGE_SCROLL_INDEX = 1;

    public static SharedPreferences getLaunchergetSharedPreferences (Context context) {
        return context.getSharedPreferences(LauncherFiles.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
    }

    public static int validateNewPage(int newPage, int pageCount) {
        // whichPage can be OVER_FIRST_PAGE_INDEX or [0, count]
        if (!(newPage == OVER_FIRST_PAGE_INDEX)) {
            newPage = Math.max(0, Math.min(newPage, pageCount));
        }
        return newPage;
    }

}
