package com.sample.rebootactivity;

/**
 * Created by y_hisano on 2017/03/06.
 */

public interface ServiceView {
    void onCreate();
    void onDestroy();
    void onBind();
    void onUnBind();
}
