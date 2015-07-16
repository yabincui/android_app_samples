package com.yabinc.networkusage;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.yabinc.logger.Log;
import com.yabinc.logger.LogFragment;
import com.yabinc.logger.LogView;
import com.yabinc.logger.LogWrapper;
import com.yabinc.logger.MessageOnlyLogFilter;

public class MainActivity extends Activity {

    public static final String TAG = "MainActivity";

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
        LogView logView = ((LogFragment) getFragmentManager().findFragmentById(R.id.log_fragment))
                .getLogView();
        logFilter.setNext(logView);
        Log.d(TAG, "Ready.");
    }

    public void onStartHttpPageClick(View view) {
        Intent intent = new Intent(this, HttpPageActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
