package cn.flyrise.feep.core.watermark;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import cn.flyrise.feep.core.R;
import cn.flyrise.feep.core.base.views.WaterMarkView;
import cn.flyrise.feep.core.common.utils.PixelUtil;

/**
 * @author ZYP
 * @since 2017-09-06 15:15
 */
public class WMSimplePaint implements IWMPaint {

	private Activity mTarget;                 // 水印依附对象
	private String mText;                     // 水印文本

	private View mCanvas;                     // 水印画布
	private ViewGroup mWaterMarkContainer;    // 画布上绘制水印的容器
	private WaterMarkView mWaterMarkPager;    // 真正绘制水印的画纸

	public WMSimplePaint(Activity target, String text) {
		this.mText = text;
		this.mTarget = target;
	}

	@Override public void draw() {
		if (mCanvas == null) {
			mCanvas = newCanvas(mTarget);
			if (mCanvas == null) {
				throw new NullPointerException("Could not to create the watermark canvas.");
			}

			mWaterMarkContainer = (ViewGroup) mCanvas.findViewById(R.id.waterMarkContainer);
			mWaterMarkPager = (WaterMarkView) mCanvas.findViewById(R.id.waterMarkPager);
			if (mWaterMarkContainer == null) {
				throw new NullPointerException("Could not found the watermark container in canvas.");
			}

			// 1. 绘制顶部透明视图(因为布局侵入到状态栏，需要计算这部分的距离)
			measureMarginTop(mCanvas.findViewById(R.id.waterMarkTop));
		}

		if (mWaterMarkPager == null) {
			throw new NullPointerException("Could not found the watermark page in canvas.");
		}

		// 2. 设置水印
		mWaterMarkPager.setWaterMark(mText);
		mWaterMarkPager.startDraw();
	}

	@Override public void update(int x, int y) {
		if (mWaterMarkContainer != null) {
			mWaterMarkContainer.scrollTo(x, y);
		}
	}

	private View newCanvas(Activity target) {
		ViewGroup rootView = (ViewGroup) target.findViewById(android.R.id.content);
		View waterMarkView = LayoutInflater.from(target).inflate(R.layout.core_watermark, null);
		rootView.addView(waterMarkView);
		return waterMarkView;
	}

	private void measureMarginTop(View topTransparentView) {
		LayoutParams layoutParams = topTransparentView.getLayoutParams();
		layoutParams.height = PixelUtil.dipToPx(60);
		topTransparentView.setLayoutParams(layoutParams);
	}

}
