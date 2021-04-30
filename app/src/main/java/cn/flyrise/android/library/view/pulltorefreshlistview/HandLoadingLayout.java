/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-4-25 上午9:15:37
 */
package cn.flyrise.android.library.view.pulltorefreshlistview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Orientation;
import com.handmark.pulltorefresh.library.internal.LoadingLayout;

import cn.flyrise.feep.R;

/**
 * 类功能描述：</br>
 *
 * @author 钟永健
 * @version 1.0</br> 修改时间：2013-4-25</br> 修改备注：</br>
 */
public class HandLoadingLayout extends LoadingLayout {
    static final int FLIP_ANIMATION_DURATION = 150;
    private Animation mRotateAnimation, mResetRotateAnimation;
    private ViewSizeType viewSizeType = ViewSizeType.LARGE;
    private TextView headerText;
    private View ipLayout;
    private LinearLayout ttLayout;

    public HandLoadingLayout(Context context, final Mode mode, final Orientation scrollDirection, TypedArray attrs) {
        super(context, mode, scrollDirection, attrs);
        findViews();
        ViewSizeType sizeType = ViewSizeType.LARGE;
        if (attrs != null && attrs.hasValue(R.styleable.PullToRefresh_ptrLoadingViewSizeType)) {
            sizeType = ViewSizeType.mapIntToValue(attrs.getInteger(R.styleable.PullToRefresh_ptrLoadingViewSizeType, ViewSizeType.LARGE.getValue()));
        }

        setViewSizeType(sizeType);

        Drawable imageDrawable = null;
        switch (mode) {
            case PULL_FROM_START:
            default:
                imageDrawable = context.getResources().getDrawable(R.drawable.hand_catch_fe);
                break;

            case PULL_FROM_END:
                imageDrawable = context.getResources().getDrawable(getDefaultDrawableResId());
                // Set Drawable, and save width/height
                initAnimation(mode);
                break;
        }

        setLoadingDrawable(imageDrawable);
        reset();
    }

    private void findViews() {
        ipLayout = findViewById(R.id.ip_layout);
        ttLayout = (LinearLayout) findViewById(R.id.tt_layout);
        headerText = (TextView) findViewById(R.id.pull_to_refresh_text);
    }

    /**
     * 设置控件的大小类型
     */
    private void setViewSizeType(ViewSizeType sizeType) {
        switch (sizeType) {
            case SMALL:
                if (viewSizeType == ViewSizeType.LARGE) {
                    if (mInnerLayout != null) {
                        mInnerLayout.removeAllViews();
                        mInnerLayout.addView(ttLayout);
                    }
                    ttLayout.addView(ipLayout, 0);
                }
                setHeaderTextSize(14);
                break;
            case LARGE:
                if (viewSizeType == ViewSizeType.SMALL) {
                    if (mInnerLayout != null) {
                        mInnerLayout.removeAllViews();
                        mInnerLayout.addView(ipLayout);
                        mInnerLayout.addView(ttLayout);
                    }
                }
                setHeaderTextSize(20);
            default:
                break;
        }
        this.viewSizeType = sizeType;
    }

