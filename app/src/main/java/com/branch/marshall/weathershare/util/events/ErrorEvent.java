package com.branch.marshall.weathershare.util.events;

/**
 * Created by marshall on 3/17/16.
 */
public class ErrorEvent {
    private Throwable mThrowable;

    public ErrorEvent(Throwable t) {
        mThrowable = t;
    }

    public Throwable getThrowableError() {
        return mThrowable;
    }
}
