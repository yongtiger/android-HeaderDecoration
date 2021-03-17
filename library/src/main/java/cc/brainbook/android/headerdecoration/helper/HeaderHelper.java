package cc.brainbook.android.headerdecoration.helper;

import android.graphics.Rect;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import cc.brainbook.android.headerdecoration.util.AdapterUtil;
import cc.brainbook.android.headerdecoration.util.DimensionUtil;
import cc.brainbook.android.headerdecoration.interfaces.HeaderAdapter;
import cc.brainbook.android.headerdecoration.util.LayoutManagerUtil;

/**
 * Calculates the position and location of header views
 */
public class HeaderHelper {

    private final HeaderAdapter<RecyclerView.ViewHolder> mHeaderAdapter;
    private final HeaderCache mHeaderCache;

    /**
     * The following fields are used as buffers for internal calculations. Their sole purpose is to avoid
     * allocating NEW Rect every time we need one.
     */
    private final Rect mMarginRect1 = new Rect();
    private final Rect mMarginRect2 = new Rect();

    public HeaderHelper(HeaderAdapter<RecyclerView.ViewHolder> headerAdapter, HeaderCache headerCache) {
        mHeaderAdapter = headerAdapter;
        mHeaderCache = headerCache;
    }

    /**
     * Determines if a view should have a sticky header.
     * The view has a sticky header if:
     * 1. It is the first element in the recycler view
     * 2. It has a valid ID associated to its position
     *
     * @param itemView      given by the RecyclerView
     * @param orientation   of the RecyclerView
     * @param position      of the list item in question
     * @param isSticky      if or not sticky
     * @return              True if the view should have a sticky header
     */
    public boolean hasStickyHeader(View itemView, int orientation, int position, boolean isSticky) {
        ///[isSticky]
        if (!isSticky) {
            return false;
        }

        ///[FIX#当header只有一行时，上顶sticky header过程中出现跳跃]
//        int offset, margin;
//        DimensionUtil.initMargins(mMarginRect1, itemView);
//        if (orientation == LinearLayout.VERTICAL) {
//            offset = itemView.getTop();
//            margin = mMarginRect1.top;
//        } else {
//            offset = itemView.getLeft();
//            margin = mMarginRect1.left;
//        }
//        return offset <= margin && mHeaderAdapter.getHeaderId(position) >= 0;
        return mHeaderAdapter.getHeaderId(position) >= 0;
    }

    public void initHeaderBounds(Rect bounds, RecyclerView recyclerView, View header, View firstView, boolean firstHeader, boolean isSticky) {
        final int orientation = LayoutManagerUtil.getOrientation(recyclerView);
        initDefaultHeaderOffset(bounds, recyclerView, header, firstView, orientation, isSticky);

        if (firstHeader && isStickyHeaderBeingPushedOffscreen(recyclerView, header)) {
            final View viewAfterNextHeader = getFirstViewUnObscuredByHeader(recyclerView, header);
            if (viewAfterNextHeader != null) {
                final int firstViewUnderHeaderPosition = recyclerView.getChildAdapterPosition(viewAfterNextHeader);
                final View secondHeader = mHeaderCache.getHeaderView(recyclerView, firstViewUnderHeaderPosition);
                translateHeaderWithNextHeader(recyclerView, LayoutManagerUtil.getOrientation(recyclerView), bounds,
                        header, viewAfterNextHeader, secondHeader);
            }
        }
    }

    private void initDefaultHeaderOffset(Rect headerMargins, RecyclerView recyclerView, View header, @NonNull View firstView, int orientation, boolean isSticky) {
        int translationX, translationY;
        DimensionUtil.initMargins(mMarginRect1, header);

        ViewGroup.LayoutParams layoutParams = firstView.getLayoutParams();
        int leftMargin = 0;
        int topMargin = 0;
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
            leftMargin = marginLayoutParams.leftMargin;
            topMargin = marginLayoutParams.topMargin;
        }

