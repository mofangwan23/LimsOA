package cn.flyrise.feep.core.image.loader;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.PixelUtil;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ZYP
 * @since 2016/5/30 09:57
 */
public class AvatarDrawable extends Drawable {

	private static final int AVATAR_BACKGROUND_COLOR[] = {0xFF77C06C, 0xFF9A89B9, 0xFF5D97F6, 0xFFFF943C, 0xFFA1887E};
	private Paint mPaint;
	private String mText;
	private String mUserId;
	private int mBackgroundColor;

	AvatarDrawable(String userId, String text) {
		mText = parseName(text.toUpperCase());
		mUserId = userId;
		mBackgroundColor = getBackgroundColor();
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	}

	private int getBackgroundColor() {
		if(TextUtils.isEmpty(mUserId)){
			return AVATAR_BACKGROUND_COLOR[0];
		}
		int random;
		try {
			int uid = Math.abs(mUserId.hashCode());
			random = uid % AVATAR_BACKGROUND_COLOR.length;
		} catch (Exception ex) {
			random = 0;
		}
		return AVATAR_BACKGROUND_COLOR[random];
	}

	public void setBackgroundColor(int color) {
		this.mBackgroundColor = color;
		invalidateSelf();
	}

	public void setText(String text) {
		this.mText = parseName(text);
		invalidateSelf();
	}

	@Override public void draw(@NonNull Canvas canvas) {
		Rect bounds = getBounds();
		int width = bounds.width();
		int height = bounds.height();
		int size = Math.min(width, height);
//        float radius = size / 2.0F;
		mPaint.setColor(mBackgroundColor);
//        canvas.drawCircle(radius, radius, radius, mPaint);
		canvas.drawRoundRect(new RectF(0.0f, 0.0f, (float) size, (float) size), 8.0f, 8.0f, mPaint);

		mPaint.setColor(Color.WHITE);
		mPaint.setTextSize(isChinese(mText) ? size / 3.2244F : size / 2.0F);

		Rect mTextBounds = new Rect();
		mPaint.getTextBounds(mText, 0, mText.length(), mTextBounds);
		float startX = size / 2.0F - mTextBounds.width() / 2.0F;
		float startY = size / 2.0F + mTextBounds.height() / 2.0F;
		if (isChinese(mText)) startY -= PixelUtil.dipToPx(CoreZygote.getContext(), 1.2F);
		canvas.drawText(mText, startX, startY, mPaint);
	}

	@Override public void setAlpha(int alpha) {
		mPaint.setAlpha(alpha);
		invalidateSelf();
	}

	@Override public void setColorFilter(ColorFilter cf) {
		mPaint.setColorFilter(cf);
		invalidateSelf();
	}

	@Override public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	private String parseName(String name) {
		if (TextUtils.isEmpty(name)) return name;
		if (isChinese(name) && name.length() > 2) {
			name = name.substring(name.length() - 2, name.length());
		}
		else {
			if (name.length() > 2) name = name.substring(0, 2).toUpperCase();
		}
		return name;
	}

	private boolean isChinese(String str) {
		if (TextUtils.isEmpty(str)) return false;
		final String regEx = "[\u4e00-\u9fa5]";
		final Pattern p = Pattern.compile(regEx);
		final Matcher m = p.matcher(str);
		return m.find();
	}
}
