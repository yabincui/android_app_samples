package com.yabinc.actionbar_sharedactionprovider;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.yabinc.logger.Log;
import com.yabinc.logger.LogFragment;
import com.yabinc.logger.LogView;
import com.yabinc.logger.LogWrapper;
import com.yabinc.logger.MessageOnlyLogFilter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends Activity {

    public static String TAG = "MainActivity";

    private final ArrayList<ContentItem> mItems = getSampleContent();

    private ShareActionProvider mShareActionProvider;

    class MyPageAdapter extends PagerAdapter {
        LayoutInflater mInflater;

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            Log.d(TAG, "destroyItem for position " + position);
            container.removeView((View)object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            if (mInflater == null) {
                mInflater = LayoutInflater.from(MainActivity.this);
            }
            final ContentItem item = mItems.get(position);
            switch (item.contentType) {
                case ContentItem.CONTENT_TYPE_TEXT: {
                    TextView textView = (TextView) mInflater.inflate(
                        R.layout.item_text, container, false
                    );
                    textView.setText(item.contentResourceId);
                    container.addView(textView);
                    Log.d(TAG, "initializeItem for position " + position + "(text): " +
                            textView.getText());
                    return textView;
                }
                case ContentItem.CONTENT_TYPE_IMAGE: {
                    ImageView imageView = (ImageView) mInflater.inflate(R.layout.item_image,
                            container, false);
                    try {
                        InputStream is = getAssets().open(item.contentAssetFilePath);
                        Drawable drawable = Drawable.createFromStream(is, null);
                        imageView.setImageDrawable(drawable);
                        container.addView(imageView);
                        Log.d(TAG, "initializeItem for position " + position + "(image): " +
                                item.contentAssetFilePath);
                        return imageView;
                    } catch (IOException e) {
                        Log.e(TAG, "load image " + item.contentAssetFilePath + " failed", e);
                    }
                }
            }
            return null;
        }
    }

    class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageSelected(int position) {
            Log.d(TAG, "select position " + position);
            MainActivity.this.setSharedIntent(position);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new MyPageAdapter());
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
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
        Log.d(TAG, "Ready");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem shareItem = menu.findItem(R.id.menu_share);
        mShareActionProvider = (ShareActionProvider) shareItem.getActionProvider();
        int currentViewPagerItem = ((ViewPager) findViewById(R.id.viewpager)).getCurrentItem();
        setSharedIntent(currentViewPagerItem);
        return true;
    }

    private void setSharedIntent(int position) {
        if (mShareActionProvider != null) {
            Log.d(TAG, "setSharedIntent for position " + position);
            ContentItem item = mItems.get(position);
            Intent shareIntent = item.getShareIntent(MainActivity.this);
            mShareActionProvider.setShareIntent(shareIntent);
        }
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

    static ArrayList<ContentItem> getSampleContent() {
        ArrayList<ContentItem> items = new ArrayList<ContentItem>();
        items.add(new ContentItem(ContentItem.CONTENT_TYPE_IMAGE, "photo_1.jpg"));
        items.add(new ContentItem(ContentItem.CONTENT_TYPE_TEXT, R.string.quote_1));
        items.add(new ContentItem(ContentItem.CONTENT_TYPE_IMAGE, "photo_2.jpg"));
        items.add(new ContentItem(ContentItem.CONTENT_TYPE_TEXT, R.string.quote_2));
        items.add(new ContentItem(ContentItem.CONTENT_TYPE_IMAGE, "photo_3.jpg"));
        items.add(new ContentItem(ContentItem.CONTENT_TYPE_TEXT, R.string.quote_3));

        return items;
    }
}
