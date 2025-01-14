package com.ldt.dancemusic.loader.medialoader;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.PlaylistsColumns;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.ldt.dancemusic.R;
import com.ldt.dancemusic.model.Playlist;

import java.util.ArrayList;

public class PlaylistLoader {
    private static final String TAG = "PlaylistLoader";


    @NonNull
    public static ArrayList<Playlist> getAllPlaylists(@NonNull final Context context) {
        return getAllPlaylists(makePlaylistCursor(context, null, null));
    }

    public static ArrayList<Playlist> getAllPlaylistsWithAuto(@NonNull final  Context context) {
        ArrayList<Playlist> playlists = new ArrayList<>();
        playlists.add(new Playlist(-1,context.getResources().getString(R.string.playlist_last_added)));
        playlists.add(new Playlist(-2,context.getResources().getString(R.string.playlist_recently_played)));
        playlists.add(new Playlist(-3,context.getResources().getString(R.string.playlist_top_tracks)));
        playlists.addAll(getAllPlaylists(context));
        for (Playlist p :
                playlists) {
            Log.d(TAG, "getAllPlaylistsWithAuto: id = "+p.id+", name = "+p.name);
        }
        return playlists;
    }

    @NonNull
    public static ArrayList<Playlist> getAllPlaylists(@Nullable final Cursor cursor) {
        ArrayList<Playlist> playlists = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                playlists.add(getPlaylistFromCursorImpl(cursor));
            } while (cursor.moveToNext());
        }
        if (cursor != null)
            cursor.close();
        return playlists;
    }

    @NonNull
    private static Playlist getPlaylistFromCursorImpl(@NonNull final Cursor cursor) {
        final int id = cursor.getInt(0);
        final String name = cursor.getString(1);
        return new Playlist(id, name);
    }

    @Nullable
    public static Cursor makePlaylistCursor(@NonNull final Context context, final String selection, final String[] values) {
        try {
            return context.getContentResolver().query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                    new String[]{
                        /* 0 */
                            BaseColumns._ID,
                        /* 1 */
                            PlaylistsColumns.NAME
                    }, selection, values, MediaStore.Audio.Playlists.DEFAULT_SORT_ORDER);
        } catch (Exception e) {
            return null;
        }
    }
}