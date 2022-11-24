package com.ldt.dancemusic.loader.medialoader;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;

import com.ldt.dancemusic.App;
import com.ldt.dancemusic.model.Dance;
import com.ldt.dancemusic.model.Song;
import com.ldt.dancemusic.provider.BlacklistStore;
import com.ldt.dancemusic.service.MusicPlayerRemote;
import com.ldt.dancemusic.util.PreferenceUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class SongLoader {
    private static final String TAG = "SongLoader";

    protected static final String BASE_SELECTION = AudioColumns.IS_MUSIC + "=1" + " AND " + AudioColumns.TITLE + " != ''";
    protected static final String[] BASE_PROJECTION = new String[]{
            BaseColumns._ID,// 0
            AudioColumns.TITLE,// 1
            AudioColumns.TRACK,// 2
            AudioColumns.YEAR,// 3
            AudioColumns.DURATION,// 4
            AudioColumns.DATA,// 5
            AudioColumns.DATE_MODIFIED,// 6
            AudioColumns.ALBUM_ID,// 7
            AudioColumns.ALBUM,// 8
            AudioColumns.ARTIST_ID,// 9
            AudioColumns.ARTIST,// 10
            AudioColumns.RELATIVE_PATH // 11
    };

    private static JSONObject songInfo = null;
    private static JSONObject json = null;

    public static File tagsFile;

    private static ArrayList<Song> allSongs = null;
    private static List<Dance> allDances = null;
    private static Map<String, Song.Tag> allTags = null;
    private static List<String> tagNames = null;
    private static List<String> customNames = null;
    private static List<String> fixedNames = null;

    @NonNull
    public static ArrayList<Song> getAllSongs(@NonNull Context context) {
        if(allSongs == null) {
            loadAllSongs(context);
        }
        return allSongs;
    }

    public static List<Dance> getAllDances(Context context) {
        if(allDances == null) loadAllSongs(context);
        return allDances;
    }

    public static Map<String, Song.Tag> getAllTags() {
        if (allTags == null) {
            getJSON();
        }
        return allTags;
    }

    public static List<String> getAllTagNames() {
        if (tagNames == null) {
            getJSON();
        }
        return tagNames;
    }

    public static List<String> getCustomTagNames() {
        if (customNames == null) {
            getJSON();
        }
        return customNames;
    }

    public static List<String> getFixedTagNames() {
        if (fixedNames == null) {
            getJSON();
        }
        return fixedNames;
    }

    public static Song.Tag getTag(int i) {
        return getAllTags().get(tagNames.get(i));
    }

    public static void createSongData() {
        json = new JSONObject();
        try {
            allTags = new HashMap<>();
            JSONArray tagArr = new JSONArray();
            addTag(new Song.Tag(Song._DURATION, Song.Tag.Type.DATETIME,6), tagArr, false);
            addTag(new Song.Tag(Song._DURATION_SUM, Song.Tag.Type.DATETIME,5), tagArr, false);
            addTag(new Song.Tag(Song._DATE, Song.Tag.Type.DATETIME,2), tagArr, false);
            addTag(new Song.Tag(Song._ALBUM, Song.Tag.Type.STRING), tagArr, false);
            addTag(new Song.Tag(Song._ARTIST, Song.Tag.Type.STRING), tagArr, false);
            addTag(new Song.Tag(Song._DANCE, Song.Tag.Type.STRING), tagArr, false);
            addTag(new Song.Tag(Song._TITLE, Song.Tag.Type.STRING), tagArr, false);
            addTag(new Song.Tag(Song._YEAR, Song.Tag.Type.INT), tagArr, false);
            addTag(new Song.Tag(Song._RATING, Song.Tag.Type.RATING, 5), tagArr, true);
            addTag(new Song.Tag(Song._TPM, Song.Tag.Type.FLOAT,1), tagArr, true);
            json.put("tags", tagArr);
            songInfo = new JSONObject();
            json.put("songs", songInfo);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private static void addTag(Song.Tag tag, JSONArray tagArr, boolean custom) {

        if(custom) {
            if(fixedNames.contains(tag.name))
                return;
            if(!customNames.contains(tag.name))
                customNames.add(tag.name);
        } else fixedNames.add(tag.name);
        allTags.put(tag.name, tag);
        tagNames.add(tag.name);
        if(tagArr != null) tagArr.put(tag.toJSON());
    }


    public static JSONObject getJSON() {
        if(json != null) return json;
        tagNames = new ArrayList<>();
        customNames = new ArrayList<>();
        fixedNames = new ArrayList<>();
        try {
            createSongData();
            String s = new String(Files.readAllBytes(tagsFile.toPath()));
            json = new JSONObject(s);
            songInfo = json.getJSONObject("songs");
            JSONArray arr = json.getJSONArray("tags");
            for(int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String name = o.getString("name");
                Song.Tag.Type type = Song.Tag.Type.fromInteger(o.getInt("type"));
                addTag(o.has("arg") ? new Song.Tag(name, type, o.getInt("arg")) : new Song.Tag(name, type), null, true);
            }
        } catch(Exception e) {
            e.printStackTrace();
            createSongData();
            saveTagFile(json);
        }
        return json;
    }


    public static void loadAllSongs(@NonNull Context context) {
        String folder = App.getInstance().getPreferencesUtility().getRootFolder("");
        Cursor cursor = makeSongCursor(
                context,
                "relative_path LIKE '" + folder + "%'",
                null,
                null
        );
        allSongs = getSongs(cursor);
        loadDances(context);

    }

    public static void loadTags(Context context) throws JSONException {
        JSONObject info = getSongInfo();
        for (Song song : getAllSongs(context)) {
            if (info.has(song.key)) {
                song.tags = info.getJSONObject(song.key);
            } else {
                info.put(song.key, song.tags);
            }
        }
        json.put("songs", info);

        saveTagFile(json);
        loadDances(context);
    }

    public static void loadDances(Context context) {
        Map<String, Dance> dances = new HashMap<>();
        for(Song song:getAllSongs(context)) {
            String dance = song.getString(Song._DANCE);
            if(dance.isEmpty())  dance = "<UNTAGGED>";
            String[] danceArr = dance.split(";");
            for(String d:danceArr) {
                if (!dances.containsKey(d)) dances.put(d, new Dance(d));
                dances.get(d).addSong(song);
            }
        }
        ArrayList<Dance> list = new ArrayList<>(dances.values());
        list.sort((d1, d2) -> d1.title.compareTo(d2.title));
        allDances = list;
    }


    public static void resetJSON() {
        json = null;
        getJSON();
    }



    public static void addTag(Song.Tag tag) {
        JSONObject json = getJSON();
        try {
            JSONArray taginfo = json.optJSONArray("tags");
            if(taginfo == null) taginfo = new JSONArray();
            addTag(tag,taginfo, true);
            json.put("tags", taginfo);
            saveTagFile(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void removeTag(Song.Tag tag, boolean removeData) {
        int index = tagNames.indexOf(tag.name);
        tagNames.remove(tag.name);
        JSONObject json = getJSON();
        try {
            JSONArray taginfo = json.optJSONArray("tags");
            if(taginfo == null) return;
            taginfo.remove(index);
            json.put("tags", taginfo);
            if(removeData) {
                Iterator<String> it = songInfo.keys();
                while (it.hasNext()) {
                    String key = it.next();
                    JSONObject song = songInfo.getJSONObject(key);
                    song.remove(tag.name);
                }
                json.put("songs", songInfo);
                for(Song song:allSongs) song.tags.remove(tag.name);
            }
            saveTagFile(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void save(Song song) {
        try {
            JSONObject songs = getSongInfo();
            songs.put(song.key, song.tags);
            json.put("songs", songs);
            saveTagFile(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveAllSongs() {
        try {
            JSONObject songs = getSongInfo();
            for(Song song:allSongs) songs.put(song.key, song.tags);
            json.put("songs", songs);
            saveTagFile(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveTagFile(JSONObject json) {
        try {
            FileWriter fw = new FileWriter(tagsFile);
            fw.write(json.toString(2));
            fw.close();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject getSongInfo() {
        if (songInfo == null) {
            getJSON();
        }
        return songInfo;
    }







    public static ArrayList<Song> getAllSongs(Context context, String sortOrder) {
        if(allSongs == null) {
            Cursor  cursor = makeSongCursor(context, null, null,sortOrder);
            allSongs = getSongs(cursor);
        }
        return allSongs;
    }

    @NonNull
    public static Song getSong(@NonNull final Context context, final int queryId, String path) {
        Cursor cursor = makeSongCursor(context, AudioColumns._ID + "=?", new String[]{String.valueOf(queryId)}, null);
        return getSong(cursor, path);
    }

    @NonNull
    public static ArrayList<Song> getSongs(@Nullable final Cursor cursor) {
        String folder = App.getInstance().getPreferencesUtility().getRootFolder("");
        ArrayList<Song> songs = new ArrayList<>();
        int i = 0;
        if (cursor != null && cursor.moveToFirst()) {
            do {
                System.out.println(i++);
                songs.add(getSongFromCursorImpl(cursor, folder));
            } while (cursor.moveToNext());
        }

        if (cursor != null)
            cursor.close();
        return songs;
    }

    @NonNull
    public static Set<String> getFolders(@NonNull final Context context) {
        Cursor cursor = makeSongCursor(
                context,
                null,
                null,
                null
        );
        Set<String> songs = new HashSet<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String path = cursor.getString(11);
                songs.add(path);
            } while (cursor.moveToNext());
        }

        if (cursor != null)
            cursor.close();
        return songs;
    }

    @NonNull
    public static Song getSong(@Nullable Cursor cursor, String path) {
        Song song;
        if (cursor != null && cursor.moveToFirst()) {
            song = getSongFromCursorImpl(cursor, path);
        } else {
            song = Song.EMPTY_SONG;
        }
        if (cursor != null) {
            cursor.close();
        }
        return song;
    }

    @NonNull
    private static Song getSongFromCursorImpl(@NonNull Cursor cursor, String path) {
        final int id = cursor.getInt(0);
        final String title = cursor.getString(1);
        final int trackNumber = cursor.getInt(2);
        final int year = cursor.getInt(3);
        final long duration = cursor.getLong(4);
        final String data = cursor.getString(5);
        final long dateModified = cursor.getLong(6)*1000;
        final int albumId = cursor.getInt(7);
        final String albumName = cursor.getString(8);
        final int artistId = cursor.getInt(9);
        final String artistName = cursor.getString(10);
        String relPath;
        String key = null;
        if(cursor.getColumnCount() > 11) {
            relPath = cursor.getString(11);
            int n = data.indexOf(path);
            key = data.substring(n + path.length());
        } else if(data.contains(path)) {
            int n = data.indexOf(path);
            relPath = data.substring(n);
            key = data.substring(n + path.length());
            n = relPath.lastIndexOf("/");
            relPath = relPath.substring(0,n+1);
        } else {
            relPath = data;
        }

        Song song = new Song(id, title, trackNumber, year, duration, data, dateModified, albumId, albumName, artistId, artistName, key.toLowerCase(), relPath);

        JSONObject json = getSongInfo();

        if (json.has(song.key)) {
            song.tags = json.optJSONObject(song.key);
        }
        return song;
    }

    @Nullable
    public static Cursor makeSongCursor(@NonNull final Context context, @Nullable final String selection, final String[] selectionValues, String root) {
        return makeSongCursor(context, selection, selectionValues, PreferenceUtil.getInstance().getSongSortOrder(), root);
    }

    @Nullable
    public static Cursor makeSongCursor(@NonNull final Context context, @Nullable String selection, String[] selectionValues, final String sortOrder, String root) {

        if (selection != null && !selection.trim().equals("")) {
            selection = addMinDurationFilter(BASE_SELECTION)  + " AND " + selection;
        } else {
            selection = addMinDurationFilter(BASE_SELECTION);
        }

        // Blacklist
        ArrayList<String> paths = BlacklistStore.getInstance(context).getPaths();
        if (!paths.isEmpty()) {
            selection = generateBlacklistSelection(selection, paths.size());
            selectionValues = addBlacklistSelectionValues(selectionValues, paths);

            Log.d(TAG, "makeSongCursor: selection ["+selection+"]");

            StringBuilder values = new StringBuilder();
            for (String value :
                    selectionValues) {
                values.append("[").append(value).append("], ");
            }
            Log.d(TAG, "makeSongCursor: values = "+ values);
        }

        if(root != null) {
            selection += String.format(" AND %s LIKE '%s%%'",  AudioColumns.RELATIVE_PATH, root);
        }

        try {
            return context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    BASE_PROJECTION, selection, selectionValues, sortOrder);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String addMinDurationFilter(String selection) {
        return selection + " AND "+ AudioColumns.DURATION+" > " + App.getInstance().getPreferencesUtility().getMinDuration();
    }

    private static String generateBlacklistSelection(String selection, int pathCount) {
        StringBuilder newSelection = new StringBuilder(selection != null && !selection.trim().equals("") ? selection + " AND " : "");
        newSelection.append(AudioColumns.DATA + " NOT LIKE ?");
        for (int i = 0; i < pathCount - 1; i++) {
            newSelection.append(" AND " + AudioColumns.DATA + " NOT LIKE ?");
        }
        return newSelection.toString();
    }

    private static String[] addBlacklistSelectionValues(String[] selectionValues, ArrayList<String> paths) {
        if (selectionValues == null) selectionValues = new String[0];
        String[] newSelectionValues = new String[selectionValues.length + paths.size()];
        System.arraycopy(selectionValues, 0, newSelectionValues, 0, selectionValues.length);
        for (int i = selectionValues.length; i < newSelectionValues.length; i++) {
            newSelectionValues[i] = paths.get(i - selectionValues.length) + "%";
        }
        return newSelectionValues;
    }
}
