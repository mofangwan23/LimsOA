package cn.flyrise.feep.commonality.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.List;

import cn.flyrise.feep.more.GuideFragment;

/**
 * 类功能描述：</br>
 * @author 钟永健
 * @version 1.0</br> 修改时间：2012-7-30</br> 修改备注：</br>
 */
public class GuideViewPagerAdapter extends FragmentPagerAdapter {

    private List<GuideFragment> mFragments;

    public GuideViewPagerAdapter(FragmentManager fm, List<GuideFragment> fragments) {
        super(fm);
        this.mFragments = fragments;

    }

    @Override public Fragment getItem(int i) {
        return mFragments == null ? null : mFragments.get(i);
    }

    @Override public int getCount() {
        return mFragments == null ? 0 : mFragments.size();
    }
}
