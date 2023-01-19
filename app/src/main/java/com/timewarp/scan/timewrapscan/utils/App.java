package com.timewarp.scan.timewrapscan.utils;

import com.timewarp.scan.timewrapscan.BuildConfig;
import com.timewarp.scan.timewrapscan.ui.defaults.SplashActivity;
import think.outside.the.box.AppClass;
import think.outside.the.box.handler.APIManager;

public class App extends AppClass {

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) {
            APIManager.isLog = true;
        }
        setClass(SplashActivity.class);
    }
}
