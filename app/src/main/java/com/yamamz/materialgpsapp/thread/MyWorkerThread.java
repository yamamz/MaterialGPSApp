package com.yamamz.materialgpsapp.thread;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by AMRI on 12/6/2016.
 */

public class MyWorkerThread extends HandlerThread {

    private Handler myWorkerHandler;

    public MyWorkerThread(String name) {
        super(name);
    }

    public void postTask(Runnable task) {
        myWorkerHandler.post(task);
    }

    public void prepareHandler() {
        myWorkerHandler = new Handler(getLooper());
    }
}