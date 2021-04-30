package cn.flyrise.feep.commonality.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

import cn.flyrise.feep.core.common.utils.CommonUtil;

/**
 * @author ZYP
 * @since 2016/8/16 11:23
 */
public class BaseFragmentPagerAdapter extends FragmentPagerAdapter {

	private List<String> mTitles;
	private List<Fragment> mFragments;

	public BaseFragmentPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
		super(fm);
		this.mFragments = fragments;
	}

	public void setTitles(List<String> titles) {
		this.mTitles = titles;
	}

	public List<Fragment> getFragments() {
		return mFragments;
	}

	@Override public Fragment getItem(int position) {
		return CommonUtil.isEmptyList(mFragments) ? null : mFragments.get(position);
	}

	@Override public int getCount() {
		return CommonUtil.isEmptyList(mFragments) ? 0 : mFragments.size();
	}

	@Override public CharSequence getPageTitle(int position) {
		if (CommonUtil.isEmptyList(mTitles)) {
			return super.getPageTitle(position);
		}
		return mTitles.get(position);
	}
}
