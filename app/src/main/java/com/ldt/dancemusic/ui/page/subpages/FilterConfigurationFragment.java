package com.ldt.dancemusic.ui.page.subpages;

import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ldt.dancemusic.R;
import com.ldt.dancemusic.loader.medialoader.SongLoader;
import com.ldt.dancemusic.model.Song;
import com.ldt.dancemusic.ui.page.librarypage.LibraryPagerAdapter;
import com.ldt.dancemusic.ui.page.librarypage.LibraryTabFragment;
import com.ldt.dancemusic.ui.page.subpages.singleplaylist.SinglePlaylistFragment;
import com.ldt.dancemusic.ui.widget.fragmentnavigationcontroller.NavigationFragment;
import com.ldt.dancemusic.util.Constants;
import com.ldt.dancemusic.util.NavigationUtil;
import com.ldt.dancemusic.util.WidgetFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.LinearLayout.VERTICAL;

public class FilterConfigurationFragment extends NavigationFragment {

    public enum BoolTypes {
        ROTATE,
        SPINNER,
        CHECKBOXES
    }


    public static JSONArray getConfiguration() {
        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            JSONArray arr = new JSONArray(content);
            return arr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONArray arr = new JSONArray();
        try {
            JSONArray line = new JSONArray();

            JSONObject o = new JSONObject();
            o.put(Constants.FIELD_FILTER, true);
            o.put(Constants.FIELD_TAG, "rating");
            o.put(Constants.FIELD_TYPE, 0);
            o.put(Constants.FIELD_TYPE2, 0);
            o.put(Constants.FIELD_TEXT, "Rating");
            o.put(Constants.FIELD_LENGTH, 3);
            o.put(Constants.FIELD_TEXTSIZE, 14);
            line.put(o);

            o = new JSONObject();
            o.put(Constants.FIELD_FILTER, true);
            o.put(Constants.FIELD_TAG, "title");
            o.put(Constants.FIELD_TYPE, 0);
            o.put(Constants.FIELD_TYPE2, 0);
            o.put(Constants.FIELD_TEXT, "Title");
            o.put(Constants.FIELD_LENGTH, 3);
            o.put(Constants.FIELD_TEXTSIZE, 14);
            line.put(o);

            arr.put(line);

            line = new JSONArray();

            o = new JSONObject();
            o.put(Constants.FIELD_FILTER, false);
            o.put(Constants.FIELD_TAG, "tpm");
            o.put(Constants.FIELD_TYPE, 0);
            o.put(Constants.FIELD_TYPE2, 0);
            o.put(Constants.FIELD_TEXT, "TPM");
            o.put(Constants.FIELD_LENGTH, 3);
            o.put(Constants.FIELD_TEXTSIZE, 14);
            line.put(o);

            o = new JSONObject();
            o.put(Constants.FIELD_FILTER, false);
            o.put(Constants.FIELD_TAG, "title");
            o.put(Constants.FIELD_TYPE, 0);
            o.put(Constants.FIELD_TYPE2, 0);
            o.put(Constants.FIELD_TEXT, "Name");
            o.put(Constants.FIELD_LENGTH, 3);
            o.put(Constants.FIELD_TEXTSIZE, 14);
            line.put(o);

            o = new JSONObject();
            o.put(Constants.FIELD_FILTER, false);
            o.put(Constants.FIELD_TAG, "rating");
            o.put(Constants.FIELD_TYPE, 0);
            o.put(Constants.FIELD_TYPE2, 0);
            o.put(Constants.FIELD_TEXT, "Rating");
            o.put(Constants.FIELD_LENGTH, 3);
            o.put(Constants.FIELD_TEXTSIZE, 14);
            line.put(o);

            arr.put(line);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arr;
    }

    public static FilterConfigurationFragment newInstance() {
        return new FilterConfigurationFragment();
    }

    public static File file;

    @BindView(R.id.status_bar) View mStatusBar;
    @BindView(R.id.lines) LinearLayout linesContainer;
    @BindView(R.id.selectedLineContainer) LinearLayout selectedLineContainer;

    int selectedLine = -1;
    JSONArray lines= null;
    WidgetFactory widgetFactory;



    @Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {

        return inflater.inflate(R.layout.screen_filter_configuration,container, false);
    }

    private Unbinder mUnbinder;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this,view);
        widgetFactory = new WidgetFactory(getContext());
        lines = getConfiguration();
        updateLines();
    }

    void updateLines() {
        linesContainer.removeAllViews();
        LibraryTabFragment libraryTab = NavigationUtil.getLibraryTab(getActivity());
        LibraryPagerAdapter pagerAdapter = libraryTab.getPagerAdapter();
        pagerAdapter.getSongChildTab().getAdapter().sortHolder.updateLines(lines);
        NavigationFragment topFragment = libraryTab.getNavigationController().getTopFragment();
        if(topFragment instanceof SinglePlaylistFragment) {
            ((SinglePlaylistFragment)topFragment).getAdapter().sortHolder.updateLines(lines);
        } else if(topFragment instanceof DancePagerFragment) {
            ((DancePagerFragment)topFragment).getAdapter().sortHolder.updateLines(lines);
        }

        try {
            for(int i = 0; i < lines.length(); i++) {
                final int idx = i;
                JSONArray line = lines.getJSONArray(i);
                int height = WRAP_CONTENT;
                if(line.length() == 0) height = (int)(20*getResources().getDisplayMetrics().scaledDensity);
                LinearLayout lineLayout = widgetFactory.createLinearLayout(MATCH_PARENT, height, HORIZONTAL);
                if(i == selectedLine) lineLayout.setBackgroundColor(getResources().getColor(R.color.tansparantYellow, null));
                lineLayout.setOnClickListener(v -> selectLine(idx));
                lineLayout.addView(widgetFactory.createFiller());
                for(int j = 0; j < line.length(); j++) {
                    lineLayout.addView(widgetFactory.createFilterView(line.getJSONObject(j)));
                    lineLayout.addView(widgetFactory.createFiller());
                }
                linesContainer.addView(lineLayout);
            }
            linesContainer.addView(widgetFactory.createButton("Add Line", v -> {
                JSONArray arr = new JSONArray();
                lines.put(arr);
                save();
                updateLines();
                selectLine(lines.length()-1);
            }));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        disableViews(linesContainer);
    }

    void disableViews(LinearLayout layout) {
        for(int i = 0; i < layout.getChildCount(); i++) {
            View v = layout.getChildAt(i);
            if(v instanceof LinearLayout) disableViews((LinearLayout)v);
            else if(v instanceof EditText
                || v instanceof Spinner
                || v instanceof RadioButton
                || v instanceof CheckBox
            ) v.setEnabled(false);
        }
    }



    void selectLine(int i) {
        if(selectedLine >= 0) {
            linesContainer.getChildAt(selectedLine).setBackgroundColor(Color.TRANSPARENT);
        }

        if(selectedLine == i) selectedLine=-1;
        else {
            selectedLine = i;
            linesContainer.getChildAt(selectedLine).setBackgroundColor(getResources().getColor(R.color.tansparantYellow, null));
        }
        loadLine();
    }

    void putIntoCurrentLine(int idx, String key, Object val)  {
        try {
            JSONArray array = lines.getJSONArray(selectedLine);
            JSONObject obj = array.getJSONObject(idx);
            obj.put(key, val);
            array.put(idx, obj);
            lines.put(selectedLine, array);
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    void loadLine() {
        List<String> ratingTypes = new ArrayList<>();
        ratingTypes.add("Text");
        ratingTypes.add("★★☆");
        ratingTypes.add("♫♫♫");
        ratingTypes.add("0 1 2");
        List<String> boolTypes = new ArrayList<>();
        boolTypes.add("true/false/all");
        boolTypes.add("Dropdown");
        boolTypes.add("Checkboxes");

        try {
            selectedLineContainer.removeAllViews();
            if (selectedLine < 0) return;
            final JSONArray line = lines.getJSONArray(selectedLine);
            final List<String> tagNames = SongLoader.getTagNames();
            selectedLineContainer.addView(widgetFactory.createButton("Remove Line", v -> {
                lines.remove(selectedLine);
                save();
                selectedLine = -1;
                updateLines();
                selectedLineContainer.removeAllViews();
            }));
            for (int i = 0; i < line.length(); i++) {
                final int idx = i;
                LinearLayout btns = widgetFactory.createLinearLayout(WRAP_CONTENT, WRAP_CONTENT, HORIZONTAL);
                if(idx > 0)
                    btns.addView(widgetFactory.createButton("↑", v -> {
                        try {
                            JSONObject o = line.getJSONObject(idx);
                            JSONObject o2 = line.getJSONObject(idx-1);
                            line.put(idx-1, o);
                            line.put(idx, o2);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        save();
                        updateLines();
                        loadLine();
                    }));
                if(idx < line.length()-1)
                    btns.addView(widgetFactory.createButton("↓", v -> {
                        try {
                            JSONObject o = line.getJSONObject(idx);
                            JSONObject o2 = line.getJSONObject(idx-1);
                            line.put(idx-1, o);
                            line.put(idx, o2);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        save();
                        updateLines();
                        loadLine();
                    }));
                btns.addView(widgetFactory.createButton("X", v -> {
                    line.remove(idx);
                    save();
                    updateLines();
                    loadLine();
                }));


                final JSONObject obj = line.getJSONObject(idx);
                final boolean filter = obj.optBoolean(Constants.FIELD_FILTER, false);
                final String tagName = obj.optString(Constants.FIELD_TAG, "");
                final int type = obj.optInt(Constants.FIELD_TYPE,0);
                LinearLayout layout = widgetFactory.createLinearLayout(MATCH_PARENT, WRAP_CONTENT, VERTICAL);
                selectedLineContainer.addView(layout);
                layout.setBackgroundResource(R.drawable.border);
                layout.addView(btns);
                layout.addView(widgetFactory.createRadioGroup(WRAP_CONTENT, WRAP_CONTENT, HORIZONTAL, new String[]{"Filter", "Sorter"}, filter?0:1, id -> {
                    putIntoCurrentLine(idx, Constants.FIELD_FILTER, id==0);
                    save();
                    loadLine();
                }));

                LinearLayout tagLayout = widgetFactory.createLinearLayout(WRAP_CONTENT, WRAP_CONTENT, HORIZONTAL);
                layout.addView(tagLayout);
                tagLayout.addView(widgetFactory.createTextView("Tag:",14));
                final int selected = tagNames.indexOf(tagName);
                tagLayout.addView(widgetFactory.createSpinner(tagNames, selected, false, n -> {
                    if(n == selected) return;
                    putIntoCurrentLine(idx, Constants.FIELD_TAG, tagNames.get(n));
                    save();
                    updateLines();
                    loadLine();
                }));

                final LinearLayout nameView = widgetFactory.createEditText("Name:", obj.optString(Constants.FIELD_TEXT, ""), 8, InputType.TYPE_CLASS_TEXT,14, name -> {
                    putIntoCurrentLine(idx, Constants.FIELD_TEXT, name);
                    save();
                    updateLines();
                });

                if(filter) {
                    if(selected < 0) continue;
                    Song.Tag tag = SongLoader.getAllTags().get(tagName);
                    switch (tag.type) {
                        case RATING:
                            layout.addView(nameView);
                            final Spinner typeSpinner = widgetFactory.createSpinner(ratingTypes,type,false,n -> {
                                putIntoCurrentLine(idx, Constants.FIELD_TYPE, n);
                                save();
                                updateLines();
                            });
                            layout.addView(typeSpinner);
                            break;
                        case BOOL:
                            layout.addView(nameView);
                            layout.addView(widgetFactory.createSpinner(boolTypes,type,false,n -> {
                                putIntoCurrentLine(idx, Constants.FIELD_TYPE, n);
                                save();
                                updateLines();
                            }));
                            break;
                        default:
                            layout.addView(nameView);
                            layout.addView(widgetFactory.createSizeControl("Length", obj.optInt(Constants.FIELD_LENGTH, 5), ems -> {
                                putIntoCurrentLine(idx, Constants.FIELD_LENGTH, ems);
                                updateLines();
                                save();
                            }));
                    }
                } else {
                    layout.addView(nameView);
                }
                layout.addView(widgetFactory.createSizeControl("Text size", obj.optInt(Constants.FIELD_TEXTSIZE,14), size -> {
                    putIntoCurrentLine(idx, Constants.FIELD_TEXTSIZE, size);
                    updateLines();
                    save();
                }));
            }

            selectedLineContainer.addView(widgetFactory.createButton("Add Filter/Sorter", v -> {
                try {
                    JSONObject o = new JSONObject();
                    o.put(Constants.FIELD_FILTER, true);
                    o.put(Constants.FIELD_TAG, "rating");
                    o.put(Constants.FIELD_TYPE, 0);
                    o.put(Constants.FIELD_TYPE2, 0);
                    o.put(Constants.FIELD_TEXT, "");
                    o.put(Constants.FIELD_LENGTH, 5);
                    o.put(Constants.FIELD_TEXTSIZE, 14);
                    line.put(o);
                    lines.put(selectedLine, line);
                    save();
                    updateLines();
                    loadLine();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }));
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    void save() {
        try {
            Files.write(file.toPath(),lines.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
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

}
