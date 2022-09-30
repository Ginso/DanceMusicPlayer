package com.ldt.dancemusic.contract;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ldt.dancemusic.R;
import com.ldt.dancemusic.glide.GlideApp;
import com.ldt.dancemusic.glide.SongGlideRequest;
import com.ldt.dancemusic.helper.songpreview.PreviewSong;
import com.ldt.dancemusic.helper.songpreview.SongPreviewController;
import com.ldt.dancemusic.helper.songpreview.SongPreviewListener;
import com.ldt.dancemusic.loader.medialoader.SongLoader;
import com.ldt.dancemusic.model.Song;
import com.ldt.dancemusic.service.MusicPlayerRemote;
import com.ldt.dancemusic.ui.AppActivity;
import com.ldt.dancemusic.ui.MusicServiceActivity;
import com.ldt.dancemusic.ui.page.librarypage.dance.DanceAdapter;
import com.ldt.dancemusic.ui.page.subpages.SonglistConfigurationFragment;
import com.ldt.dancemusic.ui.widget.CircularPlayPauseProgressBar;
import com.ldt.dancemusic.util.Constants;
import com.ldt.dancemusic.util.Tool;
import com.ldt.dancemusic.util.WidgetFactory;
import com.makeramen.roundedimageview.RoundedImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Handle Event:
 * <br>+. Click on item
 * <br>+. Long Click on item
 * <br>+. Click Preview Button on item
 * <br>+. Click Menu Button on item
 */

public abstract class AbsSongAdapter extends AbsMediaAdapter<AbsBindAbleHolder, Song> implements SongPreviewListener {
    private static final String TAG = "AbsSongAdapter";

    public AbsSongAdapter() {
        super();


    }

    @Override
    public void init(Context context) {
        super.init(context);
        if (context instanceof MusicServiceActivity) {
            SongPreviewController controller = ((MusicServiceActivity) context).getSongPreviewController();
            if (controller != null)
                controller.addSongPreviewListener(this);
        }
    }

    @Override
    protected void onDataSet() {

        if (getContext() instanceof AppActivity) {
            SongPreviewController controller = ((AppActivity) getContext()).getSongPreviewController();
            if (controller != null)
                mPreviewSong = controller.getCurrentPreviewSong();
        }
    }

    public void destroy() {
        if (getContext() instanceof AppActivity) {
            SongPreviewController controller = ((AppActivity) getContext()).getSongPreviewController();
            if (controller != null) controller.removeAudioPreviewerListener(this);
        }
        super.destroy();
    }

    public void previewAll(boolean shuffle) {
        if (getContext() instanceof AppActivity) {
            SongPreviewController preview = ((AppActivity) getContext()).getSongPreviewController();
            if (preview != null) {
                if (preview.isPlayingPreview())
                    preview.cancelPreview();
                else {
                    if (shuffle) {
                        ArrayList<Song> list = new ArrayList<>(getData());
                        Collections.shuffle(list);
                        preview.previewSongs(list);
                    } else
                        preview.previewSongs(getData());
                }
            }
        }
    }

    protected void previewThisSong(int positionData) {

        if (getContext() instanceof AppActivity) {
            SongPreviewController preview = ((AppActivity) getContext()).getSongPreviewController();
            if (preview != null) {
                if (preview.isPlayingPreview() && preview.isPreviewingSong(getData().get(positionData)))
                    preview.cancelPreview();
                else {
                    preview.previewSongs(getData().get(positionData));
                }
            }
        }
    }

    private void forceStopPreview() {
        mPreviewSong = null;
        if (getContext() instanceof AppActivity) {
            SongPreviewController controller = ((AppActivity) getContext()).getSongPreviewController();
            if (controller != null) controller.cancelPreview();
        }
    }

    private PreviewSong mPreviewSong = null;

