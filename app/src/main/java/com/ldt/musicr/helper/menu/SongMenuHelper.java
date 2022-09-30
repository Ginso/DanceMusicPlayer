package com.ldt.musicr.helper.menu;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.ldt.musicr.R;
import com.ldt.musicr.model.Song;
import com.ldt.musicr.service.MusicPlayerRemote;
import com.ldt.musicr.ui.dialog.AddToPlaylistDialog;
import com.ldt.musicr.ui.dialog.DeleteSongsDialog;
import com.ldt.musicr.ui.page.subpages.EditTagsFragment;
import com.ldt.musicr.util.MusicUtil;
import com.ldt.musicr.util.NavigationUtil;
import com.ldt.musicr.util.RingtoneManager;

/**
 * @author Karim Abou Zeid (kabouzeid)
 * modified by Le Dinh Trung (dtrung98)
 */
public class SongMenuHelper {
    @StringRes
    public static final int[] SONG_OPTION = new int[]{
            /*   R.string.play,*/
            R.string.play_next,
            R.string.add_to_queue,
            R.string.add_to_playlist,
            R.string.edit_tag,
            R.string.divider,
            R.string.share,
            R.string.set_as_ringtone,
            R.string.delete_from_device
    };

    @StringRes
    public static final int[] SONG_QUEUE_OPTION = new int[]{
            R.string.play_next,
            R.string.play_preview,
            R.string.remove_from_queue,
            R.string.add_to_playlist,
            R.string.edit_tag,
            R.string.divider,
            R.string.share,
            R.string.set_as_ringtone,
            R.string.delete_from_device
    };

    @StringRes
    public static final int[] NOW_PLAYING_OPTION = new int[]{
            R.string.repeat_it_again,
            R.string.add_to_playlist,
            R.string.edit_tag,
            R.string.divider,
            R.string.share,
            R.string.set_as_ringtone,
            R.string.delete_from_device
    };

    public static boolean handleMenuClick(@NonNull AppCompatActivity activity, @NonNull Song song, int string_res_option) {
        switch (string_res_option) {
            case R.string.set_as_ringtone:
                if (RingtoneManager.requiresDialog(activity)) {
                    RingtoneManager.showDialog(activity);
                } else {
                    RingtoneManager ringtoneManager = new RingtoneManager();
                    ringtoneManager.setRingtone(activity, song.id);
                }
                return true;
            case R.string.share:
                activity.startActivity(Intent.createChooser(MusicUtil.createShareSongFileIntent(song, activity), null));
                return true;
            case R.string.delete_from_device:
                DeleteSongsDialog.create(song).show(activity.getSupportFragmentManager(), "DELETE_SONGS");
                return true;
            case R.string.add_to_playlist:
                AddToPlaylistDialog.create(song).show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
                return true;
            case R.string.repeat_it_again:
            case R.string.play_next:
                MusicPlayerRemote.playNext(song);
                return true;
            case R.string.add_to_queue:
                MusicPlayerRemote.enqueue(song);
                return true;
            case R.string.edit_tag:
                NavigationUtil.getLibraryTab(activity).getNavigationController().presentFragment(EditTagsFragment.newInstance(song));
                return true;
        }
        return false;
    }


}
