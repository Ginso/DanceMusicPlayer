package com.ldt.dancemusic.ui.page.settingpage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.FragmentActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ldt.dancemusic.App;
import com.ldt.dancemusic.R;
import com.ldt.dancemusic.loader.medialoader.SongLoader;
import com.ldt.dancemusic.model.Song;
import com.ldt.dancemusic.ui.page.MusicServiceNavigationFragment;
import com.ldt.dancemusic.ui.page.librarypage.LibraryTabFragment;
import com.ldt.dancemusic.ui.page.subpages.FilterConfigurationFragment;
import com.ldt.dancemusic.ui.page.subpages.ParseTagsFragment;
import com.ldt.dancemusic.ui.page.subpages.SonglistConfigurationFragment;
import com.ldt.dancemusic.ui.page.subpages.TagsFragment;
import com.ldt.dancemusic.ui.widget.rangeseekbar.OnRangeChangedListener;
import com.ldt.dancemusic.ui.widget.rangeseekbar.RangeSeekBar;
import com.ldt.dancemusic.util.NavigationUtil;
import com.ldt.dancemusic.util.Tool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

public class SettingTabFragment extends MusicServiceNavigationFragment implements OnRangeChangedListener {

    @BindView(R.id.status_bar)
    View mStatusBar;

    @BindView(R.id.in_app_volume_seek_bar)
    RangeSeekBar mAppVolumeSeekBar;

    @BindView(R.id.left_right_balance_seek_bar)
    RangeSeekBar mBalanceSeekBar;

    @BindView(R.id.showOnLock)
    SwitchCompat mShowOnLock;

    @BindView(R.id.courseSelection)
    LinearLayout mCourseSelection;

    @BindView(R.id.explorer)
    LinearLayout mExplorer;

    @BindView(R.id.newCourseName)
    EditText mNewCourseName;

    @BindView(R.id.root_name)
    TextView mRootName;



    @OnCheckedChanged(R.id.showOnLock)
    void onChangedShowOnLock(boolean value) {
        App.getInstance().getPreferencesUtility().setShowOnLock(value);
        getMainActivity().setShowWhenLocked(value);
    }

