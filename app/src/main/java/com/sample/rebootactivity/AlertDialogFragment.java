package com.sample.rebootactivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * Created by y_hisano on 2017/03/06.
 */

public class AlertDialogFragment extends DialogFragment {
    public static final String FRAGMENT_TAG = AlertDialogFragment.class.getSimpleName();
    private static final String KEY_MESSAGE = "MESSAGE";

    public static AlertDialogFragment newInstance(String message) {
        Bundle args = new Bundle();
        args.putString(KEY_MESSAGE, message);

        AlertDialogFragment fragment = new AlertDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getContext())
                .setTitle(getArguments()
                .getString(KEY_MESSAGE))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
    }
}
