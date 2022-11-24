package com.ldt.dancemusic.ui.page.subpages;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.ldt.dancemusic.R;
import com.ldt.dancemusic.loader.medialoader.SongLoader;
import com.ldt.dancemusic.model.Song;
import com.ldt.dancemusic.ui.dialog.ConfirmDialog;
import com.ldt.dancemusic.ui.dialog.TextInputDialog;
import com.ldt.dancemusic.ui.page.librarypage.LibraryPagerAdapter;
import com.ldt.dancemusic.ui.page.librarypage.LibraryTabFragment;
import com.ldt.dancemusic.ui.page.subpages.singleplaylist.SinglePlaylistFragment;
import com.ldt.dancemusic.ui.widget.fragmentnavigationcontroller.NavigationFragment;
import com.ldt.dancemusic.util.Constants;
import com.ldt.dancemusic.util.NavigationUtil;
import com.ldt.dancemusic.util.PreferenceUtil;
import com.ldt.dancemusic.util.WidgetFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.LinearLayout.VERTICAL;

public class SonglistConfigurationFragment extends NavigationFragment {

    private static JSONObject getAllConfigurations() {
        try {
            String content = new String(Files.readAllBytes(file.toPath()));
            JSONObject o = new JSONObject(content);
            return o;
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject root = new JSONObject();
        try {
            JSONObject layout = createLineObject(HORIZONTAL, MATCH_PARENT, 70,
                    createTagObject(Song._DURATION, 6, 50, 14, "%s"),
                    createLineObject(VERTICAL,70, WRAP_CONTENT,
                            createTagObject(Song._TPM, 0, WRAP_CONTENT, 14, "%s TPM"),
                            createTagObject(Song._RATING, 1, WRAP_CONTENT, 14, "%s")
                    ),
                    createLineObject(VERTICAL, MATCH_PARENT, WRAP_CONTENT,
                            createTagObject(Song._TITLE, 0, WRAP_CONTENT,14, "%s"),
                            createTagObject(Song._ARTIST, 0, WRAP_CONTENT,12, "%s")
                    )
            );
            root.put("default", layout);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return root;
    }


    private static JSONObject getConfiguration(String name, JSONObject layouts) {
        try {

            JSONObject o = layouts == null ? getAllConfigurations() : layouts;
            if(o.has(name)) return o.getJSONObject(name);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new JSONObject();
    }

    public static JSONObject getConfigurationFor(String usage) {
        String name = PreferenceUtil.getInstance().getString(usage, "default");
        return getConfiguration(name, null);
    }



    private static JSONObject createTagObject(String tag, int type, int width, int textsize, String format) throws JSONException {
        JSONObject o = new JSONObject();
        o.put(Constants.FIELD_TYPE, type);
        o.put(Constants.FIELD_TAG, tag);
        o.put(Constants.FIELD_WIDTH, width);
        o.put(Constants.FIELD_TEXTSIZE, textsize);
        o.put(Constants.FIELD_FORMAT, format);
        return o;
    }

    private static JSONObject createLineObject(int orientation, int width, int height, Object... children) throws JSONException {
        JSONObject o = new JSONObject();
        o.put(Constants.FIELD_TYPE,-1 - orientation);
        o.put(Constants.FIELD_WIDTH, width);
        o.put(Constants.FIELD_HEIGHT, height);
        JSONArray arr = new JSONArray();
        for(Object obj:children) arr.put(obj);
        o.put(Constants.FIELD_CHILDREN, arr);
        return o;
    }

    public static SonglistConfigurationFragment newInstance() {
        return new SonglistConfigurationFragment();
    }

    public static File file;

    @BindView(R.id.status_bar) View mStatusBar;
    @BindView(R.id.root) View mRoot;
    @BindView(R.id.preview) LinearLayout preview;
    @BindView(R.id.scheme) LinearLayout schemeLayout;
    @BindView(R.id.configuration) LinearLayout configuration;
    @BindView(R.id.picker) Spinner picker;
    @BindView(R.id.deleteLayout) Button deleteBtn;
    @BindViews({R.id.useInSongs, R.id.useInPlaylists, R.id.useInDance}) Button[] usageButtons;

    int selectedLine = -1;
    JSONObject allLayouts = null;
    JSONObject currentLayout = null;
    String layoutName = "default";
    WidgetFactory widgetFactory;
    int indentSize;
    final Map<String, Song.Tag> allTags = SongLoader.getAllTags();
    final ArrayList<String> tags = new ArrayList<>(allTags.keySet());
    List<Song> previewSongs = new ArrayList<>();
    Random random = new Random();
    final String[] usages = {PreferenceUtil.LAYOUT_SONGS, PreferenceUtil.LAYOUT_PLAYLIST, PreferenceUtil.LAYOUT_DANCE};

    @Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {

        return inflater.inflate(R.layout.screen_songlist_configuration,container, false);
    }

    private Unbinder mUnbinder;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this,view);
        widgetFactory = new WidgetFactory(getContext());
        indentSize = widgetFactory.scale(30);
        allLayouts = getAllConfigurations();
        layoutName = allLayouts.keys().next();
        currentLayout = getConfiguration(layoutName, allLayouts);
        loadPicker();
        loadUsage();
        loadScheme();
        loadRandomPreview();
    }

    void loadPicker() {
        final List<String> layouts = new ArrayList<>();
        allLayouts.keys().forEachRemaining(layouts::add);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), R.layout.spinner_layout, layouts);
        picker.setAdapter(adapter);
        picker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String name = layouts.get(position);
                if(name.equals(layoutName)) return;
                layoutName = name;
                currentLayout = getConfiguration(name, allLayouts);
                loadUsage();
                loadScheme();
                loadRandomPreview();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        picker.setSelection(layouts.indexOf(layoutName));
        deleteBtn.setEnabled(layouts.size() > 1);
        loadUsage();
    }

    void loadUsage() {
        for(int i = 0; i < usages.length; i++) {
            String name = PreferenceUtil.getInstance().getString(usages[i], "default");
            usageButtons[i].setEnabled(!name.equals(layoutName));
        }
    }

    @OnClick(R.id.renameLayout)
    void renameLayout() {
        new TextInputDialog("Rename Layout " + layoutName, "Rename", "Name", (materialDialog, charSequence) -> {
            final String name = charSequence.toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "name cannot be empty", Toast.LENGTH_LONG).show();
                return;
            }
            if(allLayouts.has(name)) {
                Toast.makeText(getContext(), "A Layout with that name already exists", Toast.LENGTH_LONG).show();
                return;
            }
            try {

                for(String key : usages) {
                    if(PreferenceUtil.getInstance().getString(key, "default").equals(layoutName))
                        PreferenceUtil.getInstance().setString(key, name);
                }
                allLayouts.remove(layoutName);
                layoutName = name;
                allLayouts.put(name, currentLayout);
                loadPicker();
                save();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).show(getActivity().getSupportFragmentManager(), "RENAME_LAYOUT");
    }

    @OnClick(R.id.newLayout)
    void createLayout() {
        new TextInputDialog("New Layout", "Create", "Name", (materialDialog, charSequence) -> {
            final String name = charSequence.toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(getContext(), "name cannot be empty", Toast.LENGTH_LONG).show();
                return;
            }
            if(allLayouts.has(name)) {
                Toast.makeText(getContext(), "A Layout with that name already exists", Toast.LENGTH_LONG).show();
                return;
            }
            try {
                layoutName = name;
                currentLayout = new JSONObject(currentLayout.toString());
                allLayouts.put(name, currentLayout);
                loadPicker();
                save();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).show(getActivity().getSupportFragmentManager(), "CREATE_LAYOUT");
    }

    @OnClick(R.id.deleteLayout)
    void deleteLayout() {
        new ConfirmDialog("Delete Layout " + layoutName, "Delete", (dialog, action) -> {
            allLayouts.remove(layoutName);
            String first = allLayouts.keys().next();
            for(String key : new String[] {PreferenceUtil.LAYOUT_SONGS, PreferenceUtil.LAYOUT_PLAYLIST, PreferenceUtil.LAYOUT_DANCE}) {
                if(PreferenceUtil.getInstance().getString(key, "default").equals(layoutName))
                    PreferenceUtil.getInstance().setString(key, first);
            }
            layoutName = first;
            loadPicker();
            currentLayout = getConfiguration(layoutName, allLayouts);
            loadScheme();
        }).show(getActivity().getSupportFragmentManager(), "DELETE_LAYOUT");;
    }

    void loadScheme() {
        schemeLayout.removeAllViews();
        addToScheme(currentLayout, null,0);
    }

    @OnClick(R.id.title2)
    void loadRandomPreview() {
        previewSongs.clear();
        ArrayList<Song> allSongs = new ArrayList<>(SongLoader.getAllSongs(getContext()));
        int n = Math.min(5, allSongs.size());
        for(int i = 0; i < n; i++) {
            int rand = random.nextInt(allSongs.size());
            previewSongs.add(allSongs.remove(rand));
        }
        updatePreview();
    }

    void updatePreview() {
        preview.removeAllViews();
        long sum = 0;
        for(Song song:previewSongs) {
            song.durationSum = sum;
            sum += song.duration;
            widgetFactory.loadView(currentLayout, preview, song, true);
        }

    }

    @OnClick(R.id.useInSongs) void useInSongs() {useIn(0);}
    @OnClick(R.id.useInPlaylists) void useInPlaylists() {useIn(1);}
    @OnClick(R.id.useInDance) void useInDance() {useIn(2);}

    void useIn(int i) {
        PreferenceUtil.getInstance().setString(usages[i], layoutName);
        usageButtons[i].setEnabled(false);
        NavigationUtil.updateAll(getActivity());
    }

    void addToScheme(JSONObject o, JSONArray parent, int indent) {
        try {
            int type = o.getInt(Constants.FIELD_TYPE);
            int lineIdx = schemeLayout.getChildCount();
            final View view;
            Consumer<LinearLayout.LayoutParams> modifyIdent = p -> p.leftMargin = indent * indentSize;
            if(type < 0) {
                int orientation = -1 - type;
                final LinearLayout line = widgetFactory.modifyParams(widgetFactory.createLinearLayout(WRAP_CONTENT, WRAP_CONTENT, HORIZONTAL), modifyIdent);
                schemeLayout.addView(line);
                line.addView(widgetFactory.createTextView(orientation == HORIZONTAL ? "Line" : "Column"));
                line.addView(createIcon(orientation == HORIZONTAL ? R.drawable.layout_horizontal : R.drawable.layout_vertical));
                final JSONArray children = o.getJSONArray(Constants.FIELD_CHILDREN);
                for(int i = 0; i < children.length(); i++) {
                    JSONObject child = children.getJSONObject(i);
                    addToScheme(child, children, indent+1);
                }
                view = line;
            } else {
                String tagName = o.getString(Constants.FIELD_TAG);
                view = widgetFactory.modifyParams(widgetFactory.createTextView(tagName), modifyIdent);
                schemeLayout.addView(view);
            }
            view.setOnClickListener(v -> {
                if (selectedLine >= 0) {
                   schemeLayout.getChildAt(selectedLine).setBackgroundColor(Color.TRANSPARENT);
                }
                view.setBackgroundColor(getResources().getColor(R.color.tansparantYellow, null));
                selectedLine = lineIdx;
                loadObjectConfig(o, parent);
            });
            if (lineIdx == selectedLine) {
                view.setBackgroundColor(getResources().getColor(R.color.tansparantYellow, null));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public LinearLayout createSizeControl(String lbl, int size, Consumer<Integer> onChange) {
        int selected = size < 0 ? -size : 0;
        LinearLayout line = widgetFactory.createLinearLayout(WRAP_CONTENT, WRAP_CONTENT, HORIZONTAL);
        line.addView(widgetFactory.createTextView(lbl));
        line.addView(widgetFactory.createSpinner(Arrays.asList("custom", "max", "min"), selected, true,i-> {
            if(i == 0) onChange.accept(size < 0 ? 70 : size);
            else onChange.accept(-i);
        }));
        if (size > 0) {
            line.addView(widgetFactory.createSizeControl(null, size/10, i->onChange.accept(i*10)));
        }
        return line;
    }

    void loadObjectConfig(JSONObject o, JSONArray parent) {
        configuration.removeAllViews();
        try {
            int type = o.getInt(Constants.FIELD_TYPE);
            final int width = o.getInt(Constants.FIELD_WIDTH);
            View widthControl = createSizeControl("Width:", width, i -> {
                if(i==width) return;
                putAndLoad(o, parent, Constants.FIELD_WIDTH, i);
            });
            if (type < 0) {
                final int orientation = -1 - type;
                LinearLayout line = widgetFactory.createLinearLayout(WRAP_CONTENT, WRAP_CONTENT, HORIZONTAL);
                line.addView(widgetFactory.createTextView("Direction"));
                line.addView(widgetFactory.createSpinner(Arrays.asList("Line", "Column"),orientation, true, i -> {
                    if(i==orientation) return;
                    putAndLoad(o, parent, Constants.FIELD_TYPE, -1-i);
                }));
                configuration.addView(line);
                configuration.addView(widthControl);
                int height = o.getInt(Constants.FIELD_HEIGHT);
                LinearLayout heightControl = createSizeControl("Height:", height, i -> {
                    if(i==height) return;
                    putAndLoad(o, parent,Constants.FIELD_HEIGHT, i);
                });
                if(parent == null) heightControl.getChildAt(1).setEnabled(false);
                configuration.addView(heightControl);
                line = widgetFactory.createLinearLayout(WRAP_CONTENT, WRAP_CONTENT, HORIZONTAL);
                line.addView(widgetFactory.createButton("Add Container", v -> {
                    try {
                        JSONObject container = createLineObject(1-orientation, WRAP_CONTENT, WRAP_CONTENT);
                        JSONArray children = o.getJSONArray(Constants.FIELD_CHILDREN);
                        children.put(container);
                        o.put(Constants.FIELD_CHILDREN, children);
                        save();
                        loadObjectConfig(o, parent);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }));
                line.addView(widgetFactory.createButton("Add Item", v -> {
                    try {
                        JSONObject item = createTagObject(Song._DURATION, 6, WRAP_CONTENT, 14, "%s");
                        JSONArray children = o.getJSONArray(Constants.FIELD_CHILDREN);
                        children.put(item);
                        o.put(Constants.FIELD_CHILDREN, children);
                        save();
                        loadObjectConfig(o, parent);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }));
                configuration.addView(line);
            } else {
                String tagName = o.getString(Constants.FIELD_TAG);

                Song.Tag tag = allTags.get(tagName);
                LinearLayout line = widgetFactory.createLinearLayout(WRAP_CONTENT, WRAP_CONTENT, HORIZONTAL);
                final int sel = tags.indexOf(tagName);
                line.addView(widgetFactory.createTextView("Tag:"));
                line.addView(widgetFactory.createSpinner(tags,sel,true, i ->{
                    if(sel == i) return;
                    try {
                        String name = tags.get(i);
                        Song.Tag t = allTags.get(name);
                        o.put(Constants.FIELD_TAG, t.name);
                        if(t.type == Song.Tag.Type.DATETIME)
                            o.put(Constants.FIELD_TYPE, t.arg);
                        else
                            o.put(Constants.FIELD_TYPE, 0);
                        save();
                        loadObjectConfig(o, parent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }));
                configuration.addView(line);
                configuration.addView(widthControl);
                if(tag.type == Song.Tag.Type.RATING) {
                    line = widgetFactory.createLinearLayout(WRAP_CONTENT, WRAP_CONTENT, HORIZONTAL);
                    line.addView(widgetFactory.createTextView("Type:"));
                    line.addView(widgetFactory.createSpinner(Arrays.asList(new String[]{"Number", "★★☆", "♫♫♫"}),type,true, i -> {
                        if(i==type) return;
                        putAndLoad(o, parent, Constants.FIELD_TYPE, i);
                    }));
                    configuration.addView(line);
                }
                if(tag.type == Song.Tag.Type.DATETIME) {
                    line = widgetFactory.createLinearLayout(WRAP_CONTENT, WRAP_CONTENT, HORIZONTAL);
                    line.addView(widgetFactory.createTextView("Type:"));
                    line.addView(widgetFactory.createSpinner(Arrays.asList(Constants.dateFormats),type,true, i -> {
                        if(i==type) return;
                        try {
                            o.put(Constants.FIELD_TYPE, i);
                            save();
                            loadObjectConfig(o, parent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }));
                    configuration.addView(line);
                }

                String format = o.getString(Constants.FIELD_FORMAT);
                String f1 = "";
                String f2 = "";
                for(int i = 0; i < format.length(); i++) {
                    if(format.charAt(i) != '%') continue;
                    if(format.charAt(i+1) == '%') i++;
                    else {
                        f1 = format.substring(0,i).replace("%%", "%");
                        f2 = format.substring(i+2).replace("%%", "%");
                    }
                }

                EditText editText1 = widgetFactory.createEditText(10, InputType.TYPE_CLASS_TEXT);
                EditText editText2 = widgetFactory.createEditText(10, InputType.TYPE_CLASS_TEXT);
                editText1.setText(f1);
                editText2.setText(f2);
                editText1.setTag(1);
                editText2.setTag(2);
                EditText currentEditText = widgetFactory.getCurrentEditText();
                if(currentEditText != null) {
                    int t = (int) currentEditText.getTag();
                    widgetFactory.setCurrentEditText((t == 1) ? editText1 : editText2);
                    widgetFactory.focusCurrentEditText();
                }
                Consumer<String> c = s -> {
                    String s1 = editText1.getText().toString().replace("%", "%%");
                    String s2 = editText2.getText().toString().replace("%", "%%");
                    putAndLoad(o, parent, Constants.FIELD_FORMAT, s1 + "%s" + s2);
                };
                widgetFactory.createTextWatcher(editText1,c);
                widgetFactory.createTextWatcher(editText2,c);

                line = widgetFactory.createLinearLayout(WRAP_CONTENT, WRAP_CONTENT, HORIZONTAL);
                line.addView(widgetFactory.createTextView("Text before:"));
                line.addView(editText1);
                configuration.addView(line);

                line = widgetFactory.createLinearLayout(WRAP_CONTENT, WRAP_CONTENT, HORIZONTAL);
                line.addView(widgetFactory.createTextView("Text after:"));
                line.addView(editText2);
                configuration.addView(line);

                final int textSize = o.getInt(Constants.FIELD_TEXTSIZE);
                configuration.addView(widgetFactory.createSizeControl("Text size:", textSize, i -> {
                    putAndLoad(o, parent, Constants.FIELD_TEXTSIZE, i);
                }));

                final boolean bold = o.optBoolean(Constants.FIELD_BOLD, false);
                configuration.addView(widgetFactory.createCheckBox("Bold", bold, b -> putAndLoad(o, parent, Constants.FIELD_BOLD, b)));
                final boolean italic = o.optBoolean(Constants.FIELD_ITALIC, false);
                configuration.addView(widgetFactory.createCheckBox("Italic", italic, b -> putAndLoad(o, parent, Constants.FIELD_ITALIC, b)));
                final boolean underline = o.optBoolean(Constants.FIELD_UNDERLINE, false);
                configuration.addView(widgetFactory.createCheckBox("Underline", underline, b -> putAndLoad(o, parent, Constants.FIELD_UNDERLINE, b)));
                final int color = o.optInt(Constants.FIELD_COLOR, 0);
                line = widgetFactory.createLinearLayout(WRAP_CONTENT, WRAP_CONTENT, HORIZONTAL);
                line.addView(widgetFactory.createTextView("Color:"));
                line.addView(widgetFactory.createSpinner(Arrays.asList(Constants.colorList), color,true, i -> {
                    if(i==color) return;
                    putAndLoad(o, parent, Constants.FIELD_COLOR, i);
                }));
                configuration.addView(line);
            }
            final int bcolor = o.optInt(Constants.FIELD_BACKGROUND, 0);
            LinearLayout line = widgetFactory.createLinearLayout(WRAP_CONTENT, WRAP_CONTENT, HORIZONTAL);
            line.addView(widgetFactory.createTextView("Background:"));
            line.addView(widgetFactory.createSpinner(Arrays.asList(Constants.colorList2), bcolor,true, i -> {
                if(i==bcolor) return;
                putAndLoad(o, parent, Constants.FIELD_BACKGROUND, i);
            }));
            configuration.addView(line);

            if(parent != null) {
                line = widgetFactory.createLinearLayout(WRAP_CONTENT, WRAP_CONTENT, HORIZONTAL);
                int idx = 0;
                for (int i = 0; i < parent.length(); i++)
                    if (o.equals(parent.getJSONObject(i))) {
                        idx = i;
                        break;
                    }
                int finalIdx = idx;
                if (parent.length() > 1) {
                    if (idx > 0) {
                        line.addView(widgetFactory.createButton("↑", v -> {
                            try {
                                JSONObject obj = parent.getJSONObject(finalIdx - 1);
                                JSONArray arr = obj.optJSONArray(Constants.FIELD_CHILDREN);
                                int n = arr == null ? 1 : 1+arr.length();
                                selectedLine -= n;
                                parent.put(finalIdx - 1, o);
                                parent.put(finalIdx, obj);
                                save();
                                loadObjectConfig(o, parent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }));
                    }
                    if (idx < parent.length() - 1) {
                        line.addView(widgetFactory.createButton("↓", v -> {
                            try {
                                JSONObject obj = parent.getJSONObject(finalIdx + 1);
                                JSONArray arr = obj.optJSONArray(Constants.FIELD_CHILDREN);
                                int n = arr == null ? 1 : 1+arr.length();
                                selectedLine += n;
                                parent.put(finalIdx + 1, o);
                                parent.put(finalIdx, obj);
                                save();
                                loadObjectConfig(o, parent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }));
                    }
                }
                line.addView(widgetFactory.createButton("X", v -> {
                    parent.remove(finalIdx);
                    selectedLine = -1;
                    save();
                    configuration.removeAllViews();
                }));
                configuration.addView(line);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    ImageView createIcon(int icon) {
        ImageView img = new ImageView(getContext());
        int size = widgetFactory.scale(18);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.gravity = Gravity.CENTER_VERTICAL;
        params.leftMargin = widgetFactory.scale(5);
        img.setLayoutParams(params);
        img.setImageResource(icon);
        return img;
    }

    void putAndLoad(JSONObject o, JSONArray parent, String key, Object value) {
        try {
            o.put(key, value);
            save();
            loadObjectConfig(o, parent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void save() {
        try {

            Files.write(file.toPath(), allLayouts.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadScheme();
        updatePreview();
        NavigationUtil.updateAll(getActivity());
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
