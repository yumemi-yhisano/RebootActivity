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
import android.util.Log;
import android.view.View;

/**
 * Created by y_hisano on 2017/02/23.
 */

public class MainControlService extends Service implements OverlayView.ClickListener, ScreenEventRemainder.EventCallback {

    public final static String TAG = MainControlService.class.getSimpleName();
    public final static int MSG_WHAT_ID_FINISH_ACTIVITY = 10;
    public final static int MSG_WHAT_ID_CONNECTED = 11;
    public final static int MSG_WHAT_ID_SCREEN_ON = 12;
    public final static int MSG_WHAT_ID_SCREEN_OFF = 13;

    public final static String START_ID = "start_id";
    public final static int START_ID_FROM_ACTIVITY = 100;
    public final static int START_ID_FROM_RECEIVER = 101;

    private Messenger mMessenger;
    private Messenger mReplayMessenger;
    private boolean mConnected;
    private BroadcastReceiver mReceiver = new ScreenEventReceiver();
//    private ServiceView mServiceView;
    private ScreenEventRemainder mScreenEventRemainder;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        mScreenEventRemainder = new ScreenEventRemainder(getApplicationContext(), new PreferencesManager(getApplicationContext()), this);
//        mServiceView = new OverlayView(getApplicationContext(), this);

        mMessenger = new Messenger(new Handler(this.getMainLooper(), new MessengerCallback()));
//        mServiceView.onCreate();

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
//        mServiceView.onDestroy();
        unregisterReceiver(mReceiver);

        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Log.d(TAG, String.format("onStartCommand null"));
            return START_STICKY;
        }
        Log.d(TAG, String.format("onStartCommand %s start_id:%d", intent.toString(), intent.getIntExtra(START_ID, 0)));

        mScreenEventRemainder.handleIntent(intent);
        if (intent.getIntExtra(START_ID, 0) == START_ID_FROM_RECEIVER) {
            ScreenEventReceiver.completeWakefulIntent(intent);
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        mConnected = true;
//        mServiceView.onBind();

        return mMessenger.getBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind");
        mConnected = true;
//        mServiceView.onBind();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        mReplayMessenger = null;

        mConnected = false;
//        mServiceView.onUnBind();

        return true;
    }

    private void openMainActivity() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(MainControlService.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        }, 2_000);
//        Intent intent = new Intent(MainControlService.this, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        startActivity(intent);
    }

    private void closeMainActivity() {
        sendMessageToActivity(MSG_WHAT_ID_FINISH_ACTIVITY);
    }

    private void sendMessageToActivity(int what) {
        if (mReplayMessenger == null) {
            return;
        }
        Message msg = Message.obtain();
        msg.what = what;
        try {
            mReplayMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onScreenOn() {
        if (mConnected) {
            sendMessageToActivity(MSG_WHAT_ID_SCREEN_ON);
        }
        else {
            openMainActivity();
        }
    }

    @Override
    public void onScreenOff() {
        if (mConnected) {
            sendMessageToActivity(MSG_WHAT_ID_SCREEN_OFF);
        }
    }

    @Override
    public void onRemainderAwake() {
        // Do nothing.
    }

    @Override
    public void onRemainderSleep() {
        closeMainActivity();
    }

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
