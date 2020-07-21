package com.yundongjia.lib.fake_bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.BIND_AUTO_CREATE;

public class BluetoothData {

    private static final String TAG =BluetoothData.class.getSimpleName();

    private static final long MatchTimeOut = 1500L;
    private static Timer commandTimer;
    private static boolean advertising = false;
    private static Context defaultContext = null;
    private static boolean bounded = false;
    private static int APPID = 0;
    private static int mode = 0;  //0 控制 1 配对/解除配对

    private static boolean scanExit;

    private static long EXPIETIME = 2000L;
    private static int PAYLOAD_PAIR_LEN = 12;
    private static int PAYLOAD_CROL_LEN = 12;

    private static int PACKAGE_PAIR_HEAD = 0xC0; //配对开头
    private static int PACKAGE_PAIRD_HEAD = 0xE5; //已配对开头
    private static int PACKAGE_PAIR_END  = 0x3F; //配对结尾
    private static int PACKAGE_PAIR_RECEIVE_HEAD = 0xA0; //配对开头
    private static int PACKAGE_UNPAIR_RECEIVE_HEAD = 0xE0; //取消配对开头
    private static int PACKAGE_CONTROL_HEAD = 0x33; //控制HEAD


    private static byte[] address = {(byte)0xc1,(byte)0xc2,(byte)0xc3,(byte)0xc4,(byte)0xc5};

    private static Set<Integer> pairedDevices = new HashSet<>();
    private static Set<Integer> unpairDevices = new HashSet<>();
    private static Map<Integer,Long> pairedDeviceMap = new HashMap<>();
    private static Map<Integer,Long> unpairDeviceMap = new HashMap<>();
    private static BluetoothDataCallback bluetoothDataCallback;

    private static List<byte[]>matchQueue = new ArrayList<>();


    public static Thread scanThread;

    public static void initializeStatus(Context context, BluetoothDataCallback callback){
        //默认调节速度及初始化状态

        Log.i(TAG,"initializeStatus 调用");
        defaultContext = context;
        bluetoothDataCallback = callback;
        BluetoothContext.initContext(context);

//        synchronized (controlerBytes) {
//            initCrolCommand(context);

            if (binder == null) {
                Intent intent = new Intent(context, BleAdvertiseService.class);
                bounded = context.bindService(intent, connecting, BIND_AUTO_CREATE);
            }

//            if (commandTimer == null) {
//                commandTimer = new Timer(false);
//                commandTimer.scheduleAtFixedRate(new CommandTimerTask(), 0, 90);
//            }
            if(scanThread==null){
                scanExit = false;
                scanThread = new BlueScanThread(context);
                scanThread.start();
            }

//        }
    }

    public static void onStop(){
        binder.stopAdvertise();
        scanExit = true;
        scanThread = null;

    }



