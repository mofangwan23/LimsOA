package cn.flyrise.feep.commonality.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.PixelUtil;

public class CordovaButton extends RelativeLayout {
    private final View view;
    private ImageButton imgLeft;
    private ImageButton imgRight;
    private ImageButton imgReload;
    private ImageButton imgFinish;

    public CordovaButton(Context context) {
        this(context, null);
    }

    public CordovaButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        view = LayoutInflater.from(context).inflate(R.layout.cordova_button, null);
        findView();
        this.addView(view, new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, PixelUtil.dipToPx(42)));
    }

    private void findView() {
        imgLeft = (ImageButton) view.findViewById(R.id.button_left);
        imgRight = (ImageButton) view.findViewById(R.id.button_right);
        imgFinish = (ImageButton) view.findViewById(R.id.button_finish);
        imgReload = (ImageButton) view.findViewById(R.id.button_reload);
    }

    public void setLeftBtnClickListener(OnClickListener listener) {
        imgLeft.setOnClickListener(listener);
    }

    public void setRightBtnClickListener(OnClickListener listener) {
        imgRight.setOnClickListener(listener);
    }

    public void setFinishBtnClickListener(OnClickListener listener) {
        imgFinish.setOnClickListener(listener);
    }

    public void setReloadBtnClickListener(OnClickListener listener) {
        imgReload.setOnClickListener(listener);
    }

    @Override
    public void invalidate() {
        super.invalidate();
    }

}
