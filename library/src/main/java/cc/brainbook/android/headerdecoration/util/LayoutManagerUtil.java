package cc.brainbook.android.headerdecoration.util;

import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

public class LayoutManagerUtil {

    public static int getOrientation(@NonNull RecyclerView recyclerView) {
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

        if (layoutManager instanceof GridLayoutManager) {
            return ((GridLayoutManager) layoutManager).getOrientation();
        } else if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).getOrientation();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) layoutManager).getOrientation();
        }

        throw new IllegalStateException("Not valid LayoutManager.");
    }

    public static boolean getReverseLayout(@NonNull RecyclerView recyclerView) {
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

        if (layoutManager instanceof GridLayoutManager) {
            return ((GridLayoutManager) layoutManager).getReverseLayout();
        } else if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).getReverseLayout();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) layoutManager).getReverseLayout();
        }

        throw new IllegalStateException("Not valid LayoutManager.");
    }

    public static boolean getClipToPadding(@NonNull RecyclerView recyclerView) {
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        return layoutManager != null && layoutManager.getClipToPadding();
    }

    public static int getListTop(@NonNull RecyclerView recyclerView) {
        return LayoutManagerUtil.getClipToPadding(recyclerView) ? recyclerView.getPaddingTop() : 0;
    }

    public static int getListLeft(@NonNull RecyclerView recyclerView) {
        return LayoutManagerUtil.getClipToPadding(recyclerView) ? recyclerView.getPaddingLeft() : 0;
    }

    private LayoutManagerUtil() {}
}
