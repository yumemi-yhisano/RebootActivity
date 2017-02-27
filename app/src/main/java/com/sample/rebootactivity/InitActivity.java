package com.sample.rebootactivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * Created by y_hisano on 2017/02/23.
 */

public class InitActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toast.makeText(this, "InitActivity::onCreate", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, MainControlService.class);
        startService(intent);

        InitActivity.this.finish();
    }
}
