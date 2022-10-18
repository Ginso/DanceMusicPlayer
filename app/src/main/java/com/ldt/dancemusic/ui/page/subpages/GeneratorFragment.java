package com.ldt.dancemusic.ui.page.subpages;

import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Pair;
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

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexboxLayout;
import com.ldt.dancemusic.R;
import com.ldt.dancemusic.loader.medialoader.SongLoader;
import com.ldt.dancemusic.model.Dance;
import com.ldt.dancemusic.model.Song;
import com.ldt.dancemusic.ui.dialog.CreatePlaylistDialog;
import com.ldt.dancemusic.ui.widget.fragmentnavigationcontroller.NavigationFragment;
import com.ldt.dancemusic.util.Constants;
import com.ldt.dancemusic.util.WidgetFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;
import butterknife.Unbinder;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.LinearLayout.VERTICAL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GeneratorFragment extends NavigationFragment {

    @BindView(R.id.status_bar)
    View mStatusBar;
    @BindView(R.id.weights)
    LinearLayout mWeights;
    @BindView(R.id.filter)
    FlexboxLayout mFilter;

    boolean balanced = true;
    boolean ordered = false;
    WidgetFactory mWidgetFactory;
    int songsPerDance = 5;
    JSONArray filter;
    Map<String, View> weightViews = new HashMap<>();
    List<Dance> dances = SongLoader.allDances;

    public static GeneratorFragment newInstance() {
        return new GeneratorFragment();
    }

    @Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.screen_generator, container, false);
    }

    private Unbinder mUnbinder;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);
        mWidgetFactory = new WidgetFactory(getContext(), 14);
        List<String> tagNames = SongLoader.getTagNames();
        Map<String, Song.Tag> allTags = SongLoader.getAllTags();
        filter = new JSONArray();
        for (String tagName : tagNames) {
            Song.Tag tag = allTags.get(tagName);
            try {
                JSONObject o = new JSONObject();
                o.put(Constants.FIELD_FILTER, true);
                o.put(Constants.FIELD_TEXT, tagName);
                o.put(Constants.FIELD_TAG, tagName);
                o.put(Constants.FIELD_LENGTH, 3);

                if (tag.type == Song.Tag.Type.STRING)
                    continue;
                filter.put(o);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        fillWeightsLayout();
        fillFilterLayout();
    }

    void fillWeightsLayout() {
        mWeights.removeAllViews();
        final int m = mWidgetFactory.scale(30);
        if (balanced) {
            mWeights.addView(mWidgetFactory.createEditText("Songs per dance", String.valueOf(songsPerDance), 2, InputType.TYPE_CLASS_NUMBER, 20, s -> songsPerDance = Integer.parseInt(s)));
            CheckBox checkBox = mWidgetFactory.createCheckBox("fixed order", ordered, b -> {
                ordered = b;
                fillWeightsLayout();
            });
            mWidgetFactory.modifyParams(checkBox, p -> p.bottomMargin = m);
            mWeights.addView(checkBox);

            for (Dance dance : dances) {
                LinearLayout line = mWidgetFactory.createLinearLayout(WRAP_CONTENT, WRAP_CONTENT, HORIZONTAL);
                checkBox = mWidgetFactory.createCheckBox(dance.title, true);
                weightViews.put(dance.title, checkBox);
                line.addView(checkBox);
                final int idx = mWeights.getChildCount() - 2;
                if (ordered && idx > 0) {
                    line.addView(mWidgetFactory.createButton("↑", v -> {
                        dances.add(idx - 1, dances.remove(idx));
                        fillWeightsLayout();
                    }));
                }
                mWeights.addView(line);
            }
        } else {
            for (Dance dance : dances) {
                LinearLayout line = mWidgetFactory.createEditText(dance.title + ": ", String.valueOf(songsPerDance), 2, InputType.TYPE_CLASS_NUMBER, null);
                weightViews.put(dance.title, line.getChildAt(1));
                mWeights.addView(line);
            }
        }
        mWeights.addView(mWidgetFactory.modifyParams(
                mWidgetFactory.createCheckBox("set number of songs for each dance individually", !balanced, b -> {
                    balanced = !b;
                    fillWeightsLayout();
                }),
                p -> p.topMargin = m));
    }

    void fillFilterLayout() {
        mFilter.removeAllViews();
        Map<String, Song.Tag> allTags = SongLoader.getAllTags();
        int margin = mWidgetFactory.scale(20);
        for (int i = 0; i < filter.length(); i++) {
            try {
                final JSONObject o = filter.getJSONObject(i);
                String tagName = o.getString(Constants.FIELD_TAG);
                Song.Tag tag = allTags.get(tagName);
                FlexboxLayout.LayoutParams p = new FlexboxLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                p.leftMargin = margin;
                p.setAlignSelf(AlignItems.CENTER);
                if (tag.type == Song.Tag.Type.RATING) {
                    LinearLayout line = new LinearLayout(getContext());
                    line.setOrientation(HORIZONTAL);
                    line.setLayoutParams(p);
                    line.addView(mWidgetFactory.createTextView(tagName, 19));
                    List<String> choices = new ArrayList<>();
                    choices.add("all");
                    choices.add("highest");
                    for (int n = 1; n <= tag.arg; n++) {
                        choices.add(String.format(" ≥ %d", n));
                    }
                    final int selected = o.optInt("value", 0);
                    line.addView(mWidgetFactory.createSpinner(choices, selected, true, v -> {
                        if (v != selected) {
                            try {
                                o.put("value", v);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }));
                    mFilter.addView(line);
                    continue;
                }
                View filterView = mWidgetFactory.createFilterView(o, (val, update) -> {
                    try {
                        if (val instanceof Integer[]) {
                            Integer[] arr = (Integer[]) val;
                            o.put("value", arr[0]);
                            o.put("value2", arr[1]);
                        } else if (val instanceof Double[]) {
                            Double[] arr = (Double[]) val;
                            o.put("value", arr[0]);
                            o.put("value2", arr[1]);
                        } else if (val instanceof Long[]) {
                            Long[] arr = (Long[]) val;
                            o.put("value", arr[0]);
                            o.put("value2", arr[1]);
                        } else {
                            o.put("value", val);
                        }
                        if (update) fillFilterLayout();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
                filterView.setLayoutParams(p);
                mFilter.addView(filterView);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick(R.id.create)
    void create() {
        try {
            Stream<Song> stream = SongLoader.getAllSongs(getContext()).stream();
            Map<String, Song.Tag> allTags = SongLoader.getAllTags();
            List<String> ratings = new ArrayList<>();
            for (int i = 0; i < filter.length(); i++) {
                JSONObject obj = filter.getJSONObject(i);
                String tagName = obj.getString(Constants.FIELD_TAG);
                Song.Tag tag = allTags.get(tagName);
                Object val1 = obj.opt("value");
                Object val2 = obj.opt("value2");
                if (tag.type == Song.Tag.Type.RATING && val1 instanceof Integer) {
                    int val = (int) val1;
                    switch (val) {
                        case 0:
                            continue;
                        case 1:
                            ratings.add(tag.name);
                            continue;
                        default:
                            stream = stream.filter(tag.getFilter(val - 1, val2));
                            continue;
                    }
                }
                if (val1 != null || val2 != null)
                    stream = stream.filter(tag.getFilter(val1, val2));
            }
            if (ratings.size() > 1) {
                Toast.makeText(getContext(), "Only one tag is allowed be set to 'highest'", Toast.LENGTH_LONG).show();
                return;
            }
            Map<String, List<Song>> filtered = stream.collect(Collectors.groupingBy(s -> s.getString(Song._DANCE)));
            for (Dance dance : dances) {
                int n;
                if (balanced) {
                    n = ((CheckBox) weightViews.get(dance.title)).isChecked() ? songsPerDance : 0;
                } else {
                    String s = ((EditText) weightViews.get(dance.title)).getText().toString();
                    n = s.isEmpty() ? 0 : Integer.valueOf(s);
                }
                if (n <= 0) continue;
                List<Song> songs = filtered.get(dance.title);
                if (songs.size() < n) {
                    Toast.makeText(getContext(), "insufficient songs for " + dance.title, Toast.LENGTH_LONG).show();
                    return;
                }
                if (ratings.isEmpty()) {
                    Collections.shuffle(songs);
                    filtered.put(dance.title, songs.subList(0, n));
                } else {
                    String name = ratings.get(0);
                    songs.sort((s1, s2) -> s2.getInt(name) - s1.getInt(name));
                    List<Song> songs2 = new ArrayList<>();
                    List<Song> songs3 = new ArrayList<>();
                    int r = songs.get(0).getInt(name);
                    for (Song song : songs) {
                        int r2 = song.getInt(name);
                        if (r2 < r) {
                            if (songs2.size() + songs3.size() >= n) {
                                Collections.shuffle(songs3);
                                while (songs2.size() < n) songs2.add(songs3.remove(0));
                                break;
                            }
                            songs2.addAll(songs3);
                            songs3.clear();
                            r = r2;
                        }
                        songs3.add(song);
                    }
                    if (songs2.size() < n) {
                        Collections.shuffle(songs3);
                        while (songs2.size() < n) songs2.add(songs3.remove(0));
                    }
                    Collections.shuffle(songs2);
                    filtered.put(dance.title, songs2);
                }
            }
            ArrayList<Song> playlist = new ArrayList<>();
            if (balanced) {
                List<Dance> filteredDances = dances.stream()
                        .filter(d -> ((CheckBox) weightViews.get(d.title)).isChecked())
                        .collect(Collectors.toList());
                int numDances = filteredDances.size();
                if (ordered || numDances < 3) {
                    for (int i = 0; i < songsPerDance; i++)
                        for (Dance dance : filteredDances)
                            playlist.add(filtered.get(dance.title).get(i));


                } else {
                    int min = Math.min(3, numDances - 1);
                    int max = numDances + 3;
                    int[] last = new int[numDances];
                    for (int i = 0; i < numDances; i++) {
                        Dance dance = filteredDances.get(i);
                        playlist.add(filtered.get(dance.title).get(0));
                        last[i] = i;
                    }
                    int idx = numDances;
                    for(int j = 1; j < songsPerDance; j++) {
                        List<Integer> temp = new ArrayList<>();
                        for (int i = 0; i < numDances; i++) temp.add(i);
                        Collections.shuffle(temp);
                        while(!temp.isEmpty()) {
                            for (int n = 0; n < numDances; n++) {
                                if(idx-last[n] == max) {
                                    playlist.add(filtered.get(filteredDances.get(n).title).get(j));
                                    temp.remove((Integer)n);
                                    last[n]=idx;
                                    break;
                                }
                            }
                            if(idx < playlist.size()) idx++;
                            else {
                               while(true) {
                                   int n = temp.remove(0);
                                   if(idx-last[n]<min) temp.add(n);
                                   else {
                                       playlist.add(filtered.get(filteredDances.get(n).title).get(j));
                                       last[n]=idx++;
                                       break;
                                   }
                               }
                            }
                        }
                    }
                }
            } else {
                // todo
            }

            CreatePlaylistDialog.create(playlist).show(getActivity().getSupportFragmentManager(), "ADD_TO_PLAYLIST");

        } catch (Exception e) {

        }
    }

    @Override
    public void onDestroyView() {
        if (mUnbinder != null) {
            mUnbinder.unbind();
            mUnbinder = null;
        }
        super.onDestroyView();
    }

    @Override
    public void onSetStatusBarMargin(int value) {
        mStatusBar.getLayoutParams().height = value;
    }

    @OnClick(R.id.back_button)
    void back() {
        getNavigationController().dismissFragment();
    }

}
