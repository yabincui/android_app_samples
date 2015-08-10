package com.yabinc.threadsample3;

import android.os.Handler;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by yabinc on 8/9/15.
 */
public class RSSPullThread {
    public static final String LOG_TAG = "RSSPullThread";

    public static final String RSS_URL = "http://picasaweb.google.com/data/feed/base/featured?alt=rss&kind=photo&access=public&slabel=featured&hl=en_US&imgmax=1600";

    private Handler mHandler = null;

    class RSSList {
        ArrayList<String> contents = null;
        ArrayList<String> thumbnails = null;

        RSSList(ArrayList<String> contents, ArrayList<String> thumbnails) {
            this.contents = contents;
            this.thumbnails = thumbnails;
        }
    }

    public RSSPullThread(Handler handler) {
        mHandler = handler;
    }

    public void start() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    byte[] byteBuffer = downloadURL(RSS_URL);
                    if (byteBuffer != null) {
                        RSSList rssList = parseXml(byteBuffer);
                        if (rssList != null) {
                            mHandler.obtainMessage(MainActivity.RSS_LIST_UPDATE, rssList).sendToTarget();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private byte[] downloadURL(String photoUrl) throws InterruptedException {
        InputStream inputStream = null;
        byte[] byteBuffer = null;
        try {
            URL url = new URL(photoUrl);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            Log.d(LOG_TAG, "downloadPhoto(" + photoUrl + ")");
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            inputStream = httpConn.getInputStream();
            int contentSize = httpConn.getContentLength();
            Log.d(LOG_TAG, "getContentLength = " + contentSize);
            if (contentSize == -1) {
                final int READ_SIZE = 2 * 1024;
                byte[] tempBuffer = new byte[READ_SIZE];
                int bufferLeft = tempBuffer.length;
                int bufferOffset = 0;
                int readResult = 0;
                while (true) {
                    while (bufferLeft > 0) {
                        readResult = inputStream.read(tempBuffer, bufferOffset, bufferLeft);
                        if (readResult < 0) {
                            break;
                        }
                        bufferOffset += readResult;
                        bufferLeft -= readResult;
                        if (Thread.interrupted()) {
                            throw new InterruptedException();
                        }
                    }
                    if (readResult < 0) {
                        break;
                    }
                    bufferLeft = READ_SIZE;
                    int newSize = tempBuffer.length + READ_SIZE;
                    byte[] expandedBuffer = new byte[newSize];
                    System.arraycopy(tempBuffer, 0, expandedBuffer, 0, tempBuffer.length);
                    tempBuffer = expandedBuffer;
                }
                byteBuffer = new byte[bufferOffset];
                System.arraycopy(tempBuffer, 0, byteBuffer, 0, bufferOffset);
            } else {
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
        Log.d(LOG_TAG, "download photo successfully!");
        return byteBuffer;
    }

    RSSList parseXml(byte[] byteBuffer) {
        final String ITEM = "item";
        final String CONTENT = "media:content";
        final String THUMBNAIL = "media:thumbnail";

        ArrayList<String> contents = new ArrayList<String>();
        ArrayList<String> thumbnails = new ArrayList<String>();
        String curContent = null;
        String curThumbnail = null;

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new ByteArrayInputStream(byteBuffer), null);
            if (parser.getEventType() != XmlPullParser.START_DOCUMENT) {
                throw new XmlPullParserException("Invalid RSS");
            }
            while (true) {
                int nextEvent = parser.next();
                if (nextEvent == XmlPullParser.START_DOCUMENT) {
                    continue;
                } else if (nextEvent == XmlPullParser.END_DOCUMENT) {
                    break;
                } else if (nextEvent == XmlPullParser.START_TAG) {
                    String eventName = parser.getName();
                    if (eventName.equalsIgnoreCase(ITEM)) {
                        curContent = null;
                        curThumbnail = null;
                    } else {
                        boolean isContent = false;
                        boolean isThumbnail = false;
                        if (eventName.equalsIgnoreCase(CONTENT)) {
                            isContent = true;
                        } else if (eventName.equalsIgnoreCase(THUMBNAIL)) {
                            isThumbnail = true;
                        } else {
                            continue;
                        }
                        String url = parser.getAttributeValue(null, "url");
                        if (url == null) {
                            break;
                        }
                        if (isContent) {
                            curContent = url;
                        } else if (isThumbnail) {
                            curThumbnail = url;
                        }
                    }
                } else if (nextEvent == XmlPullParser.END_TAG) {
                    String eventName = parser.getName();
                    if (eventName.equalsIgnoreCase(ITEM)) {
                        if (curContent != null && curThumbnail != null) {
                            contents.add(curContent);
                            thumbnails.add(curThumbnail);
                        }
                    }
                }
             }
        } catch (Exception e) {
            e.printStackTrace();
        }
        RSSList rssList = new RSSList(contents, thumbnails);
        return rssList;
    }
}
