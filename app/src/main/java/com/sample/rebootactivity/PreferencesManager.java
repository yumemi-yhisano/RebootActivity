package com.sample.rebootactivity;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by y_hisano on 2017/02/28.
 */

public class PreferencesManager {

    private static final String KEY_REMAINDER_TIME_MILLIS = "REMAINDER_TIME_MILLIS";

    private static final String KEY_MAIN_APP_START_SCHEME = "KEY_MAIN_APP_START_SCHEME";

    private static final String KEY_MAIN_APP_FINISH_SCHEME = "KEY_MAIN_APP_FINISH_SCHEME";

    private static final String KEY_MAIN_APP_PACKAGE_NAME = "KEY_MAIN_APP_PACKAGE_NAME";

    private final SharedPreferences mPreferences;

    public PreferencesManager(Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    private SharedPreferences.Editor getEditor() {
        return mPreferences.edit();
    }

    public void setRemainderTimeMillis(long millis) {
        getEditor().putLong(KEY_REMAINDER_TIME_MILLIS, millis).apply();
    }

    public long getRemainderTimeMillis() {
        return mPreferences.getLong(KEY_REMAINDER_TIME_MILLIS, 15_000);
    }

    public void setMainAppStartScheme(String scheme) {
        getEditor().putString(KEY_MAIN_APP_START_SCHEME, scheme).apply();
    }

    public String getMainAppStartScheme() {
        return mPreferences.getString(KEY_MAIN_APP_START_SCHEME, "designpatternapp://");
    }

    public void setMainAppFinishScheme(String scheme) {
        getEditor().putString(KEY_MAIN_APP_FINISH_SCHEME, scheme).apply();
    }

    public String getMainAppFinishScheme() {
        return mPreferences.getString(KEY_MAIN_APP_FINISH_SCHEME, "designpatternapp://finish");
    }

    public void setMainAppPackageName(String name) {
        getEditor().putString(KEY_MAIN_APP_PACKAGE_NAME, name).apply();
    }

    public String getMainAppPackageName() {
        return mPreferences.getString(KEY_MAIN_APP_PACKAGE_NAME, "com.sample.designpatternapp");
    }
}
