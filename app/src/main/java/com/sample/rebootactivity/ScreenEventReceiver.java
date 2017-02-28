package com.sample.rebootactivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

/**
 * Created by y_hisano on 2017/02/28.
 */

public class ScreenEventReceiver extends WakefulBroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        ComponentName comp = new ComponentName(context.getPackageName(), ScreenEventService.class.getName());

        // サービス起動
        startWakefulService(context, intent.setComponent(comp));
    }
}