    private static BleAdvertiseService.BleAdvertiseBinder binder;
    private static ServiceConnection  connecting = new ServiceConnection(){

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG,"onServiceConnected 调用");
            binder = (BleAdvertiseService.BleAdvertiseBinder)service;
//            binder.addressBind(BluetoothData.address);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG,"onServiceDisconnected 调用");
            binder = null;
        }
    };

    public static void startAdvertise(){
        advertising = true;
        binder.startAdvertise();
    }

    public static void stopAdvertise(){
        Log.d(TAG,"stopAdvertise 调用");
        //synchronized(controlerBytes) {
            uninitializeStatus();
            advertising = false;
        //}

    }

    private static void uninitializeStatus() {
        if(commandTimer!=null) {
            Log.d(TAG,"Commander " +Thread.currentThread().getId() +" finish");
            commandTimer.cancel();
            commandTimer.purge();
            //myAdvertiser.stopAdvertising(myAdvertiseCallback);
        }
        commandTimer=null;
        if(defaultContext==null){
            return;
        }
        if(bounded && binder!=null) {
            synchronized (defaultContext) {
                Log.d(TAG, "unbindService 调用");
                try {
                    defaultContext.unbindService(connecting);
                }catch (IllegalArgumentException e){

                }
                binder = null;
                defaultContext = null;
                bounded = false;
            }
        }
    }


    //停止
    public static void stopControl(){
        Log.i(TAG,"停止命令调用");
        //enterControl();
        BluetoothDeviceBase device  =  BluetoothContext.getDevice();
        if(device!=null){
            device.setSendData(BluetoothActionEnum.ActionStop);
        }

    }


    //蓝牙回调
    // 解析设备端的地址
    private static void processAddress(BluetoothDevice device, byte[] scanRecord){
        if(scanRecord==null || scanRecord.length<15){
            return;
        }
        if(device.getName()!=null){
            return;
        }
        Log.i(TAG," 发现蓝牙信息 = "+BinaryUtils.byteToStr(scanRecord) );
        ////|02|01|1A|0B|FF|F0|FF|E5|59|13|2A|59|13|2A|0D|
        ////|02|01|1A|0B|FF|F0|FF|C0|59|13|83|59|13|83|3F|
        if((scanRecord[5] &0xFF) == 0xF0 && (scanRecord[6] &0xFF) == 0xFF){

            //Log.i(TAG,"processAddress = "+BinaryUtils.byteToStr(scanRecord));

            int type = scanRecord[7] &0xFF;

            int address = BinaryUtils.byteToInt(scanRecord,8,3,true);//(scanRecord[7] &0xFF) >> 16)|((scanRecord[8] &0xFF)>>8)|(scanRecord[8] &0xFF);
            //Log.i(TAG," 发现蓝牙设备 = "+"type "+type+" " + BinaryUtils.intToByteString(address,3) );
            //synchronized (defaultContext) {
            if (type == PACKAGE_PAIR_HEAD) {

                pairedDevices.remove(address);
                unpairDevices.add(address);
                unpairDeviceMap.put(address, System.currentTimeMillis());
                if (bluetoothDataCallback != null) {
                    Map<String,int[]> map = new HashMap<>();
                    map.put("pairedDevices",parseSetToArray(pairedDevices));
                    map.put("unpairDevices",parseSetToArray(unpairDevices));
                    bluetoothDataCallback.findDevices(map);
                }

            } else if (type == PACKAGE_PAIRD_HEAD) {
                unpairDevices.remove(address);
                pairedDevices.add(address);
                pairedDeviceMap.put(address, System.currentTimeMillis());
                if (bluetoothDataCallback != null) {
                    Map<String,int[]> map = new HashMap<>();
                    map.put("pairedDevices",parseSetToArray(pairedDevices));
                    map.put("unpairDevices",parseSetToArray(unpairDevices));
                    bluetoothDataCallback.findDevices(map);
                }
            }
            //}
        }
    }

    private static void triggerDeviceEvent(){
        clearExpireDevice();
        if (bluetoothDataCallback != null) {
            Map<String,int[]> map = new HashMap<>();
            map.put("pairedDevices",parseSetToArray(pairedDevices));
            map.put("unpairDevices",parseSetToArray(unpairDevices));
            bluetoothDataCallback.findDevices(map);
        }
        //Log.i(TAG,"设备触发");
        //bluetoothDataCallback.findPairedDevices(parseSetToArray(pairedDevices));
        //bluetoothDataCallback.findUnpairDevices(parseSetToArray(unpairDevices));
    }

    //过期设备处理
    private static void clearExpireDevice(){
        long timestame = System.currentTimeMillis();

        {
            List<Integer> keys = new ArrayList<>();
            for (int key : unpairDeviceMap.keySet()) {
                Long val = unpairDeviceMap.get(key);
                if (val != null && (timestame - val) > EXPIETIME) {
                    keys.add(key);
                }
            }

            synchronized (defaultContext) {
                for (Integer k : keys) {
                    //Log.i(TAG, "移除未配对设备" + BinaryUtils.intToByteString(k, 3));
                    unpairDeviceMap.remove(k);
                    unpairDevices.remove(k);
                }
            }

            keys = new ArrayList<>();
            for (int key : keys) {
                Long val = pairedDeviceMap.get(key);
                if (val != null && (timestame - val) > EXPIETIME) {
                    keys.add(key);
                }
            }

            synchronized (defaultContext) {
                for (Integer k : keys) {
                    //Log.i(TAG, "移除已配对设备" + BinaryUtils.intToByteString(k, 3));
                    pairedDeviceMap.remove(k);
                    pairedDevices.remove(k);
                }
            }

        }

    }

    private static int[] parseSetToArray(Set<Integer> set){
        int[] result = new int[set.size()];
        int i = 0;
        for(Integer val : set){
            result[i] = val;
            i++;
        }
        return result;
    }

    private static BluetoothAdapter.LeScanCallback leScanCallback =  new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            processAddress(device, scanRecord);
            //Log.d(TAG,"bluetooth name =" +device.getName() +" address= " + device.getAddress() + " scanRecord = " + BinaryUtils.byteToStr(scanRecord) );
        }
    };

    ///////////////////////////
    /**
     * 蓝牙扫描线程类
     */
    static class BlueScanThread extends Thread{
        private Context context;
        private BluetoothManager manager;
        private BluetoothAdapter adapter;
        private int count;


        BlueScanThread(Context context){
            this.context = context;
            manager = (BluetoothManager)context.getSystemService(context.BLUETOOTH_SERVICE);
            adapter = manager.getAdapter();
            count = 0;
        }
        public void run(){
            Log.i(TAG,"蓝牙扫描启动");
            while(true){
                if(!adapter.isEnabled()){
                    adapter.enable();
                }
                try {
                    //退出线程
                    if(scanExit){
                        Log.i(TAG,"蓝牙扫描退出");
                        adapter.stopLeScan(leScanCallback);
                        return;
                    }
                    //if(count %2==0) {
                    adapter.startLeScan(leScanCallback);
                    Thread.sleep(1000);
                    //adapter.stopLeScan(leScanCallback);
                    //}
                    triggerDeviceEvent();
                    count++;
                }catch (InterruptedException e){

                    return;

                }
            }
        }
    }

//

}
