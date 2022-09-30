package com.ldt.dancemusic.ui.widget.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.ldt.dancemusic.R;

import butterknife.BindView;

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
