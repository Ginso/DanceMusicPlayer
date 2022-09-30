package com.ldt.dancemusic.ui.page.librarypage;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ldt.dancemusic.App;
import com.ldt.dancemusic.R;
import com.ldt.dancemusic.loader.medialoader.SongLoader;
import com.ldt.dancemusic.ui.page.librarypage.dance.DanceChildTab;
import com.ldt.dancemusic.ui.page.librarypage.song.SongChildTab;
import com.ldt.dancemusic.ui.widget.fragmentnavigationcontroller.NavigationFragment;
import com.ldt.dancemusic.util.Tool;

import java.lang.ref.WeakReference;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LibraryTabFragment extends NavigationFragment {
    private static final String TAG ="LibraryTabFragment";

    @BindView(R.id.back_image)
    ImageView mBackImage;

    @BindView(R.id.tab_layout)
    TabLayout mTabLayout;

    public ViewPager getViewPager() {
        return mViewPager;
    }

    @BindView(R.id.view_pager)
    ViewPager mViewPager;

    public LibraryPagerAdapter getPagerAdapter() {
        return mPagerAdapter;
    }

    private LibraryPagerAdapter mPagerAdapter;
    @BindView(R.id.status_bar) View mStatusView;

    @Nullable
    @Override
    protected View onCreateView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.screen_tab_library,container,false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mLoadArtist != null) mLoadArtist.cancel(true);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
      //  mSearchView.onActionViewExpanded();
       // mSearchView.clearFocus();
        mStatusView.getLayoutParams().height = Tool.getStatusHeight(getResources());
        mStatusView.requestLayout();
      //  mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();


        //if(true) return;
        mViewPager.setOnTouchListener((v, event) -> getMainActivity().backStackStreamOnTouchEvent(event));
        mPagerAdapter = new LibraryPagerAdapter(getActivity(),getChildFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(5);
        mTabLayout.setupWithViewPager(mViewPager);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        // mTabLayout.setTabsFromPagerAdapter(mTabAdapter);//deprecated
        mTabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
    }

    private LoadSongsAsyncTask mLoadArtist;

    public void refresh() {

        if (mLoadArtist != null) mLoadArtist.cancel(true);
        mLoadArtist = new LoadSongsAsyncTask(this);
        mLoadArtist.execute();

    }

    public void refreshData() {
        PagerAdapter adapter = getViewPager().getAdapter();
        if(adapter instanceof LibraryPagerAdapter) {
            Fragment fragment = ((LibraryPagerAdapter) adapter).getItem(0);
            if(fragment instanceof SongChildTab) {
                ((SongChildTab) fragment).refreshData();
            }
            fragment = ((LibraryPagerAdapter) adapter).getItem(2);
            if(fragment instanceof DanceChildTab) {
                ((DanceChildTab) fragment).refreshData();
            }
        }
    }

    private static class LoadSongsAsyncTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<LibraryTabFragment> mFragment;

        public LoadSongsAsyncTask(LibraryTabFragment fragment) {
            super();
            mFragment = new WeakReference<>(fragment);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Context context = null;

            if (App.getInstance() != null)
                context = App.getInstance().getApplicationContext();

            if (context != null)
                SongLoader.loadAllSongs(App.getInstance());

            return null;
        }

        public void cancel() {
            mCancelled = true;
            cancel(true);
            mFragment.clear();
        }

        private boolean mCancelled = false;

        @Override
        protected void onPostExecute(Void v) {
            mFragment.get().refreshData();
        }


    }

}
