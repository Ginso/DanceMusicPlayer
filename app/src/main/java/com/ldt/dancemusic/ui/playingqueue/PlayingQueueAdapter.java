package com.ldt.dancemusic.ui.playingqueue;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ldt.dancemusic.R;
import com.ldt.dancemusic.contract.AbsBindAbleHolder;
import com.ldt.dancemusic.contract.AbsSongAdapter;
import com.ldt.dancemusic.helper.menu.SongMenuHelper;
import com.ldt.dancemusic.ui.bottomsheet.OptionBottomSheet;
import com.ldt.dancemusic.util.PreferenceUtil;

import org.jetbrains.annotations.NotNull;

public class PlayingQueueAdapter extends AbsSongAdapter {
    private static final String TAG = "PlayingQueueAdapter";

    public PlayingQueueAdapter() {
        super(PreferenceUtil.LAYOUT_SONGS);
    }

    @Override
    protected void onMenuItemClick(int positionInData) {
        OptionBottomSheet
                .newInstance(SongMenuHelper.SONG_QUEUE_OPTION,getData().get(positionInData))
                .show(((AppCompatActivity)getContext()).getSupportFragmentManager(), "song_popup_menu");
    }

    @NotNull
    @Override
    public AbsBindAbleHolder onCreateViewHolder(@NotNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_song_big,viewGroup,false);
        return new AbsSongAdapter.SongHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AbsBindAbleHolder absBindAbleHolder, int i) {
        if(absBindAbleHolder instanceof AbsSongAdapter.SongHolder)
        absBindAbleHolder.bind(getData().get(getDataPosition(i)));
    }
}
