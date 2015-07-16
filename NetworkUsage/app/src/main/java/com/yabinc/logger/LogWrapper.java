package com.yabinc.logger;

import android.util.Log;

/**
 * Created by yabinc on 7/10/15.
 */
public class LogWrapper implements LogNode {

    private LogNode mNext;

    public LogNode getNext() { return mNext; }

    public void setNext(LogNode node) { mNext = node; }

    @Override
    public void println(int priority, String tag, String msg, Throwable tr) {
        String useMsg = msg;
        if (useMsg == null) {
            useMsg = "";
        }
        if (tr != null) {
            msg += "\n" + Log.getStackTraceString(tr);
        }
        Log.println(priority, tag, useMsg);
        if (mNext != null) {
            mNext.println(priority, tag, msg, tr);
        }
    }

}