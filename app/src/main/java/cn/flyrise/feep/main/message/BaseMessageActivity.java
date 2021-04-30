package cn.flyrise.feep.main.message;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.adapter.BaseFragmentPagerAdapter;
import cn.flyrise.feep.core.base.component.BaseActivity;

/**
 * @author ZYP
 * @since 2017-03-30 14:09
 * FE 666 版本消息界面基类
 */
public abstract class BaseMessageActivity extends BaseActivity {

    protected ViewPager mViewPager;
    protected TabLayout mTabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_message);
    }

    @Override
    public void bindView() {
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
    }

    @Override
    public final void bindData() {
        List<Fragment> fragments = getFragments();
        List<String> titles = getTabTexts();

        BaseFragmentPagerAdapter adapter = new BaseFragmentPagerAdapter(getSupportFragmentManager(), fragments);
        adapter.setTitles(titles);
        mViewPager.setAdapter(adapter);
        mViewPager.setOffscreenPageLimit(fragments.size() > 3 ? 3 : fragments.size());

        boolean moreThanOneFragment = fragments.size() > 1;
        mTabLayout.setVisibility(moreThanOneFragment ? View.VISIBLE : View.GONE);
        if (moreThanOneFragment) {
            mTabLayout.setupWithViewPager(mViewPager);
            for (int i = 0, len = titles.size(); i < len; i++) {
                mTabLayout.getTabAt(i).setCustomView(newTabView(i, titles.get(i), getTabIcon(i)));
            }

            mTabLayout.setOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager) {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    super.onTabSelected(tab);
                    TextView tvTabName = (TextView) tab.getCustomView().findViewById(R.id.tvTabName);
                    if (tvTabName != null) {
                        tvTabName.setTextColor(Color.parseColor("#38adff"));
                        onTabClick(tvTabName.getText().toString());
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                    super.onTabUnselected(tab);
                    TextView tvTabName = (TextView) tab.getCustomView().findViewById(R.id.tvTabName);
                    if (tvTabName != null) {
                        tvTabName.setTextColor(Color.parseColor("#808080"));
                    }
                }
            });
        }
    }

    private View newTabView(int position, String title, @DrawableRes int drawableId) {
        View view = View.inflate(this, R.layout.item_message_tab, null);
        ImageView ivTabIcon = (ImageView) view.findViewById(R.id.ivTabIcon);
        ivTabIcon.setBackgroundResource(drawableId);
        TextView tvTabName = (TextView) view.findViewById(R.id.tvTabName);
        tvTabName.setText(title);

        if (position == 0) {
            tvTabName.setTextColor(Color.parseColor("#38adff"));
            view.setSelected(true);
        }
        return view;
    }

    protected void onTabClick(String name){

    }

    protected abstract List<Fragment> getFragments();

    protected abstract List<String> getTabTexts();

    protected abstract int getTabIcon(int position);


}
