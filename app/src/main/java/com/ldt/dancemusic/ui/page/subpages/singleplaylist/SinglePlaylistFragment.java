package com.ldt.dancemusic.ui.page.subpages.singleplaylist;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.ldt.dancemusic.contract.AbsMediaAdapter;
import com.ldt.dancemusic.helper.EventListener;
import com.ldt.dancemusic.helper.Reliable;
import com.ldt.dancemusic.helper.ReliableEvent;
import com.ldt.dancemusic.helper.menu.MenuHelper;

import com.ldt.dancemusic.loader.medialoader.SongLoader;
import com.ldt.dancemusic.model.Song;
import com.ldt.dancemusic.ui.base.MPViewModel;
import com.ldt.dancemusic.ui.page.MusicServiceNavigationFragment;
import com.ldt.dancemusic.ui.page.librarypage.song.SongChildAdapter;
import com.ldt.dancemusic.ui.bottomsheet.OptionBottomSheet;
import com.ldt.dancemusic.ui.widget.fragmentnavigationcontroller.PresentStyle;
import com.ldt.dancemusic.R;

import com.ldt.dancemusic.model.Playlist;
import com.ldt.dancemusic.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class SinglePlaylistFragment extends MusicServiceNavigationFragment implements EventListener<SinglePlaylistViewModel.State> {
    private static final String TAG = "PlaylistPagerFragment";
    public static final String PLAYLIST = "playlist";

    @Override
    public int getPresentTransition() {
        return PresentStyle.ACCORDION_LEFT;
    }

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefresh;

    private final SongChildAdapter mAdapter = new SongChildAdapter(false, PreferenceUtil.LAYOUT_PLAYLIST);
    public SongChildAdapter getAdapter() {
        return mAdapter;
    }
    private final SinglePlaylistHeaderAdapter mHeaderAdapter = new SinglePlaylistHeaderAdapter();

    private SinglePlaylistViewModel mViewModel;

    void onClickMenu() {
        Playlist playlist = mHeaderAdapter.getData() != null ? mHeaderAdapter.getData().mPlaylist : null;
        if (playlist != null) {
            int[] optionIds = playlist.id < 0 ? MenuHelper.AUTO_GEN_PLAYLIST_OPTION : MenuHelper.USER_PLAYLIST_OPTION;
            OptionBottomSheet.newInstance(optionIds, playlist).show(getChildFragmentManager(), "playlist_option_menu");
        }
    }



    @Override
    public void onServiceConnected() {
        refreshData();
    }

    @Override
    public void onServiceDisconnected() {

    }

    @Override
    public void onQueueChanged() {
        refreshData();
    }

    @Override
    public void onPlayingMetaChanged() {
        mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PLAY_STATE_CHANGED);
    }

    @Override
    public void onPaletteChanged() {
        //setTheme();
        mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PALETTE_CHANGED);
        super.onPaletteChanged();
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
        refreshData();
    }

    Bitmap mPreviewBitmap;

    public static SinglePlaylistFragment newInstance(Playlist playlist, @Nullable Bitmap previewBitmap) {
        SinglePlaylistFragment fragment = new SinglePlaylistFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(PLAYLIST, playlist);
        fragment.setArguments(bundle);
        fragment.mPreviewBitmap = previewBitmap;
        return fragment;
    }


    private int[] getRelativePosition(View v) {
        int[] locationInScreen = new int[2]; // view's position in scrren
        int[] parentLocationInScreen = new int[2]; // parent view's position in screen
        v.getLocationOnScreen(locationInScreen);
        View parentView = (View) v.getParent();
        parentView.getLocationOnScreen(parentLocationInScreen);
        float relativeX = locationInScreen[0] - parentLocationInScreen[0];
        float relativeY = locationInScreen[1] - parentLocationInScreen[1];
        return new int[]{(int) relativeX, (int) relativeY};
    }

    @Override
    public void onDestroyView() {
        mAdapter.destroy();

        if (mUnbinder != null) {
            mUnbinder.unbind();
            mUnbinder = null;
        }
        super.onDestroyView();
    }

    @Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.screen_single_playlist, container, false);
    }

    private ConcatAdapter mConcatAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter.init(requireContext());
        mConcatAdapter = new ConcatAdapter(mHeaderAdapter, mAdapter);
        mHeaderAdapter.setEventListener(this);

        mViewModel = ViewModelProviders.of(this).get(SinglePlaylistViewModel.class);
        ReliableEvent<SinglePlaylistViewModel.State> event = mViewModel.getStateLiveData().getValue();
        if (event == null || event.getReliable().getData() == null) {
            SinglePlaylistViewModel.State state = new SinglePlaylistViewModel.State();
            Bundle bundle = getArguments();
            state.mPlaylist = bundle == null ? null : bundle.getParcelable(PLAYLIST);
            state.mCoverImage = mPreviewBitmap;
            mViewModel.getStateLiveData().setValue(new ReliableEvent<>(Reliable.success(state), MPViewModel.ACTION_SET_PARAMS));
        }
        mViewModel.getStateLiveData().observe(this, _event -> {
            SinglePlaylistViewModel.State state = _event == null ? null : _event.getReliable().getData();
            mHeaderAdapter.setData(state);
            if(state != null) {
                final List<Song> allSongs = SongLoader.getAllSongs(getContext());
                List<Song> songs = state.songs.stream().map(song -> {
                    for (Song s : allSongs) if (song.data.equals(s.data)) return s;
                    return null;
                }).filter(Objects::nonNull).collect(Collectors.toList());
                mAdapter.setData(songs);
            } else mAdapter.setData(null);

        });
    }

    private Unbinder mUnbinder;
    @BindView(R.id.back)
    View mBackButton;

    @OnClick(R.id.back)
    void back() {
        getNavigationController().dismissFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);
        ViewCompat.setOnApplyWindowInsetsListener(view, new OnApplyWindowInsetsListener() {
            @Override
            public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
                if (mBackButton != null) {
                    int _6dp = (int) (mBackButton.getResources().getDimension(R.dimen.oneDP) * 6f);
                    ((ViewGroup.MarginLayoutParams) mBackButton.getLayoutParams()).setMargins(insets.getSystemWindowInsetLeft() + _6dp, insets.getSystemWindowInsetTop() + _6dp, _6dp, _6dp);
                }
                if (mRecyclerView != null) {
                    mRecyclerView.setPadding(insets.getSystemWindowInsetLeft(),
                            insets.getSystemWindowInsetTop(),
                            insets.getSystemWindowInsetRight(),
                            (int) (insets.getSystemWindowInsetBottom() + v.getResources().getDimension(R.dimen.bottom_back_stack_spacing)));
                }
                return ViewCompat.onApplyWindowInsets(v, insets);
            }
        });
        mRecyclerView.setAdapter(mConcatAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        mSwipeRefresh.setEnabled(false);
        mSwipeRefresh.setColorSchemeResources(R.color.flatOrange);
        mSwipeRefresh.setOnRefreshListener(this::refreshData);

        this.refreshData();
    }

    public void refreshData() {
        mViewModel.refreshData();
    }

    public static void animateAndChangeImageView(Context c, final ImageView v, final Bitmap new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, android.R.anim.fade_out);
        final Animation anim_in = AnimationUtils.loadAnimation(c, android.R.anim.fade_in);
        anim_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setImageBitmap(new_image);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }

    @Override
    public boolean handleEvent(ReliableEvent<SinglePlaylistViewModel.State> event, SinglePlaylistViewModel.State data) {
        String action = event.getAction();
        if (action == null) {
            return false;
        }
        switch (action) {
            case SinglePlaylistHeaderAdapter.ACTION_CLICK_MENU:
                onClickMenu();
                break;
            case SinglePlaylistHeaderAdapter.ACTION_CLICK_PLAY_ALL:
                mAdapter.playAll(0, true);
                break;
            case SinglePlaylistHeaderAdapter.ACTION_CLICK_SHUFFLE:
                mAdapter.shuffle();
                break;
        }
        return true;
    }
}
