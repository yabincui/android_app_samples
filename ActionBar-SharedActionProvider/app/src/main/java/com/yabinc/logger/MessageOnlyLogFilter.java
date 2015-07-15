package com.yabinc.logger;

/**
 * Created by yabinc on 7/10/15.
 */
public class MessageOnlyLogFilter implements LogNode {
    private LogNode mNext;

    public LogNode getNext() {
        return mNext;
    }

    public void setNext(LogNode next) {
        mNext = next;
    }

    @Override
    public void println(int priority, String tag, String msg, Throwable tr) {
        if (mNext != null) {
            mNext.println(Log.NONE, null, msg, null);
        }
    }
}
