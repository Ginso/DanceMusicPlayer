package com.ldt.musicr.ui.page.subpages;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ldt.musicr.R;
import com.ldt.musicr.loader.medialoader.SongLoader;
import com.ldt.musicr.model.Song;
import com.ldt.musicr.ui.widget.fragmentnavigationcontroller.NavigationFragment;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.Unbinder;

public class TagsFragment extends NavigationFragment {

    @BindView(R.id.status_bar) View mStatusBar;
    @BindView(R.id.root) View mRoot;
    @BindView(R.id.predefinedTags) LinearLayout predefinedTags;
    @BindView(R.id.customTags) LinearLayout customTags;
    @BindView(R.id.alertTitle) TextView alertText;
    @BindView(R.id.containerBackground) View containerBackground;
    @BindView(R.id.alertView) LinearLayout alertContainer;
    @BindView(R.id.alertCheckbox) CheckBox alertCheckbox;
    @BindView(R.id.tagSpinner) Spinner spinner;
    @BindView(R.id.editText) EditText newTagName;
    @BindView(R.id.netTagArgLbl) TextView newTagArgLbl;
    @BindView(R.id.editText2) EditText newTagArg;



    private Song.Tag currentTag;
    private LinearLayout mLeftLayout;
    private LinearLayout mRightLayout;
    private LinearLayout mButtonsLayout;

    public static TagsFragment newInstance() {
        return new TagsFragment();
    }

    @Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {

        return inflater.inflate(R.layout.screen_tags,container, false);
    }

    private Unbinder mUnbinder;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this,view);
        LinearLayout left = (LinearLayout) predefinedTags.getChildAt(0);
        LinearLayout right = (LinearLayout) predefinedTags.getChildAt(1);
        for(int i = 0; i < 6; i++) {
            Song.Tag tag = SongLoader.getTag(i);
            addTextView(tag, left, right);
        }

        mLeftLayout = (LinearLayout) customTags.getChildAt(0);
        mRightLayout = (LinearLayout) customTags.getChildAt(1);
        mButtonsLayout = (LinearLayout) customTags.getChildAt(2);

        addCustomTags();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.tagTypes, R.layout.spinner_layout);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

    }

    private void addCustomTags() {
        mLeftLayout.removeAllViews();
        mRightLayout.removeAllViews();
        mButtonsLayout.removeAllViews();
        for(int i = 6; i < SongLoader.getTagNames().size(); i++) {
            addCustomTag(SongLoader.getTag(i));
        }
    }

    private void addCustomTag(final Song.Tag tag) {
        TextView button = createTextView(" X ");
        button.setTextColor(Color.RED);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String format = getResources().getString(R.string.deleteTag);
                String text = String.format(format, tag.name);
                currentTag = tag;
                alertText.setText(text);
                alertContainer.setVisibility(View.VISIBLE);
                containerBackground.setVisibility(View.VISIBLE);
            }
        });

        addTextView(tag, mLeftLayout, mRightLayout);
        mButtonsLayout.addView(button);
    }

    private void addTextView(Song.Tag tag, LinearLayout left, LinearLayout right) {
        left.addView(createTextView(tag.name));
        right.addView(createTextView(tag.type.getText(getResources(), tag.arg)));
    }

    private TextView createTextView(String text) {
        TextView textView = new TextView(this.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(params);
        textView.setTextSize(20);
        textView.setText(text);
        textView.setTextColor(getResources().getColor(R.color.FlatWhite, null));
        int p = 10;
        textView.setPadding(p,p,p,p);
        textView.setBackgroundResource(R.drawable.border);
        return textView;
    }

    @OnClick(R.id.alertOk)
    public void delete() {
        SongLoader.removeTag(currentTag, alertCheckbox.isChecked());
        alertContainer.setVisibility(View.GONE);
        containerBackground.setVisibility(View.GONE);
        addCustomTags();
    }

    @OnClick(R.id.alertCancel)
    public void cancel() {
        alertContainer.setVisibility(View.GONE);
        containerBackground.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        if(mUnbinder!=null) {
            mUnbinder.unbind();
            mUnbinder = null;
        }
        super.onDestroyView();
    }

    @OnClick(R.id.back_button)
    void back() {
        getNavigationController().dismissFragment();
    }

    @Override
    public void onSetStatusBarMargin(int value) {
        mStatusBar.getLayoutParams().height = value;
    }

    @OnItemSelected(R.id.tagSpinner)
    public void onTagSelected(int pos) {
        switch (Song.Tag.Type.fromInteger(pos)) {
            case RATING:
                newTagArgLbl.setVisibility(View.VISIBLE);
                newTagArgLbl.setText("0 -");
                newTagArg.setVisibility(View.VISIBLE);
                newTagArg.setText("5");
                break;
            case FLOAT:
                newTagArgLbl.setVisibility(View.VISIBLE);
                newTagArgLbl.setText("Decimals:");
                newTagArg.setVisibility(View.VISIBLE);
                newTagArg.setText("1");
                break;
            default:
                newTagArgLbl.setVisibility(View.GONE);
                newTagArg.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.saveTag)
    public void saveTag() {
        String name = newTagName.getText().toString().trim();
        String[] forbidden = new String[] {"<", ">", "\"", "*"};
        for(String s:forbidden) {
            if(name.contains(s)) {
                Toast.makeText(getContext(), "Forbidden characters: " + String.join("", forbidden), Toast.LENGTH_LONG).show();
                return;
            }
        }

        String nameLC = name.trim().toLowerCase();
        Map<String, Song.Tag> allTags = SongLoader.getAllTags();
        for(String tagName:SongLoader.getTagNames()) {
            Song.Tag tag = allTags.get(tagName);
            if(tag.name.toLowerCase().equals(nameLC)) {
                Toast.makeText(getContext(), "Name already exists", Toast.LENGTH_LONG).show();
                return;
            }
        }
        int arg = -1;
        Song.Tag.Type type = Song.Tag.Type.fromInteger(spinner.getSelectedItemPosition());
        if(type == Song.Tag.Type.RATING || type == Song.Tag.Type.FLOAT) {
            arg = Integer.parseInt(newTagArg.getText().toString());
        }
        Song.Tag tag = new Song.Tag(name, type, arg);
        SongLoader.addTag(tag);
        addCustomTag(tag);
    }


}
