package cc.brainbook.android.headerdecoration.helper;

import android.graphics.Rect;
import android.support.v4.util.LongSparseArray;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import cc.brainbook.android.headerdecoration.interfaces.HeaderAdapter;
import cc.brainbook.android.headerdecoration.interfaces.ItemVisibilityAdapter;
import cc.brainbook.android.headerdecoration.util.LayoutManagerUtil;

public class HeaderCache {

    private final HeaderAdapter<RecyclerView.ViewHolder> mHeaderAdapter;
    private final LongSparseArray<View> mHeaderViews = new LongSparseArray<>();
    ///[UPGRADE#getHeaderViews()]
    public LongSparseArray<View> getHeaderViews() {
        return mHeaderViews;
    }
    private final LongSparseArray<Integer> mHeaderFirstChildPosition = new LongSparseArray<>();
    private final LongSparseArray<Integer> mHeaderChildCount = new LongSparseArray<>();
    private final SparseArray<Rect> mHeaderRects = new SparseArray<>();

    public HeaderCache(HeaderAdapter<RecyclerView.ViewHolder> adapter) {
        mHeaderAdapter = adapter;
    }

    /**
     * Will provide a header view for a given position in the RecyclerView
     *
     * @param recyclerView  that will display the header
     * @param position      that will be headed by the header
     * @return              a header view for the given position and list
     */
    public View getHeaderView(RecyclerView recyclerView, int position) {
        final long headerId = mHeaderAdapter.getHeaderId(position);

        View header = mHeaderViews.get(headerId);
        if (header == null) {
            //TODO - recycle views
            RecyclerView.ViewHolder viewHolder = mHeaderAdapter.onCreateHeaderViewHolder(recyclerView);
            mHeaderAdapter.onBindHeaderViewHolder(viewHolder, position);
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
     * 获取给定position的header组的元素总数
     * Re-use existing position, if any.
     *
     * @param position      the given position of header
     * @return              the child count of header
     */
    public int getHeaderChildCount(int position) {
        final long headerId = mHeaderAdapter.getHeaderId(position);

        Integer headerChildCount = mHeaderChildCount.get(headerId);
        if (headerChildCount == null) {
            final int headerFirstChildPosition = getHeaderFirstChildPosition(position);
            headerChildCount = position - headerFirstChildPosition + 1;

            for (int i = position + 1; i < ((RecyclerView.Adapter)mHeaderAdapter).getItemCount()
                    && mHeaderAdapter.getHeaderId(i) == headerId; i++) {
                headerChildCount++;
            }

            mHeaderChildCount.put(headerId, headerChildCount);
        }

        return headerChildCount;
    }

    /**
     * 获取给定position的header组的第一个元素的位置
     * Re-use existing position, if any.
     *
     * @param position      the given position of header
     * @return              the first child position of header
     */
    public int getHeaderFirstChildPosition(int position) {
        final long headerId = mHeaderAdapter.getHeaderId(position);

        Integer headerFirstChildPosition = mHeaderFirstChildPosition.get(headerId);
        if (headerFirstChildPosition == null) {
            headerFirstChildPosition = position;
            for (int i = position - 1; i >= 0 && mHeaderAdapter.getHeaderId(i) == headerId; i--) {
                headerFirstChildPosition--;
            }

            ///保存当前header组的第一个元素的position
            mHeaderFirstChildPosition.put(headerId, headerFirstChildPosition);
        }

        return headerFirstChildPosition;
    }

    /**
     * Will provide a header Rect for a given position in the RecyclerView.
     * Re-use existing Rect, if any.
     *
     * @param position      position of header
     * @return              A header Rect
     */
    public Rect getHeaderRect(int position) {
        Rect headerOffset = mHeaderRects.get(position);
        if (headerOffset == null) {
            headerOffset = new Rect();
            mHeaderRects.put(position, headerOffset);
        }
        return headerOffset;
    }

    /**
     * Gets the position of the header under the specified (x, y) coordinates.
     *
     * @param x         x-coordinate
     * @param y         y-coordinate
     * @return          position of header, or -1 if not found
     */
    public int findHeaderPositionUnder(int x, int y, ItemVisibilityAdapter visibilityAdapter) {
        for (int i = 0; i < mHeaderRects.size(); i++) {
            final Rect rect = mHeaderRects.get(mHeaderRects.keyAt(i));
            if (rect.contains(x, y)) {
                final int position = mHeaderRects.keyAt(i);
                if (visibilityAdapter == null || visibilityAdapter.isPositionVisible(position)) {
                    return position;
                }
            }
        }
        return RecyclerView.NO_POSITION;
    }

    /**
     * Invalidates cached header Rects.
     */
    public void invalidateHeaderRects() {
        mHeaderRects.clear();
    }

    /**
     * Invalidates cached header views.
     */
    public void invalidate() {
        mHeaderViews.clear();
        mHeaderChildCount.clear();
        mHeaderFirstChildPosition.clear();
    }
}
