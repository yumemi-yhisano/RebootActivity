package com.sample.rebootactivity;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

/**
 * Created by y_hisano on 2017/02/23.
 */

public class MainControlService extends Service {

    public final static String TAG = MainControlService.class.getSimpleName();
    public final static int MSG_WHAT_ID_FINISH_ACTIVITY = 10;
    public final static int MSG_WHAT_ID_CONNECTED = 11;

    public final static String START_ID = "start_id";
    public final static int START_ID_FROM_ACTIVITY = 100;
    public final static int START_ID_FROM_RECEIVER = 101;

    private Messenger mMessenger;
    private Messenger mReplayMessenger;
    private boolean mConnected;
    private BroadcastReceiver mReceiver = new ScreenEventReceiver();
    private ServiceView mServiceView = new OverlayView(this, new OverlayView.ClickListener() {
        @Override
        public void onStartActivityClick(View view) {
            openMainActivity();
        }

        @Override
        public void onCloseActivityClick(View view) {
            closeMainActivity();
        }

        @Override
        public void onCloseServiceClick(View view) {
            MainControlService.this.stopSelf();
        }
    });

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        mMessenger = new Messenger(new Handler(this.getMainLooper(), new MessengerCallback()));
        mServiceView.onCreate();

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mServiceView.onDestroy();
        unregisterReceiver(mReceiver);

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, String.format("onStartCommand %s start_id:%d", intent.toString(), intent.getIntExtra(START_ID, 0)));
        if (intent.getIntExtra(START_ID, 0) == START_ID_FROM_RECEIVER) {
            if (TextUtils.equals(intent.getAction(), Intent.ACTION_SCREEN_ON) && ! mConnected) {
                openMainActivity();
            }
            else if (TextUtils.equals(intent.getAction(), Intent.ACTION_SCREEN_OFF) && mConnected) {
                closeMainActivity();
            }
            ScreenEventReceiver.completeWakefulIntent(intent);
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        mConnected = true;
        mServiceView.onBind();

        return mMessenger.getBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind");
        mConnected = true;
        mServiceView.onBind();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        mReplayMessenger = null;

        mConnected = false;
        mServiceView.onUnBind();

        return true;
    }

    private void openMainActivity() {
        Intent intent = new Intent(MainControlService.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void closeMainActivity() {
        if (mReplayMessenger == null) {
            return;
        }
        Message msg = Message.obtain();
        msg.what = MSG_WHAT_ID_FINISH_ACTIVITY;
        try {
            mReplayMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private class MessengerCallback implements Handler.Callback {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == MainControlService.MSG_WHAT_ID_CONNECTED) {
                mReplayMessenger = msg.replyTo;
                return true;
            }
            return false;
        }
    }
}
