package com.ldt.dancemusic.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class Dance implements Parcelable {
    public final ArrayList<Song> songs;
    public String title;

    public Dance(String title) {
        this.songs = new ArrayList<>();
        this.title = title;
    }



    public List<Song> getSongs() {
        return songs;
    }

    public void addSong(Song song) {
        songs.add(song);
    }

    public int getSongCount() {
        return getSongs().size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Dance that = (Dance) o;
        return songs != null ? songs.equals(that.songs) : that.songs == null;

    }

    @Override
    public int hashCode() {
        return songs != null ? songs.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Album{" +
                "songs=" + songs +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeTypedList(songs);
    }

    protected Dance(Parcel in) {
        this.title = in.readString();
        this.songs = in.createTypedArrayList(Song.CREATOR);
    }

    public static final Creator<Dance> CREATOR = new Creator<Dance>() {
        public Dance createFromParcel(Parcel source) {
            return new Dance(source);
        }

        public Dance[] newArray(int size) {
            return new Dance[size];
        }
    };
}
