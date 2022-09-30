package com.ldt.musicr.util;

import androidx.annotation.NonNull;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import java.util.ArrayList;
import java.util.List;

public class InterpolatorUtil {

    public static List<Interpolator> getInterpolatorList() {
        List<Interpolator> interpolatorList = new ArrayList<>();
        interpolatorList.add(new LinearInterpolator());
        interpolatorList.add(new AccelerateInterpolator());
        interpolatorList.add(new DecelerateInterpolator());
        interpolatorList.add(new AccelerateDecelerateInterpolator());
        interpolatorList.add(new OvershootInterpolator());
        interpolatorList.add(new AnticipateInterpolator());
        interpolatorList.add(new AnticipateOvershootInterpolator());
        interpolatorList.add(new BounceInterpolator());
        interpolatorList.add(new FastOutLinearInInterpolator());
        interpolatorList.add(new FastOutSlowInInterpolator());
        interpolatorList.add(new LinearOutSlowInInterpolator());
        return interpolatorList;
    }

    @NonNull
    public static Interpolator getInterpolator(int id) {
        switch (id) {
            case 0:
                return new LinearInterpolator();
            case 1:
                return new AccelerateInterpolator();
            case 2:
                return new DecelerateInterpolator();
            case 3:
                return new AccelerateDecelerateInterpolator();
            case 4:
                return new OvershootInterpolator();
            case 5:
                return new AnticipateInterpolator();
            case 6:
                return new AnticipateOvershootInterpolator();
            case 7:
                return new BounceInterpolator();
            case 8:
                return new FastOutLinearInInterpolator();
            case 9:
                return new LinearInterpolator();
            case 10:
                return new LinearOutSlowInInterpolator();
            default:
                return new FastOutSlowInInterpolator();
        }
    }

    /**
     * Interpolator defining the animation curve for mScroller
     */
    public static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    };

}
