package cn.flyrise.feep.collaboration.view;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.PixelUtil;

/**
 * 类功能描述：一个包含图片和文字的控件</br>
 * @author cww
 * @version 1.0 </p> 修改时间：</br> 修改备注：</br>
 */
public class ImageAndTextButton extends LinearLayout {

    private final Context context;
    private ImageView imageview;
    private TextView textview;

    public ImageAndTextButton(Context context) {
        this(context, null);
    }

    public ImageAndTextButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        this.setOrientation(LinearLayout.HORIZONTAL);
        this.setGravity(Gravity.CENTER);
        this.setClickable(true);
        this.setBackgroundResource(R.drawable.download_bottm);
        imageview = new ImageView(context);
        imageview.setImageResource(R.drawable.add_appendix_fe);
        final LayoutParams imageparams = new LayoutParams(android.view.ViewGroup.LayoutParams.WRAP_CONTENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        imageparams.setMargins(0, 0, 0, PixelUtil.dipToPx(5));
        addView(imageview, imageparams);

        textview = new TextView(context);
        textview.setTextColor(0xff222222);
        textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        textview.setTypeface(Typeface.DEFAULT_BOLD);
        textview.setSingleLine();
        textview.setText(context.getString(R.string.collaboration_touch));
        addView(textview);
    }

    /**
     * 设置图资源
     * @param resource 图片资源
     */
    public void setImage(int resource) {
        imageview.setImageResource(resource);
    }

    /**
     * 设置文字
     */
    public void setText(String textshow) {
        textview.setText(textshow);
    }

    /**
     * 设置文字的颜色
     * @param textcolor 颜色值
     */
    public void setTextColor(int textcolor) {
        textview.setTextColor(textcolor);
    }

    /**
     * 设置文字的大小
     */
    public void setTextSize(float textsize) {
        textview.setTextSize(textsize);
    }

}
