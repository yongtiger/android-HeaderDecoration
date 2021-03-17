package cc.brainbook.android.headerdecoration.listener;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;

import cc.brainbook.android.headerdecoration.interfaces.HeaderAdapter;
import cc.brainbook.android.headerdecoration.HeaderDecoration;

public class HeaderTouchListener implements RecyclerView.OnItemTouchListener {
    private final GestureDetector mGestureDetector;
    private final RecyclerView mRecyclerView;
    private final HeaderDecoration mHeaderDecoration;
    private OnHeaderClickListener mOnHeaderClickListener;

    public interface OnHeaderClickListener {
        void onHeaderClick(View header, int position, long headerId);
    }

    public HeaderTouchListener(@NonNull final RecyclerView recyclerView,
                               final HeaderDecoration decor) {
        mGestureDetector = new GestureDetector(recyclerView.getContext(), new SingleTapDetector());
        mRecyclerView = recyclerView;
        mHeaderDecoration = decor;
    }

    public HeaderAdapter<RecyclerView.ViewHolder> getAdapter() {
        if (mRecyclerView.getAdapter() instanceof HeaderAdapter) {
            return (HeaderAdapter<RecyclerView.ViewHolder>) mRecyclerView.getAdapter();
        } else {
            throw new IllegalStateException("A RecyclerView with " +
                    HeaderTouchListener.class.getSimpleName() +
                    " requires a " + HeaderAdapter.class.getSimpleName());
        }
    }


    public void setOnHeaderClickListener(OnHeaderClickListener listener) {
        mOnHeaderClickListener = listener;
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView view, @NonNull MotionEvent e) {
        if (this.mOnHeaderClickListener != null) {
            boolean tapDetectorResponse = this.mGestureDetector.onTouchEvent(e);
            if (tapDetectorResponse) {
                // Don't return false if a single tap is detected
                return true;
            }
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                final int position = mHeaderDecoration.findHeaderPositionUnder((int)e.getX(), (int)e.getY());
                return position != -1;
            }
        }
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView view, @NonNull MotionEvent e) { /* do nothing? */ }

    @Override public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        // do nothing
    }

    private class SingleTapDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            final int position = mHeaderDecoration.findHeaderPositionUnder((int) e.getX(), (int) e.getY());
            if (position != -1) {
                final View headerView = mHeaderDecoration.getHeaderView(mRecyclerView, position);
                final long headerId = getAdapter().getHeaderId(position);
                mOnHeaderClickListener.onHeaderClick(headerView, position, headerId);
                mRecyclerView.playSoundEffect(SoundEffectConstants.CLICK);
                headerView.onTouchEvent(e);
                return true;
            }
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }
    }
}
