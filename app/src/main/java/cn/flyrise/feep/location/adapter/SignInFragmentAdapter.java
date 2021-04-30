package cn.flyrise.feep.location.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2018-5-19-14:22.
 */

public class SignInFragmentAdapter extends FragmentPagerAdapter {

	private List<Fragment> fragments;

	public SignInFragmentAdapter(FragmentManager fm, List<Fragment> fragments) {
		super(fm);
		this.fragments = fragments;
	}

	@Override
	public Fragment getItem(int position) {
		return CommonUtil.isEmptyList(fragments) ? null : fragments.get(position);
	}

	@Override
	public int getCount() {
		return CommonUtil.isEmptyList(fragments) ? 0 : fragments.size();
	}
}
