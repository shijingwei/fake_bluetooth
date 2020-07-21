package com.yundongjia.lib.fake_bluetooth;

public class BleStatus {
    ///////////////
    public static final int STATUS_IDEL= 0;      //空闲状态 允许配对
    public static final int STATUS_STOP= 1;      //停止
    public static final int STATUS_PAIR= 2;      //配对模式
    public static final int STATUS_PAIRING = 3;      //配对模式
    public static final int STATUS_UNPAIRING= 4;      //取消配对模式
    public static final int STATUS_CONTROL= 100;  //控制模式
}
