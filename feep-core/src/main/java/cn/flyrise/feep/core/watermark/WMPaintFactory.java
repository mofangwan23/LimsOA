package cn.flyrise.feep.core.watermark;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import cn.flyrise.feep.core.base.views.ListenableScrollView;
import cn.flyrise.feep.core.watermark.WMCanvasCreator.ActivityCanvasCreator;
import cn.flyrise.feep.core.watermark.WMCanvasCreator.ViewGroupCanvasCreator;

/**
 * @author ZYP
 * @since 2017-09-06 16:31
 */
public class WMPaintFactory {

	public static IWMPaint newPaint(Object target, View dependView, String watermark) {
		if (target == null) return null;
		boolean isNeedCreate = target instanceof Activity;
		if (isNeedCreate && dependView == null) {
			return new WMSimplePaint((Activity) target, watermark);
		}

		if (!isNeedCreate) {
			isNeedCreate = target instanceof ViewGroup;
			if (!isNeedCreate) {
				return null;
			}
		}

		return newActivityPaint(target, dependView, newCanvasCreator(target), watermark);
	}

	private static IWMPaint newActivityPaint(Object target, View dependView, WMCanvasCreator creator, String watermark) {
		if (creator == null) return null;

		if (dependView instanceof ListView) {
			return new WMListViewPaint<>(target, (ListView) dependView, creator, watermark);
		}
		else if (dependView instanceof RecyclerView) {
			return new WMRecyclerViewPaint<>(target, (RecyclerView) dependView, creator, watermark);
		}
		else if (dependView instanceof ListenableScrollView) {
			return new WMScrollViewPaint<>(target, (ListenableScrollView) dependView, creator, watermark);
		}
		return null;
	}

	private static WMCanvasCreator newCanvasCreator(Object target) {
		WMCanvasCreator creator = null;
		if (target instanceof Activity) {
			creator = new ActivityCanvasCreator();
		}
		else if (target instanceof ViewGroup) {
			creator = new ViewGroupCanvasCreator();
		}
		return creator;
	}

}
