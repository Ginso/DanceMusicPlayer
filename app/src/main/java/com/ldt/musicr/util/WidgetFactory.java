package com.ldt.musicr.util;

import android.content.Context;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.ldt.musicr.R;
import com.ldt.musicr.loader.medialoader.SongLoader;
import com.ldt.musicr.model.Song;
import com.ldt.musicr.ui.page.subpages.FilterConfigurationFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.LinearLayout.VERTICAL;

public class WidgetFactory {
    Context context;
    float density;
    int flatWhite;
    EditText currentEditText = null;
    int currentPos = 0;
    public int defaultTextSize;
    final Map<String, Song.Tag> allTags = SongLoader.getAllTags();
    
    public WidgetFactory(Context context) {
        this.context = context;
        density = context.getResources().getDisplayMetrics().scaledDensity;
        flatWhite = context.getColor(R.color.FlatWhite);
        defaultTextSize = 18;
    }

    public WidgetFactory(Context context, int textSize) {
        this.context = context;
        density = context.getResources().getDisplayMetrics().scaledDensity;
        flatWhite = context.getColor(R.color.FlatWhite);
        defaultTextSize = textSize;
    }

    public int scale(int size) {return (int)(size*density);}

    public View createFilterView(JSONObject o) {
        return createFilterView(o, null);
    }
    public View createFilterView(JSONObject o, BiConsumer<Object, Boolean> onClick) {
        boolean oc = onClick != null;
        Object oVal = o.opt("value");
        final Long intVal = Util.castLong(oVal);
        final String strVal = oVal instanceof String ? (String)oVal : "";
        final Long intVal2 = Util.castLong(o.opt("value2"));
        try {
            int textsize = (int)(o.optInt(Constants.FIELD_TEXTSIZE,14)*density);
            String text = o.optString(Constants.FIELD_TEXT);
            TextView titleView = createTextView(text, textsize);
            if(o.getBoolean(Constants.FIELD_FILTER)) {
                Song.Tag tag = SongLoader.getAllTags().get(o.getString(Constants.FIELD_TAG));
                if(tag == null) return createTextView("ERROR", textsize);
                LinearLayout layout;
                int type = Math.abs(o.optInt(Constants.FIELD_TYPE,0));
                final EditText editText;
                final EditText editText2;
                SimpleDateFormat format;
                switch (tag.type) {
                    case RATING:
                        layout = createLinearLayout(WRAP_CONTENT, WRAP_CONTENT, HORIZONTAL);
                        layout.addView(titleView);

                        final Long val = Util.orElse(intVal, (long)(oc ? 0 : 2));
                        if(type == Constants.RatingType.ROTATE.ordinal()) {
                            layout.addView(createTextView(" ≥ " + Util.toString(val), textsize));
                            if(oc) layout.setOnClickListener(v -> onClick.accept((int)(val+1)%(tag.arg+1), true));
                        } else if(type == Constants.RatingType.STARS.ordinal()) {
                            if(oc) titleView.setOnClickListener(v -> onClick.accept(0, true));
                            for(int i = 0; i < tag.arg; i++) {
                                TextView tv = createTextView(i < val ? "★" : "☆", textsize);
                                final int n = i+1;
                                if(oc) tv.setOnClickListener(v -> onClick.accept(n, true));
                                tv.setTextColor(Color.WHITE);
                                layout.addView(tv);
                            }
                        } else if(type == Constants.RatingType.NOTES.ordinal()) {
                            if(oc) titleView.setOnClickListener(v -> onClick.accept(0, true));

                            for(int i = 0; i < tag.arg; i++) {
                                TextView tv = createTextView("♫", textsize);
                                tv.setTextColor(Color.WHITE);
                                if(i >= val)
                                    tv.setAlpha(0.5f);
                                final int n = i+1;
                                if(oc) tv.setOnClickListener(v -> onClick.accept(n, true));
                                layout.addView(tv);
                            }
                        } else if(type == Constants.RatingType.NUMBERS.ordinal()) {
                            for(int i = 0; i <= tag.arg; i++) {
                                TextView tv = createTextView(String.valueOf(i), textsize);
                                if(i > val)
                                    tv.setTextColor(Color.GRAY);
                                final int n = i;
                                if(oc) tv.setOnClickListener(v -> onClick.accept(n, true));
                                layout.addView(tv);
                            }
                        }
                        return layout;
                    case INT:
                        layout = createLinearLayout(WRAP_CONTENT, WRAP_CONTENT, HORIZONTAL);
                        layout.addView(titleView);
                        editText = createEditText(o.getInt(Constants.FIELD_LENGTH), InputType.TYPE_CLASS_NUMBER, textsize);
                        if(intVal != null) editText.setText(Util.toString(intVal));
                        layout.addView(editText);
                        layout.addView(createTextView("-", textsize));
                        editText2 = createEditText(o.getInt(Constants.FIELD_LENGTH), InputType.TYPE_CLASS_NUMBER, textsize);
                        if(intVal2 != null) editText2.setText(Util.toString(intVal2));
                        if(oc) {
                            createTextWatcher(editText, s -> onClick.accept(new Integer[]{Util.parseInt(s), Util.parseInt(editText2.getText().toString())}, false));
                            createTextWatcher(editText2, s -> onClick.accept(new Integer[]{Util.parseInt(editText.getText().toString()), Util.parseInt(s)}, false));
                        }
                        layout.addView(editText2);
                        return layout;
                    case FLOAT:
                        Double dVal = oVal instanceof Double ? (Double)oVal : null;
                        Double dVal2 = (Double)o.opt("value2");
                        layout = createLinearLayout(WRAP_CONTENT, WRAP_CONTENT, HORIZONTAL);
                        layout.addView(titleView);
                        editText = createEditText(o.getInt(Constants.FIELD_LENGTH), InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL, textsize);
                        if(dVal!= null) editText.setText(Util.toString(dVal));
                        layout.addView(editText);
                        layout.addView(createTextView("-", textsize));
                        editText2 = createEditText(o.getInt(Constants.FIELD_LENGTH), InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL, textsize);
                        if(dVal2!= null) editText2.setText(Util.toString(dVal2));
                        if(oc) {
                            createTextWatcher(editText, s -> onClick.accept(new Double[]{Util.parseDouble(s), Util.parseDouble(editText2.getText().toString())}, false));
                            createTextWatcher(editText2, s -> onClick.accept(new Double[]{Util.parseDouble(editText.getText().toString()), Util.parseDouble(s)}, false));
                        }
                        layout.addView(editText2);
                        return layout;
                    case STRING:
                        layout = createLinearLayout(WRAP_CONTENT, WRAP_CONTENT, HORIZONTAL);
                        layout.addView(titleView);
                        editText = createEditText(o.getInt(Constants.FIELD_LENGTH), InputType.TYPE_CLASS_TEXT, textsize);
                        editText.setText(strVal);
                        if(oc) createTextWatcher(editText, s -> onClick.accept(s, false));
                        layout.addView(editText);
                        return layout;
                    case DATETIME:
                        String formatString = Constants.dateFormats[tag.arg];
                        layout = createLinearLayout(WRAP_CONTENT, WRAP_CONTENT, HORIZONTAL);
                        layout.addView(titleView);
                        if(formatString.contains(" ")) {
                            String[] split = formatString.split(" ");
                            format = new SimpleDateFormat(formatString);
                            SimpleDateFormat format1 = new SimpleDateFormat(split[0]);
                            SimpleDateFormat format2 = new SimpleDateFormat(split[1]);
                            editText = createEditText(split[0].length(), InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE, textsize);
                            editText2 = createEditText(split[1].length(), InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME, textsize);
                            EditText editText3 = createEditText(split[0].length(), InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE, textsize);
                            EditText editText4 = createEditText(split[1].length(), InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME, textsize);
                            editText.setHint(split[0]);
                            editText2.setHint(split[1]);
                            editText3.setHint(split[0]);
                            editText4.setHint(split[1]);
                            if(intVal != null) {
                                Date d = new Date(intVal);
                                editText.setText(format1.format(d));
                                editText2.setText(format2.format(d));
                            }
                            if(intVal2 != null) {
                                Date d = new Date(intVal2);
                                editText3.setText(format1.format(d));
                                editText4.setText(format2.format(d));
                            }
                            layout.addView(editText);
                            layout.addView(editText2);
                            layout.addView(createTextView("-", textsize));
                            layout.addView(editText3);
                            layout.addView(editText4);
                            if(oc) {
                                Consumer<Date> c = date -> {
                                  String s1 = editText.getText().toString() + " " + editText2.getText().toString();
                                  String s2 = editText3.getText().toString() + " " + editText4.getText().toString();
                                  Long d1 = null;
                                  Long d2 = null;
                                  try {d1 = format.parse(s1).getTime();} catch(Exception e) {}
                                  try {d2 = format.parse(s2).getTime();} catch(Exception e) {}
                                  onClick.accept(new Long[]{d1, d2}, false);
                                };
                                createDateWatcher(editText, format1, split[0].length(), c);
                                createDateWatcher(editText2, format2, split[1].length(), c);
                                createDateWatcher(editText3, format1, split[0].length(), c);
                                createDateWatcher(editText4, format2, split[1].length(), c);
                            }
                        } else {
                            format = new SimpleDateFormat(formatString);
                            int var = formatString.contains(":") ? InputType.TYPE_DATETIME_VARIATION_TIME : InputType.TYPE_DATETIME_VARIATION_DATE;
                            editText = createEditText(o.getInt(Constants.FIELD_LENGTH), InputType.TYPE_CLASS_DATETIME | var, textsize);
                            editText2 = createEditText(o.getInt(Constants.FIELD_LENGTH), InputType.TYPE_CLASS_DATETIME | var, textsize);
                            editText.setHint(formatString);
                            editText2.setHint(formatString);
                            if (intVal != null) editText.setText(format.format(new Date(intVal)));
                            if (intVal2 != null) editText2.setText(format.format(new Date(intVal2)));
                            layout.addView(editText);
                            layout.addView(createTextView("-", textsize));
                            layout.addView(editText2);
                            if (oc) {
                                Consumer<Date> c = date -> {
                                    String s1 = editText.getText().toString();
                                    String s2 = editText2.getText().toString();
                                    Long d1 = null;
                                    Long d2 = null;
                                    try {d1 = format.parse(s1).getTime();} catch(Exception e) {}
                                    try {d2 = format.parse(s2).getTime();} catch(Exception e) {}
                                    onClick.accept(new Long[]{d1, d2}, false);
                                };
                                createDateWatcher(editText, format, 16, c);
                                createDateWatcher(editText2, format, 16, c);
                            }
                        }
                        return layout;
                    case BOOL:
                        final int idx = Util.castInt(Util.orElse(intVal, (Long)0l));
                        if(type == FilterConfigurationFragment.BoolTypes.ROTATE.ordinal()) {
                            String s = text + ": " + new String[]{"all", "true", "false"}[idx];
                            TextView textView = createTextView(s, textsize);
                            if(oc) textView.setOnClickListener(v -> onClick.accept((idx+1)%3, true));
                            return textView;
                        }
                        if(type == FilterConfigurationFragment.BoolTypes.SPINNER.ordinal()) {
                            layout = createLinearLayout(WRAP_CONTENT, WRAP_CONTENT, HORIZONTAL);
                            layout.addView(titleView);
                            List<String> choices = new ArrayList<>();
                            choices.add("all");
                            choices.add("true");
                            choices.add("false");
                            layout.addView(createSpinner(choices,0,false,i->{
                                if(oc) onClick.accept(i,false);
                            }));
                            return layout;
                        }
                        if(type == FilterConfigurationFragment.BoolTypes.CHECKBOXES.ordinal()) {
                            layout = createLinearLayout(WRAP_CONTENT, WRAP_CONTENT, HORIZONTAL);
                            layout.addView(titleView);
                            CheckBox box1 = createCheckBox("true", idx != 2);
                            layout.addView(box1);
                            CheckBox box2 = createCheckBox("false", idx != 1);
                            layout.addView(box2);
                            if(oc) {
                                View.OnClickListener l = v -> {
                                    if (!box1.isChecked() && !box2.isChecked()) {
                                        box1.setChecked(true);
                                        box2.setChecked(true);
                                    }
                                    onClick.accept(box1.isChecked() ? (box2.isChecked() ? 2 : 1) : 0, false);
                                };
                                box1.setOnClickListener(l);
                                box2.setOnClickListener(l);
                            }
                            return layout;
                        }
                }
            } else {
                String s = text;
                final Long val = Util.orElse(intVal, (Long)0l);
                if(val == 1) s += " ↑";
                else if(val == 2) s += " ↓";
                TextView tv = createTextView(s, textsize);
                if(oc) tv.setOnClickListener(v -> onClick.accept((val==1) ? 2 : 1, true));
                return tv;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void loadView(JSONObject o, LinearLayout parent, Song song, boolean root) {
        try {
            int type = o.getInt(Constants.FIELD_TYPE);
            int width = o.getInt(Constants.FIELD_WIDTH);
            if(width>0) width = scale(width);
            int height = o.optInt(Constants.FIELD_HEIGHT, WRAP_CONTENT);
            if(height>0) height = scale(height);
            int bcolor = Constants.getColor2(o.optInt(Constants.FIELD_BACKGROUND,root ? 8: 0));

            LinearLayout.LayoutParams p;
            int orientation = parent.getOrientation();
            if(width == MATCH_PARENT && orientation == HORIZONTAL)
                p = new LinearLayout.LayoutParams(0,height,1);
            else if(height == MATCH_PARENT && orientation == VERTICAL)
                p = new LinearLayout.LayoutParams(width,0,1);
            else
                p = new LinearLayout.LayoutParams(width, height);
            if(orientation == HORIZONTAL) p.gravity = Gravity.CENTER_VERTICAL;
//            else p.gravity = Gravity.CENTER;
            if(type < 0) {
                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(-1-type);
                if(root) {
                    layout.setBackgroundResource(R.drawable.background_item_song_in_dance);
                    layout.getBackground().setColorFilter(new BlendModeColorFilter(bcolor, BlendMode.SRC_ATOP));
                    p.topMargin = 20;
                } else {
                    layout.setBackgroundColor(bcolor);
                }
                layout.setLayoutParams(p);
                JSONArray children = o.getJSONArray(Constants.FIELD_CHILDREN);
                parent.addView(layout);
                for(int i = 0; i < children.length(); i++)
                    loadView(children.getJSONObject(i), layout, song, false);
                if(root) {
                    ImageView img = new ImageView(context);
                    int n = scale(48);
                    int m = scale(12);
                    p = new LinearLayout.LayoutParams(n,n);
                    p.gravity = Gravity.CENTER_VERTICAL;
                    img.setColorFilter(context.getColor(R.color.FlatWhite));
                    img.setPadding(m,m,m,m);
                    img.setLayoutParams(p);
                    img.setImageResource(R.drawable.ic_more_horiz_black_24dp);
                    layout.addView(img);
                }
            } else {
                String tagName = o.getString(Constants.FIELD_TAG);
                if(!allTags.containsKey(tagName)) {
                    parent.addView(createTextView("ERROR"));
                    return;
                }
                Song.Tag tag = allTags.get(tagName);
                String s = song.getString(tag, type);
                String format = o.getString(Constants.FIELD_FORMAT);
                TextView tv = new TextView(context);
                tv.setLayoutParams(p);
                tv.setBackgroundColor(bcolor);
                SpannableString spanString = new SpannableString(String.format(format, s));
                if(o.optBoolean(Constants.FIELD_BOLD, false)) spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
                if(o.optBoolean(Constants.FIELD_ITALIC, false)) spanString.setSpan(new StyleSpan(Typeface.ITALIC), 0, spanString.length(), 0);
                if(o.optBoolean(Constants.FIELD_UNDERLINE, false)) spanString.setSpan(new UnderlineSpan(), 0, spanString.length(), 0);
                tv.setText(spanString);
                int size = o.getInt(Constants.FIELD_TEXTSIZE);
                tv.setTextSize(size);
                int color = Constants.getColor(context,o.optInt(Constants.FIELD_COLOR,0));
                tv.setTextColor(color);
                parent.addView(tv);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void loadTags(LinearLayout layout, Song song, Consumer<Song> onChange) {
        layout.removeAllViews();

        Map<String, Song.Tag> allTags = SongLoader.getAllTags();
        
        layout.addView(createTextView("Note: These changes will not be written to the audio file itself, but stored for this app"));

        final int margin = scale(20);
        for(String tagName:allTags.keySet()) {
            if(tagName.equals(Song._DURATION) || tagName.equals(Song._DATE)) continue;
            final Song.Tag tag = allTags.get(tagName);
            LinearLayout line = modifyParams(createLinearLayout(WRAP_CONTENT, WRAP_CONTENT, HORIZONTAL), p ->p.topMargin=margin);
            layout.addView(line);
            String lbl = tag.name + ": ";
            switch (tag.type) {
                case STRING:
                    line.addView(createEditText(lbl, song.getString(tag.name),10, InputType.TYPE_CLASS_TEXT,s -> {
                        song.set(tag.name, s);
                        onChange.accept(song);
                    }));
                    break;
                case INT:
                    line.addView(createEditText(lbl, song.getString(tag.name),10, InputType.TYPE_CLASS_NUMBER,s -> {
                        song.set(tag.name, s.isEmpty() ? 0 : Integer.parseInt(s));
                        onChange.accept(song);
                    }));
                    break;
                case FLOAT:
                    line.addView(createEditText(lbl, song.getString(tag.name),10, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL,s -> {
                        song.set(tag.name, s.isEmpty() ? 0 : Double.parseDouble(s));
                        onChange.accept(song);
                    }));
                    break;
                case BOOL:
                    line.addView(createCheckBox(tag.name, song.getBool(tag.name), b-> {
                        song.set(tag.name, b);
                        onChange.accept(song);
                    }));
                    break;
                case DATETIME:
                    line.addView(createTextView(lbl));
                    String formatString = Constants.dateFormats[tag.arg];
                    SimpleDateFormat format = new SimpleDateFormat(formatString);
                    Date d = song.getDate(tag.name);
                    if(formatString.contains(" ")) {
                        String[] split = formatString.split(" ");
                        final SimpleDateFormat format1 = new SimpleDateFormat(split[0]);
                        final SimpleDateFormat format2 = new SimpleDateFormat(split[1]);
                        final EditText editText = createEditText(split[0].length(), InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_DATE);
                        final EditText editText2 = createEditText(split[1].length(), InputType.TYPE_CLASS_DATETIME | InputType.TYPE_DATETIME_VARIATION_TIME);
                        editText.setHint(split[0]);
                        editText2.setHint(split[1]);
                        editText.setText(format1.format(d));
                        editText2.setText(format2.format(d));
                        line.addView(editText);
                        line.addView(editText2);
                        Consumer<Date> c = date -> {
                            String s1 = editText.getText().toString() + " " + editText2.getText().toString();
                            Long d1 = null;
                            try {d1 = format.parse(s1).getTime();} catch(Exception e) {}
                            song.set(tag.name, d1);
                            onChange.accept(song);
                        };
                        createDateWatcher(editText, format1, split[0].length(), c);
                        createDateWatcher(editText2, format2, split[1].length(), c);
                        break;
                    }
                    int var = formatString.contains(":") ? InputType.TYPE_DATETIME_VARIATION_TIME : InputType.TYPE_DATETIME_VARIATION_DATE;
                    final EditText editText = createEditText(formatString.length(), InputType.TYPE_CLASS_DATETIME | var);
                    editText.setHint(formatString);
                    editText.setText(format.format(d));
                    line.addView(editText);
                    Consumer<Date> c = date -> {
                        String s1 = editText.getText().toString();
                        Long d1 = null;
                        try {d1 = format.parse(s1).getTime();} catch(Exception e) {}
                        song.set(tag.name, d1);
                        onChange.accept(song);
                    };
                    createDateWatcher(editText, format, formatString.length(), c);
                    break;
                case RATING:
                    line.addView(createTextView(lbl));
                    int rating = song.getInt(tag.name);
                    for(int i = 0; i <= tag.arg; i++) {
                        int finalI = i;
                        TextView tv = createTextView(String.valueOf(i));
                        int p = scale(5);
                        tv.setPadding(p,0,p,0);
                        line.addView(tv);
                        if (i == rating) tv.setTextColor(Color.MAGENTA);
                        else {
                            tv.setOnClickListener(v -> {
                                song.set(tag.name, finalI);
                                onChange.accept(song);
                                loadTags(layout, song, onChange);
                            });
                        }
                    }


            }
        }
    }

    public TextView createTextView(String text) {return createTextView(text, defaultTextSize);}
    public TextView createTextView(String text, int textSize) {
        TextView tv = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        tv.setLayoutParams(params);
        tv.setText(text);
        tv.setTextColor(flatWhite);
        tv.setTextSize(textSize);
        return tv;
    }

    public EditText createEditText(int ems, int type) {
        return createEditText(ems, type, defaultTextSize);
    }
    public EditText createEditText(int ems, int type, int textSize) {
        EditText edit = new EditText(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if(ems > 0 ) edit.setEms(ems);
        edit.setLayoutParams(params);
        edit.setInputType(type);
        edit.setTextSize(textSize);
        return edit;
    }

    public LinearLayout createLinearLayout(int width, int height, int orientation) {
        LinearLayout layout = new LinearLayout(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        params.gravity = orientation == HORIZONTAL ? Gravity.CENTER_VERTICAL : Gravity.CENTER;
        layout.setLayoutParams(params);
        layout.setOrientation(orientation);
        return layout;
    }


    public RadioGroup createRadioGroup(int width, int height, int orientation, String[] choices, int selected, Consumer<Integer> onClick) {
        RadioGroup group = new RadioGroup(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        group.setLayoutParams(params);
        group.setOrientation(orientation);
        int id = 0;
        for(int i = 0; i < choices.length; i++) {
            String s = choices[i];
            final int idx = i;
            RadioButton btn = new RadioButton(context);
            params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            btn.setLayoutParams(params);
            btn.setText(s);
            btn.setOnClickListener(v -> onClick.accept(idx));
            group.addView(btn);
            if(selected == i) id = btn.getId();
        }
        group.check(id);
        return group;
    }

    public Spinner createSpinner(List<String> choices, int selected, boolean small, Consumer<Integer> onSelect) {
        Spinner spinner = new Spinner(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        spinner.setLayoutParams(params);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, small ? R.layout.spinner_layout_small : R.layout.spinner_layout, choices);
        spinner.setAdapter(adapter);
        if(selected >= 0) spinner.setSelection(selected);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                onSelect.accept(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return spinner;

    }

    public LinearLayout createEditText(String label, String text, int ems, int type, Consumer<String> onChange) {
        return createEditText(label,text,ems,type,defaultTextSize,onChange);
    }

    public LinearLayout createEditText(String label, String text, int ems, int type, int textsize, Consumer<String> onChange) {
        LinearLayout layout = createLinearLayout(WRAP_CONTENT, WRAP_CONTENT, HORIZONTAL);
        layout.addView(createTextView(label, textsize));
        EditText editText = createEditText(ems, type, textsize);
        editText.setText(text);
        createTextWatcher(editText, s -> onChange.accept(s));
        layout.addView(editText);
        return layout;
    }

    public void createTextWatcher(EditText editText, Consumer<String> consumer) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                currentEditText = editText;
                currentPos=i+i2;
                consumer.accept(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void focusCurrentEditText() {
        if(currentEditText == null) return;
        currentEditText.requestFocus();
        currentEditText.setSelection(currentPos);
        currentEditText = null;
    }

    public EditText getCurrentEditText() {
        return currentEditText;
    }

    public void setCurrentEditText(EditText editText) {
        currentEditText = editText;
    }

    public void createDateWatcher(EditText editText, SimpleDateFormat format, int length, Consumer<Date> consumer) {
        createTextWatcher(editText, s -> {
            if(s.length() == length) {
                try {
                    Date d = format.parse(s);
                    editText.setTextColor(flatWhite);
                    consumer.accept(d);
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            editText.setTextColor(Color.RED);
            consumer.accept(null);
        });
    }

    public LinearLayout createSizeControl(String text, int size, Consumer<Integer> onChange) {
        final TextView textView = createTextView(Util.toString(size),14);
        LinearLayout layout = createLinearLayout(WRAP_CONTENT, WRAP_CONTENT, HORIZONTAL);
        if(text != null) layout.addView(createTextView(text));
        Button btn = new Button(context);
        int btnSize = scale(40);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(btnSize, btnSize);
        btn.setLayoutParams(params);
        btn.setText("-");
        btn.setOnClickListener(v -> {
            int n = Util.parseInt(textView.getText().toString())-1;
            textView.setText(Util.toString(n));
            onChange.accept(n);
        });
        if(size == 1) btn.setEnabled(false);
        layout.addView(btn);
        layout.addView(textView);
        btn = new Button(context);
        params = new LinearLayout.LayoutParams(btnSize, btnSize);
        params.leftMargin = 10;
        btn.setLayoutParams(params);
        btn.setText("+");
        btn.setOnClickListener(v -> {
            int n = Util.parseInt(textView.getText().toString())+1;
            textView.setText(Util.toString(n));
            onChange.accept(n);
        });
        layout.addView(btn);
        return layout;
    }

    public CheckBox createCheckBox(String label, boolean selected) {
        return createCheckBox(label, selected, null);
    }
    public CheckBox createCheckBox(String label, boolean selected, Consumer<Boolean> onChange) {
        CheckBox box = new CheckBox(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_VERTICAL;
        box.setLayoutParams(params);
        box.setText(label);
        box.setTextSize(defaultTextSize);
        box.setChecked(selected);
        if(onChange != null) box.setOnClickListener(v -> onChange.accept(((CheckBox)v).isChecked()));
        return box;
    }

    public View createFiller() {
        View v = new View(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,1,1);
        v.setLayoutParams(params);
        return v;
    }

    public View createFillerVertical() {
        View v = new View(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(1,0,1);
        v.setLayoutParams(params);
        return v;
    }

    public Button createButton(String text, View.OnClickListener onClick) {
        Button button = new Button(context);
        int width = (text.length() == 1) ? scale(40) : WRAP_CONTENT;
        int height = (text.length() == 1) ? scale(40) : WRAP_CONTENT;

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(width, height);
        p.gravity = Gravity.CENTER;
        button.setLayoutParams(p);
        button.setText(text);
        button.setOnClickListener(onClick);
        return button;
    }

    public static <T extends View> T modifyParams(T view, Consumer<LinearLayout.LayoutParams> c) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        c.accept((LinearLayout.LayoutParams) params);
        view.setLayoutParams(params);
        return view;
    }
}
