package com.yabinc.actionbar_sharedactionprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by yabinc on 7/14/15.
 */
public class AssetProvider extends ContentProvider {

    public static String CONTENT_URI = "com.yabinc.actionbar_sharedactionprovider";

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings1, String s1) {
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    @Override
    public AssetFileDescriptor openAssetFile(Uri uri, String mode) throws FileNotFoundException {
        final String assetName = uri.getLastPathSegment();
        if (TextUtils.isEmpty(assetName)) {
            throw new FileNotFoundException();
        }
        try {
            AssetManager assetManager = getContext().getAssets();
            return assetManager.openFd(assetName);
        } catch (IOException e) {
            e.printStackTrace();
            return super.openAssetFile(uri, mode);
        }
    }
}