    @Override
    public void onSongPreviewStart(PreviewSong song) {
        int position = getData().indexOf(song.getSong());

        Log.d(TAG, "onSongPreviewStart: position = " + position);
        if (position != -1) {
            mPreviewSong = song;
            notifyItemChanged(getMediaHolderPosition(position), SONG_PREVIEW_CHANGED);
        }
    }

    @Override
    public void onSongPreviewFinish(PreviewSong song) {
        mPreviewSong = null;
        int position = getData().indexOf(song.getSong());
        Log.d(TAG, "onSongPreviewFinish: position = " + position);

        if (position != -1)
            notifyItemChanged(getMediaHolderPosition(position), SONG_PREVIEW_CHANGED);
    }

    public void playAll(int startPosition, boolean startPlaying) {
        if (!getData().isEmpty()) {
            final Handler handler = new Handler();
            handler.postDelayed(() -> {
                MusicPlayerRemote.openQueue(getData(), startPosition, startPlaying);
                Handler handler1 = new Handler();
                handler1.postDelayed(() -> {
                    notifyItemChanged(getMediaHolderPosition(mMediaPlayDataItem), PLAY_STATE_CHANGED);
                    notifyItemChanged(getMediaHolderPosition(startPosition), PLAY_STATE_CHANGED);
                    mMediaPlayDataItem = startPosition;
                }, 50);
            }, 100);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull AbsBindAbleHolder holder, int position, @NonNull List<Object> payloads) {
        if (holder instanceof SongHolder && !payloads.isEmpty())
            for (Object object : payloads) {
                Log.d(TAG, "onBindViewHolder: object " + object);
                if (object instanceof String) {
                    switch ((String) object) {
                        case PLAY_STATE_CHANGED:
                            Log.d(TAG, mName + " onBindViewHolder: " + PLAY_STATE_CHANGED);
                            ((SongHolder) holder).bindMediaPlayState();
                            ((SongHolder) holder).bindTheme();
                            break;
                        case SONG_PREVIEW_CHANGED:
                            Log.d(TAG, mName + " onBindViewHolder: " + SONG_PREVIEW_CHANGED);
                            ((SongHolder) holder).bindPreviewButton(getDataItem(getDataPosition(position)));
                            break;
                        case PALETTE_CHANGED:
                            Log.d(TAG, mName + " onBindViewHolder: " + PALETTE_CHANGED);
                            ((SongHolder) holder).bindTheme();
                            break;
                        default:
                            super.onBindViewHolder(holder, position, payloads);
                    }
                } else super.onBindViewHolder(holder, position, payloads);
            }
        else {
            Log.d(TAG, mName + " onBindViewHolder: " + position + ", " + payloads);
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull AbsBindAbleHolder absBindAbleHolder, int i) {
        Log.d(TAG, "onBindViewHolder without payload " + i);
    }

    @Override
    public void notifyOnMediaStateChanged(final String whichChanged) {
        if (whichChanged.equals(PLAY_STATE_CHANGED)) {
            Song currentPlayingSong = MusicPlayerRemote.getCurrentSong();
            if (currentPlayingSong != null && currentPlayingSong.id != -1) {

                if (isMediaPlayItemAvailable()) {

                    notifyItemChanged(getMediaHolderPosition(mMediaPlayDataItem), PLAY_STATE_CHANGED);

                    boolean isStillOldPos = getData().get(mMediaPlayDataItem).equals(currentPlayingSong);
                    if (!isStillOldPos) {
                        mMediaPlayDataItem = getData().indexOf(currentPlayingSong);
                        if (mMediaPlayDataItem != -1)
                            notifyItemChanged(getMediaHolderPosition(mMediaPlayDataItem), PLAY_STATE_CHANGED);
                    }
                }
            }
        } else if (whichChanged.equals(PALETTE_CHANGED)) {
            notifyItemRangeChanged(0, getItemCount(), PALETTE_CHANGED);
        }
    }

    public class SongHolder extends AbsMediaHolder<Song> {

        @BindView(R.id.number)
        TextView mNumber;
        @BindView(R.id.title)
        TextView mTitle;
        @BindView(R.id.description)
        TextView mDescription;

        @BindView(R.id.image)
        ImageView mImage;
        @BindView(R.id.quick_play_pause)
        ImageView mQuickPlayPause;
        @BindView(R.id.menu_button)
        View mMenuButton;

        @BindView(R.id.panel)
        View mPanel;

        @OnClick(R.id.menu_button)
        void clickMenu() {
            onMenuItemClick(getDataPosition(getAdapterPosition()));
        }

        @BindView(R.id.preview_button)
        CircularPlayPauseProgressBar mPreviewButton;

        public SongHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void onClick(View view) {
            playAll(getDataPosition(getAdapterPosition()), true);
        }

        private void resetProgress() {
            mPreviewButton.resetProgress();
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void bind(Song song) {
            Log.d(TAG, "bind");
            mNumber.setText("" + (getDataPosition(getAdapterPosition()) + 1));
            mTitle.setText(song.getTitle());
            mDescription.setText(song.getArtist());

            SongGlideRequest.Builder.from(GlideApp.with(getContext()), song)
                    .ignoreMediaStore(false)
                    .generatePalette(getContext()).build()
                    .listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            if (mImage instanceof RoundedImageView)
                                ((RoundedImageView) mImage).setBorderWidth(R.dimen.oneDP);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            if (mImage instanceof RoundedImageView)
                                ((RoundedImageView) mImage).setBorderWidth(0f);
                            return false;
                        }
                    })
                    .into(mImage);

            bindTheme();

            bindPreviewButton(song);
        }

        void bindTheme() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                ((RippleDrawable) mPanel.getBackground()).setColor(ColorStateList.valueOf(Tool.getBaseColor()));
                ((RippleDrawable) mMenuButton.getBackground()).setColor(ColorStateList.valueOf(Tool.getBaseColor()));
                ((RippleDrawable) mPreviewButton.getBackground()).setColor(ColorStateList.valueOf(Tool.getBaseColor()));
            }
            bindMediaPlayState();
        }

        private void bindPreviewButton(Song song) {
            if ((mPreviewSong == null || !mPreviewSong.getSong().equals(song))
                    && mPreviewButton.getMode() == CircularPlayPauseProgressBar.PLAYING)
                mPreviewButton.resetProgress();
            else if (mPreviewSong != null && mPreviewSong.getSong().equals(song)) {
                long timePlayed = mPreviewSong.getTimePlayed();
                //    Log.d(TAG, "bindPreviewButton: timePlayed = " + timePlayed);
                if (timePlayed == -1) mPreviewButton.resetProgress();
                else {
                    if (timePlayed < 0) timePlayed = 0;
                    if (timePlayed <= mPreviewSong.getTotalPreviewDuration())
                        mPreviewButton.syncProgress(mPreviewSong.getTotalPreviewDuration(), (int) timePlayed);
                }
            }
        }

        private void bindMediaPlayState() {
            if (MusicPlayerRemote.getCurrentSong().id == getData().get(getDataPosition(getAdapterPosition())).id) {
                mMediaPlayDataItem = getDataPosition(getAdapterPosition());
                int baseColor = DanceAdapter.lighter(Tool.getBaseColor(), 0.6f);
                mTitle.setTextColor(DanceAdapter.lighter(Tool.getBaseColor(), 0.25f));
                mDescription.setTextColor(Color.argb(0xAA, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor)));
                mQuickPlayPause.setColorFilter(baseColor);

                if (MusicPlayerRemote.isPlaying()) {
                    mQuickPlayPause.setImageResource(R.drawable.ic_volume_up_black_24dp);
                } else {
                    mQuickPlayPause.setImageResource(R.drawable.ic_volume_mute_black_24dp);
                }
            } else {
                mQuickPlayPause.setImageDrawable(null);
                int flatWhite = mQuickPlayPause.getResources().getColor(R.color.FlatWhite);
                mTitle.setTextColor(mQuickPlayPause.getResources().getColor(R.color.FlatWhite));
                mDescription.setTextColor(Color.argb(0xAA, Color.red(flatWhite), Color.green(flatWhite), Color.blue(flatWhite)));
            }
        }

        @OnClick(R.id.preview_button)
        void clickPresent() {

            if (mPreviewSong != null && mPreviewSong.getSong().equals(getData().get(getDataPosition(getAdapterPosition())))) {
                resetProgress();
                forceStopPreview();
            } else {
                previewThisSong(getDataPosition(getAdapterPosition()));
            }
        }
    }

    public class DanceSongHolder extends AbsMediaHolder<Song> {

        private Song mSong;

        @BindView(R.id.root) LinearLayout root;
        int backgroundColor = 0;

        public DanceSongHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        @Override
        public void onClick(View view) {
            playAll(getDataPosition(getAdapterPosition()), true);
            JSONObject courseInfo = SongLoader.getCurrentCourseInfo();
            if (courseInfo == null) return;
            JSONObject songs = courseInfo.optJSONObject("songs");
            if(songs == null) songs = new JSONObject();
            JSONObject song = songs.optJSONObject(mSong.key);
            try {
                if(song == null) {
                    song = new JSONObject();
                    song.put("count", 1);
                } else {
                    song.put("count", song.getInt("count")+1);
                }
                song.put("last",new Date().getTime());
                songs.put(mSong.key, song);
                courseInfo.put("songs", songs);
                SongLoader.saveCourseInfo(courseInfo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


        @SuppressLint("SetTextI18n")
        @Override
        public void bind(Song song) {
            Log.d(TAG, "bind");
            mSong = song;
            root.removeAllViews();
            JSONObject scheme = SonglistConfigurationFragment.getConfiguration();
            WidgetFactory widgetFactory = new WidgetFactory(getContext());
            try {
                int type = scheme.getInt(Constants.FIELD_TYPE);
                int height = scheme.optInt(Constants.FIELD_HEIGHT, 70);
                if(height>0) height = widgetFactory.scale(height);
                backgroundColor = Constants.getColor2(scheme.optInt(Constants.FIELD_BACKGROUND, 8));
                ViewGroup.LayoutParams params = root.getLayoutParams();
                params.height = height;
                root.setLayoutParams(params);
                root.setOrientation(-1-type);
                root.getBackground().setColorFilter(new BlendModeColorFilter(backgroundColor, BlendMode.SRC_ATOP));
                JSONArray children = scheme.getJSONArray(Constants.FIELD_CHILDREN);
                for(int i = 0; i < children.length(); i++)
                    widgetFactory.loadView(children.getJSONObject(i), root, song, false);

                ImageView img = new ImageView(getContext());
                int n = widgetFactory.scale(48);
                int m = widgetFactory.scale(12);
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(n,n);
                p.gravity = Gravity.CENTER_VERTICAL;
                img.setColorFilter(getContext().getColor(R.color.FlatWhite));
                img.setPadding(m,m,m,m);
                img.setLayoutParams(p);
                img.setImageResource(R.drawable.ic_more_horiz_black_24dp);
                img.setOnClickListener( v -> onMenuItemClick(getDataPosition(getBindingAdapterPosition())));
                root.addView(img);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            bindTheme();
        }

        void bindTheme() {
            bindMediaPlayState();
        }



        private void bindMediaPlayState() {
            int dataPosition = getDataPosition(getBindingAdapterPosition());
            if (MusicPlayerRemote.getCurrentSong().id == getData().get(dataPosition).id) {
                mMediaPlayDataItem = dataPosition;
                root.getBackground().setColorFilter(new BlendModeColorFilter(Color.parseColor("#AA00FFFF"), BlendMode.SRC_ATOP));
            } else {
                root.getBackground().setColorFilter(new BlendModeColorFilter(backgroundColor, BlendMode.SRC_ATOP));
            }
        }

    }
}
