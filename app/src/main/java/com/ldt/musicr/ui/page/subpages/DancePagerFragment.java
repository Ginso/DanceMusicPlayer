package com.ldt.musicr.ui.page.subpages;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.chrisbanes.photoview.PhotoView;
import com.ldt.musicr.R;
import com.ldt.musicr.contract.AbsMediaAdapter;
import com.ldt.musicr.model.Dance;
import com.ldt.musicr.model.Song;
import com.ldt.musicr.ui.page.MusicServiceNavigationFragment;
import com.ldt.musicr.ui.page.librarypage.song.SongChildAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTouch;
import butterknife.Unbinder;

public class DancePagerFragment extends MusicServiceNavigationFragment {
    private static final String TAG = "DancePagerFragment";
    private static final String DANCE = "dance";
    public static DancePagerFragment newInstance(Dance dance) {

        Bundle args = new Bundle();
        if(dance!=null)
        args.putParcelable(DANCE,dance);

        DancePagerFragment fragment = new DancePagerFragment();
        fragment.setArguments(args);

        return fragment;
    }
    @BindView(R.id.status_bar) View mStatusBar;

    @BindView(R.id.root) View mRoot;

    @Override
    public void onSetStatusBarMargin(int value) {
        mStatusBar.getLayoutParams().height = value;
        mStatusBar.requestLayout();
    }

    private Dance mDance;

    @BindView(R.id.title)
    TextView mArtistText;


    @BindView(R.id.big_image)
    PhotoView mBigImage;

    @BindView(R.id.group)
    Group mGroup;

    @BindView(R.id.description) TextView mWiki;

    private boolean mBlockPhotoView = true;


    @OnTouch(R.id.big_behind)
    boolean onTouchBigBehind(View view, MotionEvent event) {
        if(!mBlockPhotoView) {
            return false;
        } else {
            mRoot.onTouchEvent(event);
            return true;
        }
    }

    @BindView(R.id.fullscreen) ImageView mFullScreenButton;

    @OnClick(R.id.fullscreen)
    void fullScreen() {
        mBlockPhotoView = !mBlockPhotoView;
        if(mBlockPhotoView) {
            mGroup.setVisibility(View.VISIBLE);
            mBigImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mBigImage.setBackgroundResource(android.R.color.transparent);
            mFullScreenButton.setImageResource(R.drawable.fullscreen);
        }
        else {
            mGroup.setVisibility(View.GONE);
            mBigImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            mBigImage.setBackgroundResource(android.R.color.black);
            mFullScreenButton.setImageResource(R.drawable.minimize);
        }
    }

    @OnClick(R.id.preview_button)
    void previewAllSong() {
        mAdapter.previewAll(true);
    }

    @OnClick(R.id.back)
    void goBack() {
        getMainActivity().onBackPressed();
    }

    @OnClick(R.id.play)
    void shuffle() {
        mAdapter.shuffle();
    }

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    private final SongChildAdapter mAdapter = new SongChildAdapter();

    @Override
    public void onDestroyView() {
        mAdapter.destroy();

        if(mUnbinder!=null) {
            mUnbinder.unbind();
            mUnbinder = null;
        }
        super.onDestroyView();
    }

    @Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.screen_single_dance_top,container,false);
    }

    private Unbinder mUnbinder;
    private List<Song> songs;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter.init(requireContext());
        mAdapter.setName(TAG);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view,savedInstanceState);
        mUnbinder = ButterKnife.bind(this,view);

        Bundle bundle = getArguments();
        if(bundle!=null) {
            mDance = bundle.getParcelable(DANCE);
            songs = mDance.getSongs();
        }
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));

        refreshData();
    }



    public SongChildAdapter getAdapter() {
        return mAdapter;
    }

    private void updateSongs() {
        if(mDance ==null) return;
        mAdapter.setData(songs);
    }

    public void refreshData() {
        if(mDance ==null) return;
        mArtistText.setText(mDance.title);
        mWiki.setText(mDance.getSongCount() +" "+getResources().getString(R.string.songs));


        updateSongs();

    }

    @Override
    public void onServiceConnected() {
        refreshData();
    }


    @Override
    public void onPlayingMetaChanged() {
        Log.d(TAG, "onPlayingMetaChanged");
        mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PLAY_STATE_CHANGED);
    }

    @Override
    public void onPaletteChanged() {
        mAdapter.notifyOnMediaStateChanged(AbsMediaAdapter.PALETTE_CHANGED);
        super.onPaletteChanged();
    }

    @Override
    public void onPlayStateChanged() {
        Log.d(TAG, "onPlayStateChanged");
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



}
