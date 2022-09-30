package com.ldt.dancemusic.util;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import android.util.Log;
import android.widget.Toast;

import com.ldt.dancemusic.App;
import com.ldt.dancemusic.R;
import com.ldt.dancemusic.loader.medialoader.LastAddedLoader;
import com.ldt.dancemusic.loader.medialoader.PlaylistSongLoader;
import com.ldt.dancemusic.loader.medialoader.SongLoader;
import com.ldt.dancemusic.loader.medialoader.TopAndRecentlyPlayedTracksLoader;
import com.ldt.dancemusic.model.Playlist;
import com.ldt.dancemusic.model.Song;
import com.ldt.dancemusic.service.MusicPlayerRemote;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class MusicUtil {
    private static final String TAG = "MusicUtil";

    public static Uri getMediaStoreAlbumCoverUri(long albumId) {
        final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");

        Uri uri = ContentUris.withAppendedId(sArtworkUri, albumId);
        Log.d(TAG, "getMediaStoreAlbumCoverUri: "+uri.toString()+", "+uri.getPath());
        return uri;
    }

    public static Uri getSongFileUri(int songId) {
        return ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId);
    }

    @NonNull
    public static Intent createShareSongFileIntent(@NonNull final Song song, Context context) {
        try {
            return new Intent()
                    .setAction(Intent.ACTION_SEND)
                    .putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName(), new File(song.data)))
                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    .setType("audio/*");
        } catch (IllegalArgumentException e) {
            // TODO the data is most likely not like /storage/emulated/0/... but something like /storage/28C7-75B0/...
            e.printStackTrace();
            Toast.makeText(context, "Could not share this file, I'm aware of the issue.", Toast.LENGTH_SHORT).show();
            return new Intent();
        } catch (Exception e) {
            e.printStackTrace();
            return new Intent();
        }
    }

    @NonNull
    public static String getSongInfoString(@NonNull final Song song) {
        return MusicUtil.buildInfoString(
            song.getArtist(),
            song.getAlbum()
        );
    }

    /**
     * Build a concatenated string from the provided arguments
     * The intended purpose is to show extra annotations
     * to a music library item.
     * Ex: for a given album --> buildInfoString(album.artist, album.songCount)
     */
    public static String buildInfoString(@NonNull final String string1, @NonNull final String string2)
    {
        // Skip empty strings
        if (string1.isEmpty()) {return string2;}
        if (string2.isEmpty()) {return string1;}

        final String separator = "  •  ";

        final StringBuilder builder = new StringBuilder();
        builder.append(string1);
        builder.append(separator);
        builder.append(string2);

        return builder.toString();
    }

    public static void deleteTracks(@NonNull final Context context, @NonNull final List<Song> songs) {
        final String[] projection = new String[]{
                BaseColumns._ID, MediaStore.MediaColumns.DATA
        };
        final StringBuilder selection = new StringBuilder();
        selection.append(BaseColumns._ID + " IN (");
        for (int i = 0; i < songs.size(); i++) {
            selection.append(songs.get(i).id);
            if (i < songs.size() - 1) {
                selection.append(",");
            }
        }
        selection.append(")");

        try {
            final Cursor cursor = context.getContentResolver().query(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection.toString(),
                    null, null);
            if (cursor != null) {
                // Step 1: Remove selected tracks from the current playlist, as well
                // as from the album art cache
                cursor.moveToFirst();
                String path = App.getInstance().getPreferencesUtility().getRootFolder("");
                while (!cursor.isAfterLast()) {
                    final int id = cursor.getInt(0);
                    final Song song = SongLoader.getSong(context, id, path);
                    MusicPlayerRemote.removeFromQueue(song);
                    cursor.moveToNext();
                }

                // Step 2: Remove selected tracks from the database
                context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                        selection.toString(), null);

                // Step 3: Remove files from card
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    final String name = cursor.getString(1);
                    try { // File.delete can throw a security exception
                        final File f = new File(name);
                        if (!f.delete()) {
                            // I'm not sure if we'd ever get here (deletion would
                            // have to fail, but no exception thrown)
                            Log.e("MusicUtils", "Failed to delete file " + name);
                        }
                        cursor.moveToNext();
                    } catch (@NonNull final SecurityException e) {
                        e.printStackTrace();
                        cursor.moveToNext();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        Log.e("MusicUtils", "Failed to find file " + name);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                cursor.close();
            }
            context.getContentResolver().notifyChange(Uri.parse("content://media"), null);
            Toast.makeText(context, context.getString(R.string.deleted_x_songs, songs.size()), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static List<Song> getPlaylistSongList(@NonNull Context context, Playlist list) {
        Log.d(TAG, "getPlaylistWithListId: " + list.id);
        if (list.name.equals(context.getString(R.string.playlist_last_added)))
            return LastAddedLoader.getLastAddedSongs(context);
        else if (list.name.equals(context.getString(R.string.playlist_recently_played))) {
            return TopAndRecentlyPlayedTracksLoader.getRecentlyPlayedTracks(context);
        } else if (list.name.equals(context.getString(R.string.playlist_top_tracks))) {
            return TopAndRecentlyPlayedTracksLoader.getTopTracks(context);
        } else {
            List<Song> songlist = new ArrayList<>(PlaylistSongLoader.getPlaylistSongList(context, list.id));
            return songlist;
        }
    }
}
