package cc.brainbook.android.headerdecoration;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;

import cc.brainbook.android.headerdecoration.caching.HeaderViewCache;
import cc.brainbook.android.headerdecoration.util.DimensionUtil;
import cc.brainbook.android.headerdecoration.interfaces.HeaderAdapter;
import cc.brainbook.android.headerdecoration.interfaces.ItemVisibilityAdapter;
import cc.brainbook.android.headerdecoration.rendering.HeaderRenderer;
import cc.brainbook.android.headerdecoration.util.LayoutManagerUtil;

public class HeaderDecoration extends RecyclerView.ItemDecoration {

    ///[isSticky]
    private boolean isSticky = true;
    public void isSticky(boolean isSticky) {
        this.isSticky = isSticky;
    }
    public boolean isSticky() {
        return this.isSticky;
    }

    private final HeaderAdapter mHeaderAdapter;
    private final ItemVisibilityAdapter mVisibilityAdapter;
    private final SparseArray<Rect> mHeaderRects = new SparseArray<>();
    private final HeaderViewCache mHeaderViewCache;
    private final HeaderPositionCalculator mHeaderPositionCalculator;
    private final HeaderRenderer mHeaderRenderer;


    /**
     * The following field is used as a buffer for internal calculations. Its sole purpose is to avoid
     * allocating new Rect every time we need one.
     */
    private final Rect mTempRect = new Rect();

    public HeaderDecoration(HeaderAdapter headerAdapter) {
        this(headerAdapter, null);
    }

    private HeaderDecoration(HeaderAdapter headerAdapter,
                             ItemVisibilityAdapter visibilityAdapter) {
        this(headerAdapter,
                visibilityAdapter,
                new HeaderRenderer(),
                new HeaderViewCache(headerAdapter));
    }

    private HeaderDecoration(HeaderAdapter headerAdapter,
                             ItemVisibilityAdapter visibilityAdapter,
                             HeaderRenderer headerRenderer,
                             HeaderViewCache headerViewCache) {
        this(headerAdapter,
                visibilityAdapter,
                headerRenderer,
                headerViewCache,
                new HeaderPositionCalculator(headerAdapter, headerViewCache));
    }

    private HeaderDecoration(HeaderAdapter headerAdapter,
                             ItemVisibilityAdapter visibilityAdapter,
                             HeaderRenderer headerRenderer,
                             HeaderViewCache headerViewCache,
                             HeaderPositionCalculator headerPositionCalculator) {
        mHeaderAdapter = headerAdapter;
        mVisibilityAdapter = visibilityAdapter;
        mHeaderRenderer = headerRenderer;
        mHeaderViewCache = headerViewCache;
        mHeaderPositionCalculator = headerPositionCalculator;

    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int itemPosition = parent.getChildAdapterPosition(view);
        if (itemPosition == RecyclerView.NO_POSITION) {
            return;
        }
        if (mHeaderPositionCalculator.hasNewHeader(itemPosition, LayoutManagerUtil.isReverseLayout(parent))) {
            View header = getHeaderView(parent, itemPosition);
            setItemOffsetsForHeader(outRect, header, LayoutManagerUtil.getOrientation(parent));
        }
    }

    /**
     * Sets the offsets for the first item in a section to make room for the header view
     *
     * @param itemOffsets rectangle to define offsets for the item
     * @param header      view used to calculate offset for the item
     * @param orientation used to calculate offset for the item
     */
    private void setItemOffsetsForHeader(Rect itemOffsets, View header, int orientation) {
        DimensionUtil.initMargins(mTempRect, header);
        if (orientation == LinearLayoutManager.VERTICAL) {
            itemOffsets.top = header.getHeight() + mTempRect.top + mTempRect.bottom;
        } else {
            itemOffsets.left = header.getWidth() + mTempRect.left + mTempRect.right;
        }
    }

    @Override
    public void onDrawOver(@NonNull Canvas canvas, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDrawOver(canvas, parent, state);

        final int childCount = parent.getChildCount();
        if (childCount <= 0 || ((RecyclerView.Adapter) mHeaderAdapter).getItemCount() <= 0) {
            return;
        }

        for (int i = 0; i < childCount; i++) {
            View itemView = parent.getChildAt(i);
            int position = parent.getChildAdapterPosition(itemView);
            if (position == RecyclerView.NO_POSITION) {
                continue;
            }

            boolean hasStickyHeader = mHeaderPositionCalculator.hasStickyHeader(itemView, LayoutManagerUtil.getOrientation(parent), position);
            if (hasStickyHeader || mHeaderPositionCalculator.hasNewHeader(position, LayoutManagerUtil.isReverseLayout(parent))) {
                View header = mHeaderViewCache.getHeader(parent, position);
                //re-use existing Rect, if any.
                Rect headerOffset = mHeaderRects.get(position);
                if (headerOffset == null) {
                    headerOffset = new Rect();
                    mHeaderRects.put(position, headerOffset);
                }
                mHeaderPositionCalculator.initHeaderBounds(headerOffset, parent, header, itemView, hasStickyHeader, isSticky);
                mHeaderRenderer.drawHeader(parent, canvas, header, headerOffset);
            }
        }
    }

    /**
     * Gets the position of the header under the specified (x, y) coordinates.
     *
     * @param x x-coordinate
     * @param y y-coordinate
     * @return position of header, or -1 if not found
     */
    public int findHeaderPositionUnder(int x, int y) {
        for (int i = 0; i < mHeaderRects.size(); i++) {
            Rect rect = mHeaderRects.get(mHeaderRects.keyAt(i));
            if (rect.contains(x, y)) {
                int position = mHeaderRects.keyAt(i);
                if (mVisibilityAdapter == null || mVisibilityAdapter.isPositionVisible(position)) {
                    return position;
                }
            }
        }
        return -1;
    }

    /**
     * Gets the header view for the associated position.  If it doesn't exist yet, it will be
     * created, measured, and laid out.
     *
     * @param parent the recyclerview
     * @param position the position to get the header view for
     * @return HeaderAdapter view
     */
    public View getHeaderView(RecyclerView parent, int position) {
        return mHeaderViewCache.getHeader(parent, position);
    }

    /**
     * Determines if an item in the list should have a header that is different than the item in the
     * list that immediately precedes it. Items with no headers will always return false.
     *
     * @param position of the list item in questions
     * @param isReverseLayout TRUE if layout manager has flag isReverseLayout
     * @return true if this item has a different header than the previous item in the list
     */
    public boolean hasNewHeader(int position, boolean isReverseLayout) {
        return mHeaderPositionCalculator.hasNewHeader(position, isReverseLayout);
    }

    /**
     * Invalidates cached headers.  This does not invalidate the recyclerview, you should do that manually after
     * calling this method.
     */
    public void invalidateHeaders() {
        mHeaderViewCache.invalidate();
        mHeaderRects.clear();
    }
}
