package com.ldt.dancemusic.ui.page.subpages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ldt.dancemusic.R;
import com.ldt.dancemusic.loader.medialoader.SongLoader;
import com.ldt.dancemusic.model.Song;
import com.ldt.dancemusic.ui.widget.fragmentnavigationcontroller.NavigationFragment;
import com.ldt.dancemusic.util.NavigationUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ImportFragment extends NavigationFragment {

    @BindView(R.id.status_bar) View mStatusBar;
    @BindView(R.id.ownTags) RadioButton ownTagsBtn;
    @BindView(R.id.newTags) RadioButton newTagsBtn;
    @BindView(R.id.tagChooser) RadioGroup tagChooser;
    @BindView(R.id.conflictLabel) TextView conflictLabel;
    @BindView(R.id.conflicts) LinearLayout conflictLayout;
    @BindView(R.id.songChooser) RadioGroup songChooser;
    @BindView(R.id.root_name) TextView rootName;
    @BindView(R.id.explorer) LinearLayout explorer;

    JSONObject file;
    List<Song.Tag[]> conflicts;
    List<Song.Tag> newImportedTags;
    private Unbinder mUnbinder;

    public ImportFragment(JSONObject file) {
        super();
        this.file = file;
    }

    public static ImportFragment newInstance(JSONObject file) {return new ImportFragment(file);}

    @Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.screen_import,container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this,view);
