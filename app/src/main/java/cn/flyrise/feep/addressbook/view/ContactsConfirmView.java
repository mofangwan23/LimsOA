package cn.flyrise.feep.addressbook.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.flyrise.feep.R;

/**
 * @author ZYP
 * @since 2016-12-12 17:50
 */
public class ContactsConfirmView extends RelativeLayout {

    private TextView mTvSelectedResult;
    private Button mBtnConfirm;

    public ContactsConfirmView(Context context) {
        this(context, null);
    }

    public ContactsConfirmView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContactsConfirmView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View.inflate(context, R.layout.view_contact_confirm, this);

        mTvSelectedResult = (TextView) findViewById(R.id.tvSelectResult);
        mBtnConfirm = (Button) findViewById(R.id.btnConfirm);
    }

    public void setConfirmClickListener(OnClickListener clickListener) {
        if (mBtnConfirm != null) {
            mBtnConfirm.setOnClickListener(clickListener);
        }
    }

    public void setPreviewClickListener(OnClickListener clickListener) {
        if (mTvSelectedResult != null) {
            mTvSelectedResult.setOnClickListener(clickListener);
        }
    }

    public void updateText(String text) {
        if (mTvSelectedResult != null) {
            mTvSelectedResult.setText(text);
        }
    }


}
