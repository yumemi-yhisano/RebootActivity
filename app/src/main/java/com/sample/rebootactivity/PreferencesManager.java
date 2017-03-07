package com.sample.rebootactivity;

import android.content.Context;

import net.grandcentrix.tray.TrayPreferences;

/**
 * Created by y_hisano on 2017/02/28.
 */

public class PreferencesManager {

    private static final int TRAY_VERSION = 1;

    private static final String KEY_REMAINDER_TIME_MILLIS = "REMAINDER_TIME_MILLIS";

    private final TrayPreferences mPreferences;

    public PreferencesManager(Context context) {
        mPreferences = new TrayPreferences(context, context.getPackageName(), TRAY_VERSION);
    }

    public void setRemainderTimeMillis(long millis) {
        mPreferences.put(KEY_REMAINDER_TIME_MILLIS, millis);
    }

    public long getRemainderTimeMillis() {
        return mPreferences.getLong(KEY_REMAINDER_TIME_MILLIS, 15_000);
    }
}
