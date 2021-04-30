package cn.flyrise.feep.workplan7.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import cn.flyrise.feep.core.common.utils.PixelUtil;
import java.util.ArrayList;
import java.util.List;

public class WheelSelectionView extends ScrollView {

	public interface OnWheelViewListener {

		void onSelected(int selectedIndex, String item);
	}

	public static final int OFF_SET_DEFAULT = 1;

	private int offset = OFF_SET_DEFAULT; // 偏移量（需要在最前面和最后面补全）
	private List<String> items;
	private LinearLayout views;
	private Runnable scrollerTask;
	private int displayItemCount; // 每页显示的数量
	private int selectedIndex = 1;
	private int initialY;
	private int newCheck = 50;
	private int[] selectedAreaBorder;   // 获取选中区域的边界
	private Paint paint;
	private int itemHeight = 0;
	private int viewWidth;

	private int normalTextColor = Color.parseColor("#04121A");      // 正常的文本颜色
	private int unableTextColor = Color.parseColor("#9DA3A6");     // 不可用的文本颜色

	public WheelSelectionView(Context context) {
		this(context, null);
	}

	public WheelSelectionView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public WheelSelectionView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.setVerticalScrollBarEnabled(false);
		views = new LinearLayout(context);
		views.setOrientation(LinearLayout.VERTICAL);
		this.addView(views);

		scrollerTask = () -> {
			int newY = getScrollY();
			if (initialY - newY == 0) { // stopped
				final int remainder = initialY % itemHeight;
				final int divided = initialY / itemHeight;
				if (remainder == 0) {
					selectedIndex = divided + offset;
					onSeletedCallBack();
				}
				else {
					if (remainder > itemHeight / 2) {
						WheelSelectionView.this.post(() -> {
							WheelSelectionView.this.smoothScrollTo(0, initialY - remainder + itemHeight);
							selectedIndex = divided + offset + 1;
							onSeletedCallBack();
						});
					}
					else {
						WheelSelectionView.this.post(() -> {
							WheelSelectionView.this.smoothScrollTo(0, initialY - remainder);
							selectedIndex = divided + offset;
							onSeletedCallBack();
						});
					}
				}
			}
			else {
				initialY = getScrollY();
				WheelSelectionView.this.postDelayed(scrollerTask, newCheck);
			}
		};
	}

	public void setItems(List<String> list) {
		if (null == items) items = new ArrayList<>();
		items.clear();
		items.addAll(list);

		// 前面和后面补全
		for (int i = 0; i < offset; i++) {
			items.add(0, "");
			items.add("");
		}

		initData();
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
		selectedIndex = offset;
	}

	public void startScrollerTask() {
		initialY = getScrollY();
		this.postDelayed(scrollerTask, newCheck);
	}

	private void initData() {
		views.removeAllViews();
		displayItemCount = offset * 2 + 1;
		for (String item : items) {
			views.addView(createView(item));
		}
		refreshItemView(0);
	}

	private TextView createView(String item) {
		TextView tv = new TextView(getContext());
		tv.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		tv.setSingleLine(true);
		tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
		tv.setText(item);
		tv.setGravity(Gravity.CENTER);
		int padding = PixelUtil.dipToPx(getContext(), 10);
		tv.setPadding(padding, padding, padding, padding);
		if (0 == itemHeight) {
			itemHeight = getViewMeasuredHeight(tv);
			views.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, itemHeight * displayItemCount));
			LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) this.getLayoutParams();
			this.setLayoutParams(new LinearLayout.LayoutParams(lp.width, itemHeight * displayItemCount));
		}
		return tv;
	}

	@Override protected void onScrollChanged(int l, int t, int oldl, int oldt) {
		super.onScrollChanged(l, t, oldl, oldt);
		refreshItemView(t);
	}

	private void refreshItemView(int y) {
		int position = y / itemHeight + offset;
		int remainder = y % itemHeight;
		int divided = y / itemHeight;

		if (remainder == 0) {
			position = divided + offset;
		}
		else {
			if (remainder > itemHeight / 2) {
				position = divided + offset + 1;
			}
		}

		int childSize = views.getChildCount();
		for (int i = 0; i < childSize; i++) {
			TextView itemView = (TextView) views.getChildAt(i);
			if (null == itemView) return;
			if (position == i) {
				itemView.setTextColor(normalTextColor);
			}
			else {
				itemView.setTextColor(unableTextColor);
			}
		}
	}

	private int[] obtainSelectedAreaBorder() {
		if (null == selectedAreaBorder) {
			selectedAreaBorder = new int[2];
			selectedAreaBorder[0] = itemHeight * offset;
			selectedAreaBorder[1] = itemHeight * (offset + 1);
		}
		return selectedAreaBorder;
	}

	@Override public void setBackgroundDrawable(Drawable background) {
		if (viewWidth == 0) viewWidth = ((Activity) getContext()).getWindowManager().getDefaultDisplay().getWidth();

		if (null == paint) {
			paint = new Paint();
			paint.setColor(Color.parseColor("#E4E6E7"));
			paint.setStrokeWidth(PixelUtil.dipToPx(getContext(), 1f));
		}

		background = new Drawable() {
			@Override public void draw(@NonNull Canvas canvas) {
				canvas.drawLine(0, obtainSelectedAreaBorder()[0], viewWidth, obtainSelectedAreaBorder()[0], paint);
				canvas.drawLine(0, obtainSelectedAreaBorder()[1], viewWidth, obtainSelectedAreaBorder()[1], paint);
			}

			@Override public void setAlpha(int alpha) { }

			@Override public void setColorFilter(ColorFilter cf) { }

			@Override public int getOpacity() {
				return PixelFormat.UNKNOWN;
			}
		};

		super.setBackgroundDrawable(background);

	}

	@Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		viewWidth = w;
		setBackgroundDrawable(null);
	}

	/**
	 * 选中回调
	 */
	private void onSeletedCallBack() {
		if (null != onWheelViewListener) onWheelViewListener.onSelected(selectedIndex, items.get(selectedIndex));
	}

	public void setSeletion(int position) {
		final int p = position;
		selectedIndex = p + offset;
		this.post(() -> WheelSelectionView.this.smoothScrollTo(0, p * itemHeight));
	}

	public String getSeletedItem() {
		return items.get(selectedIndex);
	}

	public int getSeletedIndex() {
		return selectedIndex - offset;
	}

	@Override public void fling(int velocityY) {
		super.fling(velocityY / 3);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override public boolean onTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_UP) startScrollerTask();
		return super.onTouchEvent(ev);
	}

	private OnWheelViewListener onWheelViewListener;

	public void setOnWheelViewListener(OnWheelViewListener onWheelViewListener) {
		this.onWheelViewListener = onWheelViewListener;
	}

	private int getViewMeasuredHeight(View view) {
		int width = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
		view.measure(width, expandSpec);
		return view.getMeasuredHeight();
	}
}