        if (orientation == LinearLayoutManager.VERTICAL) {
            ///[FIX#当Grid reverse时，如果firstView不在最左面，其header也不在最左面，从而导致header不满行显示]
            ///header从左到右占满整行，不依赖firstView！（firstView通常在最左面，但Grid reverse时firstView不一定是最左面！）
//            translationX = firstView.getLeft() - leftMargin + mMarginRect1.left;
            translationX = LayoutManagerUtil.getListLeft(recyclerView) + mMarginRect1.left;

            ///[isSticky]
            if (isSticky) {
                translationY = Math.max(
                        firstView.getTop() - topMargin - header.getHeight() - mMarginRect1.bottom,
                        LayoutManagerUtil.getListTop(recyclerView) + mMarginRect1.top);
            } else {
                translationY = firstView.getTop() - topMargin - header.getHeight() - mMarginRect1.bottom;
            }
        } else {
            ///[FIX#当Grid reverse时，如果firstView不在最左面，其header也不在最左面，从而导致header不满行显示]
            ///header从左到右占满整行，不依赖firstView！（firstView通常在最左面，但Grid reverse时firstView不一定是最左面！）
//            translationY = firstView.getTop() - topMargin + mMarginRect1.top;
            translationY = LayoutManagerUtil.getListTop(recyclerView) + mMarginRect1.top;

            ///[isSticky]
            if (isSticky) {
                translationX = Math.max(
                        firstView.getLeft() - leftMargin - header.getWidth() - mMarginRect1.right,
                        LayoutManagerUtil.getListLeft(recyclerView) + mMarginRect1.left);
            } else {
                translationX = firstView.getLeft() - leftMargin - header.getWidth() - mMarginRect1.right;
            }
        }

