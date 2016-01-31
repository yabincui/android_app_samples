package com.example.yabinc.linkgame;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.logging.Level;

/**
 * Created by yabinc on 1/30/16.
 */
public class SelectLevelDialogFragment extends DialogFragment {
    private static final String LOG_TAG = "SelectLevelDialogFrag";

    public interface SelectLevelDialogListener {
        public void onSelectLevel(int level);
    }

    private SelectLevelDialogListener mListener;

    static SelectLevelDialogFragment newInstance(int curLevel, int maxLevel, String[] levelNames) {
        SelectLevelDialogFragment f = new SelectLevelDialogFragment();
        Bundle args = new Bundle();
        args.putInt("curLevel", curLevel);
        args.putInt("maxLevel", maxLevel);
        args.putStringArray("levelNames", levelNames);
        f.setArguments(args);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final SelectLevelDialogListener listener = (SelectLevelDialogListener)getActivity();
        int curLevel = getArguments().getInt("curLevel");
        int maxLevel = getArguments().getInt("maxLevel");
        String[] levelNames = getArguments().getStringArray("levelNames");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.select_level_fragment, null);
        ListView listView = (ListView) v.findViewById(R.id.select_level_list);

        LevelAdapter adapter = new LevelAdapter(getActivity(), curLevel, maxLevel, levelNames);
        listView.setAdapter(adapter);
        listView.setSelection(curLevel);
        builder.setView(v);
        final Dialog dialog = builder.create();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.d(LOG_TAG, "OnItemClick, i = " + i);
                listener.onSelectLevel(i + 1);
                dialog.dismiss();
            }
        });
        return dialog;
    }

    class LevelAdapter extends BaseAdapter {
        private Activity activity;
        private int curLevel;
        private int maxLevel;
        private String[] levelNames;

        public LevelAdapter(Activity activity, int curLevel, int maxLevel, String[] levelNames) {
            this.activity = activity;
            this.curLevel = curLevel;
            this.maxLevel = maxLevel;
            this.levelNames = levelNames;
        }

        @Override
        public int getCount() {
            return maxLevel;
        }

        @Override
        public Object getItem(int i) {
            return levelNames[i];
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View retView = view;
            if (retView == null) {
                retView = activity.getLayoutInflater().inflate(R.layout.level_text_item, viewGroup, false);
            }
            TextView textView = (TextView) retView.findViewById(R.id.level_text);
            textView.setText(levelNames[i]);
            textView.setTypeface((i + 1 == curLevel ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT));
            return retView;
        }
    }
}
