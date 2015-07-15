package com.yabinc.logger;

/**
 * Created by yabinc on 7/10/15.
 */
public interface LogNode {
    public void println(int priority, String tag, String msg, Throwable tr);
}
