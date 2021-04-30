package cn.flyrise.feep.email.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.PixelUtil;

/**
 * @author ZYP
 * @since 2016/7/25 11:54
 */
public class TagView extends TextView {

    private boolean isErrorTag;

    public TagView(Context context) {
        this(context, null);
    }

    public TagView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TagView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setIsErrorTag(boolean isErrorTag) {
        this.isErrorTag = isErrorTag;
    }

    public boolean isErrorTag() {
        return this.isErrorTag;
    }

    public static TagView buildTagView(Context context, String text, boolean isErrorTag) {
        TagView tagView = new TagView(context);
        tagView.setText(text);
        ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = PixelUtil.dipToPx(2);
        lp.leftMargin = PixelUtil.dipToPx(2);
        lp.rightMargin = PixelUtil.dipToPx(2);
        tagView.setGravity(Gravity.CENTER_VERTICAL);
        tagView.setLayoutParams(lp);
        tagView.setTextColor(Color.WHITE);
        tagView.setIsErrorTag(isErrorTag);
        return tagView;
    }

    public void setTagOnBackground() {
        this.setBackgroundResource(isErrorTag ? R.drawable.bg_tag_error_on : R.drawable.bg_tag_on);
    }

    public void setTagOffBackground() {
        this.setBackgroundResource(isErrorTag ? R.drawable.bg_tag_error_off : R.drawable.bg_tag_off);
    }

}
