package com.ldt.dancemusic.ui.page.subpages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.flexbox.FlexboxLayout;
import com.ldt.dancemusic.R;
import com.ldt.dancemusic.loader.medialoader.SongLoader;
import com.ldt.dancemusic.model.Song;
import com.ldt.dancemusic.ui.widget.fragmentnavigationcontroller.NavigationFragment;
import com.ldt.dancemusic.util.NavigationUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiConsumer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ParseTagsFragment extends NavigationFragment {

    @BindView(R.id.status_bar) View mStatusBar;
    @BindView(R.id.root) View mRoot;
    @BindView(R.id.flexLayout) FlexboxLayout flexbox;
    @BindView(R.id.editText) EditText edit;
    @BindView(R.id.previewLayout) LinearLayout previewLayout;

    Map<String, Song.Tag> tags = new HashMap<>();



    public static ParseTagsFragment newInstance() {
        return new ParseTagsFragment();
    }

    @Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {

        return inflater.inflate(R.layout.screen_parse_tags,container, false);
    }

    private Unbinder mUnbinder;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this,view);

        tags.put("*", new Song.Tag("", Song.Tag.Type.NONE));
        Map<String, Song.Tag> allTags = SongLoader.getAllTags();
        for(String tagName:SongLoader.getAllTagNames()) {
            Song.Tag tag = allTags.get(tagName);
            Button btn = new Button(getContext());
            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.setMargins(10,10,10,10);
            btn.setLayoutParams(params);
            btn.setText(tag.name);
            tags.put(tag.name, tag);
            btn.setOnClickListener(v -> {
                String textToInsert = "<" + tag.name + ">";
                int start = Math.max(edit.getSelectionStart(), 0);
                int end = Math.max(edit.getSelectionEnd(), 0);
                edit.getText().replace(Math.min(start, end), Math.max(start, end),
                        textToInsert, 0, textToInsert.length());
            });
            flexbox.addView(btn);
        }

    }

    @OnClick(R.id.back_button)
    void back() {
        getNavigationController().dismissFragment();
    }

    @Override
    public void onSetStatusBarMargin(int value) {
        mStatusBar.getLayoutParams().height = value;
    }

    @OnClick(R.id.preview)
    void preview() {
        previewLayout.removeAllViews();
        List<Song> songs = new ArrayList<>();
        Random random = new Random();
        ArrayList<Song> allSongs = SongLoader.getAllSongs(getContext());
        if(allSongs.size() <= 10 ) songs = allSongs;
        else {
            for (int i = 0; i < 10; i++) {
                while (true) {
                    int n = random.nextInt(allSongs.size());
                    Song song = allSongs.get(n);
                    if (!songs.contains(song)) {
                        songs.add(song);
                        break;
                    }
                }
            }
        }
        parse(songs, (song, json) -> {
            previewLayout.addView(createTextView(song.data));
            try {
                previewLayout.addView(createTextView(json.toString(2)));
            } catch (Exception e) {
                previewLayout.addView(createTextView("ERROR"));
            }
            previewLayout.addView(createTextView("-----------"));

        });

    }

    TextView createTextView(String text) {
        TextView textView = new TextView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textView.setLayoutParams(params);
        textView.setText(text);
        return textView;
    }

    @OnClick(R.id.go)
    void go() {
        parse(SongLoader.getAllSongs(getContext()), (song, json) -> {
            Iterator<String> it = json.keys();
            while(it.hasNext()) {
                String key = it.next();
                try {
                    song.tags.put(key, json.get(key));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        SongLoader.loadDances(getContext());
        SongLoader.saveAllSongs();
        Toast.makeText(getContext(), "finished", Toast.LENGTH_LONG).show();
        NavigationUtil.updateAll(getActivity());
    }

    void parse(List<Song> songs, BiConsumer<Song,JSONObject> consumer) {

        try {
            String p = edit.getText().toString();
            p = p.replace("*", "<*>");
            List<Object> patterns = new ArrayList<>();
            String[] path = p.split("/");
            for (String pattern : path) {
                while (true) {
                    int n = pattern.indexOf("<");
                    int m = pattern.indexOf(">");
                    if (n < 0) break;
                    if (m < n) toast("invalid pattern");
                    String tagName = pattern.substring(n + 1, m);
                    if (!tags.containsKey(tagName)) toast("unknown tag: " + tagName);
                    String prefix = pattern.substring(0, n);
                    patterns.add(prefix);
                    pattern = pattern.substring(m + 1);
                    patterns.add(tags.get(tagName));
                }
                patterns.add(pattern);
                patterns.add(null);
            }
            if (p.contains("><")) toast("need at least one character as delimiter between tags and/or *");
            for(Song song:songs) {
                String text = song.data.substring(0,song.data.lastIndexOf("."));
                consumer.accept(song, parseString(patterns, text.split("/"), path.length));
            }
        } catch(Exception e) {
        }
    }

    void toast(String msg) throws Exception{
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
        throw new Exception();
    }

    @Override
    public void onDestroyView() {
        if(mUnbinder!=null) {
            mUnbinder.unbind();
            mUnbinder = null;
        }
        super.onDestroyView();
    }


    private JSONObject parseString(List<Object> patterns, String[] filepath, int pathLength) throws IllegalArgumentException{
        JSONObject res = new JSONObject();
        int i = 0;
        int j = filepath.length - pathLength;
        String text = filepath[j++];
        while(i+1 < patterns.size()) {
            String prefix = (String)patterns.get(i++);
            Song.Tag tag = (Song.Tag)patterns.get(i++);
            if(tag == null) {
                if(j==filepath.length) break;
                text = filepath[j++];
                continue;
            }
            String suffix = (String)patterns.get(i);
            if(!text.startsWith(prefix)) return null;
            text = text.substring(prefix.length());
            String value = text;
            if(suffix.length()>0) {
                int m = text.indexOf(suffix);
                if (m < 0) return null;
                value = text.substring(0,m);
                text = text.substring(m);
            }
            value = value.trim();
            try {
                switch (tag.type) {
                    case INT:
                    case RATING:
                    case DATETIME:
                        res.put(tag.name, Integer.parseInt(value));
                        break;
                    case BOOL:
                        res.put(tag.name, Boolean.parseBoolean(value));
                        break;
                    case FLOAT:
                        res.put(tag.name, Double.parseDouble(value));
                        break;
                    case STRING:
                        res.put(tag.name, value);
                        break;
                }
            } catch (Exception e) {
                return null;
            }
        }
        return res;
    }

}
