package com.yabinc.threadsample3;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = "MainActivity";

    public static final String PHOTO_URL = "tt";

    private boolean mFullScreen = true;

    private View mMainView = null;

    public static final int RSS_LIST_UPDATE = 1;

    private ArrayList<String> mThumbnails = null;
    private ArrayList<String> mPhotos = null;

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == RSS_LIST_UPDATE) {
                RSSPullThread.RSSList rssList = (RSSPullThread.RSSList) msg.obj;
                for (int i = 0; i < rssList.contents.size(); ++i) {
                    Log.d(LOG_TAG, "content[" + i + "]: " + rssList.contents.get(i));
                    Log.d(LOG_TAG, "thumb[" + i + "]: " + rssList.thumbnails.get(i));
                }
                mThumbnails = rssList.thumbnails;
                mPhotos = rssList.contents;
                PhotoThumbnailFragment thumbnailFragment = (PhotoThumbnailFragment) getFragmentManager().findFragmentById(R.id.fragmentHost);
                thumbnailFragment.setThumbnails(rssList.thumbnails);
                return;
            }
            super.handleMessage(msg);
        }
    };

    void setFullScreen(boolean fullscreen) {
        mMainView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                        WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR);

        super.onCreate(savedInstanceState);

        mMainView = getLayoutInflater().inflate(R.layout.fragmenthost, null);


        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        PhotoThumbnailFragment thumbnailFragment = new PhotoThumbnailFragment();
        transaction.add(R.id.fragmentHost, thumbnailFragment);
        transaction.commit();
        //setFullScreen(true);
        setContentView(mMainView);

        RSSPullThread rssPullThread = new RSSPullThread(mHandler);
        rssPullThread.start();

        /*
        thumbnailFragment.registerThumbClickListener(new PhotoThumbnailFragment.ThumbClickListener() {
            @Override
            public void onClickThumbnail(PhotoView photoView) {
                String photo = null;
                String thumbnail = photoView.getImageURL();
                if (mThumbnails != null && thumbnail != null) {
                    for (int i = 0; i < mThumbnails.size(); ++i) {
                        if (mThumbnails.get(i).equals(thumbnail)) {
                            photo = mPhotos.get(i);
                            break;
                        }
                    }
                }

                Intent intent = new Intent(MainActivity.this, PhotoActivity.class);
                intent.putExtra(PhotoActivity.PHOTO_URL_KEY, photo);
                startActivity(intent);
            }
        });
        */
        thumbnailFragment.registerThumbClickListener(new PhotoThumbnailFragment.ThumbClickListener() {
            @Override
            public void onClickThumbnail(PhotoView photoView) {
                int selectedIndex = -1;
                String thumbnail = photoView.getImageURL();
                if (mThumbnails != null && thumbnail != null) {
                    for (int i = 0; i < mThumbnails.size(); ++i) {
                        if (mThumbnails.get(i).equals(thumbnail)) {
                            selectedIndex = i;
                        }
                    }
                }
                Intent intent = new Intent(MainActivity.this, PhotoViewerActivity.class);
                intent.putStringArrayListExtra(PhotoViewerActivity.PHOTO_URLS_KEY, mPhotos);
                intent.putExtra(PhotoViewerActivity.SELECTED_PHOTO_INDEX_KEY, selectedIndex);
                startActivity(intent);
            }
        });
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

    public void startDownload(View view) {
        TextView infoText = (TextView) findViewById(R.id.info);
        infoText.setText("start Downloading...");
        PhotoView photoView = (PhotoView) findViewById(R.id.image);
        photoView.setImageURL(PHOTO_URL);
    }
}
