package cn.flyrise.feep.media.images.repository;

import android.content.Intent;
import android.text.TextUtils;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.media.common.SelectionSpec;
import cn.flyrise.feep.media.images.bean.ImageItem;

/**
 * @author ZYP
 * @since 2017-10-17 14:24
 */
public class ImageSelectionSpec extends SelectionSpec {

	/**
	 * 默认加载的图片类型
	 */
	private static final String[] DEFAULT_EXPECT_TYPE = {".jpg", ".bmp", ".png", ".gif", ".jpeg", ".jpeg"};

	public ImageSelectionSpec(Intent intent) {
		super(intent);
	}

	/**
	 * 返回 false 表示这个 Image 丑拒...
	 */
	public boolean isExpectImage(ImageItem imageItem) {
		if (isPathExcluded(imageItem.path)) {
			return false;
		}

		if (!isTypeExpect(imageItem.path)) {
			return false;
		}

		return true;
	}

	/**
	 * 返回 true 表示这个 Image 已经被睡过了...
	 */
	public boolean isImageSelected(ImageItem imageItem) {
		if (CommonUtil.isEmptyList(mSelectedFiles)) {
			return false;
		}

		for (String path : mSelectedFiles) {
			if (TextUtils.equals(imageItem.path, path)) {
				imageItem.setHasSelected(true);
				return true;
			}
		}

		return false;
	}

	@Override protected void setExpectType(String[] expectType) {
		super.setExpectType(expectType);
		if (expectType == null || expectType.length == 0) {
			mExpectType = DEFAULT_EXPECT_TYPE;
		}
	}
}
