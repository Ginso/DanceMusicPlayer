package com.ldt.musicr.ui.widget.view;

import android.content.Context;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ldt.musicr.R;
import com.ldt.musicr.util.WidgetFactory;

import java.lang.annotation.Annotation;

import butterknife.BindView;
import butterknife.OnTextChanged;

public class MPSearchLikelyView extends ConstraintLayout  {
    @BindView(R.id.edit)
    EditText editText;

    public MPSearchLikelyView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public MPSearchLikelyView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public MPSearchLikelyView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public MPSearchLikelyView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        View.inflate(context, R.layout.compound_search_likely_view, this);
    }

    public EditText getEditText() {return editText;}


}
