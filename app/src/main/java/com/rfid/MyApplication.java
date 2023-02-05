package com.rfid;

import android.app.Application;
import android.content.Context;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

public class MyApplication extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        initALog();
    }

    //全局上下文
    public static Context getContext() {
        return mContext;
    }


    public void initALog() {
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)
                // (Optional) Whether to show thread info or not. Default true
                .methodCount(0)
                // (Optional) How many method line to show. Default 2
                .methodOffset(7)
                // (Optional) Hides internal method calls up to offset. Default 5
                .tag("CarCar")
                // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build();

        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
        Logger.addLogAdapter(new DiskLogAdapter());
    }
}
