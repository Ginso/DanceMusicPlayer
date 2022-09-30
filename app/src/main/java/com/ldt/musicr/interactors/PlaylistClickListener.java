package com.ldt.musicr.interactors;

import android.graphics.Bitmap;

import com.ldt.musicr.model.Playlist;

public interface PlaylistClickListener {
    void onClickPlaylist(Playlist playlist, Bitmap bitmap);
}
