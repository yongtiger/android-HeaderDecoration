package cc.brainbook.android.headerdecoration.helper;

import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import cc.brainbook.android.headerdecoration.interfaces.HeaderAdapter;
import cc.brainbook.android.headerdecoration.util.LayoutManagerUtil;

public class HeaderViewCache {

    private final HeaderAdapter mAdapter;
    private final LongSparseArray<View> mHeaderViews = new LongSparseArray<>();

    public HeaderViewCache(HeaderAdapter adapter) {
        mAdapter = adapter;
    }

    /**
     * Will provide a header view for a given position in the RecyclerView
     *
     * @param recyclerView that will display the header
     * @param position     that will be headed by the header
     * @return a header view for the given position and list
     */
    public View getHeader(RecyclerView recyclerView, int position) {
        final long headerId = mAdapter.getHeaderId(position);

        View header = mHeaderViews.get(headerId);
        if (header == null) {
            //TODO - recycle views
            RecyclerView.ViewHolder viewHolder = mAdapter.onCreateHeaderViewHolder(recyclerView);
            mAdapter.onBindHeaderViewHolder(viewHolder, position);
            header = viewHolder.itemView;
            if (header.getLayoutParams() == null) {
                header.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }

            int widthSpec;
            int heightSpec;

            if (LayoutManagerUtil.getOrientation(recyclerView) == LinearLayoutManager.VERTICAL) {
                widthSpec = View.MeasureSpec.makeMeasureSpec(recyclerView.getWidth(), View.MeasureSpec.EXACTLY);
                heightSpec = View.MeasureSpec.makeMeasureSpec(recyclerView.getHeight(), View.MeasureSpec.UNSPECIFIED);
            } else {
                widthSpec = View.MeasureSpec.makeMeasureSpec(recyclerView.getWidth(), View.MeasureSpec.UNSPECIFIED);
                heightSpec = View.MeasureSpec.makeMeasureSpec(recyclerView.getHeight(), View.MeasureSpec.EXACTLY);
            }

            final int childWidth = ViewGroup.getChildMeasureSpec(widthSpec,
                    recyclerView.getPaddingLeft() + recyclerView.getPaddingRight(), header.getLayoutParams().width);
            final int childHeight = ViewGroup.getChildMeasureSpec(heightSpec,
                    recyclerView.getPaddingTop() + recyclerView.getPaddingBottom(), header.getLayoutParams().height);
            header.measure(childWidth, childHeight);
            header.layout(0, 0, header.getMeasuredWidth(), header.getMeasuredHeight());
            mHeaderViews.put(headerId, header);
        }
        return header;
    }

    /**
     * Invalidates cached header views.
     */
    public void invalidate() {
        mHeaderViews.clear();
    }
}
