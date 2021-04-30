package cn.flyrise.feep.collaboration.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.utility.RichStyle;
import jp.wasabeef.richeditor.RichEditor;

/**
 * @author ZYP
 * @since 2017-04-26 14:45
 * 富文本编辑器工具栏
 */
public class RichTextToolBar extends LinearLayout implements View.OnClickListener {

    protected final static int DEFAULT_COLOR = Color.parseColor("#F2F2F2");
    protected final static int SELECT_COLOR = Color.parseColor("#E2E2E2");

    protected RichEditor mRichEditor;

    protected ImageView mBoldBtn;
    protected ImageView mVoiceBtn;
    protected ImageView mFontSizeBtn;
    protected ImageView mUnderLineBtn;
    protected ImageView mInsertImageBtn;
    protected ImageView mFontColorBtn;

    public RichTextToolBar(Context context) {
        this(context, null);
    }

    public RichTextToolBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RichTextToolBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, getToolBarLayout(), this);

        mBoldBtn = (ImageView) findViewById(R.id.iBtnBold);
        mBoldBtn.setOnClickListener(this);

        mUnderLineBtn = (ImageView) findViewById(R.id.iBtnUnderLine);
        mUnderLineBtn.setOnClickListener(this);

        mInsertImageBtn = (ImageView) findViewById(R.id.iBtnInsertImage);
        mFontColorBtn = (ImageView) findViewById(R.id.iBtnFontColor);
        mVoiceBtn = (ImageView) findViewById(R.id.iBtnVoiceInput);
        mFontSizeBtn = (ImageView) findViewById(R.id.iBtnFontSize);
    }

    public void setRichEditor(RichEditor richEditor) {
        this.mRichEditor = richEditor;
    }

    public void setImageMenuClickListener(OnClickListener clickListener) {
        mInsertImageBtn.setOnClickListener(clickListener);
    }

    public void setFontColorMenuClickListener(OnClickListener clickListener) {
        mFontColorBtn.setOnClickListener(clickListener);
    }

    public void setVoiceMenuClickListener(OnClickListener clickListener) {
        mVoiceBtn.setOnClickListener(clickListener);
    }

    public void setFontSizeMenuClickListener(OnClickListener clickListener) {
        mFontSizeBtn.setOnClickListener(clickListener);
    }

    @Override public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iBtnBold:
                this.setMenuStyle(mBoldBtn);
                if (mRichEditor != null) {
                    mRichEditor.setBold();
                }
                break;
            case R.id.iBtnUnderLine:
                this.setMenuStyle(mUnderLineBtn);
                if (mRichEditor != null) {
                    mRichEditor.setUnderline();
                }
                break;
        }
    }

    protected void setMenuStyle(ImageView menuView) {
        Object tag = menuView.getTag();
        if (tag == null) {
            menuView.setBackgroundColor(SELECT_COLOR);
            menuView.setTag("Chen_Mian_Dan_Shen_Gou");
        }
        else {
            resetToDefaultMenuStyle(menuView);
        }
    }

    protected void setMenuStyle(ImageView menuStyle, boolean isMenuSelected) {
        if (isMenuSelected) {
            menuStyle.setBackgroundColor(SELECT_COLOR);
            menuStyle.setTag("Chen_Mian_Dan_Shen_Gou");
        }
        else {
            resetToDefaultMenuStyle(menuStyle);
        }
    }

    public boolean isBold() {
        return mBoldBtn.getTag() != null;
    }

    public boolean isUnderLine() {
        return mUnderLineBtn.getTag() != null;
    }

    public void setMenuStyle(RichStyle richStyle) {
        setMenuStyle(mBoldBtn, richStyle.isBold);
        setMenuStyle(mUnderLineBtn, richStyle.isUnderLine);

        int fontColor = richStyle.getFontColor();
        if (fontColor == -1) {
            mFontColorBtn.setBackgroundColor(Color.parseColor("#000000"));
        }
        else {
            mFontColorBtn.setBackgroundColor(fontColor);
        }
    }

    protected void resetToDefaultMenuStyle(ImageView menuView) {
        if (menuView != null) {
            menuView.setBackgroundColor(DEFAULT_COLOR);
            menuView.setTag(null);
        }
    }

    public void setFontColorBtnColor(int color) {
        mFontColorBtn.setBackgroundColor(color);
    }

    public void resetAllStyle() {
        resetToDefaultMenuStyle(mBoldBtn);
        resetToDefaultMenuStyle(mUnderLineBtn);
        resetToDefaultMenuStyle(mFontColorBtn);
        setFontColorBtnColor(Color.BLACK);
    }

    public int getToolBarLayout() {
        return R.layout.view_rich_text_tool_bar;
    }
}
