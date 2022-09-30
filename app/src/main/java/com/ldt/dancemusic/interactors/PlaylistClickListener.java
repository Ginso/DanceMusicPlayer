package com.ldt.dancemusic.interactors;

import android.graphics.Bitmap;

import com.ldt.dancemusic.model.Playlist;

public interface PlaylistClickListener {
    void onClickPlaylist(Playlist playlist, Bitmap bitmap);
}
