package com.ldt.dancemusic.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.function.Consumer;

public class CheckboxDialog extends DialogFragment {
    String title;
    String text;
    String button;
    Consumer<Boolean> callback;

    boolean checked = false;

    public CheckboxDialog(String title, String text, String button, Consumer<Boolean> callback) {
        this.title = title;
        this.text = text;
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
                .checkBoxPrompt(text, false,(buttonView, isChecked) -> {
                    checked = isChecked;
                })
                .onPositive((dialog, which) -> callback.accept(checked))
                .build();
    }
}
