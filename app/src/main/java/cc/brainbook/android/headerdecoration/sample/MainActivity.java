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
import android.widget.Toast;
import android.widget.ToggleButton;

import java.security.SecureRandom;

import cc.brainbook.android.headerdecoration.interfaces.HeaderAdapter;
import cc.brainbook.android.headerdecoration.HeaderDecoration;
import cc.brainbook.android.headerdecoration.listener.HeaderTouchListener;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TAG";

    private Button mUpdateButton;
    private ToggleButton mReverseToggleButton;
    private ToggleButton mShowHeaderToggleButton;
    private ToggleButton mStickyToggleButton;

    private RecyclerView mRecyclerView;
    private AnimalsHeadersAdapter mAnimalsHeadersAdapter;
    private HeaderDecoration mHeaderDecoration;

    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initRecyclerView();
    }

    private void initView() {
        // Set update button to update all views one after another (Test for the "Dance")
        mUpdateButton = (Button) findViewById(R.id.btn_update);
        mUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Handler handler = new Handler(Looper.getMainLooper());
                for (int i = 0; i < mAnimalsHeadersAdapter.getItemCount(); i++) {
                    final int index = i;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mAnimalsHeadersAdapter.notifyItemChanged(index);
                        }
                    }, 50);
                }
            }
        });

        ///[isReverse]
        mReverseToggleButton = (ToggleButton) findViewById(R.id.tb_reverse);
        mReverseToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = mReverseToggleButton.isChecked();
                mReverseToggleButton.setChecked(isChecked);

                if (mLayoutManager instanceof GridLayoutManager) {
                    ((GridLayoutManager) mLayoutManager).setReverseLayout(isChecked);
                } else if (mLayoutManager instanceof LinearLayoutManager) {
                    ((LinearLayoutManager) mLayoutManager).setReverseLayout(isChecked);
                } else if (mLayoutManager instanceof StaggeredGridLayoutManager) {
                    ((StaggeredGridLayoutManager) mLayoutManager).setReverseLayout(isChecked);
                }

                mAnimalsHeadersAdapter.notifyDataSetChanged();
            }
        });

        ///[isShowHeader]
        mShowHeaderToggleButton = (ToggleButton) findViewById(R.id.tb_show_header);
        mShowHeaderToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = mShowHeaderToggleButton.isChecked();
                mShowHeaderToggleButton.setChecked(isChecked);

                if (isChecked) {
                    mRecyclerView.removeItemDecoration(mHeaderDecoration);
                } else {
                    mRecyclerView.addItemDecoration(mHeaderDecoration);
                }
            }
        });

        ///[isSticky]
        mStickyToggleButton = (ToggleButton) findViewById(R.id.tb_sticky);
        mStickyToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isChecked = mStickyToggleButton.isChecked();
                mStickyToggleButton.setChecked(isChecked);

                if (isChecked) {
                    mHeaderDecoration.isSticky(false);
                } else {
                    mHeaderDecoration.isSticky(true);
                }
            }
        });
    }

    private void initRecyclerView() {
        // Set adapter populated with example dummy data
        mAnimalsHeadersAdapter = new AnimalsHeadersAdapter();
        mAnimalsHeadersAdapter.addAll(getDummyDataSet());

        // Set recyclerView
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mRecyclerView.setAdapter(mAnimalsHeadersAdapter);

        // Set recyclerView layout manager
        int orientation = getLayoutManagerOrientation(getResources().getConfiguration().orientation);
        mLayoutManager = new LinearLayoutManager(this, orientation, mReverseToggleButton.isChecked());
//        ///[GridLayoutManager]
//        mLayoutManager = new GridLayoutManager(this, 4, orientation, mReverseToggleButton.isChecked());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Add header decoration
        mHeaderDecoration = new HeaderDecoration(mAnimalsHeadersAdapter);
//        ///[GridLayoutManager]StickyRecyclerGridHeadersDecoration
//        final StickyRecyclerGridHeadersDecoration mHeaderDecoration = new StickyRecyclerGridHeadersDecoration(mAnimalsHeadersAdapter, layoutManager.getSpanCount());
        mRecyclerView.addItemDecoration(mHeaderDecoration);

        // Add divider decoration
        mRecyclerView.addItemDecoration(new DividerDecoration(this));

        //////?????[GridLayoutManager]StickyRecyclerGridHeadersDecoration
        // Add touch listeners
        HeaderTouchListener headerTouchListener =
                new HeaderTouchListener(mRecyclerView, mHeaderDecoration);
        headerTouchListener.setOnHeaderClickListener(
                new HeaderTouchListener.OnHeaderClickListener() {
                    @Override
                    public void onHeaderClick(View header, int position, long headerId) {
                        Toast.makeText(MainActivity.this, "HeaderAdapter position: " + position + ", id: " + headerId,
                                Toast.LENGTH_SHORT).show();
                    }
                });
        mRecyclerView.addOnItemTouchListener(headerTouchListener);
        ///[Disable HeaderAdapter's OnItemTouchListener]
//        mRecyclerView.removeOnItemTouchListener(headerTouchListener);
//        touchListener.setOnHeaderClickListener(null); ///alternative

        //Item's OnItemTouchListener]
        ItemTouchListener itemTouchListener = new ItemTouchListener(this, new ItemTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.d(TAG, "onItemClick: ");
                mAnimalsHeadersAdapter.remove(mAnimalsHeadersAdapter.getItem(position));
            }
        });
        mRecyclerView.addOnItemTouchListener(itemTouchListener);
        ///[Disable Item's OnItemTouchListener]
//        mRecyclerView.removeOnItemTouchListener(itemTouchListener);

        mAnimalsHeadersAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                mHeaderDecoration.invalidateHeaders();
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
            implements HeaderAdapter<RecyclerView.ViewHolder> {
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
            if (position < 0) {
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

