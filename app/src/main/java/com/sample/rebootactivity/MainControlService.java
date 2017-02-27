package com.sample.rebootactivity;

import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by y_hisano on 2017/02/23.
 */

public class MainControlService extends Service {

    public final static String TAG = MainControlService.class.getSimpleName();
    public final static int MSG_WHAT_ID_FINISH_ACTIVITY = 10;
    public final static int MSG_WHAT_ID_CONNECTED = 11;

    private View mView;
    private Messenger mMessenger;
    private Messenger mReplayMessenger;
    private boolean mConnected;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        mMessenger = new Messenger(new Handler(this.getMainLooper(), new MessengerCallback()));
        configureView();
        configureStartButton(! mConnected);
        configureCloseActivityButton(mConnected);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        getWindowManager().removeView(mView);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        mConnected = true;
        configureStartButton(! mConnected);
        configureCloseActivityButton(mConnected);

        return mMessenger.getBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind");
        mConnected = true;
        configureStartButton(! mConnected);
        configureCloseActivityButton(mConnected);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        mReplayMessenger = null;

        mConnected = false;
        configureStartButton(! mConnected);
        configureCloseActivityButton(mConnected);
        return true;
    }

    private void configureView() {
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);

        mView = LayoutInflater.from(this).inflate(R.layout.overlay_view, null);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                point.x - mView.getMeasuredWidth(),
                point.y - mView.getMeasuredHeight(),
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        getWindowManager().addView(mView, params);

        getStartButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick start_button");
                Intent intent = new Intent(MainControlService.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        getCloseActivityButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick close_activity_button");
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
        });

        mView.findViewById(R.id.close_service_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick close_service_button");
                MainControlService.this.stopSelf();
            }
        });
    }

    private void configureStartButton(boolean enabled) {
        if (mView == null) {
            return;
        }
        getStartButton().setEnabled(enabled);
        getStartButton().setBackgroundColor(enabled ? 0xFFFF0000 : 0xFF454545);
    }

    private void configureCloseActivityButton(boolean enabled) {
        if (mView == null) {
            return;
        }
        getCloseActivityButton().setEnabled(enabled);
        getCloseActivityButton().setBackgroundColor(enabled ? 0xFFFF0000 : 0xFF454545);
    }

    private View getStartButton() {
        return mView.findViewById(R.id.start_button);
    }

    private View getCloseActivityButton() {
        return mView.findViewById(R.id.close_activity_button);
    }

    private WindowManager getWindowManager() {
        return (WindowManager) getSystemService(WINDOW_SERVICE);
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
