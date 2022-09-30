package com.ldt.dancemusic.glide.palette;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;

public class BitmapPaletteTranscoder implements ResourceTranscoder<Bitmap, BitmapPaletteWrapper> {
    @Nullable
    @Override
    public Resource<BitmapPaletteWrapper> transcode(@NonNull Resource<Bitmap> bitmapResource, @NonNull Options options) {
        Bitmap bitmap = bitmapResource.get();
        Palette palette = null;
        if(bitmap != null) palette = Palette.from(bitmap).generate();
        BitmapPaletteWrapper bitmapPaletteWrapper = new BitmapPaletteWrapper(bitmap,palette);
        return new BitmapPaletteResource(bitmapPaletteWrapper);
    }
}