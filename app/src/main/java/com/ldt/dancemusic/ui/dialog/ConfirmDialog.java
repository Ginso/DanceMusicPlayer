package com.ldt.dancemusic.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;

public class ConfirmDialog extends DialogFragment {
    String title;
    String button;
    MaterialDialog.SingleButtonCallback callback;

    public ConfirmDialog(String title, String button, MaterialDialog.SingleButtonCallback callback) {
        super();
        this.title = title;
        this.button = button;
        this.callback = callback;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new MaterialDialog.Builder(getActivity())
                .title(title)
                .positiveText(button)
                .negativeText(android.R.string.cancel)
                .onPositive(callback)
                .build();
    }
}
