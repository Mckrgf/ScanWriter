package com.supcon.scanwriter;

import android.app.Application;

import com.uuzuche.lib_zxing.activity.ZXingLibrary;
import com.yaobing.module_middleware.BaseApp;

/**
 * Created by tfhr on 2018/2/1.
 */

public class App extends BaseApp {


    @Override
    public void onCreate() {
        super.onCreate();
        ZXingLibrary.initDisplayOpinion(this);
    }

}
