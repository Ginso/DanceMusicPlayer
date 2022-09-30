package com.ldt.dancemusic.ui.intro;

import android.os.Bundle;

import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.animation.AccelerateDecelerateInterpolator;

import com.ldt.dancemusic.R;
import com.ldt.dancemusic.ui.widget.fragmentnavigationcontroller.NavigationControllerFragment;
import com.ldt.dancemusic.ui.widget.fragmentnavigationcontroller.PresentStyle;

/**
 * Controller này tự động loại bỏ giao diện màn hình chính và hiển thị các màn hình (PermissionRequire, NoSongInLibrary) khi cần
 */
public class IntroController {
    private static final String TAG = "IntroController";

    NavigationControllerFragment mNavigationController;

    public NavigationControllerFragment getNavigationController() {
        return mNavigationController;
    }

    public IntroController() {

    }

    public void init(AppCompatActivity activity, Bundle savedInstanceState) {
        initBackStack(activity, savedInstanceState);

    }

    private void initBackStack(AppCompatActivity activity, Bundle savedInstanceState) {
        FragmentManager fm = activity.getSupportFragmentManager();
        mNavigationController = NavigationControllerFragment.navigationController(fm, R.id.back_wall_container);
        mNavigationController.setAbleToPopRoot(true);
        mNavigationController.setPresentStyle(PresentStyle.FADE);
        mNavigationController.setDuration(250);
        mNavigationController.setInterpolator(new AccelerateDecelerateInterpolator());
        mNavigationController.presentFragment(new PermissionRequiredFragment());
        // mNavigationController.presentFragment(new MainFragment());
    }
}
