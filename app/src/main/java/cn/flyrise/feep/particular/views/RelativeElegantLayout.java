package cn.flyrise.feep.particular.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import cn.flyrise.feep.R;

/**
 * @author ZYP
 * @since 2016-10-23 14:36
 * 一个相对优雅的 LinearLayout， 主要解决 ScrollView 中嵌套 ListView 时，
 * ListView 不仅复用机制报废，还特么重复绘制 Item 的问题。
 */
public class RelativeElegantLayout extends LinearLayout {

    private RelativeElegantAdapter mRelativeElegantAdapter;
    private OnItemClickListener mItemClickListener;
    private int mDividerHeight;
    private boolean withDivider = true;
    private ShapeDrawable mDividerDrawable;


    public RelativeElegantLayout(Context context) {
        this(context, null);
    }

    public RelativeElegantLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RelativeElegantLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDividerHeight = context.getResources().getDimensionPixelSize(R.dimen.mdp_0_2);
        setOrientation(VERTICAL);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, View view, Object object);
    }

    public void withDivider(boolean withDivider) {
        this.withDivider = withDivider;
    }

    public void setAdapter(RelativeElegantAdapter adapter) {
        this.mRelativeElegantAdapter = adapter;
        if (this.mRelativeElegantAdapter != null) {
            this.mRelativeElegantAdapter.setRelativeElegantViewGroup(this);
            this.updateContainer();
        }
    }

    public void updateContainer() {
        int childCount = mRelativeElegantAdapter.getCount();
        if (childCount == 0) {
            return;
        }

        removeAllViews();
        for (int i = 0; i < childCount; i++) {
            final int position = i;
            View childView = mRelativeElegantAdapter.getView(position);
            childView.setOnClickListener(new OnClickListener() {
                @Override public void onClick(View view) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClick(position, view, mRelativeElegantAdapter.getItem(position));
                    }
                }
            });
            if (childCount > 1 && i < childCount - 1 && withDivider) {
                this.resetDrawable(childView);
            }
            this.addView(childView);
        }
    }

    public Drawable createChildDividerDrawable() {
        if (mDividerDrawable == null) {
            mDividerDrawable = new ShapeDrawable(new RectShape());
            mDividerDrawable.getPaint().setColor(Color.parseColor("#CCCCCC"));
        }
        return mDividerDrawable;
    }

    private void resetDrawable(View childView) {
        Drawable background = childView.getBackground();
        if (background == null) {
            background = new ColorDrawable(Color.WHITE);
        }

        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{createChildDividerDrawable(), background});
        layerDrawable.setLayerInset(0, 0, 0, 0, 0);
        layerDrawable.setLayerInset(1, 0, 0, 0, mDividerHeight);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            childView.setBackground(layerDrawable);
        }
        else {
            childView.setBackgroundDrawable(layerDrawable);
        }
    }

}
