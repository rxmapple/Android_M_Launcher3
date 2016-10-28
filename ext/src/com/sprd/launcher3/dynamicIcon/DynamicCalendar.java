package com.sprd.launcher3.dynamicIcon;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.android.launcher3.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by SPRD on 10/21/16.
 */
public class DynamicCalendar extends DynamicIcon {

    private static final String TAG = "DynamicCalendar";

    private static final ComponentName sCalendarComponentName = new ComponentName("com.android.calendar",
            "com.android.calendar.AllInOneActivity");

    private String mLastDate = "";
    private Paint mDatePaint;
    private Paint mWeekPaint;
    private float mDateSize;
    private float mWeekSize;
    private Drawable mCalendarBackground;

    private DynamicIconDrawCallback mCalendarCallback = new DynamicIconDrawCallback() {
        @Override
        public void drawDynamicIcon(Canvas canvas, View icon, float scale, boolean createBitmap) {
            draw(canvas, icon, scale, createBitmap);
        }
    };

    public DynamicCalendar(Context context) {
        super(context);
        mCalendarBackground = ContextCompat.getDrawable(mContext, R.drawable.ic_calendar_plate);
    }

    @Override
    protected void init() {
        Resources res = mContext.getResources();
        Typeface font = Typeface.create("sans-serif-thin", Typeface.NORMAL);

        mDatePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDatePaint.setTypeface(font);
        mDatePaint.setTextAlign(Paint.Align.CENTER);
        mDateSize = res.getDimensionPixelSize(R.dimen.dynamic_calendar_date_size);

        mWeekPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mWeekPaint.setTextAlign(Paint.Align.CENTER);
        mWeekPaint.setTypeface(font);
        mWeekPaint.setColor(0Xffff0000);
        mWeekSize = res.getDimensionPixelSize(R.dimen.dynamic_calendar_week_size);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
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
        mComponent = sCalendarComponentName;
        return sCalendarComponentName;
    }

    public Drawable getStableBackground() {
        return mCalendarBackground;
    }

    public DynamicIconDrawCallback getDynamicIconDrawCallback() {
        return mCalendarCallback;
    }

    private void draw(Canvas canvas, View icon, float scale, boolean createBitmap) {
        String day = getTodayDate();
        String dayOfWeek = getTodayWeek();

        mDatePaint.setTextSize(mDateSize*scale);
        mWeekPaint.setTextSize(mWeekSize*scale);

        Paint.FontMetrics fm = mDatePaint.getFontMetrics();

        canvas.save();
        if (createBitmap) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            canvas.drawText(dayOfWeek, width/2, (float)(height/2 - fm.descent*1.6), mWeekPaint);
            canvas.drawText(day, width/2, (float)(height/2 - fm.ascent*0.55), mDatePaint);
        } else {
            int offsetY = getOffsetY(icon);
            int iconCenterX = icon.getScrollX() + (icon.getWidth() / 2);
            int iconCenterY = icon.getScrollY() + icon.getPaddingTop()  + (offsetY / 2);
            canvas.drawText(dayOfWeek, iconCenterX, (float)(iconCenterY - fm.descent*1.6), mWeekPaint);
            canvas.drawText(day, iconCenterX, (float)(iconCenterY - fm.ascent*0.55), mDatePaint);
        }
        canvas.restore();
    }

    private String getTodayDate() {
        Calendar c = Calendar.getInstance();
        int date = c.get(Calendar.DATE);
        return String.valueOf(date);
    }

    private String getTodayWeek() {
        long time=System.currentTimeMillis();
        Date date=new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("EEEE");
        String weekday = format.format(date);
        return weekday;
    }
}
