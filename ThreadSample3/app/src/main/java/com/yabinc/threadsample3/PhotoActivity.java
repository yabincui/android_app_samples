package com.yabinc.threadsample3;

import android.app.FragmentTransaction;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class PhotoActivity extends AppCompatActivity implements PhotoFragment.OnFragmentInteractionListener {
    public static final String LOG_TAG = "PhotoActivity";

    public static final String PHOTO_URL_KEY = "photo_url";

    private View mMainView = null;

    private String mPhotoUrl = null;

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPhotoUrl = getIntent().getStringExtra(PhotoActivity.PHOTO_URL_KEY);

        mMainView = getLayoutInflater().inflate(R.layout.fragmenthost, null);


        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.add(R.id.fragmentHost, new PhotoFragment());
        transaction.commit();
        //setFullScreen(true);

        setContentView(mMainView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_photo, menu);
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
