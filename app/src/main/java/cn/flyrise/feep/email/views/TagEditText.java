package cn.flyrise.feep.email.views;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZYP
 * @since 2016/6/21 16:35
 */
public class TagEditText extends ViewGroup {

    private AutoCompleteTextView mEditText;

    private Context mContext;

    private OnTagRemoveListener mListener;

    private List<Integer> mLineHeight = new ArrayList<>();

    private List<List<View>> mAllViews = new ArrayList<>();

    private String mHintText;

    public TagEditText(Context context) {
        this(context, null);
    }

    public TagEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TagEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        final InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                if (mEditText != null) {
                    imm.showSoftInput(mEditText, InputMethodManager.SHOW_IMPLICIT);
                    mEditText.requestFocus();
                }
            }
        });
    }

    public void setEditTextHint(String editTextHint) {
        this.mHintText = editTextHint;
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);
        int maxHeight = 0;
        int lineWidth = 0;
        int lineHeight = 0;
        int cCount = getChildCount();

        if (cCount == 1) {
            mEditText.setPadding(dip2px(0), dip2px(4), dip2px(4), dip2px(4));
            mEditText.setHint(mHintText);
            MarginLayoutParams lp = (MarginLayoutParams) mEditText.getLayoutParams();
            lp.leftMargin = dip2px(0);
            lp.width = MarginLayoutParams.MATCH_PARENT;
            mEditText.setLayoutParams(lp);
            measureChild(mEditText, widthMeasureSpec, heightMeasureSpec);
            int childHeight = mEditText.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;
            sizeHeight = maxHeight = childHeight;
            setMeasuredDimension(sizeWidth, (modeHeight == MeasureSpec.EXACTLY) ? sizeHeight : maxHeight);
            return;
        }

        for (int i = 0; i < cCount; i++) {
            View child = getChildAt(i);
            if (child instanceof EditText) {
                MarginLayoutParams layoutParams = (MarginLayoutParams) child.getLayoutParams();
                layoutParams.leftMargin = dip2px(2);
                layoutParams.width = LayoutParams.WRAP_CONTENT;
                child.setLayoutParams(layoutParams);
            }

            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();


            int childWidth = child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
            int childHeight = child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin;

            if (lineWidth + childWidth > sizeWidth) {
                lineWidth = childWidth;
                maxHeight += lineHeight;
                lineHeight = childHeight;
            }
            else {
                lineWidth += childWidth;
                lineHeight = Math.max(lineHeight, childHeight);
            }

            if (i == cCount - 1) {
                maxHeight += lineHeight;
            }

            if (child instanceof EditText) {
                mEditText.setHint("");
                child.setPadding(dip2px(0), dip2px(4), dip2px(4), dip2px(4));
            }
        }
        setMeasuredDimension(sizeWidth, (modeHeight == MeasureSpec.EXACTLY) ? sizeHeight : maxHeight);
    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();
        int childCount = getChildCount();
        if (childCount != 1) {
            throw new IllegalArgumentException("Must be one child in layout.xml.");
        }

        View view = getChildAt(0);
        if (!(view instanceof AutoCompleteTextView)) {
            throw new IllegalArgumentException("The only one child must be EditText.");
        }

        mEditText = (AutoCompleteTextView) view;
        mEditText.setOnKeyListener(new OnKeyListener() {
            @Override public boolean onKey(View v, int keyCode, KeyEvent event) {
                int actionId = event.getAction();
                if (keyCode == KeyEvent.KEYCODE_DEL && actionId == KeyEvent.ACTION_DOWN) {
                    if (mEditText.getText().toString().length() == 0) {
                        return removeTagView();
                    }
                }
                return false;
            }
        });

    }

    @Override protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mAllViews.clear();
        mLineHeight.clear();

        int width = getWidth();

        int lineWidth = 0;
        int lineHeight = 0;

        List<View> lineViews = new ArrayList<>();
        int cCount = getChildCount();

        for (int i = 0; i < cCount; i++) {
            View child = getChildAt(i);
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            if (childWidth + lp.leftMargin + lp.rightMargin + lineWidth > width) {
                mLineHeight.add(lineHeight);
                mAllViews.add(lineViews);
                lineWidth = 0;
                lineViews = new ArrayList<>();
            }

            lineWidth += childWidth + lp.leftMargin + lp.rightMargin;
            lineHeight = Math.max(lineHeight, childHeight + lp.topMargin + lp.bottomMargin);
            lineViews.add(child);
        }
        mLineHeight.add(lineHeight);
        mAllViews.add(lineViews);

        int left = 0;
        int top = 0;
        int lineNums = mAllViews.size();
        for (int i = 0; i < lineNums; i++) {
            lineViews = mAllViews.get(i);
            lineHeight = mLineHeight.get(i);

            for (int j = 0; j < lineViews.size(); j++) {
                View child = lineViews.get(j);
                if (child.getVisibility() == View.GONE) {
                    continue;
                }
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

                int lc = left + lp.leftMargin;
                int tc = top + lp.topMargin;
                int rc = lc + child.getMeasuredWidth();
                int bc = tc + child.getMeasuredHeight();

                child.layout(lc, tc, rc, bc);
                left += child.getMeasuredWidth() + lp.rightMargin + lp.leftMargin;
            }
            left = 0;
            top += lineHeight;
        }
    }

    @Override public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    public AutoCompleteTextView getEditText() {
        return this.mEditText;
    }

    public boolean addTagView(String text, final boolean withError) {
        if (TextUtils.isEmpty(text)) {
            text = mEditText.getText().toString();
        }

        if (TextUtils.isEmpty(text.trim())) {
            return false;
        }

        final TagView tagView = TagView.buildTagView(mContext, text, withError);
        tagView.setTagOnBackground();

        tagView.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v) {
                Object tag = tagView.getTag();
                if (tag == null) {
                    tagView.setTagOffBackground();
                    tagView.setTag(((TextView) v).getText().toString());
                }
                else {
                    if (mListener != null) {
                        mListener.onTagRemove(tag.toString(), tagView.isErrorTag());
                    }
                    removeView(tagView);
                }
            }
        });

        int childCount = getChildCount();
        addView(tagView, childCount - 1);
        return true;
    }

    public boolean addTagView(String text) {
        return addTagView(text, false);
    }

    public boolean addTagWithError(String text) {
        return addTagView(text, true);
    }

    public boolean removeAllTagViews() {
        int childCount = getChildCount();
        if (childCount == 1) {
            return false;
        }

        List<View> views = new ArrayList<>();
        for (int i = 0; i < childCount - 1; i++) {
            views.add(getChildAt(i));
        }

        for (View view : views) {
            removeView(view);
        }

        return true;
    }

    public boolean removeTagView() {
        int childCount = getChildCount();
        if (childCount == 1) {
            return false;
        }

        TagView deleteView = (TagView) getChildAt(childCount - 2);
        Object tag = deleteView.getTag();
        if (tag == null) {
            deleteView.setTagOffBackground();
            deleteView.setTag((deleteView.getText().toString()));
            return false;
        }

        if (mListener != null) {
            mListener.onTagRemove(tag.toString(), deleteView.isErrorTag());
        }
        removeViewAt(childCount - 2);
        mEditText.setText("");
        mEditText.setSelection(0);
        return true;
    }

    public void setOnTagRemoveListener(OnTagRemoveListener listener) {
        this.mListener = listener;
    }

    public interface OnTagRemoveListener {
        void onTagRemove(String tag, boolean isErrotTag);
    }

    private int dip2px(float dipValue) {
        final float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}