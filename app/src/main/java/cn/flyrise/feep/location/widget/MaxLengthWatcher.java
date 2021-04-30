package cn.flyrise.feep.location.widget;

import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * 新建：陈冕;
 * 日期： 2017-11-20-14:33.
 * 监听输入内容是否超出最大长度，并设置光标位置
 */

public class MaxLengthWatcher implements TextWatcher {

    private int maxLen = 0;
    private EditText editText = null;

    private OnTextChatLengthListener mLengthListener;

    public MaxLengthWatcher setMaxLen(int maxLen) {
        this.maxLen = maxLen;
        return this;
    }

    public MaxLengthWatcher setEditText(EditText editText) {
        this.editText = editText;
        return this;
    }

    public MaxLengthWatcher setOnTextChatLengthListener(OnTextChatLengthListener lengthListener) {
        this.mLengthListener = lengthListener;
        return this;
    }


    public void afterTextChanged(Editable arg0) {

    }

    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                  int arg3) {

    }

    public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        Editable editable = editText.getText();
        int len = editable.length();

        if (len > maxLen) {
            int selEndIndex = Selection.getSelectionEnd(editable);
            String str = editable.toString();
            //截取新字符串
            String newStr = str.substring(0, maxLen);
            editText.setText(newStr);
            editable = editText.getText();

            //新字符串的长度
            int newLen = editable.length();
            //旧光标位置超过字符串长度
            if (selEndIndex > newLen) {
                selEndIndex = editable.length();
            }
            //设置新光标所在的位置
            Selection.setSelection(editable, selEndIndex);
            mLengthListener.onChatNumber(selEndIndex + "/" + maxLen);
        } else {
            mLengthListener.onChatNumber(len + "/" + maxLen);
        }
    }

    public interface OnTextChatLengthListener {
        void onChatNumber(String text);
    }


}
