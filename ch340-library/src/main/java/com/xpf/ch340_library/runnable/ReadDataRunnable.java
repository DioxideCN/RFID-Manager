package com.xpf.ch340_library.runnable;

import com.xpf.ch340_library.driver.InitCH340;
import com.xpf.ch340_library.inteface.CallBack;
import com.xpf.ch340_library.inteface.CallBackUtils;
import com.xpf.ch340_library.logger.InLog;
import com.xpf.ch340_library.utils.CH340Util;

public class ReadDataRunnable implements Runnable {

    private String TAG = ReadDataRunnable.class.getSimpleName();
    private boolean mStop = false; // 是否停止线程

    @Override
    public void run() {
        //读取线程 => 运行
        startReadThread();
    }

    //开始读取线程
    private void startReadThread() {
        //mStop 为 false => 不停止线程 => 开始读取线程
        while (!mStop) {
            byte[] buffer = new byte[4096];
            // 接收数据数组
            // 读取缓存区的数据长度
            int length = InitCH340.getDriver().ReadData(buffer, 4096);
            String send = new String(buffer, 0, length);
            // 无数据
            if (length == 0) {
                InLog.i(TAG, "No data~");
            } else {//有数据时的处理
                //将此处收到的数组转化为HexString
                //String hexString = CH340Util.bytesToHexString(buffer, length);
                InLog.i(TAG, "ReadHexString===" + send + ",length===" + length);
                CallBackUtils.doCallBackMethod(send); //doSomething => (send) => {}
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 停止读取任务
     */
    public void stopTask() {
        mStop = true;
    }


}
