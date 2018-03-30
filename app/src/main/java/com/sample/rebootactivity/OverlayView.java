package com.sample.rebootactivity;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by y_hisano on 2017/03/06.
 */

public class OverlayView implements ServiceView {
    private static final String TAG = OverlayView.class.getSimpleName();

    private View mView;
    private final Context mContext;
    private final ClickListener mClickListener;
    private WindowManager.LayoutParams mLayoutParams;
    private boolean mViewReady;

    public OverlayView(Context context, ClickListener clickListener) {
        mContext = context;
        mClickListener = clickListener;
    }

    @Override
    public void onCreate() {
        if (! checkDrawOverlaysPermission()) {
            return;
        }
        configureView();
        configureCloseServiceButton(true);
        configureStartActivityButton(true);
        configureCloseActivityButton(false);
    }

    @Override
    public void onDestroy() {
        if (mViewReady) {
            getWindowManager().removeView(mView);
            mViewReady = false;
        }
    }

    @Override
    public void onBind() {
        if (! mViewReady) {
            return;
        }
        configureCloseServiceButton(false);
        configureStartActivityButton(false);
        configureCloseActivityButton(true);
    }

    @Override
    public void onUnBind() {
        if (! mViewReady) {
            return;
        }
        configureCloseServiceButton(true);
        configureStartActivityButton(true);
        configureCloseActivityButton(false);
    }

    private WindowManager getWindowManager() {
        return (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
    }

    private View getStartActivityButton() {
        return mView.findViewById(R.id.start_button);
    }

    private View getCloseActivityButton() {
        return mView.findViewById(R.id.close_activity_button);
    }

    private View getCloseServiceButton() {
        return mView.findViewById(R.id.close_service_button);
    }

    private void configureStartActivityButton(boolean enabled) {
        configureButton(enabled, getStartActivityButton());
    }

    private void configureCloseActivityButton(boolean enabled) {
        configureButton(enabled, getCloseActivityButton());
    }

    private void configureCloseServiceButton(boolean enabled) {
        configureButton(enabled, getCloseServiceButton());
    }

    private void configureButton(boolean enabled, View button) {
        if (button == null) {
            return;
        }
        button.setEnabled(enabled);
        button.setBackgroundColor(enabled ? 0xFFFF0000 : 0xFF454545);
    }

    private void configureView() {
        mView = LayoutInflater.from(mContext).inflate(R.layout.overlay_view, null);
        int type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        mLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                type,
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

        getStartActivityButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick start_button");
                mClickListener.onStartActivityClick(v);
            }
        });
        getCloseActivityButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick close_activity_button");
                mClickListener.onCloseActivityClick(v);
            }
        });

        getCloseServiceButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick close_service_button");
                mClickListener.onCloseServiceClick(v);
            }
        });
        mViewReady = true;
    }

    private boolean checkDrawOverlaysPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        return Settings.canDrawOverlays(mContext);
    }

    public interface ClickListener {
        void onStartActivityClick(View view);
        void onCloseActivityClick(View view);
        void onCloseServiceClick(View view);
    }
}
