package com.sample.rebootactivity;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.view.MotionEvent;
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
    private WindowManager.LayoutParams mLayoutParams;
    private Messenger mMessenger;
    private Messenger mReplayMessenger;
    private boolean mConnected;
    private BroadcastReceiver mReceiver = new ScreenEventReceiver();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        mMessenger = new Messenger(new Handler(this.getMainLooper(), new MessengerCallback()));
        configureView();
        configureStartButton(! mConnected);
        configureCloseActivityButton(mConnected);

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        getWindowManager().removeView(mView);
        unregisterReceiver(mReceiver);

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
        configureCloseServiceButton(! mConnected);

        return mMessenger.getBinder();
    }

    @Override
    public void onRebind(Intent intent) {
        Log.d(TAG, "onRebind");
        mConnected = true;
        configureStartButton(! mConnected);
        configureCloseActivityButton(mConnected);
        configureCloseServiceButton(! mConnected);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "onUnbind");
        mReplayMessenger = null;

        mConnected = false;
        configureStartButton(! mConnected);
        configureCloseActivityButton(mConnected);
        configureCloseServiceButton(! mConnected);

        return true;
    }

    private void configureView() {
//        Point point = new Point();
//        getWindowManager().getDefaultDisplay().getSize(point);

        mView = LayoutInflater.from(this).inflate(R.layout.overlay_view, null);
        mLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        mView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                Log.d(TAG, String.format("onLayoutChange  l:%d, t:%d, r:%d, b:%d", left, top, right, bottom));
                v.removeOnLayoutChangeListener(this);
                Point point = new Point();
                getWindowManager().getDefaultDisplay().getSize(point);
                mLayoutParams.x = (point.x - (right - left))/ 2;
                mLayoutParams.y = (point.y - (bottom - top)) / 2;
                getWindowManager().updateViewLayout(mView, mLayoutParams);
            }
        });
        getWindowManager().addView(mView, mLayoutParams);
        mView.setOnTouchListener(new View.OnTouchListener() {
            private float prevX;
            private float prevY;
            private boolean mTouching;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        Log.d(TAG, "onTouch ACTION_DOWN");
                        prevX = event.getRawX();
                        prevY = event.getRawY();
                        mTouching = true;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (! mTouching) {
                            break;
                        }
                        float diffX = prevX - event.getRawX();
                        float diffY = prevY - event.getRawY();
                        Log.d(TAG, String.format("onTouch ACTION_MOVE %f, %f", diffX, diffY));
                        int viewX = (int) (mLayoutParams.x - diffX);
                        int viewY = (int) (mLayoutParams.y - diffY);
                        mLayoutParams.x = viewX;
                        mLayoutParams.y = viewY;
                        getWindowManager().updateViewLayout(mView, mLayoutParams);
                        prevX = event.getRawX();
                        prevY = event.getRawY();
                        break;

                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        Log.d(TAG, "onTouch ACTION_UP or ACTION_CANCEL");
                        mTouching = false;
                        break;
                }
                return true;
            }
        });

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

        getCloseServiceButton().setOnClickListener(new View.OnClickListener() {
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

    private void configureCloseServiceButton(boolean enabled) {
        if (mView == null) {
            return;
        }
        getCloseServiceButton().setEnabled(enabled);
        getCloseServiceButton().setBackgroundColor(enabled ? 0xFFFF0000 : 0xFF454545);
    }

    private View getStartButton() {
        return mView.findViewById(R.id.start_button);
    }

    private View getCloseActivityButton() {
        return mView.findViewById(R.id.close_activity_button);
    }

    private View getCloseServiceButton() {
        return mView.findViewById(R.id.close_service_button);
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
