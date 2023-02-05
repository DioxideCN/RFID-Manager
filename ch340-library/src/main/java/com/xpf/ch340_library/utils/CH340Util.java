package com.xpf.ch340_library.utils;

import android.support.annotation.NonNull;

import com.xpf.ch340_library.driver.InitCH340;
import com.xpf.ch340_library.logger.InLog;

/**
 * Function:CH340数据处理工具类
 */
public class CH340Util {
    private static final String TAG = CH340Util.class.getSimpleName();

    /**
     * 将数据流写入 ch340.
     * @param byteArray 字节数组
     * @param format 格式
     * @return 返回写入的结果，-1表示写入失败！
     */
    public static int writeData(@NonNull byte[] byteArray, String format) {
        // 将此处收到的数组转化为HexString
        String hexString = bytesToHexString(byteArray, byteArray.length);
        InLog.i(TAG, "WriteHexString===" + hexString);
        if ("ascii".equals(format)) {       //ASCII => String
            return InitCH340.getDriver().WriteData(byteArray, byteArray.length);
        } else if ("hex".equals(format)) {  //HEX => String
            assert hexString != null;
            return InitCH340.getDriver().WriteData(hexString.getBytes(), byteArray.length);
        } else {                            //其他 => -1
            return -1;
        }
    }

    /**
     * byte[] 转换为hexString
     * @param buffer 数据
     * @param size   字符数
     * @return 返回转换后的十六进制字符串
     */
    public static String bytesToHexString(byte[] buffer, final int size) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (buffer == null || size <= 0) return null;
        for (int i = 0; i < size; i++) {
            String hex = Integer.toHexString(buffer[i] & 0xff);
            if (hex.length() < 2) stringBuilder.append(0);
            stringBuilder.append(hex);
        }
        return stringBuilder.toString();
    }


    /**
     * 将String转化为byte[]数组
     * @param arg 需要转换的String对象
     * @return 转换后的byte[]数组
     */
    public static byte[] toByteArray2(String arg) {
        if (arg != null) {
            /* 1.先去除String中的' '，然后将String转换为char数组 */
            char[] newArray = new char[1000];
            char[] array = arg.toCharArray();
            int length = 0;
            for (int i = 0; i < array.length; i++) {
                if (array[i] != ' ') {
                    newArray[length] = array[i];
                    length++;
                }
            }
            newArray[length] = 0x0D;
            newArray[length + 1] = 0x0A;
            length += 2;

            byte[] byteArray = new byte[length];
            for (int i = 0; i < length; i++) {
                byteArray[i] = (byte)newArray[i];
            }
            return byteArray;

        }
        return new byte[] {};
    }

}
