package cn.flyrise.feep.particular.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import cn.flyrise.feep.R;

/**
 * @author ZYP
 * @since 2016-10-20 13:34
 */
public class FEFloatingActionMenu extends FrameLayout {

    private FloatingActionsMenu mFloatingActionMenus;
    private FloatingActionButton mDuDuButton;
    private FloatingActionButton mContentButton;
    private FloatingActionButton mAttachmentButton;
    private FloatingActionButton mReplyButton;

    public FEFloatingActionMenu(Context context) {
        this(context, null);
    }

    public FEFloatingActionMenu(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FEFloatingActionMenu(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.view_floating_action_button, this);
        mFloatingActionMenus = (FloatingActionsMenu) findViewById(R.id.floatingActionMenus);
        mDuDuButton = (FloatingActionButton) findViewById(R.id.duduActionButton);
        mReplyButton = (FloatingActionButton) findViewById(R.id.replyActionButton);
        mContentButton = (FloatingActionButton) findViewById(R.id.contentActionButton);
        mAttachmentButton = (FloatingActionButton) findViewById(R.id.attachmentActionButton);

        mDuDuButton.setVisibility(View.GONE);
        mReplyButton.setVisibility(View.GONE);
        mContentButton.setVisibility(View.GONE);
        mAttachmentButton.setVisibility(View.GONE);
    }

    public FloatingActionsMenu getFloatingActionMenus() {
        return this.mFloatingActionMenus;
    }

    public void collapse() {
        mFloatingActionMenus.collapse();
    }

    public void setOnlyOneButton(int drawableId, int defaultColorId, int pressColorId, OnClickListener clickListener) {
        mDuDuButton.setVisibility(View.VISIBLE);
        mReplyButton.setVisibility(View.GONE);
        mContentButton.setVisibility(View.GONE);
        mAttachmentButton.setVisibility(View.GONE);
    }

    public void setDuDuClickListener(int visibility, OnClickListener clickListener) {
        mDuDuButton.setVisibility(visibility);
        mDuDuButton.setOnClickListener(clickListener);
    }

    public void setReplyClickListener(int visibility, OnClickListener clickListener) {
        mReplyButton.setVisibility(visibility);
        mReplyButton.setOnClickListener(clickListener);
    }

    public void setContentClickListener(int visibility, OnClickListener clickListener) {
        mContentButton.setVisibility(visibility);
        mContentButton.setOnClickListener(clickListener);
    }

    public void setAttachmentClickListener(int visibility, OnClickListener clickListener) {
        mAttachmentButton.setVisibility(visibility);
        mAttachmentButton.setOnClickListener(clickListener);
    }

    public FloatingActionButton getDuDuButton() {
        return mDuDuButton;
    }

    public FloatingActionButton getContentButton() {
        return mContentButton;
    }

    public FloatingActionButton getAttachmentButton() {
        return mAttachmentButton;
    }

    public FloatingActionButton getReplyButton() {
        return mReplyButton;
    }
}
