package cn.flyrise.android.library.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.R;

public class SearchEditText extends EditText implements OnFocusChangeListener, OnKeyListener, TextWatcher {

    private static final String TAG = "SearchEditText";
    /**
     * 图标是否默认在左边
     */
    public static boolean isIconLeft = false;
//    /**
//     * 是否点击软键盘搜索
//     */
//    private boolean               pressSearch = false;
    /**
     * 软键盘搜索键监听
     */
    private OnTextWatcherListener listener;
    private OnKeyListener keylistener;

    private Drawable[] drawables;                     // 控件的图片资源
    private Drawable drawableLeft, drawableDel;     // 搜索图标和删除按钮图标
    private int eventX, eventY;                // 记录点击坐标
    private Rect rect;                          // 控件区域
    private OnEditTextIsGetFocus onEditTextIsFocus;

    public void setOnTextWatcherListener(OnTextWatcherListener listener) {
        this.listener = listener;
    }

    public void setOnKeyListener(OnKeyListener listener) {
        this.keylistener = listener;
    }

    public interface OnTextWatcherListener {
        void beforeTextChanged(CharSequence s, int start, int count, int after);

        void onTextChanged(CharSequence s, int start, int before, int count);

        void afterTextChanged(Editable s);
    }

    public SearchEditText(Context context) {
        this(context, null);
    }

    public SearchEditText(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.editTextStyle);
        init();
    }

    public SearchEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnFocusChangeListener(this);
        setOnKeyListener(this);
        addTextChangedListener(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
//    	FELog.i("search", "-------isIcon333:"+isIconLeft);
        if (isIconLeft) { // 如果是默认样式，直接绘制
            if (length() < 1) {
                drawableDel = null;
            }
            this.setCompoundDrawablesWithIntrinsicBounds(drawableLeft, null, drawableDel, null);
            super.onDraw(canvas);
        }
        else { // 如果不是默认样式，需要将图标绘制在中间
            if (drawables == null)
                drawables = getCompoundDrawables();
            if (drawableLeft == null)
                drawableLeft = drawables[0];
            String hint = String.valueOf(getHint());
            float textWidth = getPaint().measureText(hint);
            int drawablePadding = getCompoundDrawablePadding();
            int drawableWidth = 0;
            if (drawableLeft != null) {
                drawableWidth = drawableLeft.getIntrinsicWidth();
            }
            float bodyWidth = textWidth + drawableWidth + drawablePadding;
            canvas.translate((getWidth() - bodyWidth - getPaddingLeft() - getPaddingRight()) / 2, 0);
            super.onDraw(canvas);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        // 被点击时，恢复默认样式
//        if (TextUtils.isEmpty(getText().toString())) {
//            isIconLeft = hasFocus;
//        }
//        if(hasFocus){
//        	isIconLeft = true;
//        }else{
//        	isIconLeft =false;
//        }
//        if(onEditTextIsFocus!=null){
//        	onEditTextIsFocus.EditTextIsGetFocus(hasFocus);
//        }
    }

    public interface OnEditTextIsGetFocus {
        void EditTextIsGetFocus(boolean hasFocus);
    }

    /**
     * 判断文本是否获取到焦点
     */
    public void setOnEditTextIsGetFocus(OnEditTextIsGetFocus onEditTextIsFocus) {
        this.onEditTextIsFocus = onEditTextIsFocus;
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        FELog.i("search", "---------isIconLeft444:" + isIconLeft);
//    	 keylistener.onKey(v, keyCode, event);
        if (isIconLeft) {
            isIconLeft = false;
        }
        if ((keyCode == KeyEvent.KEYCODE_ENTER) && listener != null) {
            /* 隐藏软键盘 */
            InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive()) {
                imm.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
            }
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        FELog.i("search", "-------edittext点击" + isIconLeft);
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            setFocusable(true);
            setFocusableInTouchMode(true);
            requestFocus();
            findFocus();
            if (!isIconLeft) {
                isIconLeft = true;
            }
        }
        // 清空edit内容
        if (drawableDel != null && event.getAction() == MotionEvent.ACTION_UP) {
            eventX = (int) event.getRawX();
            eventY = (int) event.getRawY();
            Log.i(TAG, "eventX = " + eventX + "; eventY = " + eventY);
            if (rect == null)
                rect = new Rect();
            getGlobalVisibleRect(rect);
            rect.left = rect.right - drawableDel.getIntrinsicWidth();
            if (rect.contains(eventX, eventY)) {
                setText("");
                if (mDeleteButtonListener != null) {
                    mDeleteButtonListener.onDeleteButtonClick();
                }
                drawableDel = null;
            }
        }
        // 删除按钮被按下时改变图标样式
        if (drawableDel != null && event.getAction() == MotionEvent.ACTION_DOWN) {
            eventX = (int) event.getRawX();
            eventY = (int) event.getRawY();
            Log.i(TAG, "eventX = " + eventX + "; eventY = " + eventY);
            if (rect == null)
                rect = new Rect();
            getGlobalVisibleRect(rect);
            rect.left = rect.right - drawableDel.getIntrinsicWidth();
            if (rect.contains(eventX, eventY))
                drawableDel = this.getResources().getDrawable(R.mipmap.core_icon_delete);
        }
        else {
            drawableDel = this.getResources().getDrawable(R.drawable.edit_delete_icon);
        }
        return super.onTouchEvent(event);
    }


    @Override
    public void afterTextChanged(Editable arg0) {
        if (this.length() < 1) {
            drawableDel = null;
        }
        else {
            isIconLeft = true;
            drawableDel = this.getResources().getDrawable(R.drawable.edit_delete_icon);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    }

    @Override
    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
    }

    private OnDeleteButtonClickListener mDeleteButtonListener;

    public void setOnDeleteButtonClickListener(OnDeleteButtonClickListener listener) {
        this.mDeleteButtonListener = listener;
    }

    public interface OnDeleteButtonClickListener {
        void onDeleteButtonClick();
    }

}