package com.example.yabinc.linkgame;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by yabinc on 1/30/16.
 */
public class SelectSizeDialogFragment extends DialogFragment {

    public interface SelectSizeDialogListener {
        public void onSelectSize(boolean useDefault, int rows, int cols);
    }

    static SelectSizeDialogFragment newInstance(boolean useDefaultSize, int rows, int cols) {
        SelectSizeDialogFragment f = new SelectSizeDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean("useDefaultSize", useDefaultSize);
        args.putInt("rows", rows);
        args.putInt("cols", cols);
        f.setArguments(args);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.select_size_fragment, null);
        final SelectSizeDialogListener listener = (SelectSizeDialogListener)getActivity();
        final boolean useDefaultSize = getArguments().getBoolean("useDefaultSize");
        final int rows = getArguments().getInt("rows");
        final int cols = getArguments().getInt("cols");
        final CheckBox defaultCheckBox = (CheckBox) v.findViewById(R.id.use_default_size_checkbox);
        final EditText rowEdit = (EditText) v.findViewById(R.id.select_rows);
        final EditText colEdit = (EditText) v.findViewById(R.id.select_cols);
        defaultCheckBox.setChecked(useDefaultSize);
        defaultCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    rowEdit.setEnabled(false);
                    colEdit.setEnabled(false);
                } else {
                    rowEdit.setEnabled(true);
                    colEdit.setEnabled(true);
                }
            }
        });
        rowEdit.setEnabled(!useDefaultSize);
        rowEdit.setText("" + rows);
        colEdit.setEnabled(!useDefaultSize);
        colEdit.setText("" + cols);

        builder.setView(v)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        boolean useDefault = defaultCheckBox.isChecked();
                        int rows = -1;
                        int cols = -1;
                        if (!useDefault) {
                            String rowStr = rowEdit.getText().toString();
                            String colStr = colEdit.getText().toString();
                            try {
                                rows = Integer.parseInt(rowStr);
                                cols = Integer.parseInt(colStr);
                            } catch (NumberFormatException e) {
                                rows = -1;
                                cols = -1;
                            }
                        }
                        listener.onSelectSize(useDefault, rows, cols);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
        return builder.create();
    }
}
