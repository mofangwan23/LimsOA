package cn.flyrise.feep.core.dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import cn.flyrise.feep.core.R;
import cn.flyrise.feep.core.common.utils.PixelUtil;

/**
 * @author ZYP
 * @since 2017-05-02 14:05
 * 选择颜色的 Dialog, 颜色是固定写死的几个
 */
public class FEColorDialog extends DialogFragment {
    private static final int[] sColors = {
            Color.parseColor("#000000"),
            Color.parseColor("#795548"),
            Color.parseColor("#607D8B"),
            Color.parseColor("#9E9E9E"),

            Color.parseColor("#F44336"),
            Color.parseColor("#E91E63"),
            Color.parseColor("#9C27B0"),
            Color.parseColor("#673AB7"),

            Color.parseColor("#2196F3"),
            Color.parseColor("#00BCD4"),
            Color.parseColor("#009688"),
            Color.parseColor("#4CAF50"),

            Color.parseColor("#FFEB3B"),
            Color.parseColor("#FFC107"),
            Color.parseColor("#FF9800"),
            Color.parseColor("#FF5722")
    };
    private static final int sDrawableSize = PixelUtil.dipToPx(48);

    private int mDefaultColor;
    private GridView mGridView;
    private ColorAdapter mAdapter;
    private OnColorSelectedListener mColorSelectedListener;

    public static FEColorDialog newInstance(int defaultColor) {
        FEColorDialog dialog = new FEColorDialog();
        dialog.setDefaultColor(defaultColor);
        return dialog;
    }

    public void setDefaultColor(int defaultColor) {
        this.mDefaultColor = defaultColor;
        if (this.mDefaultColor == 0) {
            mDefaultColor = sColors[0];
        }
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        this.mColorSelectedListener = listener;
    }

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View contentView = inflater.inflate(R.layout.core_dialog_color, container, false);
        bindView(contentView);
        return contentView;
    }

    private void bindView(View view) {
        mGridView = (GridView) view.findViewById(R.id.gridView);
        mGridView.setAdapter(mAdapter = new ColorAdapter());
        mGridView.setOnItemClickListener((parent, v, position, id) -> {
            mDefaultColor = sColors[position];
            mAdapter.notifyDataSetChanged();
            if (mColorSelectedListener != null) {
                mColorSelectedListener.onColorSelected(mDefaultColor);
            }
            dismiss();
        });
    }

    private class ColorAdapter extends BaseAdapter {

        @Override public int getCount() {
            return sColors.length;
        }

        @Override public Object getItem(int position) {
            return sColors[position];
        }

        @Override public long getItemId(int position) {
            return position;
        }

        @Override public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.core_item_color, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            final int color = sColors[position];

            ShapeDrawable drawable = new ShapeDrawable(new OvalShape());
            drawable.setIntrinsicWidth(sDrawableSize);
            drawable.setIntrinsicHeight(sDrawableSize);
            drawable.getPaint().setColor(color);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                holder.imageView.setBackground(drawable);
            }
            else {
                holder.imageView.setBackgroundDrawable(drawable);
            }

            if (color == mDefaultColor) {
                holder.imageView.setImageResource(R.mipmap.core_icon_done);
            }
            else {
                holder.imageView.setImageDrawable(new ColorDrawable(color));
            }
            return convertView;
        }
    }

    private class ViewHolder {
        private ImageView imageView;

        public ViewHolder(View itemView) {
            imageView = (ImageView) itemView.findViewById(R.id.ivCircle);
        }
    }

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }
}
