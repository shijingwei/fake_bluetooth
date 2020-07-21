package com.yundongjia.lib.fake_bluetooth;

import java.util.Map;

public interface BluetoothDataCallback {

    public void findDevices(Map<String, int[]> pairedDevices);
}
