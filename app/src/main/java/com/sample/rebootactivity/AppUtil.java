package com.sample.rebootactivity;

import android.app.ActivityManager;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;
import static android.content.Context.APP_OPS_SERVICE;
import static android.content.Context.USAGE_STATS_SERVICE;

/**
 * Created by y_hisano on 2017/02/28.
 */

public class AppUtil {

    private static final String TAG = AppUtil.class.getSimpleName();

    /**
     * Android 5.1.1? から他のアプリのプロセスは取れなくなった
     * @param context
     * @param packageName
     * @return
     */
    @Deprecated
    public static boolean isAppRunning(Context context, String packageName) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();
        for (int i = 0; i < procInfos.size(); i++) {
            if (TextUtils.equals(procInfos.get(i).processName, packageName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * アプリの使用履歴を返す
     * @param context
     */
    public static List<UsageStats> getUsageStats(Context context) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager statsManager = (UsageStatsManager) context.getSystemService(USAGE_STATS_SERVICE);
            long now = System.currentTimeMillis();
            return statsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 60_000, now);
        }
        return null;
    }

    /**
     * アプリの使用履歴を取得するパーミッションが許可されているか
     *
     * @param context
     * @return
     */
    public static boolean canGetUsageStats(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return true;
        }
        AppOpsManager opsManager = (AppOpsManager) context.getSystemService(APP_OPS_SERVICE);
        int uid = android.os.Process.myUid();
        int mode = opsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, uid, context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }
}
