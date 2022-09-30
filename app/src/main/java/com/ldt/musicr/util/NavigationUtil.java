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

package com.ldt.musicr.util;


import android.app.Activity;

import androidx.annotation.NonNull;

import com.ldt.musicr.model.Playlist;
import com.ldt.musicr.ui.AppActivity;
import com.ldt.musicr.ui.CardLayerController;
import com.ldt.musicr.ui.page.librarypage.LibraryPagerAdapter;
import com.ldt.musicr.ui.page.librarypage.LibraryTabFragment;
import com.ldt.musicr.ui.page.subpages.DancePagerFragment;
import com.ldt.musicr.ui.page.subpages.singleplaylist.SinglePlaylistFragment;
import com.ldt.musicr.ui.widget.fragmentnavigationcontroller.NavigationFragment;

public class NavigationUtil {

    public static void navigateToBackStackController(@NonNull final AppActivity activity) {
        final CardLayerController.CardLayerAttribute playingQueueAttr = activity.getCardLayerController().getMyAttr(activity.getPlayingQueueLayerFragment());
        final CardLayerController.CardLayerAttribute nowPlayingAttr = activity.getCardLayerController().getMyAttr(activity.getNowPlayingController());

        if(playingQueueAttr.getState()!= CardLayerController.CardLayerAttribute.MINIMIZED&&nowPlayingAttr.getState()!= CardLayerController.CardLayerAttribute.MINIMIZED) {
            // 2 layer is maximized
            playingQueueAttr.animateToMin();
            playingQueueAttr.getParent().postDelayed(nowPlayingAttr::animateToMin,550);
        } else if(playingQueueAttr.getState()!= CardLayerController.CardLayerAttribute.MINIMIZED) {
            // only playing queue
            playingQueueAttr.animateToMin();
        } else if(nowPlayingAttr.getState()!= CardLayerController.CardLayerAttribute.MINIMIZED) {
            // only now playing
            nowPlayingAttr.animateToMin();
        }
    }

    public static void navigateToNowPlayingController(@NonNull final AppActivity activity) {
        final CardLayerController.CardLayerAttribute playingQueueAttr = activity.getCardLayerController().getMyAttr(activity.getPlayingQueueLayerFragment());
        final CardLayerController.CardLayerAttribute nowPlayingAttr = activity.getCardLayerController().getMyAttr(activity.getNowPlayingController());

        if(playingQueueAttr.getState()!= CardLayerController.CardLayerAttribute.MINIMIZED && nowPlayingAttr.getState() != CardLayerController.CardLayerAttribute.MINIMIZED) {
            // 2 layer is maximized
            playingQueueAttr.animateToMin();
        } else if(playingQueueAttr.getState()!= CardLayerController.CardLayerAttribute.MINIMIZED) {
            // playing queue is maximize, while now playing is minimize
            playingQueueAttr.animateToMin();
            playingQueueAttr.getParent().postDelayed(nowPlayingAttr::animateToMax,550);
        } else if(nowPlayingAttr.getState()== CardLayerController.CardLayerAttribute.MINIMIZED) {
            // both are minimized
            nowPlayingAttr.animateToMax();
        }
    }

    public static LibraryTabFragment getLibraryTab( Activity activity) {
        if(activity instanceof AppActivity) {
            final AppActivity appActivity = (AppActivity) activity;
            return appActivity.getBackStackController().navigateToLibraryTab(false);
        }
        return null;
    }

    public static void updateSongs(Activity activity) {
        LibraryTabFragment libraryTab = NavigationUtil.getLibraryTab(activity);
        LibraryPagerAdapter pagerAdapter = libraryTab.getPagerAdapter();
        pagerAdapter.getSongChildTab().refreshData();
        pagerAdapter.getDanceChildTab().refreshData();
        NavigationFragment topFragment = libraryTab.getNavigationController().getTopFragment();
        if(topFragment instanceof SinglePlaylistFragment) {
            ((SinglePlaylistFragment)topFragment).refreshData();
        } else if(topFragment instanceof DancePagerFragment) {
            ((DancePagerFragment)topFragment).refreshData();
        }
    }

    public static void navigateToPlaylist(@NonNull final Activity activity, final Playlist playlist) {
        if (activity instanceof AppActivity) {
            final AppActivity appActivity = (AppActivity) activity;

            LibraryTabFragment fragment = appActivity.getBackStackController().navigateToLibraryTab(true);
            if (fragment != null)
                fragment.getNavigationController().presentFragment(SinglePlaylistFragment.newInstance(playlist,null));
            navigateToBackStackController(appActivity);
        }
    }

}
