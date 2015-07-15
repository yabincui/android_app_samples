package com.yabinc.actionbarcompat;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.yabinc.logger.Log;
import com.yabinc.logger.LogFragment;
import com.yabinc.logger.LogView;
import com.yabinc.logger.LogWrapper;
import com.yabinc.logger.MessageOnlyLogFilter;

public class MainActivity extends Activity {

    public static String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeLogging();
    }

    private void initializeLogging() {
        LogWrapper logWrapper = new LogWrapper();
        Log.setLogNode(logWrapper);
        MessageOnlyLogFilter logFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(logFilter);
        LogView logView = ((LogFragment) getFragmentManager().findFragmentById(R.id.log_fragment)).getLogView();
        logFilter.setNext(logView);
        Log.d(TAG, "Ready");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem locationItem = menu.add(0, R.id.menu_location, 0, "location");
        locationItem.setIcon(R.drawable.ic_action_location);
        locationItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_settings) {
            Log.d(TAG, "menu setting selected");
            return true;
        }
        if (id == R.id.menu_refresh) {
            Log.d(TAG, "menu refresh selected");
            return true;
        }
        if (id == R.id.menu_location) {
            Log.d(TAG, "menu location selected");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
