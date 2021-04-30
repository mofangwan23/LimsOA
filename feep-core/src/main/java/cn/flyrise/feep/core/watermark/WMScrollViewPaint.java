package cn.flyrise.feep.core.watermark;

import cn.flyrise.feep.core.base.views.ListenableScrollView;

/**
 * @author ZYP
 * @since 2017-09-06 15:15
 */
public class WMScrollViewPaint<T> extends WMAbstractPaint<T, ListenableScrollView> {

	public WMScrollViewPaint(T target, ListenableScrollView dependView, WMCanvasCreator<T> creator, String text) {
		super(target, dependView, creator, text);
	}

	@Override public int measureWaterMarkContainerHeight() {
		return mDependView.getChildAt(0).getHeight();
	}

	@Override public void dispatchScrollEvent() {
		mDependView.addOnScrollChangeListener((scrollY, lastScrollY) -> getWaterMarkContainer().scrollTo(0, scrollY));
	}

	@Override protected int resetDelayTime() {
		return 1000;
	}

}
