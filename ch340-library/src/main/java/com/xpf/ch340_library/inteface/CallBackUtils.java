package com.xpf.ch340_library.inteface;

public class CallBackUtils {
    //调用接口
    private static CallBack mCallBack;
    //设置回调
    public static void setCallBack(CallBack callBack) {
        mCallBack = callBack;
    }
    //执行回调方法
    public static void doCallBackMethod(String info) {
        mCallBack.doSomeThing(info);
    }
}