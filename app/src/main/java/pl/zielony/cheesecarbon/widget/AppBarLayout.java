package pl.zielony.cheesecarbon.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import carbon.Carbon;
import carbon.drawable.RippleDrawable;
import carbon.shadow.Shadow;
import carbon.shadow.ShadowGenerator;
import carbon.shadow.ShadowShape;
import carbon.shadow.ShadowView;
import carbon.widget.CornerView;

/**
 * Created by Marcin on 2015-12-22.
 */
public class AppBarLayout extends android.support.design.widget.AppBarLayout implements ShadowView,CornerView {
    protected Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);

    public AppBarLayout(Context context) {
        super(context);
    }

    public AppBarLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        Carbon.initElevation(this, attrs, 0);
    }


    // -------------------------------
    // elevation
    // -------------------------------

    private float elevation = 0;
    private float translationZ = 0;
    private Shadow shadow;

    @Override
    public float getElevation() {
        return elevation;
    }

    @Override
    public synchronized void setElevation(float elevation) {
        if (elevation == this.elevation)
            return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            super.setElevation(elevation);
        this.elevation = elevation;
        if (getParent() != null)
            ((View) getParent()).postInvalidate();
    }

    @Override
    public float getTranslationZ() {
        return translationZ;
    }

    public synchronized void setTranslationZ(float translationZ) {
        if (translationZ == this.translationZ)
            return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            super.setTranslationZ(translationZ);
        this.translationZ = translationZ;
        if (getParent() != null)
            ((View) getParent()).postInvalidate();
    }

    @Override
    public ShadowShape getShadowShape() {
        if (cornerRadius == getWidth() / 2 && getWidth() == getHeight())
            return ShadowShape.CIRCLE;
        if (cornerRadius > 0)
            return ShadowShape.ROUND_RECT;
        return ShadowShape.RECT;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setTranslationZ(enabled ? 0 : -elevation);
    }

    @Override
    public Shadow getShadow() {
        float elevation = getElevation() + getTranslationZ();
        if (elevation >= 0.01f && getWidth() > 0 && getHeight() > 0) {
            if (shadow == null || shadow.elevation != elevation)
                shadow = ShadowGenerator.generateShadow(this, elevation);
            return shadow;
        }
        return null;
    }

    @Override
    public void invalidateShadow() {
        shadow = null;
        if (getParent() != null && getParent() instanceof View)
            ((View) getParent()).postInvalidate();
    }


    // -------------------------------
    // corners
    // -------------------------------

    private int cornerRadius;
    private Path cornersMask;
    private static PorterDuffXfermode pdMode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

    /**
     * Gets the corner radius. If corner radius is equal to 0, rounded corners are turned off. Shadows work faster when corner radius is less than 2.5dp.
     *
     * @return corner radius, equal to or greater than 0.
     */
    public int getCornerRadius() {
        return cornerRadius;
    }

    /**
     * Sets the corner radius. If corner radius is equal to 0, rounded corners are turned off. Shadows work faster when corner radius is less than 2.5dp.
     *
     * @param cornerRadius
     */
    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        invalidateShadow();
        initCorners();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (!changed)
            return;

        invalidateShadow();

        if (getWidth() == 0 || getHeight() == 0)
            return;

        initCorners();
    }

    private void initCorners() {
        if (cornerRadius > 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setClipToOutline(true);
                setOutlineProvider(ShadowShape.viewOutlineProvider);
            } else {
                cornersMask = new Path();
                cornersMask.addRoundRect(new RectF(0, 0, getWidth(), getHeight()), cornerRadius, cornerRadius, Path.Direction.CW);
                cornersMask.setFillType(Path.FillType.INVERSE_WINDING);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                setOutlineProvider(ViewOutlineProvider.BOUNDS);
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (cornerRadius > 0 && getWidth() > 0 && getHeight() > 0 && Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
            int saveCount = canvas.saveLayer(0, 0, getWidth(), getHeight(), null, Canvas.ALL_SAVE_FLAG);

            super.draw(canvas);

            paint.setXfermode(pdMode);
            canvas.drawPath(cornersMask, paint);

            canvas.restoreToCount(saveCount);
            paint.setXfermode(null);
        } else {
            super.draw(canvas);
        }
    }

}
