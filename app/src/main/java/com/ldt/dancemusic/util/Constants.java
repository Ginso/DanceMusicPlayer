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

import android.content.Context;
import android.graphics.Color;

import com.ldt.dancemusic.R;

public class Constants {


    public static final String FIELD_TYPE = "type";
    public static final String FIELD_TAG = "tag";
    public static final String FIELD_CHILDREN = "children";
    public static final String FIELD_WIDTH = "width";
    public static final String FIELD_HEIGHT = "height";
    public static final String FIELD_TEXTSIZE = "textsize";
    public static final String FIELD_FORMAT = "format";
    public static final String FIELD_BOLD = "bold";
    public static final String FIELD_ITALIC = "italic";
    public static final String FIELD_UNDERLINE = "underline";
    public static final String FIELD_COLOR = "color";
    public static final String FIELD_BACKGROUND = "background";
    public static final String FIELD_FILTER = "filter";
    public static final String FIELD_TEXT = "text";
    public static final String FIELD_LENGTH = "ems";
    public static final String FIELD_TYPE2 = "type2";

    public static final String[] dateFormats =  {
            "dd.MM.yyyy", //0
            "dd.MM.yy", //1
            "dd.MM.yyyy hh:mm", //2
            "dd.MM.yy hh:mm", //3
            "hh:mm", //4
            "hh:mm:ss", //5
            "mm:ss" //6
    };


    public enum RatingType {
        ROTATE,
        STARS,
        NOTES,
        NUMBERS;
    }

    public static final String[] colorList = {
            "White",
            "Black",
            "Red",
            "Blue",
            "Yellow",
            "Green",
            "Orange",
            "Gray"
    };
    public static int getColor(Context context, int i) {
        switch (i) {
            case 0: return context.getColor(R.color.FlatWhite);
            case 1: return Color.BLACK;
            case 2: return Color.RED;
            case 3: return Color.BLUE;
            case 4: return Color.YELLOW;
            case 5: return Color.GREEN;
            case 6: return Color.rgb(255,128,0);
            case 7: return Color.rgb(190,190,190);
            default: return 0;
        }
    }

    public static final String[] colorList2 = {
            "Transparent",
            "White",
            "Black",
            "Red",
            "Blue",
            "Yellow",
            "Green",
            "Orange",
            "gray"
    };
    public static int getColor2(int i) {
        switch (i) {
            case 0: return Color.TRANSPARENT;
            case 1: return Color.WHITE;
            case 2: return Color.BLACK;
            case 3: return Color.RED;
            case 4: return Color.BLUE;
            case 5: return Color.YELLOW;
            case 6: return Color.GREEN;
            case 7: return Color.rgb(255,128,0);
            case 8: return Color.parseColor("#66333333");
            default: return 0;
        }
    }
}
