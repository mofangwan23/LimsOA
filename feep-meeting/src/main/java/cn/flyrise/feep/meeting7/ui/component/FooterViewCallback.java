package cn.flyrise.feep.meeting7.ui.component;

import android.view.View;

public interface FooterViewCallback {

	void onLoadingMore(View footerView);

	void onLoadMoreComplete(View footerView);

	void onSetNoMore(View footerView, boolean noMore);

	public class SimpleFooterViewCallback implements FooterViewCallback {

		@Override public void onLoadingMore(View footerView) {
		}

		@Override public void onLoadMoreComplete(View footerView) {
		}

		@Override public void onSetNoMore(View footerView, boolean noMore) {
		}
	}

}
