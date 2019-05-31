package cc.brainbook.android.headerdecoration.util;

import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.View;

import static android.view.ViewGroup.LayoutParams;
import static android.view.ViewGroup.MarginLayoutParams;

/**
 * Helper to calculate various view dimensions
 */
public class DimensionUtil {

    /**
     * Populates {@link Rect} with margins for any view.
     *
     * @param margins   rect to populate
     * @param view      for which to get margins
     */
    public static void initMargins(Rect margins, @NonNull View view) {
        final LayoutParams layoutParams = view.getLayoutParams();

        if (layoutParams instanceof MarginLayoutParams) {
            final MarginLayoutParams marginLayoutParams = (MarginLayoutParams) layoutParams;
            margins.set(
                    marginLayoutParams.leftMargin,
                    marginLayoutParams.topMargin,
                    marginLayoutParams.rightMargin,
                    marginLayoutParams.bottomMargin
            );
        } else {
            margins.set(0, 0, 0, 0);
        }
    }

    private DimensionUtil() {}
}
