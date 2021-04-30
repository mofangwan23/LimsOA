package cn.flyrise.android.library.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;

import cn.flyrise.feep.R;

public class SearchEditTextImpl extends RelativeLayout {
    private Context context = null;
    private View view;
    private SearchEditText searchEditText;
    private RelativeLayout layout = null;
    private OnTextWatcherListener listener;
    private long startTime;

    public SearchEditTextImpl(Context context) {
        super(context);
        this.context = context;
        find();
    }

    public SearchEditTextImpl(Context context, AttributeSet attr) {
        super(context, attr);
        this.context = context;
        find();
        this.addView(view, new LayoutParams(context, attr));
    }

    private void find() {
        view = LayoutInflater.from(context).inflate(R.layout.searchedit_layout, null);
        searchEditText = (SearchEditText) view.findViewById(R.id.search_edittext);
        searchEditText.addTextChangedListener(new EditChangedListener());
        layout = (RelativeLayout) view.findViewById(R.id.search_layout);
        layout.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                layoutBlur();
                return false;
            }
        });

        searchEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                SearchEditText.isIconLeft = hasFocus;
            }
        });
    }

    /**
     * 设置标题
     *
     * @param hint
     */
    public void setHint(String hint) {
        searchEditText.setHint(hint);
    }

    public void layoutBlur() {
        if (SearchEditText.isIconLeft) {
            SearchEditText.isIconLeft = false;
        }
        searchEditText.setFocusable(false);
        searchEditText.setFocusableInTouchMode(false);
        searchEditText.clearFocus();
    }

    class EditChangedListener implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            listener.beforeTextChanged(s, start, count, after);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (TextUtils.isEmpty(getEditText())) {
                listener.onTextChanged(s, start, before, count);
            }
            else if (System.currentTimeMillis() - startTime > 300) {
                listener.onTextChanged(s, start, before, count);
                startTime = System.currentTimeMillis();
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            listener.afterTextChanged(s);
        }
    }


    public String getEditText() {
        return String.valueOf(searchEditText.getText()).trim();
    }

    public EditText getEditTextView() {
        return searchEditText;
    }

    //让搜索框失去焦距
    public void setEditTextLoseFocus() {
        if (!TextUtils.isEmpty(searchEditText.getText().toString().trim())) {
            searchEditText.setText("");
        }
        layoutBlur();
    }


    public interface OnTextWatcherListener {
        /**
         * 输入之前
         */
        void beforeTextChanged(CharSequence s, int start, int count, int after);

        /**
         * 输入之中
         */
        void onTextChanged(CharSequence s, int start, int before, int count);

        /**
         * 输入之后
         */
        void afterTextChanged(Editable s);
    }

    /**
     * 设置搜索框数据改变时监听加载事件
     */
    public void setOnTextWatcherListener(OnTextWatcherListener listener) {
        this.listener = listener;
    }

    public void setOnDeleteButtonClickListener(SearchEditText.OnDeleteButtonClickListener listener) {
        searchEditText.setOnDeleteButtonClickListener(listener);
    }

    public View getSearchLayout() {
        return layout;
    }

}
