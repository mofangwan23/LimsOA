package cn.flyrise.feep.addressbook.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.LinearLayout;


/**
 * @author ZYP
 * @since 2017-04-11 14:15
 */
public class LetterFloatingView extends LinearLayout {

    public LetterFloatingView(Context context) {
        this(context, null);
    }
    private OnKeyListener mKeyListener = null;

    public LetterFloatingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LetterFloatingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, cn.flyrise.feep.R.layout.addressbookoverlay, this);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                || event.getKeyCode() == KeyEvent.KEYCODE_SETTINGS) {
            if (mKeyListener != null) {
                mKeyListener.onKey(this, KeyEvent.KEYCODE_BACK, event);
                return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }



    @Override
    public void setOnKeyListener(OnKeyListener keyListener) {
        mKeyListener = keyListener;
        super.setOnKeyListener(keyListener);
    }
}
