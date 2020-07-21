package com.yundongjia.lib.fake_bluetooth;

import android.content.Context;

public class FakeBluetooth {

    public static Context appContext;

    public static void init(Context context) {
        if (null != context) {
            appContext = context;
        }

    }
}
