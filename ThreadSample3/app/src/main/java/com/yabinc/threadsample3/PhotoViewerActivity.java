package com.yabinc.threadsample3;

import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;

public class PhotoViewerActivity extends AppCompatActivity implements PhotoViewerFragment.OnFragmentInteractionListener{
    public static final String LOG_TAG = "PhotoViewerActivity";

    public static final String PHOTO_URLS_KEY = "photo_urls";
    public static final String SELECTED_PHOTO_INDEX_KEY = "selected_photo_index";

    ArrayList<String> mPhotoUrls = null;
    int mSelectedPhotoIndex = -1;

    public ArrayList<String> getPhotoUrls() {
        return mPhotoUrls;
    }

    public int getSelectedPhotoIndex() {
        return mSelectedPhotoIndex;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPhotoUrls = getIntent().getStringArrayListExtra(PHOTO_URLS_KEY);
        mSelectedPhotoIndex = getIntent().getIntExtra(SELECTED_PHOTO_INDEX_KEY, -1);
        Log.d(LOG_TAG, "PHOTO_URLS_KEY: " + mPhotoUrls.size());
        Log.d(LOG_TAG, "SELECTED_PHOTO_INDEX_KEY: " + getIntent().getIntExtra(SELECTED_PHOTO_INDEX_KEY, -1));

        View mainView = getLayoutInflater().inflate(R.layout.fragmenthost, null);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.fragmentHost, new PhotoViewerFragment());
        transaction.commit();

        setContentView(mainView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photo_viewer, menu);
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

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
