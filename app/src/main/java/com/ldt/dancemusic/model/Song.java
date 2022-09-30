package com.ldt.dancemusic.model;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;

import com.ldt.dancemusic.R;
import com.ldt.dancemusic.util.Constants;
import com.ldt.dancemusic.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.function.Predicate;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Song implements Parcelable {
    public static final Song EMPTY_SONG = new Song(-1, "", -1, -1, -1, "", -1, -1, "", -1, "", "", "");

    public static final String _DATE = "last_modified";
    public static final String _DURATION = "duration";

    public static final String _TITLE = "title";
    public static final String _YEAR = "year";
    public static final String _ALBUM = "album";
    public static final String _ARTIST = "artist";

    public static final String _DANCE = "dance";
    public static final String _RATING = "rating";
    public static final String _TPM = "tpm";


    public final int id;
    public final int trackNumber;
    public final long duration;
    public final String data;
    public final int albumId;
    public final int artistId;

    public final String key;
    public final String path;
    public JSONObject tags;
    private final String title;
    private final int year;
    private final long dateModified;
    private final String albumName;
    private final String artistName;

    public Song(int id, String title, int trackNumber, int year, long duration, String data, long dateModified, int albumId, String albumName, int artistId, String artistName, String key, String path) {
        this(id, title,trackNumber, year, duration, data, dateModified, albumId, albumName, artistId, artistName, key, path, new JSONObject());
    }

    public Song(int id, String title, int trackNumber, int year, long duration, String data, long dateModified, int albumId, String albumName, int artistId, String artistName, String key, String path, JSONObject tags) {
        this.id = id;
        this.title = title;
        this.trackNumber = trackNumber;
        this.year = year;
        this.duration = duration;
        this.data = data;
        this.dateModified = dateModified;
        this.albumId = albumId;
        this.albumName = albumName;
        this.artistId = artistId;
        this.artistName = artistName;
        this.key = key;
        this.path = path;
        this.tags = tags;
    }

    public String getTitle() { return tags.optString(_TITLE, title); }
    public int getYear() { return tags.optInt(_YEAR, year); }
    public Date getDateModified() {
        long l = tags.optLong(_DATE, dateModified);
        Date date = new Date(l);
        return date;
    }
    public String getAlbum() { return tags.optString(_ALBUM, albumName); }
    public String getArtist() { return tags.optString(_ARTIST, artistName); }

    public String getString(String key) {
        if(key.equals(_TITLE)) return getTitle();
        if(key.equals(_ALBUM)) return getAlbum();
        if(key.equals(_ARTIST)) return getArtist();
        return tags.optString(key, "");
    }
    public int getInt(String key) {
        if(key.equals(_YEAR)) return getYear();
        return tags.optInt(key, 0);
    }
    public Date getDate(String key) {
        if(key.equals(_DATE)) return getDateModified();
        if(key.equals(_DURATION)) return new Date(duration);
        return new Date(tags.optLong(key, 0));
    }
    public boolean getBool(String key) { return tags.optBoolean(key, false);}
    public double getDouble(String key) { return tags.optDouble(key, 0);}

    public String getString(Tag tag) {
        if(tag.type == Tag.Type.DATETIME) return getString(tag, tag.arg);
        return getString(tag,0);
    }
    public String getString(Tag tag, int type) {
        String name = tag.name;
        switch (tag.type) {
            case STRING: return getString(name);
            case INT: return String.valueOf(getInt(name));
            case FLOAT:
                double d = getDouble(name);
                String f = "%." + tag.arg + "f";
                return String.format(f,d);
            case BOOL: return String.valueOf(getBool(name));
            case DATETIME:
                SimpleDateFormat format = new SimpleDateFormat(Constants.dateFormats[type]);
                return format.format(getDate(name));
            case RATING:
                int rating = getInt(name);
                String s = "";
                switch (type) {
                    case 0:
                        return String.valueOf(rating);
                    case 1:
                        for(int i = 0; i < tag.arg; i++) s += i < rating ? "★" : "☆";
                        return s;
                    case 2:
                        for(int i = 0; i < rating; i++) s += "♫";
                        return s;
                }
        }
        return "";
    }

    public void set(String key, Object o) {
        try {
            tags.put(key, o);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Song song = (Song) o;

        if (id != song.id) return false;
        if (trackNumber != song.trackNumber) return false;
        if (year != song.year) return false;
        if (duration != song.duration) return false;
        if (dateModified != song.dateModified) return false;
        if (albumId != song.albumId) return false;
        if (artistId != song.artistId) return false;
        if (title != null ? !title.equals(song.title) : song.title != null) return false;
        if (data != null ? !data.equals(song.data) : song.data != null) return false;
        if (albumName != null ? !albumName.equals(song.albumName) : song.albumName != null)
            return false;
        return artistName != null ? artistName.equals(song.artistName) : song.artistName == null;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + trackNumber;
        result = 31 * result + year;
        result = 31 * result + (int) (duration ^ (duration >>> 32));
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (int) (dateModified ^ (dateModified >>> 32));
        result = 31 * result + albumId;
        result = 31 * result + (albumName != null ? albumName.hashCode() : 0);
        result = 31 * result + artistId;
        result = 31 * result + (artistName != null ? artistName.hashCode() : 0);
        result = 31 * result + (key != null ? key.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Song{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", trackNumber=" + trackNumber +
                ", year=" + year +
                ", duration=" + duration +
                ", data='" + data + '\'' +
                ", dateModified=" + dateModified +
                ", albumId=" + albumId +
                ", albumName='" + albumName + '\'' +
                ", artistId=" + artistId +
                ", artistName='" + artistName + '\'' +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.id);
        dest.writeString(this.title);
        dest.writeInt(this.trackNumber);
        dest.writeInt(this.year);
        dest.writeLong(this.duration);
        dest.writeString(this.data);
        dest.writeLong(this.dateModified);
        dest.writeInt(this.albumId);
        dest.writeString(this.albumName);
        dest.writeInt(this.artistId);
        dest.writeString(this.artistName);
        dest.writeString(this.key);
        dest.writeString(this.path);
        String val = this.tags.toString();
        dest.writeString(val);
    }

    protected Song(Parcel in) {
        this.id = in.readInt();
        this.title = in.readString();
        this.trackNumber = in.readInt();
        this.year = in.readInt();
        this.duration = in.readLong();
        this.data = in.readString();
        this.dateModified = in.readLong();
        this.albumId = in.readInt();
        this.albumName = in.readString();
        this.artistId = in.readInt();
        this.artistName = in.readString();
        this.key = in.readString();
        this.path = in.readString();
        JSONObject tags;
        try {
            String json = in.readString();
            tags = new JSONObject(json);
        } catch (JSONException e) {
            tags = new JSONObject();
        }
        this.tags = tags;
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        public Song createFromParcel(Parcel source) {
            return new Song(source);
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };

    public static class Tag {
        public enum Type {
            STRING,
            INT,
            FLOAT,
            RATING,
            BOOL,
            DATETIME,
            NONE;

            public static Type fromInteger(int x) {
                switch(x) {
                    case 0: return STRING;
                    case 1: return INT;
                    case 2: return FLOAT;
                    case 3: return RATING;
                    case 4: return BOOL;
                    case 5: return DATETIME;
                }
                return null;
            }

            public String getText(Resources resources, int arg) {

                switch (this) {
                    case STRING: return resources.getString(R.string.text);
                    case INT: return resources.getString(R.string.integer);
                    case FLOAT: return resources.getString(R.string.decimal) + String.format(" (%d)", arg);
                    case RATING: return resources.getString(R.string.rating) + String.format(" (0-%d)", arg);
                    case BOOL: return resources.getString(R.string.bool);
                    case DATETIME: return resources.getString(R.string.datetime);
                }
                return "";
            }

        }
        public String name;
        public Type type;
        public int arg;

        public Tag(String name, Type type, int arg) {
            this.name = name;
            this.type = type;
            this.arg = arg;
        }

        public Tag(String name, Type type) {
            this.name = name;
            this.type = type;
            this.arg = -1;
        }

        public Comparator<Song> getComparator(boolean asc) {
            final int p = asc ? 1 : -1;
            switch (type) {
                case STRING: return (s1,s2) -> p*s1.getString(name).compareTo(s2.getString(name));
                case FLOAT: return (s1,s2) -> p*((Double)s1.getDouble(name)).compareTo(s2.getDouble(name));
                case BOOL: return (s1,s2) -> p*((Boolean)s1.getBool(name)).compareTo(s2.getBool(name));
                case RATING:
                case INT: return (s1,s2) -> p*((Integer)s1.getInt(name)).compareTo(s2.getInt(name));
                default: return (s1,s2) -> p*s1.getDate(name).compareTo(s2.getDate(name));
            }
        }

        public Predicate<Song> getFilter(Object val1, Object val2) {
            switch (type) {
                case STRING:
                    String text = ((String)val1).toLowerCase();
                    return s -> s.getString(name).toLowerCase().contains(text);
                case FLOAT: return s -> {
                    double d = s.getDouble(name);
                    return (val1 == null || (double)val1 <= d) && (val2 == null || (double)val2 >= d);
                };
                case BOOL: return s1 -> {
                    boolean bool = s1.getBool(name);
                    if((int)val1 == 1) return bool;
                    if((int)val1 == 2) return !bool;
                    return true;
                };
                case RATING: return s -> val1 == null || s.getInt(name) >= (int) val1;
                case INT: return s -> {
                    int d = s.getInt(name);
                    return (val1 == null || (int)val1 <= d) && (val2 == null || (int)val2 >= d);
                };
                default:
                    final Date d1 = val1 == null ? null : new Date(Util.castLong(val1));
                    final Date d2 = val2 == null ? null : new Date(Util.castLong(val2));
                    return s-> {
                        Date d = s.getDate(name);
                        return (d1 == null || d1.equals(d) || d1.before(d)) && (d2 == null || d2.equals(d) || d2.after(d));
                    };
            }
        }

        public JSONObject toJSON() {
            JSONObject tagObject = new JSONObject();
            try {
                tagObject.put("name", name);
                tagObject.put("type", type.ordinal());
                if (arg > 0) tagObject.put("arg", arg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return tagObject;
        }

        public static Tag parseJSON(JSONObject o) throws JSONException {
            String name = o.getString("name");
            Type type = Type.fromInteger(o.getInt("type"));
            int arg = -1;
            if(o.has("arg")) arg = o.getInt("arg");
            return new Tag(name, type, arg);
        }
    }
}
