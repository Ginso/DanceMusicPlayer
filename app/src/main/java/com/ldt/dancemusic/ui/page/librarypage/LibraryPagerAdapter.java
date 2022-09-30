package com.ldt.dancemusic.ui.page.librarypage;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.ldt.dancemusic.R;
import com.ldt.dancemusic.ui.page.librarypage.dance.DanceChildTab;
import com.ldt.dancemusic.ui.page.librarypage.playlist.PlaylistChildTab;
import com.ldt.dancemusic.ui.page.librarypage.song.SongChildTab;

import java.util.ArrayList;

public class LibraryPagerAdapter extends FragmentPagerAdapter {
    private Context mContext;

    private ArrayList<Fragment> mData = new ArrayList<>();

    private void initData() {
        mData.add(new SongChildTab());
        mData.add(new PlaylistChildTab());
        mData.add(new DanceChildTab());
    }

    public LibraryPagerAdapter(Context context, FragmentManager fragmentManager) {
        super(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mContext = context;
        initData();
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return mData.size();
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        return mData.get(position);
    }

    public SongChildTab getSongChildTab() {
        return (SongChildTab)getItem(0);
    }

    public PlaylistChildTab getPlaylistChildTab() {
        return(PlaylistChildTab)getItem(1);
    }

    public DanceChildTab getDanceChildTab() {
        return(DanceChildTab)getItem(2);
    }

    // Returns the page mTitle for the top indicator
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getResources().getString(R.string.songs);
            case 1:
                return mContext.getResources().getString(R.string.playlists);
            case 2:
                return mContext.getResources().getString(R.string.dances);
            default:
                return null;
        }

    }
}
