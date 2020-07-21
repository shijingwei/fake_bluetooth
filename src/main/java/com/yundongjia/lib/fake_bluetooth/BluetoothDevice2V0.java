package com.yundongjia.lib.fake_bluetooth;

/**
 * 2.0 设备
 */
public class BluetoothDevice2V0  extends BluetoothDeviceBase {
    private static String TAG = BluetoothDevice2V0.class.getSimpleName();
    private static BluetoothDeviceEnum deviceType = BluetoothDeviceEnum.Device3V0;
    private static byte[] datas = {(byte)0xAA,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x55 };


    @Override
    public BluetoothDeviceEnum getDeviceType() {
        return deviceType;
    }

    @Override
    public byte[] getAddress() {
        return new byte[0];
    }

    @Override
    public byte[] getSendData() {
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
        if(index>1 && index < datas.length-2) {
            datas[index] = data;
        }
    }

    @Override
    public void setSendData(BluetoothActionEnum actintType, byte[] data) {
        changed = true;
        setSendData(actintType);
        switch (actintType) {
            case ActionId:
                System.arraycopy(data,0,datas,1,2);
                break;
        }
    }

    @Override
    public void setSendData(BluetoothActionEnum actintType, byte data) {

    }

    @Override
    public void setSendData(BluetoothActionEnum actintType) {
        changed = true;
        switch (actintType) {
            case ActionId:
            case ActionPair:
                datas[0] = (byte)0xAA;
                datas[7] = (byte)0x55;
                break;
            default:
                datas[0] = (byte)0x66;
                datas[7] = (byte)0x99;
                break;
        }
    }

}
