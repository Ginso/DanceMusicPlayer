/*
 * Copyright (C) 2015 Naman Dwivedi
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 */

package com.ldt.dancemusic.util;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;

import android.view.Display;
import android.view.WindowManager;

import java.util.Objects;

public class Util {

    public static boolean equals(Object a, Object b) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return Objects.equals(a, b);
        } else {
            return (a == b) || (a != null && a.equals(b));
        }
    }


    public static boolean isLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }


    public static Uri getAlbumArtUri(long albumId) {
        return ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
    }


    public enum IdType {
        NA(0),
        Artist(1),
        Album(2),
        Playlist(3);

        public final int mId;

        IdType(final int id) {
            mId = id;
        }

        public static IdType getTypeById(int id) {
            for (IdType type : values()) {
                if (type.mId == id) {
                    return type;
                }
            }

            throw new IllegalArgumentException("Unrecognized id: " + id);
        }
    }


    public static Point getScreenSize(@NonNull Context c) {
        Display display = ((WindowManager) c.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static Integer parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {}
        return null;
    }

    public static Double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (Exception e) {}
        return null;
    }

    public static String toString(Object o) {
        if(o==null) return "";
        return String.valueOf(o);
    }

    public static Long castLong(Object o) {
        if(o instanceof Integer) return (long)(int)o;
        if(o instanceof Long) return (Long)o;
        return null;
    }

    public static Integer castInt(Object o) {
        if(o instanceof Integer) return (int)o;
        if(o instanceof Long) return (int)(long)o;
        return null;
    }

    public static <T> T orElse(T o, T def) {
        if(o == null) return def;
        return o;
    }
}
