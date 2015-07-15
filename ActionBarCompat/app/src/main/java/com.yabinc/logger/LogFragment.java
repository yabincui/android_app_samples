package com.yabinc.logger;

import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

/**
 * Created by yabinc on 7/10/15.
 */
public class LogFragment extends Fragment {

    private LogView mLogView;
    private ScrollView mScrollView;

    public LogFragment() {}

    public View inflatViews() {
        mScrollView = new ScrollView(getActivity());
        ViewGroup.LayoutParams scrollParams = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        mScrollView.setLayoutParams(scrollParams);

        mLogView = new LogView(getActivity());
        ViewGroup.LayoutParams logParam = new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        mLogView.setClickable(true);
        mLogView.setFocusable(true);
        mLogView.setTypeface(Typeface.MONOSPACE);

        int paddingDips = 16;
        double scale = getResources().getDisplayMetrics().density;
        int paddingPixels = (int) (paddingDips * scale + 0.5);
        mLogView.setPadding(paddingPixels, paddingPixels, paddingPixels, paddingPixels);
        mLogView.setCompoundDrawablePadding(paddingPixels);

        mLogView.setGravity(Gravity.BOTTOM);
        mLogView.setTextAppearance(getActivity(), android.R.style.TextAppearance_Holo_Medium);

        mScrollView.addView(mLogView);
        return mScrollView;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflatViews();

        mLogView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
        return result;
    }

    public LogView getLogView() { return mLogView; }
}
