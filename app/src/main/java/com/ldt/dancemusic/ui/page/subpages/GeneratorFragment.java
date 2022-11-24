package com.ldt.dancemusic.ui.page.subpages;

import android.os.Bundle;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.flexbox.AlignItems;
import com.google.android.flexbox.FlexboxLayout;
import com.ldt.dancemusic.R;
import com.ldt.dancemusic.loader.medialoader.SongLoader;
import com.ldt.dancemusic.model.Dance;
import com.ldt.dancemusic.model.Song;
import com.ldt.dancemusic.ui.dialog.CreatePlaylistDialog;
import com.ldt.dancemusic.ui.page.librarypage.song.SongChildAdapter;
import com.ldt.dancemusic.ui.widget.fragmentnavigationcontroller.NavigationFragment;
import com.ldt.dancemusic.util.Constants;
import com.ldt.dancemusic.util.PreferenceUtil;
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
import butterknife.Unbinder;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.widget.LinearLayout.HORIZONTAL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GeneratorFragment extends NavigationFragment {

    @BindView(R.id.status_bar)
    View mStatusBar;
    @BindView(R.id.weights)
    LinearLayout mWeights;
    @BindView(R.id.filter)
    FlexboxLayout mFilterFlex;
    @BindView(R.id.filter2)
    LinearLayout mFilterLinear;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.save)
    Button mSaveButton;
    @BindView(R.id.duration) TextView mDuration;

    boolean balanced = true;
    boolean ordered = false;
    WidgetFactory mWidgetFactory;
    int songsPerDance = 5;
    JSONArray filter;
    Map<String, View> weightViews = new HashMap<>();
    List<Dance> dances = SongLoader.getAllDances(getContext());
    ArrayList<Song> playlist;
    ViewGroup mFilter;

    public static GeneratorFragment newInstance() {
        return new GeneratorFragment();
    }
    private final SongChildAdapter mAdapter = new SongChildAdapter(false, PreferenceUtil.LAYOUT_PLAYLIST);
    public SongChildAdapter getAdapter() {
        return mAdapter;
    }

    @Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.screen_generator, container, false);
    }

    private Unbinder mUnbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter.init(requireContext());
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);
        ViewCompat.setOnApplyWindowInsetsListener(view, new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {

                if (mRecyclerView != null) {
                    mRecyclerView.setPadding(insets.getSystemWindowInsetLeft(),
                            insets.getSystemWindowInsetTop(),
                            insets.getSystemWindowInsetRight(),
                            (int) (insets.getSystemWindowInsetBottom() + v.getResources().getDimension(R.dimen.bottom_back_stack_spacing)));
                }
                return ViewCompat.onApplyWindowInsets(v, insets);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        mWidgetFactory = new WidgetFactory(getContext(), 14);
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        mFilter = (displayMetrics.widthPixels < mWidgetFactory.scale(550)) ? mFilterLinear : mFilterFlex;

        List<String> tagNames = SongLoader.getAllTagNames();
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
            mWeights.addView(mWidgetFactory.createEditText("Songs per dance", String.valueOf(songsPerDance), 2, InputType.TYPE_CLASS_NUMBER, 20,
                    s -> {
                        if (!s.isEmpty()) songsPerDance = Integer.parseInt(s);
            }));
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

                ViewGroup.LayoutParams p;
                if(mFilter instanceof FlexboxLayout) {
                    p = new FlexboxLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                    ((FlexboxLayout.LayoutParams)p).setAlignSelf(AlignItems.CENTER);
                    ((FlexboxLayout.LayoutParams)p).rightMargin = margin;
                } else {
                    p = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
                    ((LinearLayout.LayoutParams)p).rightMargin = margin;
                }
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
                                fillFilterLayout();
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
                if (n <= 0) {
                    filtered.remove(dance.title);
                    continue;
                }
                List<Song> songs = filtered.get(dance.title);
                if (songs == null || songs.size() < n) {
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
            playlist = new ArrayList<>();
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
                    for (int j = 1; j < songsPerDance; j++) {
                        List<Integer> temp = new ArrayList<>();
                        for (int i = 0; i < numDances; i++) temp.add(i);
                        Collections.shuffle(temp);
                        while (!temp.isEmpty()) {
                            for (int n = 0; n < numDances; n++) {
                                if (idx - last[n] == max) {
                                    playlist.add(filtered.get(filteredDances.get(n).title).get(j));
                                    temp.remove((Integer) n);
                                    last[n] = idx;
                                    break;
                                }
                            }
                            if (idx < playlist.size()) idx++;
                            else {
                                while (true) {
                                    int n = temp.remove(0);
                                    if (idx - last[n] < min) temp.add(n);
                                    else {
                                        playlist.add(filtered.get(filteredDances.get(n).title).get(j));
                                        last[n] = idx++;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                List<String> filteredDances = new ArrayList<>(filtered.keySet());
                int numDances = filteredDances.size();

                int[] wanted = new int[numDances];
                int[] have = new int[numDances];
                Arrays.fill(have, 0);
                double[] ratios = new double[numDances];
                int sum = 0;

                for (int i = 0; i < numDances; i++) {
                    int n = filtered.get(filteredDances.get(i)).size();
                    wanted[i] = n;
                    sum += n;
                }
                for (int i = 0; i < numDances; i++) ratios[i] = sum/(wanted[i]+1.0);
                boolean preventDoubles = true;
                while(true) {
                    int count = 0;
                    int last = -1;
                    while (playlist.size() < sum) {
                        double min = sum;
                        List<Integer> next = new ArrayList<>();
                        for (int i = 0; i < numDances; i++) {
                            if (preventDoubles && last == i) continue;
                            double x = (have[i] + 1) * ratios[i]; // next position if evenly spread
                            if (x < min) {
                                min = x;
                                next.clear();
                            }
                            if (x == min) next.add(i);
                        }
                        // use the one(s) whose next calculated position is closest
                        Collections.shuffle(next);
                        for (int i : next) {
                            if (have[i] == wanted[i]) continue;
                            playlist.add(filtered.get(filteredDances.get(i)).remove(0));
                            have[i]++;
                            last = i;
                        }
                        count++;
                        if (count == sum * 2) break; // prevent infty loops
                    }
                    if (playlist.size() == sum) break;
                    else {
                        if(preventDoubles) //try again without
                            preventDoubles = false;
                        else { // i think this can't happen, but just to be sure...
                            Toast.makeText(getContext(), "Something went wrong", Toast.LENGTH_LONG).show();
                            return;
                        }
                    }
                }
            }

            updatePlaylist();
            mSaveButton.setEnabled(true);
            long s = 0;
            for(Song song:playlist) s += song.getDuration();
            s /= 1000;
            long h = s/3600;
            s %= 3600;
            long min = s/60;
            s %= 60;
            mDuration.setText(String.format("Duration: %02d:%02d:%02d", h, min, s));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updatePlaylist() {
        mAdapter.setData(playlist);
    }

    @OnClick(R.id.save)
    void save() {
        CreatePlaylistDialog.create(playlist).show(getActivity().getSupportFragmentManager(), "ADD_TO_PLAYLIST");
    }

    @Override
    public void onDestroyView() {
        if (mUnbinder != null) {
            mUnbinder.unbind();
            mUnbinder = null;
        }
        mAdapter.destroy();
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
