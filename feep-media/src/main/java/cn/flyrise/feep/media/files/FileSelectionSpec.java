package cn.flyrise.feep.media.files;

import android.content.Intent;
import android.text.TextUtils;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.media.common.SelectionSpec;

/**
 * @author ZYP
 * @since 2017-10-23 10:51
 */
public class FileSelectionSpec extends SelectionSpec {

	/**
	 * 默认的文件加载类型
	 */
	private static final String[] DEFAULT_EXPECT_TYPE = {
			".rar", ".zip", ".doc", ".docx", ".xls",
			".xlsx", ".txt", ".log", ".7z", ".pdf",
			".ppt", ".pptx", ".iso", "wav"
	};

	public FileSelectionSpec(Intent intent) {
		super(intent);
	}

	@Override protected void setExpectType(String[] expectType) {
		super.setExpectType(expectType);
		if (mExpectType == null || mExpectType.length == 0) {
			mExpectType = DEFAULT_EXPECT_TYPE;
		}
	}

	public boolean isFileSelected(String filePath) {
		if (CommonUtil.isEmptyList(mSelectedFiles)) {
			return false;
		}

		for (String path : mSelectedFiles) {
			if (TextUtils.equals(path, filePath)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * return false 表示这个 file 丑拒，不用拿出来丢人现眼了...
	 */
	public boolean isExpectFile(FileItem file) {
		if (isPathExcluded(file.path)) {
			return false;
		}

		if (file.name.startsWith(".")) {
			return false;
		}

		if (file.isDir()) {
			return true;
		}

		if (!isTypeExpect(file.path)) {
			return false;
		}
		return true;
	}
}