//        Resources resources = getResources();
//        try {
//            List<Song.Tag> existingTags = SongLoader.getAllTags();
//            List<Song.Tag> newTags = SongLoader.getAllTags();
//            List<String> tags = existingTags.subList(6,existingTags.size()).stream().map(tag -> tag.name).collect(Collectors.toList());
//            ownTagsBtn.setText(ownTagsBtn.getText() + " (" + String.join(", ", tags) + ")");
//            JSONArray tagArr = file.getJSONArray("tags");
//            tags.clear();
//            conflicts = new ArrayList<>();
//            for(int i = 6; i < tagArr.length(); i++) {
//                Song.Tag tag = Song.Tag.parseJSON(tagArr.getJSONObject(i));
//                newTags.add(tag);
//                tags.add(tag.name);
//                boolean isNew = true;
//                for(Song.Tag tag2:existingTags) {
//                    if(tag2.name.equalsIgnoreCase(tag.name)) {
//                        isNew = false;
//                        if(tag2.type != tag.type || tag2.arg != tag.arg) {
//                            conflicts.add(new Song.Tag[]{tag2, tag});
//                        }
//                    }
//                }
//                if(isNew) newTags.add(tag);
//            }
//            newTagsBtn.setText(newTagsBtn.getText() + " (" + String.join(", ", tags) + ")");
//            if(conflicts.isEmpty()) {
//                conflictLabel.setVisibility(View.GONE);
//            }
//            conflictLayout.removeAllViews();
//            for (Song.Tag[] conflict:conflicts) {
//                RadioGroup group = new RadioGroup(getContext());
//                final LinearLayout names = new LinearLayout(getContext());
//                group.setOrientation(LinearLayout.HORIZONTAL);
//                TextView tv = new TextView(getContext());
//                RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                tv.setLayoutParams(params);
//                tv.setText(conflict[0].name);
//                group.addView(tv);
//
//                RadioButton radio = new RadioButton(getContext());
//                params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                radio.setLayoutParams(params);
//                radio.setText(conflict[0].type.getText(resources, conflict[0].arg));
//                radio.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        names.setVisibility(View.GONE);
//                    }
//                });
//                group.addView(radio);
//
//                radio = new RadioButton(getContext());
//                params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                radio.setLayoutParams(params);
//                radio.setText(conflict[1].type.getText(resources, conflict[1].arg));
//                radio.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        names.setVisibility(View.GONE);
//                    }
//                });
//                group.addView(radio);
//
//                radio = new RadioButton(getContext());
//                params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//                radio.setLayoutParams(params);
//                radio.setText("rename");
//                radio.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        names.setVisibility(View.VISIBLE);
//                    }
//                });
//                radio.setChecked(true);
//                group.addView(radio);
//
//                EditText editText = new EditText(getContext());
//                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
//                p.rightMargin = 10;
//                editText.setLayoutParams(p);
//                editText.setText((conflict[0].name + "1"));
//                names.addView(editText);
//
//                editText = new EditText(getContext());
//                p = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
//                p.leftMargin = 10;
//                editText.setLayoutParams(p);
//                editText.setText((conflict[0].name + "2"));
//                names.addView(editText);
//
//
//                conflictLayout.addView(group);
//                conflictLayout.addView(names);
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }

    @OnClick(R.id.allTags)
    public void onChooseTag(View v) {
        if(!conflicts.isEmpty()) {
            conflictLabel.setVisibility(View.VISIBLE);
            conflictLayout.setVisibility(View.VISIBLE);
        }
    }

    @OnClick({R.id.ownTags, R.id.newTags})
    public void onDeselectTag(View v) {
        conflictLabel.setVisibility(View.GONE);
        conflictLayout.setVisibility(View.GONE);
    }

    @OnClick(R.id.changeRoot)
    public void changeRoot(View v) {

    }

    @OnClick(R.id.importBtn)
    public void importSongs(View v) {
        try {
            JSONObject songInfo = new JSONObject();
            JSONArray newTags = file.getJSONArray("newTags");
            JSONArray oldTags = SongLoader.getJSON().getJSONArray("newTags");

            switch (tagChooser.getCheckedRadioButtonId()) {
                case R.id.ownTags:
                    songInfo.put("tags", newTags);
                    break;
                case R.id.newTags:
                    songInfo.put("tags", oldTags);
                    break;
                case R.id.allTags:
                    for(Song.Tag tag:newImportedTags)
                        oldTags.put(tag.toJSON());

                    for(int i = 0; i < conflicts.size(); i++) {
                        RadioGroup group = (RadioGroup) conflictLayout.getChildAt(2*i);
                        Song.Tag[] conflict = conflicts.get(i);
                        switch (getSelectedIdx(group)) {
                            case 0:
                               oldTags.put(i,conflict[0].toJSON());
                               break;
                            case 2:
                                LinearLayout names = (LinearLayout)conflictLayout.getChildAt(2*i+1);
                                EditText edit0 = (EditText) names.getChildAt(0);
                                EditText edit1 = (EditText) names.getChildAt(1);
                                conflict[0].name = edit0.getText().toString();
                                conflict[1].name = edit1.getText().toString();
                                oldTags.put(i, conflict[0].toJSON());
                                oldTags.put(conflict[1].toJSON());
                                break;
                        }
                    }
                    songInfo.put("tags", oldTags);
                    break;
            }

            JSONObject songs = file.getJSONObject("songs");
            JSONObject oldSongs = SongLoader.getJSON().getJSONObject("songs");
            JSONObject newSongs = new JSONObject();
            Iterator<String> it = songs.keys();
            while(it.hasNext()) {
                String key = it.next();
                JSONObject song = songs.getJSONObject(key);
                newSongs.put(key.toLowerCase(), song);
            }
            switch (songChooser.getCheckedRadioButtonId()) {
                case R.id.ownSongs:
                    songInfo.put("songs", oldSongs);
                    break;
                case R.id.newSongs:
                    songInfo.put("songs", newSongs);
                    break;
                case R.id.allSongsOverwrite:
                    deepMerge(newSongs, oldSongs);
                    songInfo.put("songs", oldSongs);
                    break;
                case R.id.allSongsKeep:
                    deepMerge(oldSongs, newSongs);
                    songInfo.put("songs", newSongs);
                    break;

            }
            songs = songInfo.getJSONObject("songs");
            JSONArray tags = songInfo.getJSONArray("tags");
            List<String> tagNames = new ArrayList<>();
            for(int i = 0; i < tags.length(); i++) tagNames.add(tags.getJSONObject(i).getString("name"));
            it = songs.keys();
            while(it.hasNext()) {
                String key = it.next();
                JSONObject song = songs.getJSONObject(key);
                Iterator<String> it2 = song.keys();
                while(it2.hasNext()) {
                    String t = it2.next();
                    if(!tagNames.contains(t)) song.remove(t);
                }
                songs.put(key, song);
            }
            songInfo.put("songs", songs);
            SongLoader.saveTagFile(songInfo);
            SongLoader.resetJSON();
            SongLoader.loadTags();
            NavigationUtil.getLibraryTab(getActivity()).refreshData();
            Toast.makeText(getContext(), "import finished", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void deepMerge(JSONObject o1, JSONObject o2) {
        try {
            Iterator<String> it = o1.keys();
            while (it.hasNext()) {
                String key = it.next();
                Object obj = o1.get(key);
                if (o2.has(key)) {
                    Object obj2 = o2.get(key);
                    if(obj instanceof JSONObject && obj2 instanceof JSONObject) {
                        deepMerge((JSONObject)obj, (JSONObject)obj2);
                    }
                }
                o2.put(key, obj);
            }
        } catch(Exception e) {

        }
    }



    private int getSelectedIdx(RadioGroup group) {
        int idx = 0;
        for(int i = 0; i < group.getChildCount(); i++) {
            View v = group.getChildAt(i);
            if(v instanceof RadioButton) {
                if(((RadioButton) v).isChecked()) return idx;
                idx++;
            }
        }
        return -1;
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
