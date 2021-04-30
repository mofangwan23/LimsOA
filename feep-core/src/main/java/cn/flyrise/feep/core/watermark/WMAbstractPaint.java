package cn.flyrise.feep.core.watermark;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import cn.flyrise.feep.core.R;
import cn.flyrise.feep.core.base.views.WaterMarkView;

/**
 * @author ZYP
 * @since 2017-09-06 15:49
 */
public abstract class WMAbstractPaint<T, V extends View> implements IWMPaint {

	protected T mTarget;                        // 水印依附对象（Activity or ViewGroup）
	protected String mText;                     // 水印文本
	protected View mCanvas;                     // 水印画布
	protected ViewGroup mWaterMarkContainer;    // 画布上绘制水印的容器
	protected WaterMarkView mWaterMarkPager;    // 真正绘制水印的画纸

	protected V mDependView;
	private WMCanvasCreator mCanvasCreator;

	public WMAbstractPaint(T target, V dependView, WMCanvasCreator<T> creator, String text) {
		this.mText = text;
		this.mTarget = target;
		this.mCanvasCreator = creator;
		this.mDependView = dependView;
	}

	@Override public void draw() {
		if (mCanvas == null) {
			mCanvas = mCanvasCreator.newCanvas(mTarget);
			if (mCanvas == null) {
				throw new NullPointerException("Could not to create the watermark canvas.");
			}

			mWaterMarkContainer = mCanvas.findViewById(R.id.waterMarkContainer);
			mWaterMarkPager = mCanvas.findViewById(R.id.waterMarkPager);
			if (mWaterMarkContainer == null) {
				throw new NullPointerException("Could not found the watermark container in canvas.");
			}

			// 1. 绘制顶部透明视图(因为布局侵入到状态栏，需要计算这部分的距离)
			mCanvasCreator.resetTopTransparentHeight(mCanvas.findViewById(R.id.waterMarkTop), mDependView);
		}

		if (mWaterMarkPager == null) {
			throw new NullPointerException("Could not found the watermark page in canvas.");
		}

		// 2. 设置水印
		mWaterMarkPager.setWaterMark(mText);

		// 3. 重新计算水印容器的高度
		getWaterMarkContainer().postDelayed(() -> resetWaterMarkContainerHeight(), resetDelayTime());

		// 4. 处理滑动事件
		dispatchScrollEvent();
	}

	@Override public void update(int x, int y) {
		getWaterMarkContainer().scrollTo(x, y);
	}

	protected ViewGroup getWaterMarkContainer() {
		return mWaterMarkContainer;
	}

	protected void resetWaterMarkContainerHeight() {
		mWaterMarkPager.startDraw();
		LayoutParams layoutParams = getWaterMarkContainer().getLayoutParams();
		layoutParams.height = measureWaterMarkContainerHeight();
		getWaterMarkContainer().setLayoutParams(layoutParams);
		getWaterMarkContainer().scrollTo(0, 0);
	}

	protected int resetDelayTime() {
		return 500;
	}

	/**
	 * 测量水印布局的整体高度
	 */
	public abstract int measureWaterMarkContainerHeight();

	/**
	 * 处理水印的滑动事件
	 */
	public abstract void dispatchScrollEvent();
}
