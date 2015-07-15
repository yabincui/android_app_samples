package com.yabinc.actionbar_sharedactionprovider;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by yabinc on 7/13/15.
 */
public class ContentItem {

    public static final int CONTENT_TYPE_IMAGE = 0;
    public static final int CONTENT_TYPE_TEXT = 1;

    public final int contentType;
    public final int contentResourceId;
    public final String contentAssetFilePath;

    public ContentItem(int type, int resourceId) {
        contentType = type;
        contentResourceId = resourceId;
        contentAssetFilePath = null;
    }

    public ContentItem(int type, String assetFilePath) {
        contentType = type;
        contentResourceId = 0;
        contentAssetFilePath = assetFilePath;
    }

    public Intent getShareIntent(Context context) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        switch (contentType) {
            case CONTENT_TYPE_IMAGE:
                intent.setType("image/jpg");
                intent.putExtra(Intent.EXTRA_STREAM, getContentUri());
                break;
            case CONTENT_TYPE_TEXT:
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, context.getString(contentResourceId));
                break;
        }
        return intent;
    }

    public Uri getContentUri() {
        if (!TextUtils.isEmpty(contentAssetFilePath)) {
            return Uri.parse("content://" + AssetProvider.CONTENT_URI + "/" + contentAssetFilePath);
        }
        return null;
    }
}
