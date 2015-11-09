package com.example.ulrich.ipmap;

import android.util.Log;

/**
 * Logger
 */
public final class AppLog {
    private static final String TAG = "IpMap";

    private AppLog() {
    }

    public static void d(String msg, Object... args) {
        if (args != null && args.length > 0) {
            msg = String.format(msg, args);
        }
        Log.d(TAG, msg);
    }

    public static void e(Throwable t, String msg, Object... args) {
        if (args != null && args.length > 0) {
            msg = String.format(msg, args);
        }
        Log.e(TAG, msg, t);
    }
}
