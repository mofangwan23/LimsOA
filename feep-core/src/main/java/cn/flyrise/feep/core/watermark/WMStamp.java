package cn.flyrise.feep.core.watermark;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ZYP
 * @since 2017-09-06 16:27
 */
public class WMStamp {

	private static final class Singleton {

		private static final WMStamp sInstance = new WMStamp();
	}


	public static WMStamp getInstance() {
		return Singleton.sInstance;
	}

	private String mWaterMark;                   // 水印文本
	private String mBackUpWaterMark;             // 备用水印
	private WaterMarkExecutor mWaterMarkExecutor = new DefaultWaterMarkExecutor();

	/**
	 * 设置水印内容
	 */
	public void setWaterMark(String userName, String department) {
		this.mWaterMark = userName;
		this.mBackUpWaterMark = department;
	}

	public void launchWaterMarkExecutor() {
		this.mWaterMarkExecutor = new ActualWaterMarkExecutor();
	}

	public void resetWaterMarkExecutor() {
		this.mWaterMarkExecutor = new DefaultWaterMarkExecutor();
	}

	public void appendWaterMark(String text) {
		if (TextUtils.isEmpty(text)) {
			this.mWaterMark = mWaterMark + "-" + mBackUpWaterMark;
			return;
		}

		if (text.length() > 4) {
			int length = text.length();
			text = text.substring(length - 4, length);
		}
		this.mWaterMark = mWaterMark + " " + text;
	}


	public void draw(Object target) {
		mWaterMarkExecutor.draw(target, null);
	}

	public void draw(Object target, View dependView) {
		mWaterMarkExecutor.draw(target, dependView);
	}

	public void update(Object target, int x, int y) {
		mWaterMarkExecutor.update(target, x, y);
	}

	public void clearWaterMark(Object object) {
		mWaterMarkExecutor.clear(object);
	}

	private interface WaterMarkExecutor {

		void draw(Object target, View dependView);      // 绘制水印

		void update(Object target, int x, int y);       // 更新水印位置

		void clear(Object target);                      // 清理水印

	}

	private class DefaultWaterMarkExecutor implements WaterMarkExecutor {   // Do nothing.

		@Override public void draw(Object target, View dependView) {
		}

		@Override public void update(Object target, int x, int y) {
		}

		@Override public void clear(Object target) {
		}
	}

	private class ActualWaterMarkExecutor implements WaterMarkExecutor {

		private Map<Object, IWMPaint> mWaterMarkPainterMap = new HashMap<>();

		@Override public void draw(Object target, View dependView) {
			IWMPaint paint = mWaterMarkPainterMap.get(target);
			if (paint == null) {
				paint = WMPaintFactory.newPaint(target, dependView, mWaterMark);
				if (paint != null) {
					mWaterMarkPainterMap.put(target, paint);
				}
			}

			if (paint != null) {
				paint.draw();
			}
		}

		@Override public void update(Object target, int x, int y) {
			if (!mWaterMarkPainterMap.containsKey(target)
					|| !(target instanceof Activity)) {
				return;
			}

			IWMPaint painter = mWaterMarkPainterMap.get(target);
			if (painter != null) {
				painter.update(x, y);
			}
		}

		@Override public void clear(Object target) {
			if (mWaterMarkPainterMap.containsKey(target)) {
				mWaterMarkPainterMap.remove(target);
			}
		}
	}

	public String getWaterMarkText() {
		if (mWaterMarkExecutor instanceof DefaultWaterMarkExecutor) {
			return null;
		}
		return mWaterMark;
	}

}
