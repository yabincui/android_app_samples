package com.yabinc.threadsample3;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by yabinc on 8/9/15.
 */
public class PhotoView extends ImageView {

    private String mImageURL = null;

    private PhotoManager.PhotoTask mDownloadTask = null;

    private boolean mIsDrawn = false;

    public PhotoView(Context context) {
        super(context);
    }

    public PhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhotoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if ((!mIsDrawn) && (mImageURL != null)) {
            PhotoManager.startDownload(this);
            mIsDrawn = true;
        }
        super.onDraw(canvas);
    }

    void setImageURL(String photoURL) {
        if (mImageURL != null) {
            if (!mImageURL.equals(photoURL)) {
                PhotoManager.removeDownload(this);
            } else {
                return;
            }
        }
        mImageURL = photoURL;
        if (mIsDrawn && mImageURL != null) {
            PhotoManager.startDownload(this);
        }
        setImageResource(R.drawable.imagenotstartdownloading);
    }

    String getImageURL() {
        return mImageURL;
    }

    void setDownloadTask(PhotoManager.PhotoTask task) {
        mDownloadTask = task;
    }

    PhotoManager.PhotoTask getDownloadTask() {
        return mDownloadTask;
    }
}
