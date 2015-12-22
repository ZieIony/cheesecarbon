package pl.zielony.cheesecarbon.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import carbon.Carbon;
import carbon.drawable.RippleDrawable;
import carbon.drawable.RippleView;
import carbon.internal.ElevationComparator;
import carbon.shadow.Shadow;
import carbon.shadow.ShadowGenerator;
import carbon.shadow.ShadowView;

/**
 * Created by Marcin on 2015-12-22.
 */
public class CoordinatorLayout extends android.support.design.widget.CoordinatorLayout {

    public CoordinatorLayout(Context context) {
        super(context);
    }

    public CoordinatorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CoordinatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    List<View> views;
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    @Override
    protected void dispatchDraw(@NonNull Canvas canvas) {
        views = new ArrayList<View>();
        for (int i = 0; i < getChildCount(); i++)
            views.add(getChildAt(i));
        Collections.sort(views, new ElevationComparator());

        super.dispatchDraw(canvas);
    }

    @Override
    protected boolean drawChild(@NonNull Canvas canvas, @NonNull View child, long drawingTime) {
        if (!child.isShown())
            return super.drawChild(canvas, child, drawingTime);

        if (!isInEditMode() && child instanceof ShadowView && Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
            ShadowView shadowView = (ShadowView) child;
            Shadow shadow = shadowView.getShadow();
            if (shadow != null) {
                paint.setAlpha((int) (ShadowGenerator.ALPHA * ViewHelper.getAlpha(child)));

                float childElevation = shadowView.getElevation() + shadowView.getTranslationZ();

                float[] childLocation = new float[]{(child.getLeft() + child.getRight()) / 2, (child.getTop() + child.getBottom()) / 2};
                Matrix matrix = carbon.internal.ViewHelper.getMatrix(child);
                matrix.mapPoints(childLocation);

                int[] location = new int[2];
                getLocationOnScreen(location);
                float x = childLocation[0] + location[0];
                float y = childLocation[1] + location[1];
                x -= getRootView().getWidth() / 2;
                y += getRootView().getHeight() / 2;   // looks nice
                float length = (float) Math.sqrt(x * x + y * y);

                int saveCount = canvas.save(Canvas.MATRIX_SAVE_FLAG);
                canvas.translate(
                        x / length * childElevation / 2,
                        y / length * childElevation / 2);
                canvas.translate(
                        child.getLeft(),
                        child.getTop());

                canvas.concat(matrix);
                shadow.draw(canvas, child, paint);
                canvas.restoreToCount(saveCount);
            }
        }

        if (child instanceof RippleView) {
            RippleView rippleView = (RippleView) child;
            RippleDrawable rippleDrawable = rippleView.getRippleDrawable();
            if (rippleDrawable != null && rippleDrawable.getStyle() == RippleDrawable.Style.Borderless) {
                int saveCount = canvas.save(Canvas.MATRIX_SAVE_FLAG);
                canvas.translate(
                        child.getLeft(),
                        child.getTop());
                rippleDrawable.draw(canvas);
                canvas.restoreToCount(saveCount);
            }
        }

        return super.drawChild(canvas, child, drawingTime);
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int child) {
        return views != null ? indexOfChild(views.get(child)) : child;
    }

}
