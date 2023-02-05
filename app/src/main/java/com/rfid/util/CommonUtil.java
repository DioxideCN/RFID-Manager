package com.rfid.util;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommonUtil {

    public static List<String> getBBList (String epc) {
        String reg = "bb 2 (.*?) 7e";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(epc);       // 指定要匹配的字符串
        List<String> matchStrs = new ArrayList<>();
        while (matcher.find()) {                      // 此处find（）每次被调用后，会偏移到下一个匹配
            if (!cutEpc(matcher.group()).equals("")) {
                matchStrs.add(cutEpc(matcher.group()));   // 获取当前匹配的值
            }
        }
        return matchStrs;
    }

    /**
     * 判断传入的 EPC 数据集中是否已存在三次重复数据
     * @param epcs 传入的 EPC 数据集
     * @return string 若有: 返回重复的段 若无: 返回空字符串
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static List<String> getRightEpc(List<String> epcs) {
        Map<String,Integer> map = new HashMap<>();
        List<String> result_list = new ArrayList<>();
        // 使用哈希表进行重复次数迭代
        for (String epc : epcs) {
            if(map.containsKey(epc)) {
                map.put(epc,map.get(epc) + 1);
            } else {
                map.put(epc,0);
            }
        }
        map.forEach((k,v) -> {
            if (v >= 2) {
                result_list.add(k);
            }
        });
        return result_list;
    }

    /**
     * 分割出正确的 epc
     * @param epc String BB开头 7E结尾 的数据
     */
    public static String cutEpc(String epc) {
        StringBuilder result = new StringBuilder();
        epc = epc.toUpperCase();
        String[] epcs = epc.split(" ");
        if(epcs.length < 20) {
            return "";
        }
        // EPC 必须以 BB 开头 以 7E 结尾
        if(epcs[0].equals("BB") && epcs[epcs.length-1].equals("7E")) {
            for(int i = 8; i < 20; i++) {
                if (epcs[i].length() > 2) {
                    return "";
                } else if (epcs[i].length() == 2) {
                    // 补空格
                    result.append(epcs[i]).append(i!=19?" ":"");
                } else if (epcs[i].length() == 1) {
                    // 单位补0
                    result.append("0").append(epcs[i]).append(i!=19?" ":"");
                }

            }
        }
        return result.toString();
    }

    /**
     * 传入一个长整型时间戳 返回对应格式的时间表示字符串
     * @param timestamp String 时间戳
     */
    public static String convertTimestamp2Date(String timestamp) {
        if(timestamp.contains(":")) {
            return timestamp;
        }
        String pattern = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

        return simpleDateFormat.format(new Date(Long.parseLong(timestamp)));
    }

}
