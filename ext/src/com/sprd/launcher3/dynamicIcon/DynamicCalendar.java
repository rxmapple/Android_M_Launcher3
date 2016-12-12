package com.sprd.launcher3.dynamicIcon;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.android.launcher3.BubbleTextView;
import com.android.launcher3.R;
import com.sprd.launcher3.ext.UtilitiesExt;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by SPRD on 10/21/16.
 */
public class DynamicCalendar extends DynamicIcon {

    private static final String TAG = "DynamicCalendar";

    private static final ComponentName sCalendarComponentName = new ComponentName("com.android.calendar",
            "com.android.calendar.AllInOneActivity");
    // The proportion of the date font occupied in the dynamic calendar icon.
    private static final float DATE_SIZE_FACTOR = 0.6f;
    // The proportion of the week font occupied in the dynamic calendar icon.
    private static final float WEEK_SIZE_FACTOR = 0.18f;

    private String mLastDate = "";
    private int[] mDefaultDate;
    private Paint mDatePaint;
    private Paint mWeekPaint;
    private Drawable mCalendarBackground;

    private DynamicIconDrawCallback mCalendarCallback = new DynamicIconDrawCallback() {
        @Override
        public void drawDynamicIcon(Canvas canvas, View icon, float scale, int[] center) {
                draw(canvas, icon, scale, center);
        }
    };

    public DynamicCalendar(Context context, int type) {
        super(context, type);

        mCalendarBackground = ContextCompat.getDrawable(mContext, R.drawable.ic_calendar_plate);
        mComponent = ComponentName.unflattenFromString(
                mContext.getResources().getString(R.string.default_dynamic_calendar));
        if (!isAppInstalled(mComponent)) {
            mComponent = sCalendarComponentName;
        }
        mIsChecked = isAppInstalled() && UtilitiesExt.getLauncherSettingsBoolean(mContext,
                DynamicIconSettingsFragment.PRE_DYNAMIC_CALENDAR,
                mContext.getResources().getBoolean(R.bool.config_show_dynamic_calendar));
    }

    @Override
    protected void init() {
        Typeface font = Typeface.create("sans-serif-thin", Typeface.NORMAL);

        mDatePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDatePaint.setTypeface(font);
        mDatePaint.setTextAlign(Paint.Align.CENTER);

        mWeekPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWeekPaint.setTextAlign(Paint.Align.CENTER);
        mWeekPaint.setTypeface(font);
        mWeekPaint.setColor(0Xffff0000);

        mDefaultDate = mContext.getResources().getIntArray(R.array.config_defaultCalendarDate);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        mContext.registerReceiver(this, filter);
    }

    @Override
    protected boolean hasChanged() {
        if (mLastDate.equals(getTodayDate())) {
            return false;
        } else {
            mLastDate = getTodayDate();
            return true;
        }
    }

    public ComponentName getComponentName() {
        return mComponent;
    }

    public Drawable getStableBackground() {
        return mCalendarBackground;
    }

    public DynamicIconDrawCallback getDynamicIconDrawCallback() {
        return mCalendarCallback;
    }

    private void draw(Canvas canvas, View icon, float scale, int[] center) {
        if (canvas == null || center == null || !(icon instanceof BubbleTextView)) {
            return;
        }

        String day;
        String dayOfWeek;
        if (mIsChecked) {
            day = getTodayDate();
            dayOfWeek = getTodayWeek();
        } else  {
            GregorianCalendar date = new GregorianCalendar(mDefaultDate[0], mDefaultDate[1], mDefaultDate[2]);
            SimpleDateFormat format = new SimpleDateFormat("EEEE");
            String weekday = format.format(date.getTime());

            day = Integer.toString(mDefaultDate[2]);
            dayOfWeek = weekday;
        }

        int iconSize = ((BubbleTextView) icon).getIconSize();
        float dateSize = iconSize * DATE_SIZE_FACTOR;
        float weekSize = iconSize * WEEK_SIZE_FACTOR;
        mDatePaint.setTextSize(scale * dateSize);
        mWeekPaint.setTextSize(scale * weekSize);

        Paint.FontMetrics fm = mDatePaint.getFontMetrics();
        float dateHeight = -fm.ascent;
        float dateBaseline = (float)(center[1] - fm.ascent * 0.58);

        canvas.save();
        canvas.drawText(dayOfWeek, center[0], dateBaseline - dateHeight, mWeekPaint);
        canvas.drawText(day, center[0], dateBaseline, mDatePaint);
        canvas.restore();
    }

    private String getTodayDate() {
        Calendar c = Calendar.getInstance();
        int date = c.get(Calendar.DATE);
        return String.valueOf(date);
    }

    private String getTodayWeek() {
        long time = System.currentTimeMillis();
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("EEEE");
        String weekday = format.format(date);
        return weekday;
    }
}
