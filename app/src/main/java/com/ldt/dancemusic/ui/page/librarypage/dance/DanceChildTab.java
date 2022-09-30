package com.ldt.dancemusic.ui.page.librarypage.dance;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ldt.dancemusic.R;
import com.ldt.dancemusic.contract.AbsMediaAdapter;
import com.ldt.dancemusic.loader.medialoader.SongLoader;
import com.ldt.dancemusic.model.Dance;
import com.ldt.dancemusic.ui.page.MusicServiceFragment;
import com.ldt.dancemusic.ui.page.librarypage.LibraryTabFragment;
import com.ldt.dancemusic.ui.page.subpages.DancePagerFragment;
import com.ldt.dancemusic.ui.widget.fragmentnavigationcontroller.NavigationFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class DanceChildTab extends MusicServiceFragment implements DanceAdapter.DanceClickListener {
    public static final String TAG = "ArtistChildTab";

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @Nullable
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    public final DanceAdapter mAdapter = new DanceAdapter();
    Unbinder mUnBinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.screen_tab_dance_list, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter.init(requireContext());
        mAdapter.setName(TAG);
        mAdapter.setDanceClickListener(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnBinder = ButterKnife.bind(this, view);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        mRecyclerView.setAdapter(mAdapter);
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

        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setOnRefreshListener(this::refresh);
        }
        refresh();

    }

    public void refresh() {
        Fragment fragment = getParentFragment();
        if(fragment instanceof LibraryTabFragment)
            ((LibraryTabFragment) fragment).refresh();
    }

    @Override
    public void onDestroyView() {


        mAdapter.destroy();
        if (mUnBinder != null)
            mUnBinder.unbind();

        super.onDestroyView();
    }



    public void refreshData() {
        mAdapter.setData(SongLoader.allDances);

    }

    @Override
    public void onDanceItemClick(Dance dance) {
        NavigationFragment sf = DancePagerFragment.newInstance(dance);
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof NavigationFragment)
            ((NavigationFragment) parentFragment).getNavigationController().presentFragment(sf);
    }

    @Override
    public void onServiceConnected() {

    }

    @Override
    public void onServiceDisconnected() {

    }

    @Override
    public void onQueueChanged() {

    }

    @Override
    public void onPlayingMetaChanged() {
        mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PLAY_STATE_CHANGED);
    }

    @Override
    public void onPlayStateChanged() {
        mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PLAY_STATE_CHANGED);
    }

    @Override
    public void onRepeatModeChanged() {

    }

    @Override
    public void onShuffleModeChanged() {

    }

    @Override
    public void onMediaStoreChanged() {
        refresh();
    }


}
