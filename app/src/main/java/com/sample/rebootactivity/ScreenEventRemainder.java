package com.sample.rebootactivity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import static android.content.Context.ALARM_SERVICE;

/**
 * Created by y_hisano on 2017/02/28.
 */

public class ScreenEventRemainder {

    private static final String TAG = ScreenEventRemainder.class.getSimpleName();

    private final Context mContext;
    private final PreferencesManager mPreferencesManager;
    private final EventCallback mEventCallback;

    public ScreenEventRemainder(Context context, PreferencesManager preferencesManager, EventCallback eventCallback) {
        mContext = context;
        mPreferencesManager = preferencesManager;
        mEventCallback = eventCallback;
    }

    public boolean handleIntent(Intent intent) {
        if (intent == null || TextUtils.isEmpty(intent.getAction())) {
            return false;
        }

        if (TextUtils.equals(intent.getAction(), Intent.ACTION_SCREEN_ON)) {
            cancelAllRemainder();
            mEventCallback.onScreenOn();
            return true;
        }
        else if (TextUtils.equals(intent.getAction(), Intent.ACTION_SCREEN_OFF)) {
            cancelAllRemainder();
            registerRemainder(Remainder.SLEEP);
            mEventCallback.onScreenOff();
            return true;
        }
        else if (TextUtils.equals(intent.getAction(), Remainder.AWAKE.name())) {
            mEventCallback.onRemainderAwake();
            return true;
        }
        else if (TextUtils.equals(intent.getAction(), Remainder.SLEEP.name())) {
            mEventCallback.onRemainderSleep();
            return true;
        }
        return false;
    }

    private void registerRemainder(Remainder remainder) {
        Intent intent = new Intent(mContext, ScreenEventReceiver.class);
        intent.setAction(remainder.name());

        PendingIntent pIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        long trigger = System.currentTimeMillis() + mPreferencesManager.getRemainderTimeMillis();
        getAlarmManager().set(AlarmManager.RTC_WAKEUP, trigger, pIntent);
    }

    private void cancelAllRemainder() {
        for (Remainder remainder : Remainder.values()) {
            Intent intent = new Intent(mContext, ScreenEventReceiver.class);
            intent.setAction(remainder.name());
            PendingIntent contentIntent = PendingIntent.getBroadcast(mContext, 0, intent, 0);

            getAlarmManager().cancel(contentIntent);
            contentIntent.cancel();
        }
    }

    private AlarmManager getAlarmManager() {
        return (AlarmManager) mContext.getSystemService(ALARM_SERVICE);
    }

    public enum Remainder {
        SLEEP,
        AWAKE,
    }

    public interface EventCallback {
        void onScreenOn();
        void onScreenOff();
        void onRemainderAwake();
        void onRemainderSleep();
    }
}
