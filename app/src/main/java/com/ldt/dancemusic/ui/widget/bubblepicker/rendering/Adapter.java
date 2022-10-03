package com.ldt.dancemusic.ui.widget.bubblepicker.rendering;

import com.ldt.dancemusic.ui.widget.bubblepicker.model.PickerItem;

public abstract class Adapter implements Decorator  {

    public abstract boolean onBindItem(PickerItem item, boolean create, int i);

    public  abstract int getItemCount();

}