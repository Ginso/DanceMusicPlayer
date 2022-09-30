package com.ldt.dancemusic.ui.page.subpages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.ldt.dancemusic.R;
import com.ldt.dancemusic.loader.medialoader.SongLoader;
import com.ldt.dancemusic.model.Song;
import com.ldt.dancemusic.ui.widget.fragmentnavigationcontroller.NavigationFragment;
import com.ldt.dancemusic.util.NavigationUtil;
import com.ldt.dancemusic.util.WidgetFactory;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class EditTagsFragment extends NavigationFragment {


    public static EditTagsFragment newInstance(Song song) {
        return new EditTagsFragment(song);
    }


    @BindView(R.id.status_bar) View mStatusBar;
    @BindView(R.id.title) TextView mTitle;
    @BindView(R.id.container) LinearLayout mContainer;

    final Song song;

    public EditTagsFragment(Song song) {
        super();
        this.song = song;
    }

    @Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {

        return inflater.inflate(R.layout.screen_edit_tags,container, false);
    }

    private Unbinder mUnbinder;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);
        mTitle.setText("Tags of " + song.key);
        WidgetFactory widgetFactory = new WidgetFactory(getContext());
        widgetFactory.defaultTextSize = 24;
        widgetFactory.loadTags(mContainer, song, s -> {
            SongLoader.save(s);
            NavigationUtil.updateSongs(getActivity());
        });
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
