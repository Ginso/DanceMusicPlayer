package com.ldt.dancemusic.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;

public class TextInputDialog extends DialogFragment {
    String title;
    String button;
    String hint;
    MaterialDialog.InputCallback consumer;

    public TextInputDialog(String title, String button, String hint, MaterialDialog.InputCallback consumer) {
        this.title = title;
        this.button = button;
        this.hint = hint;
        this.consumer = consumer;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        return new MaterialDialog.Builder(getActivity())
                .title(title)
                .positiveText(button)
                .negativeText(android.R.string.cancel)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(hint, "", false, consumer)
                .build();
    }
}
