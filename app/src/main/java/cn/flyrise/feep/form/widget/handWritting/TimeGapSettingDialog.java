package cn.flyrise.feep.form.widget.handWritting;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import cn.flyrise.feep.core.common.utils.PixelUtil;

/**
 * 手写间隔时间设定对话框
 * 
 * @author Jingwei
 * @version 1.0
 */
class TimeGapSettingDialog extends Dialog {

    private final Context                  context;
    private OnOkListener     mListener;
    private final int                      initialTimeGap;
    private static final int currentTimeTextId = 80000010;
    private static final int sbId              = 80000011;

    public interface OnOkListener {
        void onSetTimeGap(int timeGap);
    }

    public void setOnOkListener(OnOkListener l) {
        this.mListener = l;
    }

    public TimeGapSettingDialog(Context context, int initialValue) {
        super(context, android.R.style.Theme_DeviceDefault_Dialog_NoActionBar);// 改成这样才正常
        this.context = context;
        this.initialTimeGap = initialValue;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final RelativeLayout rl = new RelativeLayout(context);
        final RelativeLayout.LayoutParams l = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        rl.setGravity(Gravity.CENTER);
        rl.setLayoutParams(l);

        final SeekBar sb = new SeekBar(context);
        final RelativeLayout.LayoutParams sbLp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 50);
        sbLp.addRule(RelativeLayout.CENTER_IN_PARENT);
        sbLp.leftMargin = 15;
        sbLp.rightMargin = 15;
        sb.setLayoutParams(sbLp);
        sb.setId(sbId);
        sb.setMax(1900);
        sb.setProgress(this.initialTimeGap - 100);

        final Button button = new Button(context);
        button.setText("OK");
        final RelativeLayout.LayoutParams buttonLp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        buttonLp.width = LayoutParams.WRAP_CONTENT;
        buttonLp.height = LayoutParams.WRAP_CONTENT;
        buttonLp.addRule(RelativeLayout.BELOW, sb.getId());
        buttonLp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        buttonLp.topMargin = 35;
        button.setLayoutParams(buttonLp);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onSetTimeGap(sb.getProgress() + 100);
                TimeGapSettingDialog.this.dismiss();
            }
        });

        final TextView currentTimeText = new TextView(context);
        currentTimeText.setId(currentTimeTextId);
        currentTimeText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        currentTimeText.setTextColor(0xff0000ff);
        currentTimeText.setText(Integer.toString(this.initialTimeGap) + " ms");
        final RelativeLayout.LayoutParams cttLp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        cttLp.bottomMargin = 25;
        cttLp.addRule(RelativeLayout.ABOVE, sb.getId());
        cttLp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        currentTimeText.setLayoutParams(cttLp);

        final TextView tv = new TextView(context);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
        tv.setTextColor(0xff0000ff);
        tv.setText("设置间隔时间");
        final RelativeLayout.LayoutParams textLp = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        textLp.bottomMargin = PixelUtil.dipToPx(20);
        textLp.addRule(RelativeLayout.ABOVE, currentTimeText.getId());
        textLp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        tv.setLayoutParams(textLp);

        sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentTimeText.setText(Integer.toString(progress + 100) + " ms");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });

        rl.addView(sb);
        rl.addView(button);
        rl.addView(tv);
        rl.addView(currentTimeText);

        this.setContentView(rl);

        final WindowManager manager = getWindow().getWindowManager();
        final int height = (int) (manager.getDefaultDisplay().getHeight() * 0.6f);
        final int width = (int) (manager.getDefaultDisplay().getWidth() * 0.75f);
        final WindowManager.LayoutParams lp = getWindow().getAttributes(); // 获取对话框当前的参数值
        lp.height = height; // 高度设置为屏幕的0.5
        lp.width = width; // 宽度设置为屏幕的0.8
        lp.alpha = 0.7f;
        getWindow().setAttributes(lp);
    }
}
