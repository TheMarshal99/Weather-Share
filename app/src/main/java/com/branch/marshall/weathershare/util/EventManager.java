package com.branch.marshall.weathershare.util;

import android.os.Handler;

import com.squareup.otto.Bus;

/**
 * Created by marshall on 3/17/16.
 */
public class EventManager {
    private static Object lock = new Object();
    private static EventManager sInstance;

    public static EventManager getInstance() {
        if (sInstance == null) {
            synchronized (lock) {
                if (sInstance == null) {
                    sInstance = new EventManager();
                }
            }
        }

        return sInstance;
    }

    private Handler mHandler;
    private Bus mBus;

    private EventManager() {
        mHandler = new Handler();
        mBus = new Bus();
    }

    public void post(final Object event) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBus.post(event);
            }
        });
    }

    public void registerListener(final Object listener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBus.register(listener);
            }
        });
    }

    public void unregisterListener(final Object listener) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mBus.unregister(listener);
            }
        });
    }
}
