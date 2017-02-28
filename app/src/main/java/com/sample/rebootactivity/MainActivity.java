package com.sample.rebootactivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private final static String TAG = MainActivity.class.getSimpleName();

    private Messenger mMessenger;
    private Messenger mReplayMessenger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, MainControlService.class);
        bindService(intent, this, BIND_AUTO_CREATE);
//        intent = new Intent(this, MainControlService.class);
//        startService(intent);
        mReplayMessenger = new Messenger(new Handler(this.getMainLooper(), new ReplayCallback()));
    }

    @Override
    protected void onStop() {
        unbindService(this);
        super.onStop();
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        Log.d(TAG, "onServiceConnected");
        mMessenger = new Messenger(service);
        Message msg = Message.obtain();
        msg.what = MainControlService.MSG_WHAT_ID_CONNECTED;
        msg.replyTo = mReplayMessenger;
        try {
            mMessenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        Log.d(TAG, "onServiceDisconnected");
        mMessenger = null;
    }

    private class ReplayCallback implements Handler.Callback {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == MainControlService.MSG_WHAT_ID_FINISH_ACTIVITY) {
                MainActivity.this.finish();
            }
            return false;
        }
    }
}
