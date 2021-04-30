package cn.flyrise.feep.media.images.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import cn.flyrise.feep.media.images.ImageBrowserFragment;
import cn.flyrise.feep.media.images.bean.ImageItem;

import java.util.List;

/**
 * @author ZYP
 * @since 2017-10-20 14:05
 */
public class ImagePreviewAdapter extends FragmentPagerAdapter {

	private List<ImageItem> previewImageItems;
	private OnImagePreviewClickListener mClickListener;

	public void setPreviewImageItems(List<ImageItem> previewImageItems){
		this.previewImageItems = previewImageItems;

	}
	public ImagePreviewAdapter(FragmentManager fm) {
		super(fm);
	}

	public void setOnImagePreviewClickListener(OnImagePreviewClickListener listener) {
		this.mClickListener = listener;
	}

	@Override public Fragment getItem(int position) {
		return previewImageItems == null ? null : ImageBrowserFragment.newInstance(previewImageItems.get(position), mClickListener);
	}

	@Override public int getCount() {
		return previewImageItems == null ? 0 : previewImageItems.size();
	}

	public interface OnImagePreviewClickListener {

		void onImagePreviewClick();

	}
}
