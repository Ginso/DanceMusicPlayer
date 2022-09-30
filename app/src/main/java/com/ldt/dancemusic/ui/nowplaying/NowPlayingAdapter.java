package com.ldt.dancemusic.ui.nowplaying;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ldt.dancemusic.R;
import com.ldt.dancemusic.loader.medialoader.SongLoader;
import com.ldt.dancemusic.model.Song;
import com.ldt.dancemusic.util.NavigationUtil;
import com.ldt.dancemusic.util.WidgetFactory;
import com.squareup.picasso.Callback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NowPlayingAdapter extends RecyclerView.Adapter<NowPlayingAdapter.ItemHolder> implements Callback {
    private static final String TAG ="NowPlayingAdapter";
    private ArrayList<Song> mData = new ArrayList<>();
    private Context mContext;
    public Activity mActivity;
    public Context getContext() {
        return mContext;
    }
    public void init(Context context) {
        mContext = context;
    }

    public void setData(List<Song> data) {
        if(mData.equals(data)) {
            Log.d(TAG, "setData: equal");
            return;
        }
        mData.clear();
        if(data!=null) {
            mData.addAll(data);
        }
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_art_now_playing,viewGroup,false);
        return new ItemHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHolder itemHolder, int i) {
        itemHolder.bind(mData.get(i));
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onError(Exception e) {

    }
    TextView TPMCounter;
    TextView counterView;
    Calendar firtClick = null;
    int counter;

    public class ItemHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.container)
        LinearLayout mContainer;

        public ItemHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
        private void bind(Song song) {

            WidgetFactory widgetFactory = new WidgetFactory(getContext());
            widgetFactory.defaultTextSize = 24;
            widgetFactory.loadTags(mContainer, song, s -> {
                SongLoader.save(s);
                NavigationUtil.updateSongs(mActivity);
            });
            mContainer.addView(widgetFactory.modifyParams(widgetFactory.createTextView("TPM/BPM Counter", 22),p -> p.gravity = Gravity.CENTER));
            mContainer.addView(widgetFactory.modifyParams(widgetFactory.createTextView("Tap on the every beat to count BPM or the first of every takt for TPM:", 22),p -> p.gravity = Gravity.CENTER));


            int size = widgetFactory.scale(100);
            LinearLayout layout = widgetFactory.createLinearLayout(size,size,LinearLayout.VERTICAL);
            layout.setBackgroundColor(mContext.getColor(R.color.itemBackground));
            layout.addView(widgetFactory.createFillerVertical());
            TPMCounter = widgetFactory.modifyParams(widgetFactory.createTextView("0", 26),p -> p.gravity = Gravity.CENTER);
            layout.setOnClickListener(v -> {
                Calendar now = Calendar.getInstance();
                if(firtClick == null) {
                    firtClick = now;
                    counter = 0;
                } else {
                    counter++;
                    long l = now.getTimeInMillis() - firtClick.getTimeInMillis();
                    double tpm = counter*60000.0 / l;
                    TPMCounter.setText(String.format("%.1f", tpm));
                }
                counterView.setText((counter+1) + " Taps");
            });
            layout.addView(TPMCounter);
            counterView = widgetFactory.modifyParams(widgetFactory.createTextView("0 Taps", 16),p -> p.gravity = Gravity.CENTER);
            layout.addView(counterView);

            layout.addView(widgetFactory.createFillerVertical());
            mContainer.addView(layout);
            mContainer.addView(widgetFactory.createButton("reset",v->{
                firtClick = null;
                counter = 0;
                TPMCounter.setText("0");
                counterView.setText("0 Taps");
            }));
        }
    }
}
