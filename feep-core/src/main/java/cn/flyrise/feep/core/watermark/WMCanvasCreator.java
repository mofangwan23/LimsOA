package cn.flyrise.feep.core.watermark;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import cn.flyrise.feep.core.R;
import cn.flyrise.feep.core.common.FELog;

/**
 * @author ZYP
 * @since 2017-09-06 15:21
 */
public interface WMCanvasCreator<T> {

	View newCanvas(T target);

	void resetTopTransparentHeight(View topTransparentView, View dependView);

	class ActivityCanvasCreator implements WMCanvasCreator<Activity> {

		@Override public View newCanvas(Activity target) {
			ViewGroup rootView = (ViewGroup) target.findViewById(android.R.id.content);
			View waterMarkView = LayoutInflater.from(target).inflate(R.layout.core_watermark, null);
			rootView.addView(waterMarkView);
			return waterMarkView;
		}

		@Override public void resetTopTransparentHeight(View topTransparentView, View dependView) {
			int[] location = new int[2];
			dependView.getLocationOnScreen(location);
			LayoutParams layoutParams = topTransparentView.getLayoutParams();
			layoutParams.height = location[1];
			topTransparentView.setLayoutParams(layoutParams);
		}
	}

	class ViewGroupCanvasCreator implements WMCanvasCreator<ViewGroup> {

		@Override public View newCanvas(ViewGroup target) {
			View waterMarkView = LayoutInflater.from(target.getContext()).inflate(R.layout.core_watermark, null);
			target.addView(waterMarkView);
			return waterMarkView;
		}

		@Override public void resetTopTransparentHeight(View topTransparentView, View dependView) {
			// Don't need to implement this.
		}
	}
}
