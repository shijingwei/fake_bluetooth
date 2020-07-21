package com.yundongjia.lib.fake_bluetooth;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 设备的环境变量
 */
public class BluetoothContext {
    private static final String FileName = "MobileSerail.dat";
    private static int serial = 0;

    private static final String TAG = BluetoothContext.class.getSimpleName();
    private static BluetoothDeviceBase device;
    private static Map<String,Object> map = new HashMap<>();
    private static Context context;

    //初始设备类型
    static {
        //addDeviceMap(new BluetoothDevice2V0());
        addDeviceMap(new BluetoothDevice3V0());
        addDeviceMap(new BluetoothDevice4V0());
        addDeviceMap(new BluetoothDevice4V1());
    }

    /**
     * 增加设备信息到环境变量
     * @param bluetoothDevice
     */
    private static void addDeviceMap(BluetoothDeviceBase bluetoothDevice){
        String key = bluetoothDevice.getDeviceType().name();
        if(map.get(key)!=null){
            Log.d(TAG,key +" 设备已经已经存在，不要重复加入");
            return;
        }

        //默认设备为空时，则置为当前的设备
        if(device==null){
            device = bluetoothDevice;
        }
        map.put(key,bluetoothDevice);
    }

    /**
     * 获得当前的设备
     * @return
     */
    public static BluetoothDeviceBase getDevice(){
        //设备初始化在switchDevice中设置
        if(device==null){
            Log.d(TAG,"设备变量没有被初始化");
        }
        return device;
    }

    /**
     * 设备切换
     * 设备切换逻辑 UI界面选择设备  ->  MainActivity.setDevice -> this.switchDevice
     * @param deviceEnum
     */
    public static void switchDevice(BluetoothDeviceEnum deviceEnum){
        String key = deviceEnum.name();
        Object obj = map.get(key);
        if(obj==null){
            Log.e(TAG,key+" 没有找到设备");
        }else {
            if(obj instanceof BluetoothDeviceBase){
                device = (BluetoothDeviceBase) obj;
            }else{
                Log.e(TAG,key+" 设备类型不匹配");
            }
        }
    }

    public static Context getContext(){
        if(context==null){
            Log.i(TAG,"没有初始化上下文");
        }
        return context;
    }

    public static  void initContext(Context sysContext){
        context = sysContext;

        //生成APPID
        int serial = getOrCreateAddress(context);

        //填充APPID
        fillAppID(serial);

    }

    /**
     * 填充APPID到设备
     */
    private static void fillAppID(int serial){
        byte[] serials = BinaryUtils.intToByte(serial,2);
        for (Object device: map.values()) {

            if(device instanceof BluetoothDeviceBase){
                ((BluetoothDeviceBase) device).setSendData(BluetoothActionEnum.ActionId, serials);
            }
        }
    }

    /**
     * APPID 生成
     * @param context
     * @return
     */
    private static int getOrCreateAddress(Context context){
        if(serial!=0){
            return serial;
        }
        File file = new File(context.getFilesDir(),FileName);
        try {
            if (file.exists()) {
                FileInputStream fis = context.openFileInput(FileName);
                byte[] buffer = new byte[2];
                fis.read(buffer);
                serial = (buffer[0]&0XFF)|((buffer[1]<<8)&0XFF00);

            } else {
                while ((serial = new Random().nextInt())==0);
                FileOutputStream fos = context.openFileOutput(FileName, Context.MODE_PRIVATE);
                byte[] buffer = new byte[2];
                buffer[0] = (byte) (serial & 0xFF);
                buffer[1] = (byte) ((serial>>8) & 0xFF);
                fos.write(buffer);

            }
        }catch (IOException ioe){

        }
        return serial;
    }
}
