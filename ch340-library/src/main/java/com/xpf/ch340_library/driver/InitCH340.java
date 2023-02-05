package com.xpf.ch340_library.driver;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

import com.xpf.ch340_library.R;
import com.xpf.ch340_library.logger.InLog;
import com.xpf.ch340_library.runnable.ReadDataRunnable;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.wch.ch34xuartdriver.CH34xUARTDriver;

/**
 * Function:初始化ch340驱动
 */
public class InitCH340 {

    private static final String TAG = InitCH340.class.getSimpleName();
    private static final String ACTION_USB_PERMISSION = "com.linc.USB_PERMISSION";
    private static final ExecutorService mThreadPool = Executors.newSingleThreadExecutor();
    private static final byte parity = 0;
    private static final byte stopBit = 1;
    private static final byte dataBit = 8;
    private static final int baudRate = 115200;  //波特率
    private static final byte flowControl = 0;
    @SuppressLint("StaticFieldLeak")
    private static CH34xUARTDriver driver;
    private static boolean isOpenDeviceCH340 = false;
    private static ReadDataRunnable readDataRunnable;
    private static UsbManager mUsbManager;
    private static IUsbPermissionListener listener;
    private static UsbDevice mUsbDevice;

    public static UsbDevice getmUsbDevice() {
        return mUsbDevice;
    }

    public static void setListener(IUsbPermissionListener listener) {
        InitCH340.listener = listener;
    }

    /**
     * 初始化 ch340 参数.
     * @param context Application context.
     */
    public static void initCH340(Context context) {
        if (context == null) return;
        Context appContext = context.getApplicationContext();
        mUsbManager = (UsbManager) appContext.getSystemService(Context.USB_SERVICE);
        if (mUsbManager != null) {
            HashMap<String, UsbDevice> deviceHashMap = mUsbManager.getDeviceList();
            InLog.e(TAG, " deviceHashMap.size()= " + deviceHashMap.size());
            for (UsbDevice device : deviceHashMap.values()) {
                InLog.i(TAG, "ProductId:" + device.getProductId() + ",VendorId:" + device.getVendorId());
                if (device.getProductId() == 29987 && device.getVendorId() == 6790) {
                    mUsbDevice = device;
                    if (mUsbManager.hasPermission(device)) {
                        loadDriver(appContext, mUsbManager);
                    } else {
                        if (listener != null) {
                            listener.result(false);
                        }
                    }
                    break;
                }
            }
        }
    }

    /**
     * 加载 ch340 驱动.
     */
    public static void loadDriver(Context appContext, UsbManager usbManager) {
        driver = new CH34xUARTDriver(usbManager, appContext, ACTION_USB_PERMISSION);
        // 判断系统是否支持USB HOST
        if (!driver.UsbFeatureSupported()) {
            Dialog dialog = new AlertDialog.Builder(appContext)
                    .setTitle(appContext.getString(R.string.app_tip))
                    .setMessage(appContext.getString(R.string.app_message))
                    .setPositiveButton(appContext.getString(R.string.app_ok),(arg0, arg1) -> {}).create();
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();
            //Your mobile phone does not support USB HOST, please change other phones to try again
            InLog.e(TAG, "您的手机不支持USB HOST，请更换其他手机再试！");
        } else {
            openCH340();
        }
    }

    /**
     * 配置并打开 ch340.
     */
    private static void openCH340() {
        int ret_val = driver.ResumeUsbList();
        InLog.d(TAG, ret_val + "");
        //ResumeUsbList方法用于枚举 CH34X 设备以及打开相关设备
        if (ret_val == -1) {

            InLog.d(TAG, ret_val + "无法打开设备！");
            driver.CloseDevice();
        } else if (ret_val == 0) {
            if (!driver.UartInit()) {  //对串口设备进行初始化操作
                InLog.d(TAG, ret_val + "设备串口初始化时发生错误！");
                InLog.d(TAG, ret_val + "无法打开设备！");
                return;
            }
            InLog.d(TAG, ret_val + "设备成功打开！");
            if (!isOpenDeviceCH340) {
                isOpenDeviceCH340 = true;
                configParameters();//配置ch340的参数、需要先配置参数
            }
        } else {
            InLog.d(TAG, "无法找到该串口下的设备！");
        }
    }

    /**
     * 配置串口波特率
     */
    private static void configParameters() {
        boolean isSetConfig = driver.SetConfig(baudRate, dataBit, stopBit, parity, flowControl);
        if (isSetConfig) {
            InLog.i(TAG, "串口波特率设置成功！");
            if (readDataRunnable == null) {
                readDataRunnable = new ReadDataRunnable();
            }
            mThreadPool.execute(readDataRunnable);
        } else {
            InLog.e(TAG, "串口波特率设置失败！");
        }
    }

    /**
     * 关闭线程池
     */
    public static void shutdownThreadPool() {
        if (!mThreadPool.isShutdown()) {
            mThreadPool.shutdown();
        }
    }

    /**
     * ch340 是否打开
     */
    public static boolean isIsOpenDeviceCH340() {
        return isOpenDeviceCH340;
    }

    /**
     * 获取 ch340 驱动.
     */
    public static CH34xUARTDriver getDriver() {
        return driver;
    }

    public static UsbManager getmUsbManager() {
        return mUsbManager;
    }

    public interface IUsbPermissionListener {
        void result(boolean isGranted);
    }
}
