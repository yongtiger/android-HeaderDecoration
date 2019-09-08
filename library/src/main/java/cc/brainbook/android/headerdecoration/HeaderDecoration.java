package cc.brainbook.android.headerdecoration;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import cc.brainbook.android.headerdecoration.helper.HeaderHelper;
import cc.brainbook.android.headerdecoration.helper.HeaderCache;
import cc.brainbook.android.headerdecoration.util.AdapterUtil;
import cc.brainbook.android.headerdecoration.util.DimensionUtil;
import cc.brainbook.android.headerdecoration.interfaces.HeaderAdapter;
import cc.brainbook.android.headerdecoration.interfaces.ItemVisibilityAdapter;
import cc.brainbook.android.headerdecoration.util.HeaderUtil;
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
    private final HeaderCache mHeaderCache;
    private final HeaderHelper mHeaderHelper;
    private GridLayoutManager.SpanSizeLookup mSpanSizeLookup;

    /**
     * The following field is used as a buffer for internal calculations.
     * Its sole purpose is to avoid allocating NEW Rect every time we need one.
     */
    private final Rect mMarginRect = new Rect();

    public HeaderDecoration(HeaderAdapter<RecyclerView.ViewHolder> headerAdapter) {
        this(headerAdapter, null);
    }

    public HeaderDecoration(HeaderAdapter<RecyclerView.ViewHolder> headerAdapter,
                             ItemVisibilityAdapter visibilityAdapter) {
        this(headerAdapter,
                visibilityAdapter,
                new HeaderCache(headerAdapter));
    }

    private HeaderDecoration(HeaderAdapter headerAdapter,
                             ItemVisibilityAdapter visibilityAdapter,
                             HeaderCache headerViewCache) {
        this(headerAdapter,
                visibilityAdapter,
                headerViewCache,
                new HeaderHelper(headerAdapter, headerViewCache));
    }

    private HeaderDecoration(HeaderAdapter headerAdapter,
                             ItemVisibilityAdapter visibilityAdapter,
                             HeaderCache headerViewCache,
                             HeaderHelper headerPositionCalculator) {
        mHeaderAdapter = headerAdapter;
        mVisibilityAdapter = visibilityAdapter;
        mHeaderCache = headerViewCache;
        mHeaderHelper = headerPositionCalculator;

    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect,
                               @NonNull View view,
                               @NonNull final RecyclerView parent,
                               @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        final int itemPosition = parent.getChildAdapterPosition(view);
        if (itemPosition == RecyclerView.NO_POSITION) {
            return;
        }
        if (AdapterUtil.hasNewHeader(mHeaderAdapter, itemPosition, LayoutManagerUtil.getReverseLayout(parent))) {
            final View header = getHeaderView(parent, itemPosition);
            setItemOffsetsForHeader(outRect, header, LayoutManagerUtil.getOrientation(parent));
        }

        ///[GridLayoutManager]
        ///https://blog.csdn.net/qian520ao/article/details/76167193
        if (parent.getLayoutManager() instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) parent.getLayoutManager();
            if (gridLayoutManager != null) {
                if (mSpanSizeLookup == null) {
                    mSpanSizeLookup = new GridLayoutManager.SpanSizeLookup() {
                        @Override
                        public int getSpanSize(int position) {
                            ///每组header的最后一个元素要补齐span为占满整行（无论是否Grid reverse）
                            ///注意：无论是否Grid reverse，每行都是从左到右依次递增的！
                            final int headerItemPosition = LayoutManagerUtil.getReverseLayout(parent) ? position :
                                    AdapterUtil.getNextItemPosition((RecyclerView.Adapter) mHeaderAdapter, position, false);
                            if (AdapterUtil.hasNewHeader(mHeaderAdapter, headerItemPosition, LayoutManagerUtil.getReverseLayout(parent))) {
                                final int headerFirstChildPosition = mHeaderCache.getHeaderFirstChildPosition(position);
                                return gridLayoutManager.getSpanCount() - ((position - headerFirstChildPosition) % gridLayoutManager.getSpanCount());
//                                final int headerChildCount = mHeaderCache.getHeaderChildCount(position);
//                                return LayoutManagerUtil.getSpanCount(parent) - ((headerChildCount - 1) % LayoutManagerUtil.getSpanCount(parent));
                            } else {
                                return 1;
                            }
                        }
                    };
                    gridLayoutManager.setSpanSizeLookup(mSpanSizeLookup);
                }

                ///设置每组header的第一行所有元素的header偏移量
                setGridItemOffsetsForHeader(outRect, parent, gridLayoutManager, itemPosition);
            }
        }
    }

    /**
     * 设置每组header的第一行所有元素的header偏移量
     *
     * @param outRect           rectangle to define offsets for the item
     * @param recyclerView      the parent recycler view for drawing the header into
     * @param gridLayoutManager GridLayoutManager
     * @param position          of the list item in questions
     */
    private void setGridItemOffsetsForHeader(@NonNull Rect outRect,
                     @NonNull final RecyclerView recyclerView,
                     GridLayoutManager gridLayoutManager,
                     int position) {
        final int headerFirstChildPosition = mHeaderCache.getHeaderFirstChildPosition(position);
        if (LayoutManagerUtil.getReverseLayout(recyclerView)) {
            ///当Grid reverse时，获取header的第一行position，然后设置该行此后所有元素的header偏移量
            final int headerMultiple = (mHeaderCache.getHeaderChildCount(position) - 1) / gridLayoutManager.getSpanCount();
            final int headerLineFirstPosition = headerFirstChildPosition + gridLayoutManager.getSpanCount() * headerMultiple;
            if (position >= headerLineFirstPosition && position < headerLineFirstPosition + gridLayoutManager.getSpanCount()
                    && mHeaderAdapter.getHeaderId(position) == mHeaderAdapter.getHeaderId(headerLineFirstPosition)
                    && !AdapterUtil.hasNewHeader(mHeaderAdapter, position, LayoutManagerUtil.getReverseLayout(recyclerView))) {
                final View header = getHeaderView(recyclerView, headerLineFirstPosition);
                setItemOffsetsForHeader(outRect, header, LayoutManagerUtil.getOrientation(recyclerView));
            }
        } else {
            ///当Grid不是reverse时，获取header第一个元素的position，然后设置该行此后所有元素的header偏移量
            if (position > headerFirstChildPosition && position < headerFirstChildPosition + gridLayoutManager.getSpanCount()
                    && mHeaderAdapter.getHeaderId(position) == mHeaderAdapter.getHeaderId(headerFirstChildPosition)) {
                final View header = getHeaderView(recyclerView, headerFirstChildPosition);
                setItemOffsetsForHeader(outRect, header, LayoutManagerUtil.getOrientation(recyclerView));
            }
        }
    }

    /**
     * Sets the offsets for the first item in a section to make room for the header view
     *
     * @param itemOffsets   rectangle to define offsets for the item
     * @param header        view used to calculate offset for the item
     * @param orientation   used to calculate offset for the item
     */
    private void setItemOffsetsForHeader(Rect itemOffsets, View header, int orientation) {
        DimensionUtil.initMargins(mMarginRect, header);
        if (orientation == LinearLayoutManager.VERTICAL) {
            itemOffsets.top = header.getHeight() + mMarginRect.top + mMarginRect.bottom;
        } else {
            itemOffsets.left = header.getWidth() + mMarginRect.left + mMarginRect.right;
        }
    }

    @Override
    public void onDrawOver(@NonNull Canvas canvas,
                           @NonNull RecyclerView parent,
                           @NonNull RecyclerView.State state) {
        super.onDrawOver(canvas, parent, state);

        final int childCount = parent.getChildCount();
        if (childCount <= 0 || ((RecyclerView.Adapter) mHeaderAdapter).getItemCount() <= 0) {
            return;
        }

        ///[FIX#点击sticky header时position总为0]
        mHeaderCache.invalidateHeaderRects();

        for (int i = 0; i < childCount; i++) {
            final View itemView = parent.getChildAt(i);
            final int position = parent.getChildAdapterPosition(itemView);
            if (position == RecyclerView.NO_POSITION) {
                continue;
            }

            ///Note: hasStickyHeader will be true only at the first element in the recycler view.
            // Otherwise, when GridLayoutManager, it may cause multiple displays to be repeated.
            final boolean hasStickyHeader = i == (LayoutManagerUtil.getReverseLayout(parent) ? childCount - 1 : 0)
                    && mHeaderHelper.hasStickyHeader(itemView, LayoutManagerUtil.getOrientation(parent), position, isSticky);

            if (hasStickyHeader || AdapterUtil.hasNewHeader(mHeaderAdapter, position, LayoutManagerUtil.getReverseLayout(parent))) {
                final View header = mHeaderCache.getHeaderView(parent, position);
                final Rect headerOffset = mHeaderCache.getHeaderRect(position);
                mHeaderHelper.initHeaderBounds(headerOffset, parent, header, itemView, hasStickyHeader, isSticky);
                drawHeader(parent, canvas, header, headerOffset);
            }
        }
    }

    /**
     * Draws a header to a canvas, offsetting by some x and y amount
     *
     * @param recyclerView the parent recycler view for drawing the header into
     * @param canvas       the canvas on which to draw the header
     * @param header       the view to draw as the header
     * @param offset       a Rect used to define the x/y offset of the header.
     *                     Specify x/y offset by setting the {@link Rect#left} and {@link Rect#top} properties, respectively.
     */
    private void drawHeader(@NonNull RecyclerView recyclerView, @NonNull Canvas canvas, View header, Rect offset) {
        canvas.save();

        if (LayoutManagerUtil.getClipToPadding(recyclerView)) {
            // Clip drawing of headers to the padding of the RecyclerView. Avoids drawing in the padding
            HeaderUtil.initClipRectForHeader(mMarginRect, recyclerView, header);
            canvas.clipRect(mMarginRect);
        }

        canvas.translate(offset.left, offset.top);

        header.draw(canvas);
        canvas.restore();
    }

    public View getHeaderView(RecyclerView parent, int position) {
        return mHeaderCache.getHeaderView(parent, position);
    }

    public int findHeaderPositionUnder(int x, int y) {
        return mHeaderCache.findHeaderPositionUnder(x, y, mVisibilityAdapter);
    }

    /**
     * Invalidates cached headers.
     * This does not invalidate the recycler view, you should do that manually after calling this method.
     */
    public void invalidateHeaders() {
        mHeaderCache.invalidate();
    }

    ///[UPGRADE#handle click-event in RecyclerView.ItemDecoration]
    ///https://github.com/edubarr/header-decor/issues/36
    ///While the real header is scroll off the screen, the visible one is drawing on canvas directly ,not like a normal interactive widget.
    //
    //You have these options:
    //1) Override RecyclerView.onInterceptTouchEvent(), though with some invasiveness so I prefer the next one.
    //2) Make use of RecyclerView.addOnItemTouchListener(), remember the motion event argument has been translated into RecyclerView's coordinate system.
    //3) Use a real header view, but that will go a little far I think.
    /**
     * Checks if the view on the header is clicked
     *
     * @param parent the parent recycler view for drawing the header into
     * @param x : getX
     * @param y : getY
     * @param viewId : resource id of view you clicked.
     * @return
     */
    public boolean isViewClicked(RecyclerView parent, int x, int y, @IdRes int viewId) {
        final int position = findHeaderPositionUnder(x, y);
        if (position == RecyclerView.NO_POSITION) {
            return false;
        }

        final View itemView = getHeaderView(parent, position);
        if (itemView == null) {
            return false;
        }

        final View child = itemView.findViewById(viewId);
        if (child == null) {
            return false;
        }

        final Rect headerOffset = mHeaderCache.getHeaderRect(position);
        final Rect childRect = new Rect(
                child.getLeft() + headerOffset.left,
                child.getTop() + headerOffset.top,
                child.getRight() + headerOffset.left,
                child.getBottom() + headerOffset.top
        );

        return childRect.contains(x, y);
    }
}
