package com.sprd.launcher3.dynamicIcon;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.android.launcher3.BubbleTextView;
import com.android.launcher3.R;

import java.util.Calendar;

/**
 * Created by SPRD on 10/21/16.
 */
public class DynamicDeskclock extends DynamicIcon {

    private static String TAG = "DynamicDeskclock";

    private static final ComponentName sDeskClockComponentName = new ComponentName("com.android.deskclock",
            "com.android.deskclock.DeskClock");
    // Fraction of the length of second hand.
    private static final float SECOND_LENGTH_FACTOR = 0.4f;
    // Fraction of the length of minute hand.
    private static final float MINUTE_LENGTH_FACTOR = 0.32f;
    // Fraction of the length of hour hand.
    private static final float HOUR_LENGTH_FACTOR = 0.23f;

    private Paint mSecondPaint;
    private Paint mMinutePaint;
    private Paint mHourPaint;
    private int mSecondWidth;
    private int mMinuteWidth;
    private int mHourWidth;
    private int mCenterRadius;

    private int mLastHour;
    private int mLastMinute;
    private int mLastSecond;

    private Handler mSecondsHandler;

    private DynamicIconDrawCallback mClockCallback = new DynamicIconDrawCallback() {
        @Override
        public void drawDynamicIcon(Canvas canvas, View icon, float scale, int[] center) {
            draw(canvas, icon, scale, center);
        }
    };

    public DynamicDeskclock(Context context) {
        super(context);
    }

    @Override
    protected void init() {
        Resources res = mContext.getResources();

        mCenterRadius = res.getDimensionPixelSize(R.dimen.dynamic_clock_center_radius);

        mSecondPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mSecondPaint.setColor(res.getColor(R.color.dynamic_clock_second_hand));
        mSecondWidth = res.getDimensionPixelSize(R.dimen.dynamic_clock_second_width);

        mMinutePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mMinutePaint.setColor(res.getColor(R.color.dynamic_clock_minute_hand));
        mMinuteWidth = res.getDimensionPixelSize(R.dimen.dynamic_clock_minute_width);

        mHourPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mHourPaint.setColor(res.getColor(R.color.dynamic_clock_hour_hand));
        mHourWidth = res.getDimensionPixelOffset(R.dimen.dynamic_clock_hour_width);

        HandlerThread secondThread =  new HandlerThread("sec-thread");
        secondThread.start();
        mSecondsHandler = new Handler(secondThread.getLooper());
        Long nextSecond = SystemClock.uptimeMillis() / 1000 * 1000 + 1000;
        mSecondsHandler.postAtTime(new Runnable() {
            @Override
            public void run() {
                forceUpdateView();
                mSecondsHandler.postAtTime(this, SystemClock.uptimeMillis() / 1000 * 1000 + 1000);
            }
        }, nextSecond);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        mContext.registerReceiver(this, filter);
    }

    @Override
    protected boolean hasChanged() {
        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);

        if (mLastSecond != second
                || mLastMinute != minute
                || mLastHour != hour) {
            mLastSecond = second;
            mLastMinute = minute;
            mLastHour = hour;
            return true;
        } else {
            return false;
        }
    }

    public ComponentName getComponentName() {
        mComponent = sDeskClockComponentName;
        return sDeskClockComponentName;
    }

    public Drawable getStableBackground() {
        Drawable clockBackground = ContextCompat.getDrawable(mContext, R.drawable.ic_dial_plate);
        return clockBackground;
    }

    public DynamicIconDrawCallback getDynamicIconDrawCallback() {
        return mClockCallback;
    }

    private void draw(Canvas canvas, View icon, float scale, int[] center) {
        if (canvas == null || center == null || !(icon instanceof BubbleTextView)) {
            return;
        }

        int iconSize = ((BubbleTextView) icon).getIconSize();
        float secondLength = iconSize * SECOND_LENGTH_FACTOR;
        float minuteLength = iconSize * MINUTE_LENGTH_FACTOR;
        float hourLength = iconSize * HOUR_LENGTH_FACTOR;

        scale = Math.abs(scale);
        mSecondPaint.setStrokeWidth(mSecondWidth * scale);
        mMinutePaint.setStrokeWidth(mMinuteWidth * scale);
        mHourPaint.setStrokeWidth(mHourWidth * scale);

        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);

        mLastSecond = second;
        mLastMinute = minute;
        if(hour >=12) {
            hour -= 12;
        }
        mLastHour = hour;

        float Seconds = second;
        float Minutes = minute + second / 60.0f;
        float Hour = hour + Minutes / 60.0f;

        double radianSecond = (Seconds / 60.0f * 360f)/180f * Math.PI;
        double radianMinute = (Minutes / 60.0f * 360f)/180f * Math.PI;
        double radianHour = (Hour / 12.0f * 360f)/180f * Math.PI;

        float secondX = (float) (scale * secondLength * Math.sin(radianSecond));
        float secondY = (float) (scale * secondLength * Math.cos(radianSecond));

        float minuteX = (float) (scale * minuteLength * Math.sin(radianMinute));
        float minuteY = (float) (scale * minuteLength * Math.cos(radianMinute));

        float hourX = (float) (scale * hourLength * Math.sin(radianHour));
        float hourY = (float) (scale * hourLength * Math.cos(radianHour));

        canvas.save();
        canvas.drawCircle(center[0], center[1], scale * mCenterRadius, mSecondPaint);
        canvas.drawLine(center[0], center[1], center[0] + hourX, center[1] - hourY, mHourPaint);
        canvas.drawLine(center[0], center[1], center[0] + minuteX, center[1] - minuteY, mMinutePaint);
        canvas.drawLine(center[0], center[1], center[0] + secondX, center[1] - secondY, mSecondPaint);
        canvas.restore();
    }
}
