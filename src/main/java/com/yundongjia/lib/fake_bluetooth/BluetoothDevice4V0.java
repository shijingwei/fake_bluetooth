package com.yundongjia.lib.fake_bluetooth;

import android.util.Log;

public class BluetoothDevice4V0 extends BluetoothDeviceBase {
    private static String TAG = BluetoothDevice4V0.class.getSimpleName();
    private static BluetoothDeviceEnum deviceType = BluetoothDeviceEnum.Device4V0;
    private static byte[] datas = {(byte)0xAA,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x55 };
    //private static boolean changed  = true;

    @Override
    public BluetoothDeviceEnum getDeviceType() {
        return deviceType;
    }

    @Override
    public byte[] getSendData() {
        //Log.d(TAG,"getSendData changed = " + changed);
        changed = false;
        return datas;
    }

    @Override
    public void setSendData(byte[] data) {
        changed = true;
    }

    @Override
    public void setSendData(int index, byte data) {
        changed = true;
        //Log.d(TAG,"sendData index = " + index + " data " + data);
        synchronized (datas) {
            if (index > 1 && index < datas.length - 2) {
                datas[index] = data;
            }
        }
    }

    @Override
    public void setSendData(BluetoothActionEnum actintType, byte[] data) {
        changed = true;
        setSendData(actintType);
        synchronized (datas) {
            switch (actintType) {
                case ActionId:
                    System.arraycopy(data, 0, datas, 1, 2);
                    break;
            }
        }
    }

    @Override
    public void setSendData(BluetoothActionEnum actintType, byte data) {
        changed = true;
        setSendData(actintType);
        synchronized (datas) {
            switch (actintType) {
                case ActionDataHorizontal:
                    setSendData(3,data);
                    break;
                case ActionDataVertical:
                    setSendData(4,data);
                    break;
                default:
            }
        }
    }

    @Override
    public void setSendData(BluetoothActionEnum actintType) {
        changed = true;
        //Log.d(TAG,"setSendData actintType =" +actintType.toString());
        synchronized (datas) {
            switch (actintType) {
                case ActionId:
                case ActionPair:
                    datas[0] = (byte) 0xAA;
                    datas[7] = (byte) 0x55;
                    datas[3] = (byte) 0x00;
                    datas[4] = (byte) 0x00;
                    datas[5] = (byte) 0x00;
                    datas[6] = (byte) 0x00;
                    break;
                case ActionStop:
                    datas[0] = (byte) 0x77;
                    datas[7] = (byte) 0x88;
                    datas[3] = (byte) 0x00;
                    datas[4] = (byte) 0x00;
                    datas[5] = (byte) 0x00;
                    datas[6] = (byte) 0x00;
                default:
                    datas[0] = (byte) 0x77;
                    datas[7] = (byte) 0x88;
                    datas[3] = (byte) 0x00;
                    datas[4] = (byte) 0x00;
                    datas[5] = (byte) 0x00;
                    datas[6] = (byte) 0x00;
                    break;
            }
        }
    }
}
