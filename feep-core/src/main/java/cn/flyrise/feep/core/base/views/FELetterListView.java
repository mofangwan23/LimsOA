package cn.flyrise.feep.core.base.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.PixelUtil;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

public class FELetterListView extends View {

	private OnTouchingLetterChangedListener onTouchingLetterChangedListener;
	//	private final String[] defaultLetter = {"☆",
//			"A", "B", "C", "D", "E", "F", "G",
//			"H", "I", "J", "K", "L", "M", "N",
//			"O", "P", "Q", "R", "S", "T",
//			"U", "V", "W", "X", "Y", "Z",
//			"#"};
	private final int MAXSIZE = 28; //26个字母加上* 和 #
	private List<String> showLetters = new ArrayList<>();
	private int choose = -1;
	private final Paint paint = new Paint();
	private boolean showBkg = false;
	private int mContentHeight;

	public FELetterListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public FELetterListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public FELetterListView(Context context) {
		super(context);
	}

	public void setShowLetters(List<String> letters) {
		this.showLetters.clear();
		Observable.from(letters)
				.filter(s -> s.charAt(0) >= 'A' && s.charAt(0) <= 'Z').toList()
				.subscribe(strings -> {
					showLetters = strings;
					showLetters.add(0, "☆");
					showLetters.add("#");
					invalidate();
				}, exception -> exception.printStackTrace());
	}

	@Override
	protected void onDraw(Canvas canvas) {
//        if (showBkg) {
//            canvas.drawColor(Color.parseColor("#40000000"));
//        }
		if (CommonUtil.isEmptyList(showLetters)) {
			return;
		}
		final int height = getHeight();
		final int width = getWidth();
		final int singleHeight = height / MAXSIZE;
		mContentHeight = singleHeight * showLetters.size();
		for (int i = 0; i < showLetters.size(); i++) {
			paint.setColor(Color.parseColor("#89000000"));
			paint.setAntiAlias(true);
			paint.setTextSize(PixelUtil.dipToPx(12));
			if (i == choose) {
				paint.setColor(Color.parseColor("#3399ff"));
				paint.setFakeBoldText(true);

			}
			final float xPos = width / 2 - paint.measureText(showLetters.get(i)) / 2;
			final float yPos = singleHeight * i + singleHeight;
			canvas.drawText(showLetters.get(i), xPos, yPos, paint);
			paint.reset();
		}
		super.onDraw(canvas);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		final float y = event.getY();
		final int oldChoose = choose;
		final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
		final int c = (int) (y / mContentHeight * showLetters.size());

		switch (action) {
			case MotionEvent.ACTION_DOWN:
				showBkg = true;
				if (oldChoose != c && listener != null) {
					if (c > 0 && c < showLetters.size()) {
						listener.onTouchingLetterChanged(showLetters.get(c));
						choose = c;
						invalidate();
					}
				}

				break;
			case MotionEvent.ACTION_MOVE:
				if (oldChoose != c && listener != null) {
					if (c > 0 && c < showLetters.size()) {
						listener.onTouchingLetterChanged(showLetters.get(c));
						choose = c;
						invalidate();
					}
				}
				break;
			case MotionEvent.ACTION_UP:
				showBkg = false;
				choose = -1;
				invalidate();
				break;
		}
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);
	}

	public void setOnTouchingLetterChangedListener(OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
		this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
	}

	public interface OnTouchingLetterChangedListener {

		void onTouchingLetterChanged(String s);
	}

}
