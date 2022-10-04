package com.ldt.dancemusic.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.MediaStore;

import androidx.preference.PreferenceManager;
import androidx.annotation.NonNull;

import com.ldt.dancemusic.App;

import java.util.ArrayList;
import java.util.List;

public final class PreferenceUtil {
    private static final String SONG_CHILD_SORT_ORDER = "song_child_sort_order";

    public static final String SONG_SORT_ORDER = "song_sort_order";

    public static final String COLORED_NOTIFICATION = "colored_notification";
    public static final String CLASSIC_NOTIFICATION = "classic_notification";

    public static final String AUDIO_DUCKING = "audio_ducking";
    public static final String GAPLESS_PLAYBACK = "gapless_playback";

    public static final String LAST_ADDED_CUTOFF = "last_added_interval";
    public static final String RECENTLY_PLAYED_CUTOFF = "recently_played_interval";

    public static final String ALBUM_ART_ON_LOCKSCREEN = "album_art_on_lockscreen";
    public static final String BLURRED_ALBUM_ART = "blurred_album_art";

    public static final String IGNORE_MEDIA_STORE_ARTWORK = "ignore_media_store_artwork";

    public static final String INITIALIZED_BLACKLIST = "initialized_blacklist";

    private static final String USE_ARTIST_IMAGE_AS_BACKGROUND = "use_artist_image_as_bg";
    public static final String IN_APP_VOLUME = "in_app_volume";
    private static final String AUDIO_MIN_DURATION = "audio_min_duration";
    public static final String BALANCE_VALUE = "balance_value";

    public static final String SHOW_ON_LOCK = "show_on_lock";

    public static final String ROOT_FOLDER = "root_folder";
    public static final String SEARCH_TAGS = "search_tags";

    public static final String SHOW_HINTS = "show_hints";


    private static PreferenceUtil sInstance;

    public boolean isFirstTime() {
        return isFirstTime;
    }

    public void setFirstTime(boolean firstTime) {
        isFirstTime = firstTime;
    }

    private boolean isFirstTime = true;

    private final SharedPreferences mPreferences;

    private PreferenceUtil(@NonNull final Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static PreferenceUtil getInstance() {
        if (sInstance == null) {
            sInstance = new PreferenceUtil(App.getInstance().getApplicationContext());
        }
        return sInstance;
    }

    public void registerOnSharedPreferenceChangedListener(SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener) {
        mPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    public void unregisterOnSharedPreferenceChangedListener(SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener) {
        mPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }


    public final boolean coloredNotification() {
        return mPreferences.getBoolean(COLORED_NOTIFICATION, true);
    }

    public final boolean classicNotification() {
        return mPreferences.getBoolean(CLASSIC_NOTIFICATION, false);
    }

    public final boolean gaplessPlayback() {
        return mPreferences.getBoolean(GAPLESS_PLAYBACK, false);
    }

    public final boolean audioDucking() {
        return mPreferences.getBoolean(AUDIO_DUCKING, true);
    }

    public final boolean albumArtOnLockscreen() {
        return mPreferences.getBoolean(ALBUM_ART_ON_LOCKSCREEN, true);
    }

    public final boolean blurredAlbumArt() {
        return mPreferences.getBoolean(BLURRED_ALBUM_ART, false);
    }

    public final boolean ignoreMediaStoreArtwork() {
        return mPreferences.getBoolean(IGNORE_MEDIA_STORE_ARTWORK, false);
    }

    public final String getSongSortOrder() {
        return mPreferences.getString(SONG_SORT_ORDER, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
    }

    // The last added cutoff time is compared against the Android media store timestamps, which is seconds based.
    public long getLastAddedCutoffTimeSecs() {
        return getCutoffTimeMillis(LAST_ADDED_CUTOFF) / 1000;
    }

    // The recently played cutoff time is compared against the internal (private) database timestamps, which is milliseconds based.
    public long getRecentlyPlayedCutoffTimeMillis() {
        return getCutoffTimeMillis(RECENTLY_PLAYED_CUTOFF);
    }

    private long getCutoffTimeMillis(final String cutoff) {
        final CalendarUtil calendarUtil = new CalendarUtil();
        long interval;

        switch (mPreferences.getString(cutoff, "")) {
            case "today":
                interval = calendarUtil.getElapsedToday();
                break;

            case "this_week":
                interval = calendarUtil.getElapsedWeek();
                break;

             case "past_seven_days":
                interval = calendarUtil.getElapsedDays(7);
                break;

            case "past_three_months":
                interval = calendarUtil.getElapsedMonths(3);
                break;

            case "this_year":
                interval = calendarUtil.getElapsedYear();
                break;

            case "this_month":
            default:
                interval = calendarUtil.getElapsedMonth();
                break;
        }

        return (System.currentTimeMillis() - interval);
    }


    public void setInitializedBlacklist() {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(INITIALIZED_BLACKLIST, true);
        editor.apply();
    }

    public final boolean initializedBlacklist() {
        return mPreferences.getBoolean(INITIALIZED_BLACKLIST, false);
    }


    public final int getSongChildSortOrder() {
        return mPreferences.getInt(SONG_CHILD_SORT_ORDER,1);
    }

    public final boolean isUsingArtistImageAsBackground() {
        return mPreferences.getBoolean(USE_ARTIST_IMAGE_AS_BACKGROUND,false);
    }

    public final void setShowOnLock(boolean value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(SHOW_ON_LOCK,value);
        editor.apply();
    }

    public final boolean showOnLock() {
        return mPreferences.getBoolean(SHOW_ON_LOCK,false);
    }

    public final void setRootFolder(String value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(ROOT_FOLDER,value);
        editor.apply();
    }

    public final String getRootFolder(String def) {
        return mPreferences.getString(ROOT_FOLDER,def);
    }

    public final void setInAppVolume(float value) {
        if(value<0) value = 0;
        else if(value>1) value = 1;
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putFloat(IN_APP_VOLUME, value);
        editor.apply();
    }

    public final float getInAppVolume() {
        return mPreferences.getFloat(IN_APP_VOLUME,1);
    }

    public final int getMinDuration() {
        return mPreferences.getInt(AUDIO_MIN_DURATION,10000);
    }

    public SharedPreferences getSharePreferences() {
        return mPreferences;
    }

    public void notFirstTime() {
        setFirstTime(false);
    }

    public float getBalanceValue() {
        return mPreferences.getFloat(BALANCE_VALUE,0.5f);
    }

    public final void setBalanceValue(float value) {
        if(value<0) value = 0;
        else if(value>1) value = 1;
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putFloat(BALANCE_VALUE, value);
        editor.apply();
    }

    public final void setSearchTags(List<String> values) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(SEARCH_TAGS,String.join(";", values));
        editor.apply();
    }

    public final List<String> getSearchTags() {
        String s = mPreferences.getString(SEARCH_TAGS,"title;artist");
        List<String> tags = new ArrayList<>();
        if(!s.isEmpty()) {
            String[] split = s.split(";");
            for(String t:split) tags.add(t);
        }
        return tags;
    }

    public final boolean showHints() {
        return mPreferences.getBoolean(SHOW_HINTS, true);
    }

    public final void deactivateHints() {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(SHOW_HINTS, false);
        editor.apply();
    }

}
