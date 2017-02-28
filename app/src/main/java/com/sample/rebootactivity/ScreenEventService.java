package com.sample.rebootactivity;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by y_hisano on 2017/02/28.
 */

public class ScreenEventService extends IntentService {

    private static final String TAG = ScreenEventService.class.getSimpleName();

    private PreferencesManager mPreferencesManager;

    public ScreenEventService() {
        super("ScreenEventService");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mPreferencesManager = new PreferencesManager(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Log.d(TAG, String.format("onHandleIntent intent %s", intent.toString()));
            handleIntent(intent);
        } finally {
            ScreenEventReceiver.completeWakefulIntent(intent);
        }
    }

    private void handleIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        if (TextUtils.equals(intent.getAction(), Intent.ACTION_SCREEN_ON)) {
            cancelAllRemainder();
            registerRemainder(Remainder.AWAKE);
        }
        else if (TextUtils.equals(intent.getAction(), Intent.ACTION_SCREEN_OFF)) {
            cancelAllRemainder();
            registerRemainder(Remainder.SLEEP);
        }
        else if (TextUtils.equals(intent.getAction(), Remainder.AWAKE.name())) {
            invokeIntent(mPreferencesManager.getMainAppStartScheme());
        }
        else if (TextUtils.equals(intent.getAction(), Remainder.SLEEP.name())) {
            invokeIntent(mPreferencesManager.getMainAppFinishScheme());
        }
    }

    private void invokeIntent(String schema) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(schema));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void registerRemainder(Remainder remainder) {
        Intent intent = new Intent(getApplicationContext(), ScreenEventReceiver.class);
        intent.setAction(remainder.name());

        PendingIntent pIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        long trigger = System.currentTimeMillis() + mPreferencesManager.getRemainderTimeMillis();
        getAlarmManager().set(AlarmManager.RTC_WAKEUP, trigger, pIntent);
    }

    private void cancelAllRemainder() {
        for (Remainder remainder : Remainder.values()) {
            Intent intent = new Intent(getApplicationContext(), ScreenEventReceiver.class);
            intent.setAction(remainder.name());
            PendingIntent contentIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

            getAlarmManager().cancel(contentIntent);
            contentIntent.cancel();
        }
    }

    private AlarmManager getAlarmManager() {
        return (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    public enum Remainder {
        SLEEP,
        AWAKE,
    }
}
