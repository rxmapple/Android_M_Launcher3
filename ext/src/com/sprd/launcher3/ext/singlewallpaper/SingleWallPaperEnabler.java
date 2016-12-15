package com.sprd.launcher3.ext.singlewallpaper;

import java.io.File;

import android.app.ActionBar;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import com.android.gallery3d.common.BitmapCropTask;
import com.android.gallery3d.common.BitmapUtils;
import com.android.launcher3.CropView;
import com.android.launcher3.LauncherFiles;
import com.android.launcher3.R;
import com.android.launcher3.WallpaperCropActivity;
import com.android.launcher3.WallpaperPickerActivity;
import com.android.launcher3.WallpaperPickerActivity.FileWallpaperInfo;
import com.android.launcher3.util.WallpaperUtils;
import com.sprd.launcher3.ext.LogUtils;

/**
 * Created by SPREADTRUM\hualan.su on 10/20/16.
 */

public class SingleWallPaperEnabler implements OnCheckedChangeListener,
        OnLayoutChangeListener {

    private static final String TAG = "Launcher3.SingleWallPaperEnabler";
    private static final String DEFAULT_WALLPAPER = "default.jpg";
    private Point mDefaultWallpaperSize;
    private boolean mIsSingleMode;
    private boolean mIsSourceChange;
    private int mCropViewHeight = 0;
    private boolean mIsPortrait;
    private boolean mIsLayoutChange;
    private WallpaperCropActivity mWallpaperCropActivity;
    private Switch mSwitchButton;
    private CropView mCropView;

    public SingleWallPaperEnabler(WallpaperCropActivity wallpaperCropActivity) {
        mWallpaperCropActivity = wallpaperCropActivity;
        mDefaultWallpaperSize = WallpaperUtils.getDefaultWallpaperSize(
                wallpaperCropActivity.getResources(),
                wallpaperCropActivity.getWindowManager());
    }

    public Switch createSwitchButton(ActionBar actionBar,CropView cropView){
        actionBar.setCustomView(R.layout.actionbar_single_wallpaper);
        mSwitchButton = (Switch)actionBar.getCustomView().findViewById(R.id.single_wallpaper_switch);
        mCropView = cropView;
        setOnChangeListener();
        return mSwitchButton;
    }

    private void setOnChangeListener() {
        mSwitchButton.setOnCheckedChangeListener(this);
        mCropView.addOnLayoutChangeListener(this);
    }

    public boolean needCheckLayoutChange(Object req) {
        synchronized (mWallpaperCropActivity) {
            return req != mSwitchButton.getTag();
        }
    }

    public boolean checkLayoutChange(Object req, boolean isChecked) {
        synchronized (mWallpaperCropActivity) {
            mIsSourceChange = true;
            mSwitchButton.setTag(req);
        }
        if (mSwitchButton.getVisibility() != View.VISIBLE) {
            mSwitchButton.setVisibility(View.VISIBLE);
        }
        onCheckedChanged(mSwitchButton, isChecked);
        if (LogUtils.DEBUG) {
            Log.d(TAG, "mIsLayoutChange --" + mIsLayoutChange);
        }
        return mIsLayoutChange;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        synchronized (mWallpaperCropActivity) {
            if (mIsSingleMode != isChecked || mIsSourceChange) {
                if (LogUtils.DEBUG) {
                    Log.d(TAG, "mOldIsChecked = " + mIsSingleMode);
                    Log.d(TAG, "isChecked = " + isChecked);
                }
                setSingleMode(isChecked);
                mSwitchButton.setText(mIsSingleMode ? R.string.single_wallpaper
                        : R.string.scroll_wallpaper);
                checkCropViewLayoutChange();
                mIsSourceChange = false;
                buttonView.setChecked(mIsSingleMode);
            }
        }
    }

    private void setSingleMode(boolean isSingleMode) {
        Point displaySize = getDisplayPoint();
        boolean isPortrait = displaySize.x < displaySize.y;
        if (mCropViewHeight == 0 || mIsSingleMode != isSingleMode
                || isPortrait != mIsPortrait) {
            mIsSingleMode = isSingleMode;
            mIsPortrait = isPortrait;
            computeCropViewHeight(displaySize.x,displaySize.y);
        }
    }

    private void computeCropViewHeight(int screenWidth,int screenHight) {
        if (!mIsSingleMode) {
            screenHight = screenHight * screenWidth
                    / mDefaultWallpaperSize.x;
        }
        if (LogUtils.DEBUG) {
            Log.d(TAG, "cumputeCropViewHeight screenHight = " + screenHight
                    + " isSingleMode = " + mIsSingleMode
                    + " mCropViewHeight = " + mCropViewHeight);
        }
        if (mCropViewHeight != screenHight) {
            mCropViewHeight = screenHight;
        }
    }

    private void checkCropViewLayoutChange() {
        if (mCropView.getMeasuredHeight() != mCropViewHeight) {
            mIsLayoutChange = true;
            mWallpaperCropActivity.setWallpaperButtonEnabled(false);
            mCropView.onPause();
            mCropView.setVisibility(View.INVISIBLE);

            if (LogUtils.DEBUG) {
                Log.d(TAG, "changeCropViewLayout mGLSurfaceViewHeight = "
                        + mCropView.getMeasuredHeight() + " isSingleMode = "
                        + mIsSingleMode + " mCropViewHeight = "
                        + mCropViewHeight);
            }
            if (mCropView.getLayoutParams() instanceof FrameLayout.LayoutParams) {
                ((FrameLayout.LayoutParams) mCropView.getLayoutParams()).gravity = Gravity.CENTER;
            } else if (mCropView.getLayoutParams() instanceof RelativeLayout.LayoutParams) {
                ((RelativeLayout.LayoutParams) mCropView.getLayoutParams())
                        .addRule(RelativeLayout.CENTER_IN_PARENT);
            }
            mCropView.getLayoutParams().height = mCropViewHeight;
        }
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right,
            int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        synchronized (mWallpaperCropActivity) {
            if (LogUtils.DEBUG) {
                Log.d(TAG,
                        "onLayoutChange -- CropView Height = "
                                + mCropView.getHeight() + "isLayoutChange = "
                                + mIsLayoutChange
                                + " mSwitchButton.getTag() = "
                                + mSwitchButton.getTag());
            }
            if (mIsLayoutChange) {
                mCropView.setVisibility(View.VISIBLE);
                mCropView.onResume();
                mWallpaperCropActivity.onLayoutChangeComplete(
                        mSwitchButton.getTag(), true);
                mIsLayoutChange = false;
                mWallpaperCropActivity.setWallpaperButtonEnabled(true);
            }
        }
    }

    private Point getDisplayPoint() {
        Display d = mWallpaperCropActivity.getWindowManager()
                .getDefaultDisplay();
        Point displaySize = new Point();
        d.getSize(displaySize);
        return displaySize;
    }

    public void cropImageAndSetWallpaper(Uri uri, Resources res, int resId,
            BitmapCropTask.OnBitmapCroppedHandler onBitmapCroppedHandler,
            final boolean finishActivityWhenDone) {
        final Point outSize;
        Point displaySize = getDisplayPoint();
        boolean isPortrait = displaySize.x < displaySize.y;
        final int outSizeY;
        if (mSwitchButton.isChecked()) {
            outSize = displaySize;
            outSizeY = outSize.y;
        } else {
            outSize = mDefaultWallpaperSize;
            if (!isPortrait) {
                outSizeY = displaySize.y;
            } else {
                outSizeY = outSize.y;
            }
        }
        RectF crop = mCropView.getCrop();
        if (LogUtils.DEBUG) {
            Log.d(TAG, "cropImageAndSetWallpaper crop" + crop.top + " x "
                    + crop.left);
            Log.d(TAG, "cropImageAndSetWallpaper crop" + crop.bottom + " x "
                    + crop.right);
            Log.d(TAG, "cropImageAndSetWallpaper out" + outSize.x + " x "
                    + outSizeY);
        }

        Runnable onEndCrop = new Runnable() {
            public void run() {
                mWallpaperCropActivity.updateSingleWallpaperDimensions(
                        outSize.x, outSizeY);
                if (finishActivityWhenDone) {
                    mWallpaperCropActivity.setResult(Activity.RESULT_OK);
                    mWallpaperCropActivity.finish();
                }
            }
        };

        BitmapCropTask cropTask = null;
        if (uri != null) {
            int rotation = BitmapUtils.getRotationFromExif(
                mWallpaperCropActivity.getContext(), uri);
            cropTask = new BitmapCropTask(mWallpaperCropActivity, uri, crop,
                    rotation, outSize.x, outSizeY, true, false, onEndCrop);
        } else if (res != null && resId != 0) {
            int rotation = BitmapUtils.getRotationFromExif(res, resId);
            cropTask = new BitmapCropTask(mWallpaperCropActivity, res, resId,
                    crop, rotation, outSize.x, outSizeY, true, false, onEndCrop);
        }

        if (cropTask != null) {
            if (onBitmapCroppedHandler != null) {
                cropTask.setOnBitmapCropped(onBitmapCroppedHandler);
            }
            cropTask.execute();
        }
    }

    // WallpaperPickerActivity add method-------------------------start
    private File getDefaultFile(
            WallpaperPickerActivity wallpaperPickerActivity, boolean isThumb) {
        return new File(wallpaperPickerActivity.getFilesDir(),
                Build.VERSION.SDK_INT
                        + "_"
                        + (isThumb ? LauncherFiles.DEFAULT_WALLPAPER_THUMBNAIL
                                : DEFAULT_WALLPAPER));
    }

    private boolean saveDefaultWallpaper(
            WallpaperPickerActivity wallpaperPickerActivity, Bitmap b,
            boolean isThumb) {
        // Delete old thumbnails.
        new File(wallpaperPickerActivity.getFilesDir(),
                LauncherFiles.DEFAULT_WALLPAPER_THUMBNAIL_OLD).delete();
        new File(wallpaperPickerActivity.getFilesDir(),
                LauncherFiles.DEFAULT_WALLPAPER_THUMBNAIL).delete();

        for (int i = Build.VERSION_CODES.JELLY_BEAN; i < Build.VERSION.SDK_INT; i++) {
            new File(wallpaperPickerActivity.getFilesDir(), i
                    + "_"
                    + (isThumb ? LauncherFiles.DEFAULT_WALLPAPER_THUMBNAIL
                            : DEFAULT_WALLPAPER)).delete();
        }
        return wallpaperPickerActivity.writeDefaultImageToFileAsJpeg(
                getDefaultFile(wallpaperPickerActivity, isThumb), b);
    }

    public FileWallpaperInfo getDefaultFileWallpaper(
            WallpaperPickerActivity wallpaperPickerActivity) {
        File defaultThumbFile = getDefaultFile(wallpaperPickerActivity, true);
        File defaultFile = getDefaultFile(wallpaperPickerActivity, false);
        Bitmap thumb = null;
        Bitmap wallpaper = null;
        boolean defaultWallpaperExists = false;
        if (defaultFile.exists()) {
            thumb = BitmapFactory
                    .decodeFile(defaultThumbFile.getAbsolutePath());
            defaultWallpaperExists = true;
        } else {
            Resources res = wallpaperPickerActivity.getResources();
            Point defaultThumbSize = new Point(
                    res.getDimensionPixelSize(R.dimen.wallpaperThumbnailWidth),
                    res.getDimensionPixelSize(R.dimen.wallpaperThumbnailHeight));
            Drawable wallpaperThumbDrawable = WallpaperManager.getInstance(
                    wallpaperPickerActivity).getBuiltInDrawable(
                    defaultThumbSize.x, defaultThumbSize.y, true, 0.5f, 0.5f);
            if (wallpaperThumbDrawable != null) {
                thumb = Bitmap.createBitmap(defaultThumbSize.x,
                        defaultThumbSize.y, Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(thumb);
                wallpaperThumbDrawable.setBounds(0, 0, defaultThumbSize.x,
                        defaultThumbSize.y);
                wallpaperThumbDrawable.draw(c);
                c.setBitmap(null);
            }

            if (thumb != null) {
                saveDefaultWallpaper(wallpaperPickerActivity, thumb, true);
            }

            Drawable wallpaperDrawable = WallpaperManager.getInstance(
                    wallpaperPickerActivity).getBuiltInDrawable();
            if (wallpaperDrawable != null) {
                wallpaper = Bitmap.createBitmap(
                        wallpaperDrawable.getIntrinsicWidth(),
                        wallpaperDrawable.getIntrinsicHeight(),
                        Bitmap.Config.ARGB_8888);
                Canvas c = new Canvas(wallpaper);
                wallpaperDrawable.setBounds(0, 0,
                        wallpaperDrawable.getIntrinsicWidth(),
                        wallpaperDrawable.getIntrinsicHeight());
                wallpaperDrawable.draw(c);
                c.setBitmap(null);
            }
            if (wallpaper != null) {
                defaultWallpaperExists = saveDefaultWallpaper(
                        wallpaperPickerActivity, wallpaper, false);
                defaultFile = getDefaultFile(wallpaperPickerActivity, false);
            }
        }
        if (defaultWallpaperExists) {
            return new FileWallpaperInfo(defaultFile, new BitmapDrawable(thumb));
        }
        return null;
    }
    // WallpaperPickerActivity add method-------------------------end

    public void setEnabled(boolean enabled) {
        mSwitchButton.setEnabled(enabled);
    }

    public void setVisibility(int visibility) {
        // TODO Auto-generated method stub
        mSwitchButton.setVisibility(visibility);
    }
}