    /**
     * 初始化动画
     */
    private void initAnimation(final Mode mode) {
        final int rotateAngle = mode == Mode.PULL_FROM_START ? -180 : 180;
        final Interpolator interpolator = new LinearInterpolator();

        mRotateAnimation = new RotateAnimation(0, rotateAngle, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateAnimation.setInterpolator(interpolator);
        mRotateAnimation.setDuration(FLIP_ANIMATION_DURATION);
        mRotateAnimation.setFillAfter(true);

        mResetRotateAnimation = new RotateAnimation(rotateAngle, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        mResetRotateAnimation.setInterpolator(interpolator);
        mResetRotateAnimation.setDuration(FLIP_ANIMATION_DURATION);
        mResetRotateAnimation.setFillAfter(true);
    }

    @Override
    protected void onLoadingDrawableSet(Drawable imageDrawable) {
        if (null != imageDrawable) {
            final int dHeight = imageDrawable.getIntrinsicHeight();
            final int dWidth = imageDrawable.getIntrinsicWidth();

            /**
             * We need to set the width/height of the ImageView so that it is square with each side the size of the largest drawable dimension. This is so that it doesn't clip when rotated.
             */
            final ViewGroup.LayoutParams lp = mHeaderImage.getLayoutParams();
            lp.width = lp.height = Math.max(dHeight, dWidth);
            mHeaderImage.requestLayout();

            /**
             * We now rotate the Drawable so that is at the correct rotation, and is centered.
             */
            mHeaderImage.setScaleType(ScaleType.MATRIX);
            final Matrix matrix = new Matrix();
            matrix.postTranslate((lp.width - dWidth) / 2f, (lp.height - dHeight) / 2f);
            matrix.postRotate(getDrawableRotationAngle(), lp.width / 2f, lp.height / 2f);
            mHeaderImage.setImageMatrix(matrix);
        }
    }

    @Override
    protected void onPullImpl(float scaleOfLayout) {
        // NO-OP
    }

    @Override
    protected void pullToRefreshImpl() {
        if (mHeaderImage != null) {
            // Only start reset Animation, we've previously show the rotate anim
            if (mRotateAnimation == null) {
                setHeaderImage(R.drawable.hand_catch_fe);
            }
            else if (mRotateAnimation == mHeaderImage.getAnimation()) {
                mHeaderImage.startAnimation(mResetRotateAnimation);
            }
        }
    }

    @Override
    protected void refreshingImpl() {
        if (mHeaderImage != null) {
            if (mRotateAnimation == null) {
                setHeaderImage(R.drawable.hand_catch_fe);
            }
            else {
                mHeaderImage.clearAnimation();
            }
            mHeaderImage.setVisibility(View.GONE);
            mHeaderProgress.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void releaseToRefreshImpl() {
        if (mHeaderImage != null) {
            if (mRotateAnimation == null) {
                setHeaderImage(R.drawable.hand_unfettered_fe);
                return;
            }
            mHeaderImage.startAnimation(mRotateAnimation);
        }
    }

    @Override
    protected void resetImpl() {
        if (mHeaderImage != null) {
            if (mRotateAnimation == null) {
                setHeaderImage(R.drawable.hand_catch_fe);
            }
            else {
                mHeaderImage.clearAnimation();
            }
            mHeaderProgress.setVisibility(View.GONE);
            mHeaderImage.setVisibility(View.GONE);

            if (viewSizeType == ViewSizeType.LARGE) {
                final String label = DateUtils.formatDateTime(getContext(), System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);
                setLastUpdatedLabel(label);
            }
        }
    }

    /**
     * 设置ImageView（mHeaderImage）的背景图片
     */
    private void setHeaderImage(int drawableResId) {
        if (mHeaderImage != null) {
            mHeaderImage.setImageDrawable(getContext().getResources().getDrawable(drawableResId));
        }
    }

    /**
     * 设置文字大小
     */
    public void setHeaderTextSize(float size) {
        if (null != headerText) {
            headerText.setTextSize(size);
        }
    }

    @Override
    protected int getDefaultDrawableResId() {
        return R.drawable.default_ptr_flip;
    }

    private float getDrawableRotationAngle() {
        float angle = 0f;
        switch (mMode) {
            case PULL_FROM_END:
                if (mScrollDirection == Orientation.HORIZONTAL) {
                    angle = 90f;
                }
                else {
                    angle = 180f;
                }
                break;

            case PULL_FROM_START:
                if (mScrollDirection == Orientation.HORIZONTAL) {
                    angle = 270f;
                }
                break;

            default:
                break;
        }

        return angle;
    }

    /**
     * 控件的大小类型
     */
    public enum ViewSizeType {
        /**
         * 适合比较小的横向距离的view
         */
        LARGE(0x0),
        /**
         * 适合比较大的横向距离的view
         */
        SMALL(0x1);

        ViewSizeType(int sizeType) {
            value = sizeType;
        }

        static ViewSizeType mapIntToValue(final int sizeType) {
            for (final ViewSizeType value : ViewSizeType.values()) {
                if (sizeType == value.getValue()) {
                    return value;
                }
            }
            return LARGE;
        }

        private int value;

        int getValue() {
            return value;
        }

    }
}
