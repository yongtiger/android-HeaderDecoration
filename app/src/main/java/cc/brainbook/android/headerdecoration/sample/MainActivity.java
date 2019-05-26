package cc.brainbook.android.headerdecoration.sample;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.security.SecureRandom;

import cc.brainbook.android.headerdecoration.StickyRecyclerHeadersAdapter;
import cc.brainbook.android.headerdecoration.StickyRecyclerHeadersDecoration;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TAG";

    private Button mUpdateButton;
    private ToggleButton mIsReverseButton;
    private ToggleButton mIsShowHeaderButton;
    private ToggleButton mIsStickyButton;

    private RecyclerView mRecyclerView;
    private AnimalsHeadersAdapter mAdapter;
    private StickyRecyclerHeadersDecoration mHeadersDecor;

    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initRecyclerView();
    }

    private void initView() {
        // Set mUpdateButton to update all views one after another (Test for the "Dance")
        mUpdateButton = (Button) findViewById(R.id.button_update);
        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handler = new Handler(Looper.getMainLooper());
                for (int i = 0; i < mAdapter.getItemCount(); i++) {
                    final int index = i;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.notifyItemChanged(index);
                        }
                    }, 50);
                }
            }
        });

        ///[isReverse]
        mIsReverseButton = (ToggleButton) findViewById(R.id.button_is_reverse);
        mIsReverseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = mIsReverseButton.isChecked();
                mIsReverseButton.setChecked(isChecked);

                if (mLayoutManager instanceof GridLayoutManager) {
                    ((GridLayoutManager) mLayoutManager).setReverseLayout(isChecked);
                } else if (mLayoutManager instanceof LinearLayoutManager) {
                    ((LinearLayoutManager) mLayoutManager).setReverseLayout(isChecked);
                } else if (mLayoutManager instanceof StaggeredGridLayoutManager) {
                    ((StaggeredGridLayoutManager) mLayoutManager).setReverseLayout(isChecked);
                }

                mAdapter.notifyDataSetChanged();
            }
        });

        ///[isShowHeader]
        mIsShowHeaderButton = (ToggleButton) findViewById(R.id.button_is_show_header);
        mIsShowHeaderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = mIsShowHeaderButton.isChecked();
                mIsShowHeaderButton.setChecked(isChecked);

                if (isChecked) {
                    mRecyclerView.removeItemDecoration(mHeadersDecor);
                } else {
                    mRecyclerView.addItemDecoration(mHeadersDecor);
                }
            }
        });

        ///[isSticky]
        mIsStickyButton = (ToggleButton) findViewById(R.id.button_is_sticky);
        mIsStickyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = mIsStickyButton.isChecked();
                mIsStickyButton.setChecked(isChecked);

                if (isChecked) {
                    mHeadersDecor.isSticky(false);
                } else {
                    mHeadersDecor.isSticky(true);
                }
            }
        });
    }

    private void initRecyclerView() {
        // Set mAdapter populated with example dummy data
        mAdapter = new AnimalsHeadersAdapter();
        mAdapter.addAll(getDummyDataSet());

        // Set mRecyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mRecyclerView.setAdapter(mAdapter);

        // Set mRecyclerView layout manager
        int orientation = getLayoutManagerOrientation(getResources().getConfiguration().orientation);
        mLayoutManager = new LinearLayoutManager(this, orientation, mIsReverseButton.isChecked());
//        ///[GridLayoutManager]
//        mLayoutManager = new GridLayoutManager(this, 4, orientation, mIsReverseButton.isChecked());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Add header decoration
        mHeadersDecor = new StickyRecyclerHeadersDecoration(mAdapter);
//        ///[GridLayoutManager]StickyRecyclerGridHeadersDecoration
//        final StickyRecyclerGridHeadersDecoration mHeadersDecor = new StickyRecyclerGridHeadersDecoration(mAdapter, layoutManager.getSpanCount());
        mRecyclerView.addItemDecoration(mHeadersDecor);

        // Add divider decoration
        mRecyclerView.addItemDecoration(new DividerDecoration(this));

        //////?????[GridLayoutManager]StickyRecyclerGridHeadersDecoration
//        // Add touch listeners
//        StickyRecyclerHeadersTouchListener touchListener =
//                new StickyRecyclerHeadersTouchListener(mRecyclerView, mHeadersDecor);
//        touchListener.setOnHeaderClickListener(
//                new StickyRecyclerHeadersTouchListener.OnHeaderClickListener() {
//                    @Override
//                    public void onHeaderClick(View header, int position, long headerId) {
//                        Toast.makeText(MainActivity.this, "Header position: " + position + ", id: " + headerId,
//                                Toast.LENGTH_SHORT).show();
//                    }
//                });
//        mRecyclerView.addOnItemTouchListener(touchListener);
//        ///[Disable Header's OnItemTouchListener]
////        mRecyclerView.removeOnItemTouchListener(touchListener);
////        touchListener.setOnHeaderClickListener(null); ///alternative

        //Item's OnItemTouchListener]
        RecyclerItemClickListener itemClickListener = new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.d(TAG, "onItemClick: ");
                mAdapter.remove(mAdapter.getItem(position));
            }
        });
        mRecyclerView.addOnItemTouchListener(itemClickListener);
        ///[Disable Item's OnItemTouchListener]
//        mRecyclerView.removeOnItemTouchListener(itemClickListener);

        mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                mHeadersDecor.invalidateHeaders();
            }
        });

    }

    private String[] getDummyDataSet() {
        return getResources().getStringArray(R.array.animals);
    }

    private int getLayoutManagerOrientation(int activityOrientation) {
        if (activityOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            return LinearLayoutManager.VERTICAL;
        } else {
            return LinearLayoutManager.HORIZONTAL;
        }
    }

    private class AnimalsHeadersAdapter extends AnimalsAdapter<RecyclerView.ViewHolder>
            implements StickyRecyclerHeadersAdapter<RecyclerView.ViewHolder> {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_item, parent, false);
            return new RecyclerView.ViewHolder(view) {
            };
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            TextView textView = (TextView) holder.itemView;
            textView.setText(getItem(position));
        }

        @Override
        public long getHeaderId(int position) {
            if (position < 0) { ///[FIX#https://github.com/timehop/sticky-headers-recyclerview/issues/21]
                return -1;
            } else {
                return getItem(position).charAt(0);
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateHeaderViewHolder(ViewGroup parent) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_header, parent, false);
            return new RecyclerView.ViewHolder(view) {
            };
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int position) {
            TextView textView = (TextView) holder.itemView;
            textView.setText(String.valueOf(getItem(position).charAt(0)));
            holder.itemView.setBackgroundColor(getRandomColor());
        }

        private int getRandomColor() {
            SecureRandom rgen = new SecureRandom();
            return Color.HSVToColor(150, new float[]{
                    rgen.nextInt(359), 1, 1
            });
        }

    }
}

