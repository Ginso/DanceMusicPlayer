package com.ldt.dancemusic.ui.page.librarypage.song;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ldt.dancemusic.R;
import com.ldt.dancemusic.contract.AbsMediaAdapter;
import com.ldt.dancemusic.loader.medialoader.SongLoader;
import com.ldt.dancemusic.model.Song;
import com.ldt.dancemusic.ui.page.MusicServiceFragment;
import com.ldt.dancemusic.util.PreferenceUtil;
import com.ldt.dancemusic.util.Tool;
import com.ldt.dancemusic.util.WidgetFactory;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class SongChildTab extends MusicServiceFragment implements PreviewRandomPlayAdapter.FirstItemCallBack {
    public static final String TAG = "SongChildTab";

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.searchTags)
    LinearLayout searchTagsLayout;

    @BindView(R.id.searchTagsContainer)
    ScrollView searchTagsContainer;

    List<String> searchTags;

//    @BindView(R.id.preview_shuffle_list)
//    RecyclerView mPreviewRecyclerView;




    private void initSortOrder() {
    }
//    @BindView(R.id.top_background) View mTopBackground;
//    @BindView(R.id.bottom_background) View mBottomBackground;
//    @BindView(R.id.random_header) View mRandomHeader;
//    @BindView(R.id.shuffle_button) View ShuffleButton;



    private final SongChildAdapter mAdapter = new SongChildAdapter(PreferenceUtil.LAYOUT_SONGS);
//    PreviewRandomPlayAdapter mPreviewAdapter;

    public SongChildAdapter getAdapter() {
        return mAdapter;
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.screen_songs_tab, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter.init(requireContext());
        mAdapter.setName(TAG);
        mAdapter.setCallBack(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        initSortOrder();
        ViewCompat.setOnApplyWindowInsetsListener(mRecyclerView, new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                v.setPadding(insets.getSystemWindowInsetLeft(),
                        0,
                        insets.getSystemWindowInsetRight(),
                        (int) (insets.getSystemWindowInsetBottom() + v.getResources().getDimension(R.dimen.bottom_back_stack_spacing)));
                return ViewCompat.onApplyWindowInsets(v, insets);
            }
        });
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mAdapter);
        searchTags = PreferenceUtil.getInstance().getSearchTags();
        refreshData();
    }

    @Override
    public void onDestroyView() {
        mAdapter.destroy();
        super.onDestroyView();
    }

    public void refreshData() {
        ArrayList<Song> allSongs = SongLoader.getAllSongs(getContext());
        mAdapter.setData(allSongs);
    }



    @Override
    public void onPlayingMetaChanged() {
        if (mAdapter != null)
            mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PLAY_STATE_CHANGED);
    }

    @Override
    public void onPaletteChanged() {
        if (mRecyclerView instanceof FastScrollRecyclerView) {
            FastScrollRecyclerView recyclerView = ((FastScrollRecyclerView) mRecyclerView);
            recyclerView.setPopupBgColor(Tool.getHeavyColor());
            recyclerView.setThumbColor(Tool.getHeavyColor());
        }
        mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PALETTE_CHANGED);
        super.onPaletteChanged();
    }

    @Override
    public void onPlayStateChanged() {
        if (mAdapter != null)
            mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PLAY_STATE_CHANGED);
    }

    @Override
    public void onMediaStoreChanged() {
        SongLoader.allSongs = null;
        refreshData();
    }

    @Override
    public void onFirstItemCreated(Song song) {

    }

    @OnTextChanged(R.id.edit)
    void search(CharSequence s) {
        final String search = s.toString().toLowerCase();
        List<Song> songs = SongLoader.getAllSongs(getContext());
        if(s.length() > 0) songs = songs.stream().filter(song -> {
            Map<String, Song.Tag> allTags = SongLoader.getAllTags();
            for(String tagName:searchTags) {
                Song.Tag tag = allTags.get(tagName);
                if(song.getString(tag).toLowerCase().contains(search)) return true;
            }
            return false;
        }).collect(Collectors.toList());
        mAdapter.setData(songs);
        mAdapter.sortHolder.updateSongs(false);
    }

    @OnClick(R.id.search_settings)
    void toggleSearchSettings() {
        if(searchTagsContainer.getVisibility() == View.GONE) {
            WidgetFactory widgetFactory = new WidgetFactory(getContext(), 20);
            searchTagsContainer.setVisibility(View.VISIBLE);
            searchTagsLayout.removeAllViews();
            searchTagsLayout.addView(widgetFactory.createTextView("Search in:"));
            List<String> allTags = SongLoader.getTagNames();

            final int margin = widgetFactory.scale(10);
            for(final String tag:allTags) {
                final TextView tv = widgetFactory.modifyParams(widgetFactory.createTextView(tag + (searchTags.contains(tag) ? " ✓" : "")), p -> p.topMargin = margin);
                tv.setOnClickListener(v -> {
                    if(searchTags.contains(tag)) {
                        searchTags.remove(tag);
                        tv.setText(tag);
                    } else {
                        searchTags.add(tag);
                        tv.setText(tag + " ✓");
                    }
                    PreferenceUtil.getInstance().setSearchTags(searchTags);
                });
                searchTagsLayout.addView(tv);
            }
        } else {
            searchTagsContainer.setVisibility(View.GONE);
        }
    }
}
