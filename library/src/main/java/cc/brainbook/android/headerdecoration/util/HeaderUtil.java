package cc.brainbook.android.headerdecoration.util;

import android.graphics.Rect;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

public abstract class HeaderUtil {

    /**
     * Initializes a clipping rect for the header based on the margins of the header and the padding of the
     * recycler.
     * FIXME: Currently right margin in VERTICAL orientation and bottom margin in HORIZONTAL
     * orientation are clipped so they look accurate, but the headers are not being drawn at the
     * correctly smaller width and height respectively.
     *
     * @param clipRect      {@link Rect} for clipping a provided header to the padding of a recycler view
     * @param recyclerView  for which to provide a header
     * @param header        for clipping
     */
    public static void initClipRectForHeader(Rect clipRect, RecyclerView recyclerView, View header) {
        DimensionUtil.initMargins(clipRect, header);
        if (LayoutManagerUtil.getOrientation(recyclerView) == LinearLayout.VERTICAL) {
            clipRect.set(
                    recyclerView.getPaddingLeft(),
                    recyclerView.getPaddingTop(),
                    recyclerView.getWidth() - recyclerView.getPaddingRight() - clipRect.right,
                    recyclerView.getHeight() - recyclerView.getPaddingBottom());
        } else {
            clipRect.set(
                    recyclerView.getPaddingLeft(),
                    recyclerView.getPaddingTop(),
                    recyclerView.getWidth() - recyclerView.getPaddingRight(),
                    recyclerView.getHeight() - recyclerView.getPaddingBottom() - clipRect.bottom);
        }
    }

}
