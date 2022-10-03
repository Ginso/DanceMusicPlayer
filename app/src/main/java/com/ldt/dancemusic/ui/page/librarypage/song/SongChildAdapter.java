package com.ldt.dancemusic.ui.page.librarypage.song;

import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.LinearLayout;

import com.ldt.dancemusic.R;
import com.ldt.dancemusic.contract.AbsBindAbleHolder;
import com.ldt.dancemusic.contract.AbsSongAdapter;

import com.ldt.dancemusic.helper.menu.SongMenuHelper;
import com.ldt.dancemusic.loader.medialoader.SongLoader;
import com.ldt.dancemusic.model.Song;
import com.ldt.dancemusic.service.MusicPlayerRemote;
import com.ldt.dancemusic.ui.bottomsheet.OptionBottomSheet;
import com.ldt.dancemusic.ui.page.subpages.FilterConfigurationFragment;
import com.ldt.dancemusic.util.Constants;
import com.ldt.dancemusic.util.WidgetFactory;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.widget.LinearLayout.HORIZONTAL;

public class SongChildAdapter extends AbsSongAdapter
        implements FastScrollRecyclerView.SectionedAdapter,
        FastScrollRecyclerView.MeasurableAdapter {

    private static final String TAG = "SongChildAdapter";

    public SongChildAdapter() {
        super();
    }

    public int mRandomItem = 0;
    private Random mRandom = new Random();
    public SortHolder sortHolder;

    @Override
    protected void onDataSet() {
        super.onDataSet();
        randomize();
    }

    public void destroy() {
        removeCallBack();
        super.destroy();
    }

    private int[] mOptionRes = SongMenuHelper.SONG_OPTION;

    @Override
    protected void onMenuItemClick(int positionInData) {
        OptionBottomSheet
                .newInstance(mOptionRes,getData().get(positionInData))
                .show(((AppCompatActivity)getContext()).getSupportFragmentManager(), "song_popup_menu");
    }

    @Override
    public int getItemViewType(int position) {
       if(position==0) return R.layout.item_sort_song_child;
       return R.layout.item_dancesong_bigger;
    }

    @Override
    protected int getMediaHolderPosition(int dataPosition) {
        return dataPosition + 1;
    }

    @Override
    protected int getDataPosition(int itemHolderPosition) {
        return itemHolderPosition - 1;
    }

    @Override
    public int getItemCount() {
        return getData().size() + 1;
    }


    @NotNull
    @Override
    public AbsBindAbleHolder onCreateViewHolder(@NotNull ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(viewType,viewGroup,false);

        switch(viewType) {
            case R.layout.item_sort_song_child:
                return new SortHolder(v);
            case R.layout.item_dancesong_bigger:
                return new DanceItemHolder(v);
            default:
                return new ItemHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NotNull AbsBindAbleHolder itemHolder, int position) {
        if(itemHolder instanceof SortHolder) {
            sortHolder = ((SortHolder) itemHolder);
            sortHolder.bind(null);
        } else
            itemHolder.bind(getData().get(getDataPosition(position)));
    }


    public void randomize() {
        if(getData().isEmpty()) return;
        mRandomItem = mRandom.nextInt(getData().size());
        if(mCallBack!=null) mCallBack.onFirstItemCreated(getData().get(mRandomItem));
    }

    public SongChildAdapter setCallBack(PreviewRandomPlayAdapter.FirstItemCallBack callBack) {
        mCallBack = callBack;
        return this;
    }

    public void removeCallBack() {
        mCallBack = null;
    }

    private PreviewRandomPlayAdapter.FirstItemCallBack mCallBack;

    public void shuffle() {
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            MusicPlayerRemote.openQueue(getData(), mRandomItem,true);
            //MusicPlayer.playAll(mContext, mSongIDs, mRandomItem, -1, Util.IdType.NA, false);
            Handler handler1 = new Handler() ;
            handler1.postDelayed(() -> {
                notifyItemChanged(getMediaHolderPosition(mMediaPlayDataItem));
                notifyItemChanged(getMediaHolderPosition(mRandomItem));
                mMediaPlayDataItem = mRandomItem;
                randomize();
            },50);
        },100);
    }


    @NonNull
    @Override
    public String getSectionName(int position) {
        if(position==0) return "A";
        if(getData().get(position-1).getTitle().isEmpty())
        return "A";
        return getData().get(position-1).getTitle().substring(0,1);
    }

    @Override
    public int getViewTypeHeight(RecyclerView recyclerView, int i) {
        return recyclerView.getResources().getDimensionPixelSize(R.dimen.item_song_child_height);
    }

    public class SortHolder extends AbsBindAbleHolder {
//        @BindView(R.id.sort_text) TextView mSortText;
//        @OnClick(R.id.sort_parent)
//        void sortClicked() {
//            sortHolderClicked();
//        }

        /*@BindView(R.id.sort_by_tpm) TextView mTPM;
        @OnClick(R.id.sort_by_tpm)
        void sortByTPM() {
            resetNames();
            int order = mSortOrderListener.getSavedOrder();
            if(order == 1) {
                mSortOrderListener.onOrderChanged(4, "");
                mTPM.setText("TPM ↓");
            } else {
                mSortOrderListener.onOrderChanged(1, "");
                mTPM.setText("TPM ↑");
            }
        }

        @BindView(R.id.sort_by_name) TextView mName;
        @OnClick(R.id.sort_by_name)
        void sortByName() {
            resetNames();
            int order = mSortOrderListener.getSavedOrder();
            if(order == 0) {
                mSortOrderListener.onOrderChanged(3, "");
                mName.setText("Name ↓");
            } else {
                mSortOrderListener.onOrderChanged(0, "");
                mName.setText("Name ↑");
            }
        }

        @BindView(R.id.sort_by_rating) TextView mRating;
        @OnClick(R.id.sort_by_rating)
        void sortByRating() {
            resetNames();
            int order = mSortOrderListener.getSavedOrder();
            if(order == 2) {
                mSortOrderListener.onOrderChanged(5, "");
                mRating.setText("Rating ↓");
            } else {
                mSortOrderListener.onOrderChanged(2, "");
                mRating.setText("Rating ↑");
            }
        }

        void resetNames() {
            mTPM.setText("TPM");
            mName.setText("Name");
            mRating.setText("Rating");
        }

        @BindView(R.id.takt) TextView mTaktView;
        @OnClick({R.id.takt})
        public void filterTakt(View v) {

            int takt = Integer.parseInt(mTaktView.getText().toString().substring(7));
            int rating = Integer.parseInt(mRatingView.getText().toString().substring(9));
            takt = (takt+1)%6;
            mTaktView.setText(String.format("Takt ≥ %d", takt));
            mSortOrderListener.onFilterChanged(takt, rating);
        }

        @BindView(R.id.rating) TextView mRatingView;
        @OnClick({R.id.rating})
        public void filterRating(View v) {

            int takt = Integer.parseInt(mTaktView.getText().toString().substring(7));
            int rating = Integer.parseInt(mRatingView.getText().toString().substring(9));
            rating = (rating+1)%6;
            mRatingView.setText(String.format("Rating ≥ %d", rating));
            mSortOrderListener.onFilterChanged(takt, rating);
        }*/

        @BindView(R.id.sortFilterHeaders) LinearLayout root;
        JSONArray headerLines;
        List<Pair<String, Boolean>> sort;

        WidgetFactory widgetFactory;
        public SortHolder(View view) {
            super(view);
            widgetFactory = new WidgetFactory(getContext());
            ButterKnife.bind(this,view);
            sort = new ArrayList<>();
        }


        @Override
        public void bind(Object object) {
            if(headerLines == null) {
                headerLines = FilterConfigurationFragment.getConfiguration();
                updateLines();
            }
            widgetFactory.focusCurrentEditText();
        }

        public void updateLines(JSONArray lines) {
            headerLines = lines;
            updateLines();
        }

        public void updateLines() {
            root.removeAllViews();
            try {
                for(int i = 0; i < headerLines.length(); i++) {
                    int finalI = i;
                    JSONArray line = headerLines.getJSONArray(i);
                    LinearLayout lineLayout = widgetFactory.createLinearLayout(MATCH_PARENT, WRAP_CONTENT,HORIZONTAL);
                    root.addView(lineLayout);
                    lineLayout.addView(widgetFactory.createFiller());
                    for(int j = 0; j < line.length(); j++) {
                        int finalJ = j;
                        final JSONObject o = line.getJSONObject(j);
                        View filterView = widgetFactory.createFilterView(o, (val, reload) -> {
                            boolean sort = setValue(finalI, finalJ, val);
                            updateSongs(sort);
                            if(reload) updateLines();
                        });
                        lineLayout.addView(filterView);
                        lineLayout.addView(widgetFactory.createFiller());
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        boolean setValue(int i, int j, Object val) {
            try {
                JSONArray line = headerLines.getJSONArray(i);
                JSONObject o = line.getJSONObject(j);
                if(val instanceof Integer[]) {
                    Integer[] arr = (Integer[])val;
                    o.put("value", arr[0]);
                    o.put("value2", arr[1]);
                } else if(val instanceof Double[]) {
                    Double[] arr = (Double[])val;
                    o.put("value", arr[0]);
                    o.put("value2", arr[1]);
                } else if(val instanceof Long[]) {
                    Long[] arr = (Long[])val;
                    o.put("value", arr[0]);
                    o.put("value2", arr[1]);
                } else {
                    o.put("value", val);
                    if (!o.getBoolean(Constants.FIELD_FILTER)) {
                        String tag = o.getString(Constants.FIELD_TAG);
                        for (Pair<String, Boolean> p : sort) {
                            if (p.first.equals(tag)) {
                                sort.remove(p);
                                break;
                            }
                        }
                        sort.add(new Pair<>(tag, (int) val == 1));
                        return true;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }

        public void updateSongs(boolean sortOnly) {
            try {
                Map<String, Song.Tag> tagMap = SongLoader.getAllTags();
                List<Song> songs = sortOnly ? getData() : getAllData();
                Stream<Song> stream = songs.stream();
                String sorter = sort.isEmpty() ? "" : sort.get(sort.size() - 1).first;
                for (int n = 0; n < headerLines.length(); n++) {
                    JSONArray arr = headerLines.getJSONArray(n);
                    for (int m = 0; m < arr.length(); m++) {
                        JSONObject obj = arr.getJSONObject(m);
                        String tagName = obj.getString(Constants.FIELD_TAG);
                        Song.Tag tag = tagMap.get(tagName);
                        if (obj.getBoolean(Constants.FIELD_FILTER)) {
                            if(!sortOnly) {
                                Object val1 = obj.opt("value");
                                Object val2 = obj.opt("value2");
                                if(val1 != null || val2 != null)
                                    stream = stream.filter(tag.getFilter(val1, val2));
                            }
                        } else {
                            if(!tagName.equals(sorter))
                                obj.put("value", 0);
                        }
                    }
                }

                for(Pair<String, Boolean> p:sort) {
                    if(!tagMap.containsKey(p.first)) continue;
                    Song.Tag tag = tagMap.get(p.first);
                    stream = stream.sorted(tag.getComparator(p.second));
                }
                modifyData(stream.collect(Collectors.toList()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }


    }

    public class ItemHolder extends AbsSongAdapter.SongHolder {
        public ItemHolder(View view) {
            super(view);
        }
    }

    public class DanceItemHolder extends AbsSongAdapter.DanceSongHolder {
        public DanceItemHolder(View view) {
            super(view);
        }
    }
}