    @Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.screen_setting_tab,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
        mAppVolumeSeekBar.setOnRangeChangedListener(this);
        mBalanceSeekBar.setOnRangeChangedListener(this);
        refreshData();
        onPaletteChanged();
        getMainActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

    }

    private float mCurrentInAppVolume = 1.0f;
    private float mCurrentBalanceValue = 0.5f;

    private void refreshInAppVolume() {
        mCurrentInAppVolume = App.getInstance().getPreferencesUtility().getInAppVolume();
        if(mCurrentInAppVolume>=0) {
            mAppVolumeSeekBar.setValue(100*mCurrentInAppVolume);
        } else {

        }
    }

    private void refreshBalanceValue() {
        mCurrentBalanceValue = App.getInstance().getPreferencesUtility().getBalanceValue();
        if(mCurrentBalanceValue<0) mCurrentBalanceValue =0;
        else if(mCurrentBalanceValue>1) mCurrentBalanceValue = 1;
        mBalanceSeekBar.setValue(100*mCurrentBalanceValue);
    }

    void refreshData() {
        refreshInAppVolume();
        refreshBalanceValue();
        mShowOnLock.setChecked(App.getInstance().getPreferencesUtility().showOnLock());

        JSONArray courseInfo = SongLoader.getCourseInfo();
        mCourseSelection.removeAllViews();
        int curr = App.getInstance().getPreferencesUtility().getCurrentCourse();
        addCourse("none", -1, curr == -1);
        for(int i = 0; i < courseInfo.length(); i++) {
            try {
                JSONObject course = courseInfo.getJSONObject(i);
                addCourse(course.getString("name"), i, curr == i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        String root = App.getInstance().getPreferencesUtility().getRootFolder(null);
        if(root == null) {
            mRootName.setText("none");
            mRootName.setTextColor(Color.RED);
        } else {
            mRootName.setText(new File(root).getName());
            mRootName.setTextColor(getResources().getColor(R.color.FlatWhite, null));
        }
    }

    private void addCourse(String name, int idx, boolean selected) {
        TextView tv = new TextView(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 100);
        tv.setLayoutParams(params);
        tv.setText(name);
        if (selected) tv.setTextColor(-16711681);
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                App.getInstance().getPreferencesUtility().setCurrentCourse(idx);
                refreshData();
            }
        });
        mCourseSelection.addView(tv);
    }


    @OnClick(R.id.addNewCourse)
    public void newCourse() {
        String name = mNewCourseName.getText().toString();
        JSONArray courseInfo = SongLoader.getCourseInfo();
        JSONObject o = new JSONObject();
        try {
            o.put("name", name);
            courseInfo.put(o);
            SongLoader.saveCourseInfo();
            refreshData();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onSetStatusBarMargin(int value) {
        mStatusBar.getLayoutParams().height = value;
    }

    @Override
    public void onPaletteChanged() {
        super.onPaletteChanged();

        int color = Tool.getBaseColor();
        int alpha_color = Color.argb(0x22,Color.red(color),Color.green(color),Color.blue(color));
        int[][] states = new int[][] {
                new int[] {-android.R.attr.state_checked},
                new int[] {android.R.attr.state_checked},
        };

        int[] thumbColors = new int[] {
                0xFF888888,
                color,
        };

        int[] trackColors = new int[] {
                0x22000000,
                alpha_color,
        };

        //  checkBox.setSupportButtonTintList(new ColorStateList(states, thumbColors));
        DrawableCompat.setTintList(DrawableCompat.wrap(mShowOnLock.getThumbDrawable()), new ColorStateList(states, thumbColors));
        DrawableCompat.setTintList(DrawableCompat.wrap(mShowOnLock.getTrackDrawable()), new ColorStateList(states, trackColors));


        mAppVolumeSeekBar.setProgressColor(color);
        mAppVolumeSeekBar.requestLayout();

        mBalanceSeekBar.setProgressColor(color);
        mBalanceSeekBar.requestLayout();



    }
    public void setCurrentInAppVolume(float volume) {
        float vol = volume;
        if(vol<0) vol = 0;
        else if(vol>1) vol = 1;
        mCurrentInAppVolume = vol;
        App.getInstance().getPreferencesUtility().setInAppVolume(vol);
    }

    private void setCurrentBalanceValue(float value) {
        float vol = value;
        if(vol<0) vol = 0;
        else if(vol>1) vol = 1;
        mCurrentBalanceValue = vol;
        App.getInstance().getPreferencesUtility().setBalanceValue(vol);
    }


    @Override
    public void onRangeChanged(RangeSeekBar view, float leftValue, float rightValue, boolean isFromUser) {
        if(isFromUser) {
            switch (view.getId()) {
                case R.id.in_app_volume_seek_bar:
                        setCurrentInAppVolume(leftValue / 100);
                    break;
                case R.id.left_right_balance_seek_bar:
                    setCurrentBalanceValue(leftValue/100);
                    break;
            }
        }
    }


    @Override
    public void onStartTrackingTouch(RangeSeekBar view, boolean isLeft) {

    }

    @Override
    public void onStopTrackingTouch(RangeSeekBar view, boolean isLeft) {

    }

    private LoadArtistAsyncTask mLoadArtist;




    @OnClick(R.id.changeRoot)
    public void changeRoot() {
        if (mLoadArtist != null) mLoadArtist.cancel(true);
        mLoadArtist = new LoadArtistAsyncTask(this);
        mLoadArtist.execute();

    }

    Folder currentFolder = null;
    public void setFolderResult(Folder folder) {
        mExplorer.setVisibility(View.VISIBLE);
        currentFolder = folder;
        mExplorer.removeViews(1, mExplorer.getChildCount()-1);
        if(folder.parent != null) mExplorer.addView(createFolderView(folder.parent, ".."));
        for(Folder f: folder.children) {
            mExplorer.addView(createFolderView(f, f.name));
        }
    }

    private TextView createFolderView(final Folder f, String text) {
        TextView textView = new TextView(this.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = 5;
        params.bottomMargin = 5;
        textView.setLayoutParams(params);
        textView.setTextSize(20);
        textView.setText(text);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFolderResult(f);
            }
        });
        return textView;
    }

    @OnClick(R.id.saveRoot)
    public void saveRoot() {
        App.getInstance().getPreferencesUtility().setRootFolder(currentFolder.toString());
        mRootName.setText(currentFolder.name);
        mRootName.setTextColor(getContext().getColor(R.color.FlatWhite));

        mExplorer.setVisibility(View.GONE);
        FragmentActivity activity = getActivity();
        if(activity != null) {
            LibraryTabFragment libraryTab = NavigationUtil.getLibraryTab(activity);
            if(libraryTab != null)
                libraryTab.refresh();
        }
    }

    private static class LoadArtistAsyncTask extends AsyncTask<Void, Void, Set<String>> {
        private WeakReference<SettingTabFragment> mFragment;

        public LoadArtistAsyncTask(SettingTabFragment fragment) {
            super();
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        protected Set<String> doInBackground(Void... voids) {
            Set<String> result = new HashSet<>();
            Context context = null;

            if (App.getInstance() != null)
                context = App.getInstance().getApplicationContext();

            if (context != null)
                result = SongLoader.getFolders(App.getInstance());
            else return null;

            return result;
        }

        public void cancel() {
            mCancelled = true;
            cancel(true);
            mFragment.clear();
        }

        private boolean mCancelled = false;

        @Override
        protected void onPostExecute(Set<String> asyncResult) {
            if (mCancelled) return;
            SettingTabFragment fragment = mFragment.get();
            if (fragment != null && !fragment.isDetached()) {
                Folder root = new Folder("");
                for(String s:asyncResult) root.addchild(s.split("/"));
                fragment.setFolderResult(root);
            }
        }


    }

    private static class Folder {
        public String name;
        public Folder parent;
        public List<Folder> children;

        public Folder(String name) {
            this.name = name;
            children = new ArrayList<>();
        }

        public void addChild(Folder f) {
            children.add(f);
            f.parent = this;
        }

        public void addchild(String[] path) {
            Folder f = this;
            for(String s:path) {
                Folder f2 = f.get(s);
                if(f2 == null) {
                    f2 = new Folder(s);
                    f.addChild(f2);
                }
                f=f2;
            }
        }

        public Folder get(String s) {
            for(Folder f:children)
                if(f.name.equalsIgnoreCase(s))
                    return f;
            return null;
        }

        public String toString() {
            if(parent == null) return "/";
            String s = parent.toString();
            if(s.equals("/")) s = "";
            return s + name + "/";
        }

    }



    final static int IMPORT = 1;
    final static int EXPORT = 2;
    @OnClick(R.id.import_tags)
    public void importLogs() {
        if(!checkRoot()) return;
        alert("Warning: this will overwrite all existing tags", () -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/json");
            startActivityForResult(intent, IMPORT);
        }, null);
    }

    @OnClick(R.id.export_tags)
    public void exportLogs() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, SongLoader.tagsFile.getName());

        startActivityForResult(intent, EXPORT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if(resultCode != Activity.RESULT_OK) return;
        if (requestCode == IMPORT) {
            if (resultData != null) {
                Uri uri = resultData.getData();
                try {
                    InputStream inputStream = getMainActivity().getContentResolver().openInputStream(uri);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    while(true) {
                        int i = inputStream.read();
                        if(i == -1) break;
                        byteArrayOutputStream.write(i);
                    }
                    String content = byteArrayOutputStream.toString();
                    JSONObject o = new JSONObject(content);
//                    getNavigationController().presentFragment(ImportFragment.newInstance(o));
                    JSONObject songs = o.getJSONObject("songs");
                    JSONObject songs2 = new JSONObject();
                    Iterator<String> it = songs.keys();
                    while(it.hasNext()) {
                        String key = it.next();
                        songs2.put(key.toLowerCase(), songs.getJSONObject(key));
                    }
                    o.put("songs", songs2);
                    JSONArray tags = o.getJSONArray("tags");
                    for(int i = 0; i < tags.length(); i++) {
                        JSONObject tag = tags.getJSONObject(i);
                        Song.Tag.parseJSON(tag);
                    }
                    SongLoader.saveTagFile(o);
                    SongLoader.resetJSON();
                    SongLoader.loadTags();
                    NavigationUtil.getLibraryTab(getActivity()).refreshData();
                    Toast.makeText(getContext(), "import finished", Toast.LENGTH_SHORT).show();
                } catch(Exception e) {
                    Toast.makeText(getContext(), "Could not import", Toast.LENGTH_SHORT).show();
                }
            }
        }
        if (requestCode == EXPORT) {
            if (resultData != null) {
                Uri uri = resultData.getData();
                try {
                    OutputStream outputStream = getMainActivity().getContentResolver().openOutputStream(uri);
                    outputStream.write(SongLoader.getJSON().toString(2).getBytes());
                    outputStream.close();
                } catch(Exception e) {
                    Toast.makeText(getContext(), "Could not export", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @OnClick(R.id.tags)
    void goToCustomTags() {
        getNavigationController().presentFragment(TagsFragment.newInstance());
    }

    @OnClick(R.id.parseTags)
    void goToParseTags() {
        if(!checkRoot()) return;
        getNavigationController().presentFragment(ParseTagsFragment.newInstance());
    }

    @OnClick(R.id.configureFilter)
    void goToFilterConfiguration() {
        getNavigationController().presentFragment(FilterConfigurationFragment.newInstance());
    }

    @OnClick(R.id.configureSonglist)
    void goToSonglistConfiguration() {
        getNavigationController().presentFragment(SonglistConfigurationFragment.newInstance());
    }

    @BindView(R.id.alertView) LinearLayout alertView;
    @BindView(R.id.alertTitle) TextView alertText;
    @BindView(R.id.alertBackground) View alertBackground;
    Runnable onOk;
    Runnable onCancel;

    void alert(String msg, Runnable onOk, Runnable onCancel) {
        this.onOk = onOk;
        this.onCancel = onCancel;
        alertText.setText(msg);
        alertView.setVisibility(View.VISIBLE);
        alertBackground.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.alertOk)
    public void onAlertOk() {
        if(onOk != null) onOk.run();
        alertView.setVisibility(View.GONE);
        alertBackground.setVisibility(View.GONE);
    }

    @OnClick(R.id.alertCancel)
    public void onAlertCancel() {
        if(onCancel != null) onCancel.run();
        alertView.setVisibility(View.GONE);
        alertBackground.setVisibility(View.GONE);
    }

    public boolean checkRoot() {
        String root = App.getInstance().getPreferencesUtility().getRootFolder(null);
        if(root == null) {
            Toast.makeText(getContext(),"Please specify your root folder first", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

}
