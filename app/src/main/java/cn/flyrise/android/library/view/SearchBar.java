package cn.flyrise.android.library.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.DevicesUtil;

public class SearchBar extends RelativeLayout {
    private SearchEditText editText;
    private ListView listView;
    private final ImageView magnifier;
    private final LinearLayout linearLayout;
    private PopupWindow popupWindowResult;
    private final ListView lv;
    private View custom;
    private TextWatcher textWatcher;
    private Button cancelButton;
    private View view;
    private long startTime;
    String lastTest = "";
    String text = "";

    /**
     * 公共控件搜索框
     *
     * @param context Activity.this
     */
    public SearchBar (Context context) {
        this (context, null);
    }

    /**
     * @param context Activity.this
     * @param attrs   AttributeSet
     */
    public SearchBar (Context context, AttributeSet attrs) {
        super (context, attrs);
        inputManager = (InputMethodManager) context.getSystemService (Context.INPUT_METHOD_SERVICE);
        this.setLayoutParams (new LayoutParams (android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
        /*--End--*/
        /*--生成输入框及清除按钮--*/
        linearLayout = new LinearLayout (context);
        view = LayoutInflater.from (context).inflate (R.layout.searchedit_layout, null);
        editText = (SearchEditText) view.findViewById (R.id.search_edittext);
        magnifier = new ImageView (context);
        /*--End--*/
        /*--添加控件--*/
        linearLayout.setFocusable (true);
        linearLayout.setFocusableInTouchMode (true);
        linearLayout.setLayoutParams (new LayoutParams (1, 1));
        this.addView (linearLayout);
        final LayoutParams lp1 = new LayoutParams (LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        lp1.addRule (CENTER_VERTICAL);
        this.addView (view, lp1);
        buttonParams = new LayoutParams (LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        /*--浮动结果框--*/
        lv = new ListView (context);
        lv.setCacheColorHint (0);
        lv.setFadingEdgeLength (0);
        lv.setScrollbarFadingEnabled (true);
        lv.setScrollingCacheEnabled (true);
        lv.setVerticalScrollBarEnabled (false);
        lv.setBackgroundColor (0xfff2f2f2);
        lv.setDivider (getResources().getDrawable (R.drawable.listview_divider_line));
        popupWindowResult = createPopupWindow (lv);
        setListener ();
        setBackgroundColor (getResources ().getColor (R.color.home_title_color));
    }

    /**
     * 添加一个取消按钮，作用是隐藏此搜索控件
     */
    public void addCancelButton () {
        cancelButton = new Button (getContext ());
        cancelButton.setId (View.NO_ID);
        cancelButton.setTextColor (0xFF848284);
        cancelButton.setPadding (-1, -1, 2, 0);
        cancelButton.setShadowLayer (1, 1, 1, 0xffffffff);
        cancelButton.setText (getContext ().getString (R.string.dialog_default_cancel_button_text));
        cancelButton.setBackgroundResource (R.drawable.search_btn_fe);
        buttonParams.setMargins (0, 0, 1, 0);
        this.addView (cancelButton, buttonParams);
        cancelButton.setOnClickListener (new OnClickListener () {
            @Override
            public void onClick (View v) {
                // 隐藏软键盘
                if (inputManager.isActive ()) {
                    inputManager.hideSoftInputFromWindow (v.getWindowToken (), 0);
                }
                SearchBar.this.setVisibility (GONE);
            }
        });
        cancelButton.setOnTouchListener (new OnTouchListener () {
            @Override
            public boolean onTouch (View v, MotionEvent event) {
                if (event.getAction () == MotionEvent.ACTION_DOWN) {
                    cancelButton.setBackgroundResource (R.drawable.search_btn_lighthight_fe);
                    cancelButton.setTextColor (0xFF8CA2AD);
                } else if (event.getAction () == MotionEvent.ACTION_UP) {
                    cancelButton.setTextColor (0xFF848284);
                    cancelButton.setBackgroundResource (R.drawable.search_btn_fe);
                    setEditTextLostFocus ();
                    /*--隐藏输入法--*/
                    final InputMethodManager imm = (InputMethodManager) getContext ().getSystemService (Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow (editText.getWindowToken (), 0);
                    /*--End--*/
                }
                return false;
            }
        });
    }

    private void setListener () {
        editText.addTextChangedListener (new TextWatcher () {
            @Override
            public void onTextChanged (CharSequence s, int start, int before, int count) {
                lastTest = text;
                text = editText.getText ().toString ().trim ();
                if (textWatcher != null) {
                    if (text.length () > lastTest.length ()) {
                        if (System.currentTimeMillis () - startTime > 1000) {
                            textWatcher.onTextChanged (s, start, before, count);
                            startTime = System.currentTimeMillis ();
                        }
                    } else {
                        textWatcher.onTextChanged (s, start, before, count);
                    }

                }
            }

            @Override
            public void beforeTextChanged (CharSequence s, int start, int count, int after) {
                if (textWatcher != null) {
                    textWatcher.beforeTextChanged (s, start, count, after);
                }
            }

            @Override
            public void afterTextChanged (Editable s) {
                if (s.length () == 0) {
                    if (cancelButton != null) {
                        magnifier.setVisibility (GONE);
                        cancelButton.setVisibility (VISIBLE);
                    }
                } else {
                    if (cancelButton != null) {
                        magnifier.setVisibility (VISIBLE);
                        cancelButton.setVisibility (GONE);
                    }
                }
                if (textWatcher != null) {
                    textWatcher.afterTextChanged (s);
                }
            }
        });
    }

    /**
     * 创建一个popupwindow
     */
    public PopupWindow createPopupWindow (View view) {
        final PopupWindow popupWindow = new PopupWindow (view);
        popupWindow.setFocusable (true);
        popupWindow.setOutsideTouchable (true);
        popupWindow.setBackgroundDrawable (getResources().getDrawable (R.drawable.txt_fe));
        return popupWindow;
    }

    /**
     * 取消 PopupWindow
     */
    public void dismissPopupWindow () {
        if (popupWindowResult != null) {
            popupWindowResult.dismiss ();
        }
    }

    @Override
    protected void onWindowVisibilityChanged (int visibility) {
        super.onWindowVisibilityChanged (visibility);
    }

    /**
     * 让EditText失去脚垫
     */
    public void setEditTextLostFocus () {
        editText.setFocusable (false);
        editText.setFocusableInTouchMode (false);
        linearLayout.setFocusable (true);
        linearLayout.setFocusableInTouchMode (true);
        editText.setFocusable (true);
        editText.setFocusableInTouchMode (true);
        final InputMethodManager imm = (InputMethodManager) getContext ().getSystemService (Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow (editText.getWindowToken (), 0);
    }

    private int state = 0;                                 // 0放大镜居中,1右边,2左边
    private final float[] move = {0, 0.5f, -0.5f, 0.5f, -0.5f, 0};
    private boolean searching = false;
    private final InputMethodManager inputManager;
    private OnClickListener clickListener;
    private final LayoutParams buttonParams;

    private void doAnimation () {
        final TranslateAnimation ta = new TranslateAnimation (Animation.RELATIVE_TO_SELF, move[state], Animation.RELATIVE_TO_SELF, move[state + 3], Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
        ta.setDuration (state == 1 ? 1000 : 500);
        state = (state + 1) % 3;
        magnifier.startAnimation (ta);
        magnifier.getAnimation ().setAnimationListener (new AnimationListener () {
            @Override
            public void onAnimationStart (Animation animation) {
            }

            @Override
            public void onAnimationRepeat (Animation animation) {
            }

            @Override
            public void onAnimationEnd (Animation animation) {
                if (searching || state != 0) {
                    doAnimation ();
                }
            }
        });
    }

    public boolean isDoingSearch () {
        return searching;
    }

    /**
     * 添加一个文本监听器，就是当文本改变前后中所做的事情的监听
     */
    public void addTextChangedListener (TextWatcher listener) {
        this.textWatcher = listener;
    }

    @Override
    public boolean dispatchTouchEvent (MotionEvent ev) {
        return super.dispatchTouchEvent (ev);
    }

    @Override
    public void onWindowFocusChanged (boolean hasFocus) {
        super.onWindowFocusChanged (hasFocus);
    }

    @Override
    public void setOnClickListener (OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void startSearchingAnimation () {
        searching = true;
        doAnimation ();
    }

    public void stopSearchingAnimation () {
        searching = false;
    }

    public String getText () {
        return editText.getText ().toString ();
    }

    public void setText (String text) {
        if (text == null) {
            text = "";
        }
        editText.setText (text);
    }

    /**
     * --设置提示文字信息--
     */
    public void setHint (String hint) {
        editText.setHint (hint);
    }

    public ListView getResultListView () {
        return listView;
    }

    public void showResult (BaseAdapter adapter) {
        if (popupWindowResult.getContentView () != lv) {
            popupWindowResult = createPopupWindow (lv);
        }
        lv.setAdapter (adapter);
        showPopupWindow ();
    }

    public void showResult (View view) {
        custom = view;
        custom.setVisibility (View.VISIBLE);
        if (popupWindowResult.getContentView () != custom) {
            popupWindowResult = createPopupWindow (custom);
        }
        popupWindowResult.getBackground ().setAlpha (255);
        showPopupWindow ();
    }

    /**
     * 显示PopupWindow
     */
    public void showPopupWindow () {
        popupWindowResult.setWidth (getWidth ());// 两边小一点,并非完全等宽
        popupWindowResult.setHeight (DevicesUtil.getScreenHeight () / 2);// 高度算全屏幕的1/4吧
        try {
            popupWindowResult.showAsDropDown (this);
        } catch (final Exception e) {
            e.printStackTrace ();
        }
    }

    public void hideResult () {
        if (popupWindowResult != null) {
            try {
                custom.setVisibility (View.GONE);
                popupWindowResult.getBackground ().setAlpha (0);
                popupWindowResult.update (0, 0);// 这句可能没用了,经常出现null异常
            } catch (final Exception e) {
                e.printStackTrace ();
            }
        }
    }

    public void dismissResult () {
        popupWindowResult.dismiss ();
    }

    public boolean isResuleShowing () {
        return popupWindowResult.isShowing () && popupWindowResult.getHeight () > 0;
    }

    public void setResultClickListener (OnItemClickListener listener) {
        lv.setOnItemClickListener (listener);
    }

    public void setResultLongClickListener (OnItemLongClickListener listener) {
        lv.setOnItemLongClickListener (listener);
    }

    public void setResultTouchListener (OnTouchListener listener) {
        popupWindowResult.setTouchInterceptor (listener);
    }

    /**
     * 是否显示中
     */
    public boolean isShowing () {
        boolean isShowing = false;
        if (View.VISIBLE == getVisibility ()) {
            isShowing = true;
        } else if (View.GONE == getVisibility () || View.INVISIBLE == getVisibility ()) {
            isShowing = false;
        }
        return isShowing;
    }
}
