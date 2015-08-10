package com.yabinc.threadsample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ViewAnimator;
import android.R.color;

import com.yabinc.logger.Log;
import com.yabinc.logger.LogFragment;
import com.yabinc.logger.LogView;
import com.yabinc.logger.LogWrapper;
import com.yabinc.logger.MessageOnlyLogFilter;

public class MainActivity extends AppCompatActivity {

    public static String TAG = "MainActivity";

    private DownloadStateReceiver mDownloadStateReceiver;

    private int[] colorArray = new int[] {color.holo_blue_bright, color.holo_blue_dark,
            color.holo_green_light, color.holo_green_dark,
            color.holo_orange_light, color.holo_orange_dark,
            color.holo_red_light, color.holo_red_dark,
    };
    private int noDelayColorIndex = 0;
    private int delayThreadColorIndex = 0;
    private int runByThreadPoolIndex = 0;

    private ThreadManager threadManager = new ThreadManager();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView imageView = (ImageView) findViewById(R.id.star_image);
        imageView.setImageResource(R.drawable.star);
        GridView gridView = (GridView) findViewById(R.id.grid_view);
        gridView.setAdapter(new GridImageAdapter(this));
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeLogging();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, BackService.class);
        startService(intent);

        IntentFilter downloadIntentFilter = new IntentFilter(Constants.BROADCAST_ACTION);
        downloadIntentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        mDownloadStateReceiver = new DownloadStateReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(mDownloadStateReceiver,
                downloadIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Intent intent = new Intent(this, BackService.class);
        stopService(intent);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mDownloadStateReceiver);
    }

    private void initializeLogging() {
        LogWrapper logWrapper = new LogWrapper();
        Log.setLogNode(logWrapper);
        MessageOnlyLogFilter logFilter = new MessageOnlyLogFilter();
        logWrapper.setNext(logFilter);
        LogView logView = ((LogFragment) getFragmentManager().findFragmentById(R.id.log_fragment))
                .getLogView();
        logFilter.setNext(logView);
        Log.d(TAG, "Ready");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        ViewAnimator viewAnimator = (ViewAnimator) findViewById(R.id.viewanimator);
        switch (viewAnimator.getDisplayedChild()) {
            case 0: {
                MenuItem showTitle = menu.findItem(R.id.menu_show_title);
                showTitle.setVisible(false);
                break;
            }
            case 1: {
                MenuItem showLog = menu.findItem(R.id.menu_show_log);
                showLog.setVisible(false);
                break;
            }
            case 2: {
                MenuItem showImage = menu.findItem(R.id.menu_show_image);
                showImage.setVisible(false);
                break;
            }
            case 3: {
                MenuItem showGridImage = menu.findItem(R.id.menu_show_grid_image);
                showGridImage.setVisible(false);
                break;
            }
            case 4: {
                MenuItem showDelayThreadButton = menu.findItem(R.id.menu_show_delay_thread_button);
                showDelayThreadButton.setVisible(false);
                break;
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        ViewAnimator viewAnimator = (ViewAnimator) findViewById(R.id.viewanimator);
        if (id == R.id.menu_show_title) {
            viewAnimator.setDisplayedChild(0);
            invalidateOptionsMenu();
            return true;
        }
        if (id == R.id.menu_show_log) {
            viewAnimator.setDisplayedChild(1);
            invalidateOptionsMenu();
            return true;
        }
        if (id == R.id.menu_show_image) {
            viewAnimator.setDisplayedChild(2);
            invalidateOptionsMenu();
            return true;
        }
        if (id == R.id.menu_show_grid_image) {
            viewAnimator.setDisplayedChild(3);
            invalidateOptionsMenu();
            return true;
        }
        if (id == R.id.menu_show_delay_thread_button) {
            viewAnimator.setDisplayedChild(4);
            invalidateOptionsMenu();
            return true;
        }
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onNoDelayClick(View v) {
        Button noDelayButton = (Button) v;
        int color = getResources().getColor(colorArray[noDelayColorIndex]);
        noDelayButton.setBackgroundColor(color);
        Log.d(TAG, "onNoDelayClick, bg " + color);
        noDelayColorIndex = (noDelayColorIndex + 1) % colorArray.length;
    }

    public void onDelayThreadClick(View v) {
        final Button delayThreadButton = (Button)v;
        final int color = getResources().getColor(colorArray[delayThreadColorIndex]);
        threadManager.delayedRun(2000, new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        delayThreadButton.setBackgroundColor(color);
                    }
                });
            }
        });
        Log.d(TAG, "onDelayThreadClick, bg " + color);
        delayThreadColorIndex = (delayThreadColorIndex + 1) % colorArray.length;
    }

    public void onRunByThreadPoolClick(View v) {
        final Button runByThreadPoolButton = (Button)v;
        final int color = getResources().getColor(colorArray[runByThreadPoolIndex]);
        threadManager.runOnThreadPool(new Runnable() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        runByThreadPoolButton.setBackgroundColor(color);
                    }
                });
            }
        });
        Log.d(TAG, "onRunByThreadPoolClick, bg " + color);
        runByThreadPoolIndex = (runByThreadPoolIndex + 1) % colorArray.length;
    }

    public void onRunBusyThreadClick(View v) {
        threadManager.runOnNewThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Busy thread started");
                while (true) {
                }
            }
        });
    }

    private class DownloadStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "receive broadcast in DownloadStateReceiver: " + intent.getIntExtra(
                    Constants.EXTENDED_DATA_STATUS, -1));
        }
    }

    private class GridImageAdapter extends BaseAdapter {
        private Context mContext;
        private int mCount = 18;

        public GridImageAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return mCount;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            GridView gridView = (GridView) parent;
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(
                        new GridView.LayoutParams(gridView.getColumnWidth(),
                                gridView.getColumnWidth()));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setImageResource(R.drawable.star);
            return imageView;
        }
    }
}
