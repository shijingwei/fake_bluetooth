package com.yundongjia.lib.fake_bluetooth;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import static android.content.Context.BIND_AUTO_CREATE;

public class BluetoothUtils {
    private static final String TAG = BluetoothUtils.class.getSimpleName();
    private static Context defaultContext = null;
    private static BluetoothDataCallback bluetoothDataCallback;
    private static boolean bounded = false;
    private static BleAdvertiseService.BleAdvertiseBinder binder;
    private static ServiceConnection connecting;

    static {
        connecting = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                Log.d(TAG, "onServiceConnected 调用");
                binder = (BleAdvertiseService.BleAdvertiseBinder) service;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.d(TAG, "onServiceDisconnected 调用");
                binder = null;
            }
        };
    }

    public static void initialize(Context context, BluetoothDataCallback callback) {
        Log.i(TAG,"initialize 调用");
        defaultContext = context;
        bluetoothDataCallback = callback;
        BluetoothContext.initContext(context);

        if (binder == null) {
            Intent intent = new Intent(context, BleAdvertiseService.class);
            bounded = context.bindService(intent, connecting, BIND_AUTO_CREATE);
        }

    }

    public static void destory() {
        Log.d(TAG, "destory 调用");
        if(defaultContext==null){
            return;
        }
        if(bounded && binder!=null) {
            synchronized (defaultContext) {
                defaultContext.unbindService(connecting);
                binder = null;
                defaultContext = null;
                bounded = false;
            }
        }
    }

    public static void pause() {
        Log.d(TAG, "pause 调用");
        if(defaultContext==null){
            return;
        }
        if(bounded && binder!=null) {
            synchronized (defaultContext) {
                defaultContext.unbindService(connecting);
                binder = null;
                defaultContext = null;
                bounded = false;
            }
        }
    }


    public static void setHorizonalValue(Integer num) {
        BluetoothContext.getDevice().setSendData(BluetoothActionEnum.ActionDataHorizontal,(byte)(num&0xff));
    }

    public static void setVerticalValue(Integer num) {
        BluetoothContext.getDevice().setSendData(BluetoothActionEnum.ActionDataVertical,(byte)(num&0xff));
    }

    public static void startPair() {
        BluetoothContext.getDevice().setSendData(BluetoothActionEnum.ActionId);
        startAdvertise();
    }

    public static void startScan() {
    }

    public static void stopScan() {
    }

    public static void stopPair() {
        BluetoothContext.getDevice().setSendData(BluetoothActionEnum.ActionControl);
    }

    public static void setStop() {
        BluetoothContext.getDevice().setSendData(BluetoothActionEnum.ActionStop);
        binder.stopAdvertise();
    }

    public static void startAdvertise(){
        //Log.i(TAG,"startAdvertise");
        if(bounded && binder!=null) {
            synchronized (defaultContext) {
                binder.startAdvertise();
            }
        }
    }

    public static void stopAdvertise(){
        if(bounded && binder!=null) {
            synchronized (defaultContext) {
                binder.stopAdvertise();
            }
        }
    }
}
