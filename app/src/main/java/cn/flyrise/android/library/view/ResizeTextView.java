/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-11-13 下午04:25:55
 */

package cn.flyrise.android.library.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;

import cn.flyrise.feep.R;


/**
 * 类功能描述：</br> 可自动调整字体大小的TextView 根据父View分配的宽度自动调整字体大小，以适应控件宽度
 * @author zms
 * @version 1.0 </p> 修改时间：</br> 修改备注：</br>
 */
public class ResizeTextView extends TextView {

	/**
	 * 行数是否可变,如果行数是不可变的则缩小字体至固定的行数， 如果行数是可变的则缩小字体至固定的高度
	 */
	private boolean lineCountResizeable = false;
	private int maxLines = 1;
	/**
	 * 重置时字体大小的缩放量
	 */
	private final float resizeAmount = -0.5f;

	/**
	 * 是否需要重新计算
	 */
	private boolean isStale;

	public ResizeTextView(Context context) {
		super(context);
	}

	public ResizeTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ResizeTextView, 0, 0);
		lineCountResizeable = a.getBoolean(R.styleable.ResizeTextView_lineCountResizeable, Boolean.FALSE);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		resizeContentSize();
		super.onDraw(canvas);
	}

	/**
	 * 根据固定行数或者固定高度 重新设置内容的字体大小
	 */
	private void resizeContentSize() {
		if (isStale) {
			final TextPaint itemsPaint = new TextPaint(getPaint());
			if (lineCountResizeable) {
				resizeByFixHeight(itemsPaint);
			}
			else {
				resizeByFixRowCount(itemsPaint);
			}
			isStale = false;
		}
	}

	/**
	 * 行数不可变，缩放至固定行数以容纳所有内容
	 */
	private void resizeByFixRowCount(TextPaint itemsPaint) {
		for (; ; ) {
			final Layout layout = createMeasureLayout(itemsPaint);
			if (layout.getLineCount() > getMaxLines()) {
				itemsPaint.setTextSize(itemsPaint.getTextSize() + resizeAmount);
			}
			else {
				itemsPaint.setTextSize(getPaint().getTextSize());
				break;
			}
		}
		setTextSize(TypedValue.COMPLEX_UNIT_PX, itemsPaint.getTextSize());
	}

	/**
	 * 高度不可变，缩放至固定高度以容纳所有内容
	 */
	private void resizeByFixHeight(TextPaint itemsPaint) {
		int lines = 0;
		for (; ; ) {
			final Layout layout = createMeasureLayout(itemsPaint);
			if (layout.getHeight() > getHeight()) {
				itemsPaint.setTextSize(itemsPaint.getTextSize() + resizeAmount);
			}
			else {
				lines = layout.getLineCount();
				break;
			}
		}

		// 行数是可变的，所以计算后重置行数
		setMaxLines(lines);
		setTextSize(TypedValue.COMPLEX_UNIT_PX, itemsPaint.getTextSize());
	}

	/**
	 * 用于计算内容所占行数或者高度的布局
	 */
	private Layout createMeasureLayout(TextPaint paint) {
		return new StaticLayout(getText().toString(), paint, getWidth() - getPaddingLeft() - getPaddingRight(), Alignment.ALIGN_NORMAL,
				lineSpacingMultiplier, lineAdditionalVerticalPadding, false);
	}

	private float lineSpacingMultiplier = 1.0f;
	private float lineAdditionalVerticalPadding = 0.0f;

	@Override
	public void setLineSpacing(float add, float mult) {
		this.lineAdditionalVerticalPadding = add;
		this.lineSpacingMultiplier = mult;
		super.setLineSpacing(add, mult);
	}

	@Override
	public void setMaxLines(int maxLines) {
		super.setMaxLines(maxLines);
		this.maxLines = maxLines;
		isStale = true;
	}

	@Override
	protected void onTextChanged(CharSequence text, int start, int before, int after) {
		super.onTextChanged(text, start, before, after);
		isStale = true;
	}

	@Override
	public int getMaxLines() {
		return maxLines;
	}

}
