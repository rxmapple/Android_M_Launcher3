package com.sprd.launcher3.ext;

import android.os.Build;
import android.os.SystemProperties;
import android.util.Log;

/**
 * Created by rxmapple on 2016/9/18.
 */
public final class LogUtils {

    private static final String MODULE_NAME = "Launcher3";

    private static LogUtils INSTANCE;

    public static boolean DEBUG = false;
    public static boolean DEBUG_LOADER = false;
    public static boolean DEBUG_WIDGET = false;
    public static boolean DEBUG_RECEIVER = false;
    public static boolean DEBUG_RESUME_TIME = false;
    public static boolean DEBUG_DUMP_LOG = false;

    /** use android properties to control debug on/off. */
    // define system properties
    private static final String PROP_DEBUG_ALL = "launcher.debug.all";
    private static final String PROP_DEBUG = "launcher.debug";
    private static final String PROP_DEBUG_LOADER = "launcher.debug.loader";
    private static final String PROP_DEBUG_WIDGET = "launcher.debug.widget";
    private static final String PROP_DEBUG_RECEIVER = "launcher.debug.receiver";
    private static final String PROP_DEBUG_RESUME_TIME = "launcher.debug.resumetime";
    private static final String PROP_DEBUG_DUMP_LOG = "launcher.debug.dumplog";
    /** end */

    static {
        if (SystemProperties.getBoolean(PROP_DEBUG_ALL, false)) {
            DEBUG = true;
            DEBUG_LOADER = true;
            DEBUG_WIDGET = true;
            DEBUG_RECEIVER = true;
            DEBUG_RESUME_TIME = true;
            DEBUG_DUMP_LOG = true;
        } else {
            DEBUG = SystemProperties.getBoolean(PROP_DEBUG, !Build.TYPE.equals("user"));
            DEBUG_LOADER = SystemProperties.getBoolean(PROP_DEBUG_LOADER, false);
            DEBUG_WIDGET = SystemProperties.getBoolean(PROP_DEBUG_WIDGET, false);
            DEBUG_WIDGET = SystemProperties.getBoolean(PROP_DEBUG_RECEIVER, false);
            DEBUG_RESUME_TIME = SystemProperties.getBoolean(PROP_DEBUG_RESUME_TIME, false);
            DEBUG_DUMP_LOG = SystemProperties.getBoolean(PROP_DEBUG_DUMP_LOG, false);
        }
    }
    /**
     * private constructor here, It is a singleton class.
     */
    private LogUtils() {
    }


    public static LogUtils getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LogUtils();
        }
        return INSTANCE;
    }

    /**
     * The method prints the log, level error.
     *
     * @param tag the tag of the class.
     * @param msg the message to print.
     */
    public static void e(String tag, String msg) {
        Log.e(MODULE_NAME, tag + ", " + msg);
    }

    /**
     * The method prints the log, level error.
     *
     * @param tag the tag of the class.
     * @param msg the message to print.
     * @param t an exception to log.
     */
    public static void e(String tag, String msg, Throwable t) {
        Log.e(MODULE_NAME, tag + ", " + msg, t);
    }

    /**
     * The method prints the log, level warning.
     *
     * @param tag the tag of the class.
     * @param msg the message to print.
     */
    public static void w(String tag, String msg) {
        Log.w(MODULE_NAME, tag + ", " + msg);
    }

    /**
     * The method prints the log, level warning.
     *
     * @param tag the tag of the class.
     * @param msg the message to print.
     * @param t an exception to log.
     */
    public static void w(String tag, String msg, Throwable t) {
        Log.w(MODULE_NAME, tag + ", " + msg, t);
    }

    /**
     * The method prints the log, level debug.
     *
     * @param tag the tag of the class.
     * @param msg the message to print.
     */
    public static void i(String tag, String msg) {
        Log.i(MODULE_NAME, tag + ", " + msg);
    }

    /**
     * The method prints the log, level debug.
     *
     * @param tag the tag of the class.
     * @param msg the message to print.
     * @param t an exception to log.
     */
    public static void i(String tag, String msg, Throwable t) {
        Log.i(MODULE_NAME, tag + ", " + msg, t);
    }

    /**
     * The method prints the log, level debug.
     *
     * @param tag the tag of the class.
     * @param msg the message to print.
     */
    public static void d(String tag, String msg) {
        Log.d(MODULE_NAME, tag + ", " + msg);
    }

    /**
     * The method prints the log, level debug.
     *
     * @param tag the tag of the class.
     * @param msg the message to print.
     * @param t An exception to log.
     */
    public static void d(String tag, String msg, Throwable t) {
        Log.d(MODULE_NAME, tag + ", " + msg, t);
    }

    /**
     * The method prints the log, level debug.
     *
     * @param tag the tag of the class.
     * @param msg the message to print.
     */
    public static void v(String tag, String msg) {
        Log.v(MODULE_NAME, tag + ", " + msg);
    }

    /**
     * The method prints the log, level debug.
     *
     * @param tag the tag of the class.
     * @param msg the message to print.
     * @param t An exception to log.
     */
    public static void v(String tag, String msg, Throwable t) {
        Log.v(MODULE_NAME, tag + ", " + msg, t);
    }

}
