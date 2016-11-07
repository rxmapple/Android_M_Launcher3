package com.sprd.launcher3.ext.defaultpage;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.launcher3.CellLayout;
import com.android.launcher3.Launcher;
import com.android.launcher3.R;
import com.sprd.launcher3.ext.LogUtils;

/**
 * Created by SPREADTRUM\hualan.su on 10/20/16.
 */

public class HomeImageView extends ImageView implements View.OnClickListener {
    private String TAG = "Launcher3.HomeImageView";
    private Launcher mLauncher;
    private CellLayout mParentCellLayout;
    private boolean mIsDefaultHome;
    private static int mDefaultColor = Color.argb(255, 0, 255, 255);

    public HomeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

     public HomeImageView(Context context,CellLayout cellLayout) {
        super(context);
        mLauncher = (Launcher) context;
        mParentCellLayout = cellLayout;
        mParentCellLayout.addView(this);
        setImageResource(R.drawable.home_icon);
        setVisibility(View.GONE);
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        changeDefaultHome();
    }

    private void changeDefaultHome() {
        int oldHomeIndex = mLauncher.getWorkspace().getDefaultPage();
        int currentIndex = mLauncher.getWorkspace().indexOfChild(mParentCellLayout);
        if(oldHomeIndex != currentIndex){
            
            DefaultPageUtil.saveSharedDefaultPage(mLauncher, currentIndex);
            this.updateImageTintList(true);
            
            View oldHomeCellLayout = mLauncher.getWorkspace().getChildAt(oldHomeIndex);
            if(oldHomeCellLayout instanceof CellLayout){
                HomeImageView setHomeImageView = ((CellLayout) oldHomeCellLayout)
                        .getHomeImageView();
                if(setHomeImageView != null){
                    setHomeImageView.updateImageTintList(false);
                }
            }
        }
    }

    public void setLayout(int left, int top, int width) {
        int homeImageWidth = getMeasuredWidth();
        int homeImageHeight = getMeasuredHeight();
        if (DefaultPageUtil.isHorizontalMode(homeImageWidth,homeImageHeight)) {
            if(LogUtils.DEBUG){
                Log.d(TAG,  "setLayout Horizontal mode mParentCellLayout  move down");
            }
            mParentCellLayout.setTop(mParentCellLayout.getTop() +  homeImageHeight / 2);
            mParentCellLayout.setBottom(mParentCellLayout.getBottom() +homeImageHeight /2);

            LinearLayout overviewPanel = (LinearLayout)mLauncher.getOverviewPanel();
            if (overviewPanel != null ) {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) overviewPanel.getLayoutParams();
                if(lp.height != LayoutParams.WRAP_CONTENT) {
                    if(LogUtils.DEBUG){
                        Log.d(TAG,  "setLayout Horizontal mode overviewPanel move down");
                    }
                    lp.height = LayoutParams.WRAP_CONTENT;
                    lp.topMargin = 0;
                    overviewPanel.setLayoutParams(lp);
                    overviewPanel.setGravity(Gravity.BOTTOM);
                }
            }
        }
        int homeImageViewleft = left + (width - homeImageWidth) / 2;
        layout(homeImageViewleft, top, homeImageViewleft
                + homeImageWidth, top + homeImageHeight);
    }

    public void updateVisibility(){
        if (getVisibility() != View.VISIBLE && isHomeShow()) {
            setVisibility(View.VISIBLE);
        }else if (getVisibility() != View.GONE && !isHomeShow()) {
            setVisibility(View.GONE);
        }
    }

    public void updateImageTintList(boolean defaultHome){
        if (mIsDefaultHome != defaultHome) {
            mIsDefaultHome = defaultHome;
            if (mIsDefaultHome) {
                setImageTintList(ColorStateList.valueOf(mDefaultColor));
            }else{
                setImageTintList(null);
            }
        }
    }

    public boolean isDefaultHome(){
        return mIsDefaultHome;
    }

    public boolean isHomeShow() {
        return !mParentCellLayout.isHotseat()
                && mLauncher.getWorkspace().isInOverviewMode();
    }
}
