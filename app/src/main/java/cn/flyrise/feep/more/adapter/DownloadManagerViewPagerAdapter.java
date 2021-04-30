package cn.flyrise.feep.more.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

import cn.flyrise.feep.core.common.utils.CommonUtil;

/**
 * @author ZYP
 * @since 2016/8/15 10:37
 */
public class DownloadManagerViewPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragments = null;
    private List<String> mTitles = null;

    public DownloadManagerViewPagerAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.mFragments = fragments;
    }

    public void setTitles(List<String> titles) {
        this.mTitles = titles;
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
