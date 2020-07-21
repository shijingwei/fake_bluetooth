package com.yundongjia.lib.fake_bluetooth;

import android.util.Log;

public abstract class BluetoothDeviceBase {

    private static String TAG = BluetoothDeviceBase.class.getSimpleName();
    private static byte[] address = {(byte)0xc1,(byte)0xc2,(byte)0xc3,(byte)0xc4,(byte)0xc5};
    protected static boolean changed  = true;

    /**
     * 设备类型
     * @return BluetoothDeviceEnum
     */
    abstract BluetoothDeviceEnum getDeviceType();

    public abstract byte[] getSendData();
    public abstract void setSendData(int index,byte data);
    public abstract void setSendData(BluetoothActionEnum actintType,byte[] data);
    public abstract void setSendData(BluetoothActionEnum actintType,byte data);
    public abstract void setSendData(BluetoothActionEnum actintType);
    public void setSendData(byte[] data){

    }

    public byte[] getAddress(){
        return address;
    }

    public byte[] getIfChanged(){
        if(changed) {
            byte[] result =  getSendData();
            Log.i(TAG,BinaryUtils.byteToStr(result));
            return result;
        }else{
            return null;
        }
    }

}