        headerMargins.set(translationX, translationY, translationX + header.getWidth(),
                translationY + header.getHeight());
    }

    private boolean isStickyHeaderBeingPushedOffscreen(RecyclerView recyclerView, View stickyHeader) {
        final View viewAfterHeader = getFirstViewUnObscuredByHeader(recyclerView, stickyHeader);
        if (viewAfterHeader == null) {
            return false;
        }
        final int firstViewUnderHeaderPosition = recyclerView.getChildAdapterPosition(viewAfterHeader);
        if (firstViewUnderHeaderPosition == RecyclerView.NO_POSITION) {
            return false;
        }

        if (firstViewUnderHeaderPosition > 0
                && AdapterUtil.hasNewHeader(mHeaderAdapter, firstViewUnderHeaderPosition, LayoutManagerUtil.getReverseLayout(recyclerView))) {
            final View nextHeader = mHeaderCache.getHeaderView(recyclerView, firstViewUnderHeaderPosition);
            DimensionUtil.initMargins(mMarginRect1, nextHeader);
            DimensionUtil.initMargins(mMarginRect2, stickyHeader);

            if (LayoutManagerUtil.getOrientation(recyclerView) == LinearLayoutManager.VERTICAL) {
                final int topOfNextHeader = viewAfterHeader.getTop() - mMarginRect1.bottom - nextHeader.getHeight() - mMarginRect1.top;
                final int bottomOfThisHeader = recyclerView.getPaddingTop() + stickyHeader.getBottom() + mMarginRect2.top + mMarginRect2.bottom;
                return topOfNextHeader < bottomOfThisHeader;
            } else {
                final int leftOfNextHeader = viewAfterHeader.getLeft() - mMarginRect1.right - nextHeader.getWidth() - mMarginRect1.left;
                final int rightOfThisHeader = recyclerView.getPaddingLeft() + stickyHeader.getRight() + mMarginRect2.left + mMarginRect2.right;
                return leftOfNextHeader < rightOfThisHeader;
            }
        }

        return false;
    }

    private void translateHeaderWithNextHeader(RecyclerView recyclerView, int orientation, Rect translation,
                                               View currentHeader, View viewAfterNextHeader, View nextHeader) {
        DimensionUtil.initMargins(mMarginRect1, nextHeader);
        DimensionUtil.initMargins(mMarginRect2, currentHeader);
        if (orientation == LinearLayoutManager.VERTICAL) {
            final int topOfStickyHeader = LayoutManagerUtil.getListTop(recyclerView) + mMarginRect2.top + mMarginRect2.bottom;
            final int shiftFromNextHeader = viewAfterNextHeader.getTop() - nextHeader.getHeight() - mMarginRect1.bottom - mMarginRect1.top - currentHeader.getHeight() - topOfStickyHeader;
            if (shiftFromNextHeader < topOfStickyHeader) {
                translation.top += shiftFromNextHeader;
                ///[FIX#点击sticky header时position总为0]
                translation.bottom += shiftFromNextHeader;
            }
        } else {
            final int leftOfStickyHeader = LayoutManagerUtil.getListLeft(recyclerView) + mMarginRect2.left + mMarginRect2.right;
            final int shiftFromNextHeader = viewAfterNextHeader.getLeft() - nextHeader.getWidth() - mMarginRect1.right - mMarginRect1.left - currentHeader.getWidth() - leftOfStickyHeader;
            if (shiftFromNextHeader < leftOfStickyHeader) {
                translation.left += shiftFromNextHeader;
                ///[FIX#点击sticky header时position总为0]
                translation.right += shiftFromNextHeader;
            }
        }
    }

    /**
     * Returns the first item currently in the RecyclerView that is not obscured by a header.
     *
     * @param parent        RecyclerView containing all the list items
     * @return first        item that is fully beneath a header
     */
    @Nullable
    private View getFirstViewUnObscuredByHeader(@NonNull RecyclerView parent, @NonNull View firstHeader) {
        final int step = LayoutManagerUtil.getReverseLayout(parent) ? -1 : 1;
        final int from = LayoutManagerUtil.getReverseLayout(parent) ? parent.getChildCount() - 1 : 0;
        for (int i = from; i >= 0 && i <= parent.getChildCount() - 1; i += step) {
            View child = parent.getChildAt(i);
            if (!itemIsObscuredByHeader(parent, child, firstHeader, LayoutManagerUtil.getOrientation(parent))) {
                return child;
            }
        }
        return null;
    }

    /**
     * Determines if an item is obscured by a header
     *
     * @param parent        RecyclerView containing all the list items
     * @param item          to determine if obscured by header
     * @param header        that might be obscuring the item
     * @param orientation   of the {@link RecyclerView}
     * @return true         if the item view is obscured by the header view
     */
    private boolean itemIsObscuredByHeader(@NonNull RecyclerView parent, @NonNull View item, View header, int orientation) {
        final RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) item.getLayoutParams();
        DimensionUtil.initMargins(mMarginRect1, header);

        final int adapterPosition = parent.getChildAdapterPosition(item);
        if (adapterPosition == RecyclerView.NO_POSITION || mHeaderCache.getHeaderView(parent, adapterPosition) != header) {
            // Resolves https://github.com/timehop/sticky-headers-recyclerview/issues/36
            // Handles an edge case where a trailing header is smaller than the current sticky header.
            return false;
        }

        if (orientation == LinearLayoutManager.VERTICAL) {
            final int itemTop = item.getTop() - layoutParams.topMargin;
            final int headerBottom = LayoutManagerUtil.getListTop(parent) + header.getBottom() + mMarginRect1.bottom + mMarginRect1.top;
            return itemTop < headerBottom;
        } else {
            final int itemLeft = item.getLeft() - layoutParams.leftMargin;
            final int headerRight = LayoutManagerUtil.getListLeft(parent) + header.getRight() + mMarginRect1.right + mMarginRect1.left;
            return itemLeft < headerRight;
        }
    }

}
