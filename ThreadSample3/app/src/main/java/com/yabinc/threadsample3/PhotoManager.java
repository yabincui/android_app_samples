package com.yabinc.threadsample3;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.LruCache;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by yabinc on 8/9/15.
 */
public class PhotoManager {

    public static final String TAG = "PhotoManager";

    public static final String USER_AGENT = "Mozilla/5.0 (Linux, U; Android "
            + Build.VERSION.RELEASE + ";"
            + Locale.getDefault().toString() +"; " + android.os.Build.DEVICE
            + "/" + android.os.Build.ID + ")";

    public static final int DOWNLOAD_COMPLETE = 1;
    public static final int DOWNLOAD_FAILED = 2;

    private static final int NUMBER_OF_DECODE_TRIES = 2;
    private static final long SLEEP_TIME_MILLISECONDS = 250;


    static class PhotoTask {
        String url;
        int targetWidth;
        int targetHeight;
        WeakReference<PhotoView> photoView;
        byte[] byteBuffer;
        Bitmap bitmap;
        Thread thread;

        PhotoTask(PhotoView photoView) {
            this.url = photoView.getImageURL();
            this.targetWidth = photoView.getWidth();
            this.targetHeight = photoView.getHeight();
            this.photoView = new WeakReference<PhotoView>(photoView);
            byteBuffer = null;
            bitmap = null;
            thread = null;
        }
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case DOWNLOAD_COMPLETE:
                case DOWNLOAD_FAILED:
                    PhotoTask task = (PhotoTask) msg.obj;
                    PhotoView view = task.photoView.get();
                    if (view != null && view.getImageURL() == task.url && view.getDownloadTask() == task) {
                        if (msg.what == DOWNLOAD_COMPLETE) {
                            view.setImageBitmap(task.bitmap);
                            sInstance.mPhotoCache.put(task.url, task.byteBuffer);
                        } else {
                            view.setImageResource(R.drawable.emptyphoto);
                        }
                        view.setDownloadTask(null);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    private static final int CORE_POOL_SIZE = 8;
    private static final int MAXIMUM_POOL_SIZE = 8;
    private static final int KEEP_ALIVE_TIME = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
    private BlockingQueue<Runnable> mDownloadQueue = new LinkedBlockingQueue<Runnable>();

    private ThreadPoolExecutor mThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
            KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, mDownloadQueue);

    private static final int IMAGE_CACHE_SIZE = 1024 * 1024 * 4;

    private final LruCache<String, byte[]> mPhotoCache = new LruCache<String, byte[]>(IMAGE_CACHE_SIZE) {
        @Override
        protected int sizeOf(String key, byte[] value) {
            return value.length;
        }
    };

    private static PhotoManager sInstance = null;

    static {
        sInstance = new PhotoManager();
    }

    public PhotoManager getInstance() {
        return sInstance;
    }

    private PhotoManager() {
    }

    public static void startDownload(PhotoView photoView) {
        final PhotoTask task = new PhotoTask(photoView);
        task.byteBuffer = sInstance.mPhotoCache.get(photoView.getImageURL());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (sInstance) {
                        task.thread = Thread.currentThread();
                    }
                    if (task.byteBuffer == null) {
                        task.byteBuffer = sInstance.downloadPhoto(task.url);
                    }
                    if (task.byteBuffer != null) {
                        Bitmap bitmap = sInstance.decodePhoto(task.byteBuffer, task.targetWidth, task.targetHeight);
                        task.bitmap = bitmap;
                        if (bitmap != null) {
                            Message msg = sInstance.mHandler.obtainMessage(DOWNLOAD_COMPLETE, task);
                            msg.sendToTarget();
                            return;
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                } finally {
                    synchronized (sInstance) {
                        task.thread = null;
                    }
                }
                Message msg = sInstance.mHandler.obtainMessage(DOWNLOAD_FAILED, task);
                msg.sendToTarget();
            }
        };
        photoView.setDownloadTask(task);
        sInstance.mThreadPool.execute(runnable);
    }

    public static void removeDownload(final PhotoView photoView) {
        PhotoTask task = photoView.getDownloadTask();
        if (task != null) {
            photoView.setDownloadTask(null);
            synchronized (sInstance) {
                if (task.thread != null) {
                    task.thread.interrupt();
                }
            }
        }
    }

    private byte[] downloadPhoto(String photoUrl) throws InterruptedException {
        InputStream inputStream = null;
        byte[] byteBuffer = null;
        try {
            URL url = new URL(photoUrl);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            // httpConn.setRequestProperty("User-Agent", USER_AGENT);
            Log.d(TAG, "downloadPhoto(" + photoUrl + ")" + ": User-Agent=" + USER_AGENT);
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            inputStream = httpConn.getInputStream();
            int contentSize = httpConn.getContentLength();
            Log.d(TAG, "getContentLength = " + contentSize);
            if (contentSize == -1) {
                throw new InterruptedException();
            }
            byteBuffer = new byte[contentSize];
            int remainingLength = contentSize;
            int bufferOffset = 0;

            while (remainingLength > 0) {
                int readResult = inputStream.read(byteBuffer, bufferOffset, remainingLength);
                if (readResult < 0) {
                    throw new EOFException();
                }


                bufferOffset += readResult;
                remainingLength -= readResult;
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
            }

        } catch (MalformedURLException localMalformedURLException) {
            localMalformedURLException.printStackTrace();
            return null;
        } catch (IOException localIOException) {
            localIOException.printStackTrace();
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                }
            }
        }
        Log.d(TAG, "download photo successfully!");
        return byteBuffer;
    }

    Bitmap decodePhoto(byte[] byteBuffer, int targetWidth, int targetHeight) {
        Bitmap returnBitmap = null;
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(byteBuffer, 0, byteBuffer.length, bitmapOptions);

        int hScale = (targetHeight != 0) ? bitmapOptions.outHeight / targetHeight : 1;
        int wScale = (targetWidth != 0) ? bitmapOptions.outWidth / targetWidth : 1;
        int sampleSize = Math.max(hScale, wScale);

        if (sampleSize > 1) {
            bitmapOptions.inSampleSize = sampleSize;
        }
        bitmapOptions.inJustDecodeBounds = false;

        for (int i = 0; i < NUMBER_OF_DECODE_TRIES; ++i) {
            try {
                returnBitmap = BitmapFactory.decodeByteArray(byteBuffer, 0, byteBuffer.length, bitmapOptions);
            } catch (Throwable e) {
                Log.e(TAG, "out of memory in decode stage.");
                java.lang.System.gc();

                if (Thread.interrupted()) {
                    return null;
                }
                try {
                    Thread.sleep(SLEEP_TIME_MILLISECONDS);
                } catch (InterruptedException interruptedException) {
                    return null;
                }
            }
        }
        Log.d(TAG, "decodePhoto successfully!");
        return returnBitmap;
    }
}
