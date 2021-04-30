package com.hyphenate.easeui.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import java.util.List;

public class EaseImagePreviewAdapter extends FragmentPagerAdapter {

	public EaseImagePreviewAdapter(FragmentManager fm) {
		super(fm);
	}

	private List<String> previewImageUrls;
	private OnImagePreviewClickListener mClickListener;

	public void setPreviewImageItems(List<String> previewImageUrls){
		this.previewImageUrls = previewImageUrls;
	}

	public void setOnImagePreviewClickListener(OnImagePreviewClickListener listener) {
		this.mClickListener = listener;
	}

	@Override public Fragment getItem(int position) {
		return previewImageUrls == null ? null : EaseImagePreviewFragment.newInstance(previewImageUrls.get(position), mClickListener);
	}

	@Override public int getCount() {
		return previewImageUrls == null ? 0 : previewImageUrls.size();
	}

	public interface OnImagePreviewClickListener {

		void onImagePreviewClick();

	}
}
