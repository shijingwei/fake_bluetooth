package com.yundongjia.lib.fake_bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.util.Log;

import com.example.nirjon.bledemo4_advertising.util.BLEUtil;

import static android.bluetooth.le.AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY;
import static android.bluetooth.le.AdvertiseSettings.ADVERTISE_TX_POWER_HIGH;

public  class BleAdvertiseService extends Service {
    private static String TAG = BleAdvertiseService.class.getSimpleName();
    private BleAdvertiseBinder binder;
    private boolean stop = false;
    private BleAdvertiseThread bleAdvertiseThread;


    //private static final int BLE_PAYLOAD_LENGTH = 22;
    //private byte[] calculatedPayload = new byte[BLE_PAYLOAD_LENGTH];
    private AdvertiseData myAdvertiseData;


    private static final long SCAN_PERIOD = 100;
    private static final long ADVERTISE_PERIOD = 50;
    private static final int MANUFACTURERID = 0xFF00;
    private static BluetoothManager myManager;
    private static BluetoothAdapter myAdapter;
    private static BluetoothLeAdvertiser myAdvertiser;
    private static AdvertiseSettings myAdvertiseSettings;
    //private static AdvertiseCallback myAdvertiseCallback;
    private Handler handler = new Handler();

    private static long lasttime = 0;

    private static int mode = 0; // 0 cottrol; 1 match;
    private static boolean switchMode = false;

    //广播回调
    private static AdvertiseCallback myAdvertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            super.onStartSuccess(settingsInEffect);
            long current = System.currentTimeMillis();
            //Log.i(TAG,"广播成功 time = "+ current + " last= "+lasttime  +" escape = " + (current-lasttime));
            lasttime = current;
        }
        @Override
        public void onStartFailure(int errorCode) {
            super.onStartFailure(errorCode);
            long current = System.currentTimeMillis();
            //Log.i(TAG,"蓝牙广播失败 errcode = " + errorCode  +" time=" + current + " last= "+lasttime  +" escape = " + (current-lasttime));;
            lasttime = current;
        }
    };

    private void  startAdertiseThread(){
        if(bleAdvertiseThread==null){
            bleAdvertiseThread = new BleAdvertiseThread();
            Log.i(TAG,"create bleAdvertiseThread" );
            bleAdvertiseThread.start();
        }

    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG,"create");
//        if(bleAdvertiseThread==null){
//            bleAdvertiseThread = new BleAdvertiseThread();
//            Log.i(TAG,"create bleAdvertiseThread" );
//        }
//        bleAdvertiseThread.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop = true;
        Log.i(TAG,"destory");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG,"bind");
        bleutoothSettingInit();

//        if(bleAdvertiseThread==null){
//            bleAdvertiseThread = new BleAdvertiseThread();
//            Log.i(TAG,"create bleAdvertiseThread" );
//        }
//        bleAdvertiseThread.start();

        if(binder==null){
            binder = new BleAdvertiseBinder();
            Log.i(TAG,"create BleAdvertiseBinder");
        }
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stop = true;
        Log.i(TAG,"unbind");
        return super.onUnbind(intent);

    }

    public class BleAdvertiseBinder extends Binder {
        @Override
        public void attachInterface(IInterface owner, String descriptor) {
            super.attachInterface(owner, descriptor);
            Log.i(TAG,"attachInterface");
        }
//        public void addressBind(byte[] address){
//            System.arraycopy(address,0,addressData,0,addressData.length);
//        }
//        public void advertise(byte[] command) {
//            advertiseing = true;
//            //mode = 0;
//            //Log.i(TAG,"拷贝命令到服务"+BinaryUtils.byteToStr(command));
//            //Log.i(TAG,"拷贝命令到服务前"+BinaryUtils.byteToStr(commandDatas));
//            System.arraycopy(command,0,commandDatas,0,commandDatas.length);
//            //Log.i(TAG,"拷贝命令到服务后"+BinaryUtils.byteToStr(commandDatas));
//        }
        public void match(byte[] command) {
            //mode = 1;
            //System.arraycopy(command,0,matchDatas,0,matchDatas.length);
        }
        public void stopAdvertise(){
            stop = true;
            bleAdvertiseThread = null;
        }

        public void startAdvertise(){
            stop = false;
            startAdertiseThread();
        }

        private void switchMode(int param) {
            if(mode!=param){
                mode = param;
            }
        }

    }


    /**
     * 蓝牙广播线程
     */
    class BleAdvertiseThread extends Thread {

        @Override
        public void run(){

//            ////////////
//            //临时退出 Begin
//            int i= 0;
//            i++;
//            Log.d(TAG,"调试临时退出");
//            if (i>0){
//                return;
//            }
//            i++;
//            //临时退出 End

            long timestamp = System.currentTimeMillis();

            while(true){

                if(stop){
                    Log.i(TAG,"BleAdvertiseThread  "+Thread.currentThread().getId()+" finish");
                    myAdvertiser.stopAdvertising(myAdvertiseCallback);
                    break;
                }
                //Log.i(TAG,"BleAdvertiseThread  "+Thread.currentThread().getId()+" runing");

                try {

                    if (myAdvertiser == null) {
                        Log.i(TAG,"reutrn myAdvertiser is  null");
                        return;
                    }
                    if (myAdvertiseCallback == null) {
                        Log.i(TAG,"reutrn myAdvertiseCallback is  null");
                        return;
                    }


                    byte[] addressData = BluetoothContext.getDevice().getAddress();
                    byte[] commandDatas = BluetoothContext.getDevice().getIfChanged();
                    if(commandDatas!=null) {
                        myAdvertiser.stopAdvertising(myAdvertiseCallback);

                        //Log.d(TAG, "蓝牙广播数据 命令:" + BinaryUtils.byteToStr(commandDatas));

                        byte[] calculatedPayload = new byte[addressData.length+commandDatas.length+ 5 ];
                        BLEUtil.get_rf_payload(addressData, addressData.length, commandDatas, commandDatas.length, calculatedPayload);

                        myAdvertiseData = new AdvertiseData.Builder().addManufacturerData(MANUFACTURERID, calculatedPayload).build();
                        myAdvertiser.startAdvertising(myAdvertiseSettings, myAdvertiseData, myAdvertiseCallback);
                    }else{
                        //Log.i(TAG,"command data not changed  time escape = " + (System.currentTimeMillis() - timestamp));
                    }

                    sleep(ADVERTISE_PERIOD);
                } catch (Exception e) {
                    Log.e(TAG,"蓝牙广播出错 " + e.getMessage());
                }

            }

        }

    };


    /**
     * 系统蓝牙设备创建及设置
     */
    private void bleutoothSettingInit() {

        Log.i(TAG,"bleutoothSettingInit调用");

        if(myManager==null) {
            myManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        }
        if(myAdapter == null) {
            myAdapter = myManager.getAdapter();
        }

        if(myAdvertiser==null) {
            myAdvertiser = myAdapter.getBluetoothLeAdvertiser();
        }

        if(myAdvertiseSettings==null) {
            myAdvertiseSettings = new AdvertiseSettings.Builder()
                    .setAdvertiseMode(ADVERTISE_MODE_LOW_LATENCY)
                    .setConnectable(true)
                    .setTimeout(0)
                    .setTxPowerLevel(ADVERTISE_TX_POWER_HIGH)
                    .build();
        }


    }

}
