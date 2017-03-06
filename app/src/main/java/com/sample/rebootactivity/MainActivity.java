package com.sample.rebootactivity;

import android.app.usage.UsageStats;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static android.R.attr.start;
import static java.lang.String.format;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private final static String TAG = MainActivity.class.getSimpleName();

    private Messenger mMessenger;
    private Messenger mReplayMessenger;

    private Calendar mCalendar = Calendar.getInstance();
    private SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.ENGLISH);

    private TextView mUsageStatsPermissionView;
    private TextView mUsageStatsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        View view = LayoutInflater.from(this).inflate(R.layout.activity_main, null);
        setContentView(view);

        mUsageStatsPermissionView = (TextView) view.findViewById(R.id.usage_stats_permission);
        mUsageStatsView = (TextView) view.findViewById(R.id.usage_stats);

        view.findViewById(R.id.goto_setting_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
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
    protected void onResume() {
        super.onResume();
        mUsageStatsPermissionView.setText(AppUtil.canGetUsageStats(this) ? "Granted" : "Denied");

        configureUsageStatsView();
    }

    private void configureUsageStatsView() {
        String text = "FirstDatetime        LastDatetime         LastDatetimeUsed     Package";
        List<UsageStats> statsList = AppUtil.getUsageStats(this);
        if (statsList != null) {
            for (UsageStats stats : statsList) {
                if (!TextUtils.isEmpty(text)) {
                    text += "\n";
                }
                String firstTimeStamp = getDatetime(stats.getFirstTimeStamp());
                String lastTimeStamp = getDatetime(stats.getLastTimeStamp());
                String lastTimeUsed = getDatetime(stats.getLastTimeUsed());

                text += format(Locale.ENGLISH, "%s  %s  %s  %s",
                        firstTimeStamp, lastTimeStamp, lastTimeUsed, stats.getPackageName());
            }
        }
        mUsageStatsView.setText(text);
    }

    private String getDatetime(long timestamp) {
        mCalendar.setTimeInMillis(timestamp);
        return mSimpleDateFormat.format(mCalendar.getTime());
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
            Log.d(TAG, String.format("handleMessage %s", msg.toString()));
            if (msg.what == MainControlService.MSG_WHAT_ID_FINISH_ACTIVITY) {
                MainActivity.this.finish();
            }
            return false;
        }
    }
}
