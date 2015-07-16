package com.yabinc.threadsample;

import android.app.IntentService;
import android.content.Intent;

import com.yabinc.logger.Log;

/**
 * Created by yabinc on 7/15/15.
 */
public class BackService extends IntentService {
    public static String TAG = "BackService";

    private BroadcastNotifier mBroadcaster = new BroadcastNotifier(this);

    public BackService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        while (true) {
            long endTime = System.currentTimeMillis() + 1000;
            while (System.currentTimeMillis() < endTime) {
                synchronized (this) {
                    try {
                        wait(endTime - System.currentTimeMillis());
                    } catch (Exception e) {
                    }
                }
                Log.d(TAG, "currTime " + System.currentTimeMillis());
                mBroadcaster.broadcastIntentWithState(Constants.STATE_ACTION_TIMEINTERVAL);
            }
        }
    }
}
