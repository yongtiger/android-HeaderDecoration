package cc.brainbook.android.headerdecoration.util;

import androidx.recyclerview.widget.RecyclerView;

import cc.brainbook.android.headerdecoration.interfaces.HeaderAdapter;

public abstract class AdapterUtil {

    /**
     * Determines if an item in the list should have a header that is different than the item in the
     * list that immediately precedes it. Items with no headers will always return false.
     *
     * @param headerAdapter     HeaderAdapter
     * @param position          of the list item in questions
     * @param isReverseLayout   TRUE if layout manager has flag getReverseLayout
     * @return                  TRUE if this item has a different header than the previous item in the list
     */
    public static boolean hasNewHeader(HeaderAdapter headerAdapter, int position, boolean isReverseLayout) {
        if (indexOutOfBounds((RecyclerView.Adapter) headerAdapter, position)) {
            return false;
        }

        final long headerId = headerAdapter.getHeaderId(position);
        if (headerId < 0) {
            return false;
        }

        long prevItemHeaderId = getPrevItemHeaderId(headerAdapter, position, isReverseLayout);
        final int firstItemPosition = getFirstItemPosition((RecyclerView.Adapter) headerAdapter, isReverseLayout);

        ///[FIX#Reverse时下拉到顶，最上面元素（即最后一个position）的header不显示]
        if (isReverseLayout && position == firstItemPosition) {
            return false;
        }

        return position == firstItemPosition || headerId != prevItemHeaderId;
    }

    public static long getPrevItemHeaderId(HeaderAdapter headerAdapter, int position, boolean isReverseLayout) {
        long prevItemHeaderId = -1;
        final int prevItemPosition = getPrevItemPosition((RecyclerView.Adapter) headerAdapter, position, isReverseLayout);
        if (prevItemPosition != RecyclerView.NO_POSITION) {
            prevItemHeaderId = headerAdapter.getHeaderId(prevItemPosition);
        }
        return prevItemHeaderId;
    }

    public static long getNextItemHeaderId(HeaderAdapter headerAdapter, int position, boolean isReverseLayout) {
        long nextItemHeaderId = -1;
        final int nextItemPosition = getNextItemPosition((RecyclerView.Adapter) headerAdapter, position, isReverseLayout);
        if (nextItemPosition != RecyclerView.NO_POSITION) {
            nextItemHeaderId = headerAdapter.getHeaderId(nextItemPosition);
        }
        return nextItemHeaderId;
    }

    public static int getFirstItemPosition(RecyclerView.Adapter adapter, boolean isReverseLayout) {
        return isReverseLayout ? adapter.getItemCount() - 1 : 0;
    }

    public static int getLastItemPosition(RecyclerView.Adapter adapter, boolean isReverseLayout) {
        return isReverseLayout ? 0 : adapter.getItemCount() - 1;
    }

    public static int getPrevItemPosition(RecyclerView.Adapter adapter, int position, boolean isReverseLayout) {
        final int prevItemPosition = position + (isReverseLayout ? 1 : -1);
        return indexOutOfBounds(adapter, prevItemPosition) ? RecyclerView.NO_POSITION : prevItemPosition;
    }

    public static int getNextItemPosition(RecyclerView.Adapter adapter, int position, boolean isReverseLayout) {
        final int nextItemPosition = position + (isReverseLayout ? -1 : 1);
        return indexOutOfBounds(adapter, nextItemPosition) ? RecyclerView.NO_POSITION : nextItemPosition;
    }

    public static boolean indexOutOfBounds(RecyclerView.Adapter adapter, int position) {
        return position < 0 || position >= adapter.getItemCount();
    }

}
