package com.sample.rebootactivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import static com.sample.rebootactivity.MainControlService.START_ID;
import static com.sample.rebootactivity.MainControlService.START_ID_FROM_ACTIVITY;

/**
 * Created by y_hisano on 2017/02/23.
 */

public class InitActivity extends AppCompatActivity {

    private final static int REQUEST_CODE_SETTINGS = 123;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toast.makeText(this, "InitActivity::onCreate", Toast.LENGTH_SHORT).show();
        startIntentAndFinishActivity();
    }

    private void startIntentAndFinishActivity() {
        if (canDrawOverlays()) {
            Intent intent = new Intent(this, MainControlService.class);
            intent.putExtra(START_ID, START_ID_FROM_ACTIVITY);
            startService(intent);
        } else {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_CODE_SETTINGS);
        }

        InitActivity.this.finish();
    }

    private boolean canDrawOverlays() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        return Settings.canDrawOverlays(this);
    }
}
