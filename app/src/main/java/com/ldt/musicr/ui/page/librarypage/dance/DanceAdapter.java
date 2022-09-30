package com.ldt.musicr.ui.page.librarypage.dance;

import android.graphics.Color;
import android.graphics.Outline;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.TextView;

import com.ldt.musicr.R;
import com.ldt.musicr.contract.AbsBindAbleHolder;
import com.ldt.musicr.contract.AbsMediaAdapter;
import com.ldt.musicr.helper.menu.MenuHelper;
import com.ldt.musicr.model.Dance;
import com.ldt.musicr.ui.bottomsheet.OptionBottomSheet;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class DanceAdapter extends AbsMediaAdapter<AbsBindAbleHolder, Dance> implements FastScrollRecyclerView.SectionedAdapter {
    private static final String TAG = "ArtistAdapter";



    public interface DanceClickListener {
        void onDanceItemClick(Dance dance);
    }

    private DanceClickListener mListener;

    public void setDanceClickListener(DanceClickListener listener) {
        mListener = listener;
    }

    @Override
    protected void onMenuItemClick(int positionInData) {
        Log.d("TEST", "TEST");
    }


    @Override
    protected void onDataSet() {

    }



    @NonNull
    @Override
    public AbsBindAbleHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ItemHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_dance_child, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull AbsBindAbleHolder itemHolder, int i) {
        if (itemHolder instanceof ItemHolder) {
            ((ItemHolder) itemHolder).bind(getData().get(i));
        }
    }

    @Override
    protected boolean onLongPressedItem(AbsBindAbleHolder holder, int position) {
        super.onLongPressedItem(holder, position);
        OptionBottomSheet.newInstance(MenuHelper.DANCE_OPTION, getData().get(position)).show(((AppCompatActivity) getContext()).getSupportFragmentManager(), "dance_option");
        return true;
    }

    public static int lighter(int color, float factor) {
        int red = (int) ((Color.red(color) * (1 - factor) / 255 + factor) * 255);
        int green = (int) ((Color.green(color) * (1 - factor) / 255 + factor) * 255);
        int blue = (int) ((Color.blue(color) * (1 - factor) / 255 + factor) * 255);
        return Color.argb(Color.alpha(color), red, green, blue);
    }


    @NonNull
    @Override
    public String getSectionName(int i) {
        if (getData().get(getDataPosition(i)).title.isEmpty())
            return "";
        return getData().get(getDataPosition(i)).title.substring(0, 1);
    }

    class ItemHolder extends AbsMediaHolder {

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mImage.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline outline) {
                        outline.setOval(0, 0, view.getWidth(), view.getHeight());
                    }
                });
                mImage.setClipToOutline(true);
            }
        }

        @BindView(R.id.title)
        TextView mArtist;

        @BindView(R.id.image)
        ImageView mImage;

        @BindView(R.id.genre_one)
        TextView mGenreOne;

        @BindView(R.id.genre_two)
        TextView mGenreTwo;

        @BindView(R.id.panel)
        View mPanel;
        @BindView(R.id.panel_color)
        View mPanelColor;

        @OnLongClick(R.id.panel)
        boolean onLongClickPanel() {
            return onLongPressedItem(this, getAdapterPosition());
        }

        @OnClick(R.id.panel)
        void goToThisArtist() {
            if (mListener != null) mListener.onDanceItemClick(getData().get(getAdapterPosition()));
        }

        @BindView(R.id.root)
        View mRoot;

        @BindView(R.id.count)
        TextView mCount;

        public void bind(Dance dance) {
            mArtist.setText(dance.title);
            mCount.setText(String.format("%d %s", dance.getSongCount(), mCount.getContext().getResources().getString(R.string.songs)));
            mGenreOne.setVisibility(View.GONE);
//            if (genres == null) {
//                mGenreOne.setText("⋯");
//                mGenreTwo.setVisibility(View.GONE);
//                Log.d(TAG, "load genre item " + getAdapterPosition());
//
//                GenreArtistTask task = mGenreArtistTaskMap.get(artist);
//                if (task == null) {
//                    task = new GenreArtistTask(ArtistAdapter.this, artist, getAdapterPosition());
//                    mGenreArtistTaskMap.put(artist, task);
//                    task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                }
//
//            } else bindGenre(genres);
//            //try {
//            loadArtistImage(artist);
            // } catch (Exception ignored) {}
        }

    }



    @Override
    public void onBindViewHolder(@NonNull AbsBindAbleHolder holder, int position, @NonNull List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

}
