package com.example.yabinc.sudogame;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;

/**
 * Created by yabinc on 6/29/16.
 */
public class SelectFixCountAndReasonableFragment extends DialogFragment {

    public interface SelectFixCountAndReasonableDialogListener {
        public void onSelectFixCountAndReasonable(int fixCount, boolean reasonable);
    }

    static SelectFixCountAndReasonableFragment newInstance(int fixCount, boolean reasonable) {
        SelectFixCountAndReasonableFragment f = new SelectFixCountAndReasonableFragment();
        Bundle args = new Bundle();
        args.putInt("fixCount", fixCount);
        args.putBoolean("reasonable", reasonable);
        f.setArguments(args);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v = inflater.inflate(R.layout.select_fixcount_and_reasonable, null);
        final SelectFixCountAndReasonableDialogListener listener =
                (SelectFixCountAndReasonableDialogListener)getActivity();
        final int fixCount = getArguments().getInt("fixCount");
        final boolean reasonable = getArguments().getBoolean("reasonable");
        final EditText fixCountEdit = (EditText) v.findViewById(R.id.select_fixcount);
        final CheckBox reasonableCheckBox = (CheckBox) v.findViewById(R.id.select_reasonable);
        fixCountEdit.setText("" + fixCount);
        fixCountEdit.setEnabled(true);
        reasonableCheckBox.setChecked(reasonable);
        builder.setView(v).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String fixCountStr = fixCountEdit.getText().toString();
                int fixCount = -1;
                try {
                    fixCount = Integer.parseInt(fixCountStr);
                } catch (NumberFormatException e) {
                    fixCount = -1;
                }
                boolean reasonable = reasonableCheckBox.isChecked();
                listener.onSelectFixCountAndReasonable(fixCount, reasonable);
            }
        })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        return builder.create();
    }
}
