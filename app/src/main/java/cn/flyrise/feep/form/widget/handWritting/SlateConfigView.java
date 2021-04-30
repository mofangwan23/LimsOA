package cn.flyrise.feep.form.widget.handWritting;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.PixelUtil;

/**
 * 手写板设置工具条
 * @author Jingwei
 * @version 1.0
 */
public class SlateConfigView extends RelativeLayout {

	// 由于很多控件的布局属性要动态设定 所以没有用xml布局而是直接在代码中布局
	// 不过还是建议改成用xml写好基本布局再在代码中修改属性

	private int panelSwitchId = 0x12345670;

	private Button panelSwitch;               // 控制设置面板隐藏/显示的开关
	private FESlate slate;                     // 要控制的FESlate
	private final Context context;
	private int buttonHeight;              // 每个按钮的高度
	private int buttonWidth;               // 每个按钮的宽度
	private int buttonGap;                 // 按钮之间的水平距离
	boolean isOpen = false;

	public SlateConfigView(Context context) {
		super(context);
		this.context = context;

		init1();

		this.setBackgroundColor(Color.rgb(255, 0, 255));
		this.setPadding(0, 0, 0, 0);
	}

	private void init1() {
		// layout后回调 因为要用到SlateConfigView本身的尺寸
		this.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				SlateConfigView.this.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				// 一开始只是一个按钮 宽度填满view 宽高相等
				LayoutParams lp = (LayoutParams) SlateConfigView.this.getLayoutParams();
				lp.height = PixelUtil.dipToPx(35);
				lp.width = PixelUtil.dipToPx(60);
				SlateConfigView.this.setLayoutParams(lp);
				LayoutParams lps = new LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, PixelUtil.dipToPx(25));
				lps.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
				lps.bottomMargin = PixelUtil.dipToPx(6);
				// 设定开关按钮的属性
				panelSwitch = new Button(context);
				panelSwitch.setPadding(0, 0, 0, 0);
				panelSwitch.setBackgroundResource(R.drawable.workplan_time_btn);
				panelSwitch.setId(panelSwitchId);
				panelSwitch.setText(getContext().getResources().getString(R.string.handwrite_deleteword));
				panelSwitch.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
				panelSwitch.setTextColor(context.getResources().getColor(R.color.userinfo_detail_title));
				panelSwitch.setGravity(Gravity.CENTER);
				// 设定开关按钮的点击响应事件
				panelSwitch.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// 将这个开关当作删除按钮
						slate.deleteLastCharOnWhiteboard();
						/***
						 * RelativeLayout.LayoutParams lp = (LayoutParams) SlateConfigView.this.getLayoutParams(); if(!isMenuShowing) { //当前菜单没有显示 则弹出菜单 lp.height = LayoutParams.MATCH_PARENT; SlateConfigView.this.setLayoutParams(lp); lp = (LayoutParams) panelSwitch.getLayoutParams(); lp.height = panelSwitch.getWidth(); panelSwitch.setLayoutParams(lp);
						 * panelSwitch.setImageDrawable(getResources().getDrawable(R.drawable.handwritting_up_arrow)); SlateConfigView.this.setBackgroundColor(0x80000000); showMenu(); isMenuShowing = true; } else { //当前菜单显示中 则收起菜单 SlateConfigView.this.removeViews(1, numOfButtons - 1); lp.height = SlateConfigView.this.getWidth(); SlateConfigView.this.setLayoutParams(lp);
						 * SlateConfigView.this.setBackgroundColor(Color.TRANSPARENT); panelSwitch.setImageDrawable(getResources().getDrawable(R.drawable.handwritting_gear)); isMenuShowing = false; }
						 */
					}

				});

				SlateConfigView.this.addView(panelSwitch, lps);
				/**
				 * panelSwitch.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
				 *
				 * @Override public void onGlobalLayout() {
				 */
			}
		});

	}

	public SlateConfigView(Context context, AttributeSet as) {
		super(context, as);
		this.context = context;
		init1();
	}

	// 两个矩阵 用于做颜色变换 实现按钮的被点击时外观的改变
	private float[] BT_SELECTED = new float[]{2, 0, 0, 0, 2, 0, 2, 0, 0, 2, 0, 0, 2, 0, 2, 0, 0, 0, 1, -0.3f};

	private float[] BT_NOT_SELECTED = new float[]{1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0};

	// 触摸按钮时变换颜色 以显示按下/松开的状态变换
	private final OnTouchListener imageButtonOnTouchListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (!(v instanceof ImageButton)) {
				return false;
			}
			ImageButton ib = (ImageButton) v;
			if (null == ib.getDrawable()) {
				return false;
			}
			Drawable d = ib.getDrawable();

			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				d.setColorFilter(new ColorMatrixColorFilter(BT_SELECTED));
			}
			else if (event.getAction() == MotionEvent.ACTION_UP) {
				d.setColorFilter(new ColorMatrixColorFilter(BT_NOT_SELECTED));
			}
			ib.setImageDrawable(d);
			return false;
		}

	};

	/**
	 * @param slate 要控制的FESlate对象 设定该设置面板要控制的FESlate对象
	 */
	public void setSlate(FESlate slate) {
		this.slate = slate;
	}

}
