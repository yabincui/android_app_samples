package com.yabinc.threadsample3;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by yabinc on 8/9/15.
 */
public class PhotoThumbnailFragment extends Fragment implements View.OnClickListener {

    public static final String LOG_TAG = "PhotoThumbnailFragment";

    private int mThumbCount = 100;
    private int mGridItemWidth = 200;
    private int mGridItemHeight = 200;

    private GridViewAdapter mAdapter = null;

    private ArrayList<String> mThumbnails = null;

    interface ThumbClickListener {
        public void onClickThumbnail(PhotoView photoView);
    }

    private ArrayList<ThumbClickListener> mThumbClickListeners = new ArrayList<ThumbClickListener>();

    public void registerThumbClickListener(ThumbClickListener listener) {
        mThumbClickListeners.add(listener);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View localView = inflater.inflate(R.layout.gridlist, container, false);
        GridView gridView = (GridView) localView.findViewById(R.id.gridView);
        mAdapter = new GridViewAdapter(getActivity());
        gridView.setAdapter(mAdapter);
        gridView.setColumnWidth(mGridItemWidth);

        return localView;
    }

    public void setThumbnails(ArrayList<String> thumbnails) {
        this.mThumbnails = thumbnails;
        this.mThumbCount = thumbnails.size();
        mAdapter.notifyDataSetChanged();
    }

    class GridViewAdapter extends BaseAdapter {
        private Context mContext;

        public GridViewAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mThumbCount;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup group) {

            PhotoView photoView;
            if (convertView == null) {
                photoView = new PhotoView(mContext);
                photoView.setLayoutParams(new ViewGroup.LayoutParams(mGridItemWidth, mGridItemHeight));
                photoView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                photoView.setPadding(8, 8, 8, 8);
                photoView.setOnClickListener(PhotoThumbnailFragment.this);
            } else {
                photoView = (PhotoView) convertView;
            }
            if (mThumbnails == null || mThumbnails.size() <= position) {
                photoView.setImageResource(R.drawable.emptyphoto);
            } else {
                photoView.setImageURL(mThumbnails.get(position));
            }
            return photoView;
        }
    }

    @Override
    public void onClick(View v) {
        PhotoView photoView = (PhotoView) v;
        Log.d(LOG_TAG, "thumbnail onClick");
        for (int i = 0; i < mThumbClickListeners.size(); ++i) {
            mThumbClickListeners.get(i).onClickThumbnail(photoView);
        }
    }
}
