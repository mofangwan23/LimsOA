package cn.flyrise.feep.media.common;

import android.content.Intent;
import android.text.TextUtils;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-10-23 10:52
 */
public class SelectionSpec {

	/**
	 * 单选 boolean
	 */
	public static final String EXTRA_SINGLE_CHOICE = "extra_single_choice";

	/**
	 * 是否选中 boolean
	 */
	public static final String EXTRA_POSITION_SELECTED = "extra_position_selected";

	/**
	 * 接受的文件/图片类型 string[]
	 */
	public static final String EXTRA_EXPECT_TYPE = "extra_expect_type";

	/**
	 * 不加载指定路径下的文件/图片 string[]
	 */
	public static final String EXTRA_EXCEPT_PATH = "extra_except_path";

	/**
	 * 用于已经选择的文件/图片 ArrayList<String>
	 */
	public static final String EXTRA_SELECTED_FILES = "extra_selected_files";

	/**
	 * 能选择的最大上限 int
	 */
	public static final String EXTRA_MAX_SELECT_COUNT = "extra_max_select_count";

	/**
	 * 需要显示的文件/图片类型
	 */
	protected String[] mExpectType;

	/**
	 * 不加载指定路径下的文件/图片
	 */
	protected String[] mExceptPath;

	/**
	 * 已经选中的文件/图片
	 */
	protected final ArrayList<String> mSelectedFiles;

	/**
	 * 允许选择图片数量的最大值
	 */
	protected int mMaxSelectCount;

	/**
	 * 是否单选
	 */
	protected boolean isSingleChoice;

	public SelectionSpec(Intent intent) {
		this.setExpectType(intent.getStringArrayExtra(EXTRA_EXPECT_TYPE));
		this.mExceptPath = intent.getStringArrayExtra(EXTRA_EXCEPT_PATH);
		this.mSelectedFiles = intent.getStringArrayListExtra(EXTRA_SELECTED_FILES);
		this.mMaxSelectCount = intent.getIntExtra(EXTRA_MAX_SELECT_COUNT, Integer.MAX_VALUE);
		this.isSingleChoice = intent.getBooleanExtra(EXTRA_SINGLE_CHOICE, false);
	}

	/**
	 * 这个路径是否被排除在外
	 */
	protected boolean isPathExcluded(String path) {
		if (mExceptPath == null || mExceptPath.length == 0|| TextUtils.isEmpty(path)) {
			return false;
		}
		for (String p : mExceptPath) {
			if (p !=null && path.startsWith(p)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * If the image suffix is expect, then return true, else return false.(来和妲己玩耍把...
	 * @param path 什么鬼 img、jpg、png 啦...
	 */
	protected boolean isTypeExpect(String path) {
		for (String t : mExpectType) {
			if (path.endsWith(t)) {
				return true;
			}
		}
		return false;
	}

	public int getMaxSelectCount() {
		int selectedFileSize = CommonUtil.isEmptyList(mSelectedFiles) ? 0 : mSelectedFiles.size();
		return selectedFileSize + mMaxSelectCount;
	}

	public boolean isSingleChoice() {
		return isSingleChoice;
	}

	protected void setExpectType(String[] expectType) {
		this.mExpectType = expectType;
	}

	public List<String> getSelectedFiles() {
		return mSelectedFiles;
	}
}
