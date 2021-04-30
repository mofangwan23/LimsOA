//
// GuideActivity.java
// feep
//
// Created by ZhongYJ on 2012-07-30
// Copyright 2011 flyrise. All rights reserved.
//
package cn.flyrise.feep.more;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.R;
import cn.flyrise.feep.auth.server.setting.ServerSettingActivity;
import cn.flyrise.feep.commonality.adapter.GuideViewPagerAdapter;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.more.widget.ViewpagerSubscript;
import cn.squirtlez.frouter.FRouter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZYP
 */
public class GuideActivity extends BaseActivity {

	public static final String CATEGORY = "Category";
	public static final int CATEGORY_MAIN = 1;  // 首次登录，即将进入主页面
	public static final int CATEGORY_MORE = 2;  // 从 更多 模块进入
	public static final int CATEGORY_ABOUT = 3; // 从 关于 模块进入

	private int mCategory;
	private TextView mTvEnter;
	private ViewPager mViewPager;
	private GuideViewPagerAdapter mGuideAdapter;
	private ViewpagerSubscript mSubscript;
	private List<GuideFragment> mFragments = new ArrayList<>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent intent = getIntent();
		if (intent != null) mCategory = intent.getIntExtra(CATEGORY, CATEGORY_MAIN);
		mFragments.add(GuideFragment.newInstance(R.drawable.guide_a_fe));
		mFragments.add(GuideFragment.newInstance(R.drawable.guide_b_fe));
		mFragments.add(GuideFragment.newInstance(R.drawable.guide_c_fe));
		mFragments.add(GuideFragment.newInstance(R.drawable.guide_d_fe));
		mFragments.add(GuideFragment.newInstance(R.drawable.guide_e_fe));
		setContentView(R.layout.activity_guide);
	}


	@Override
	protected boolean optionStatusBar() {
		return false;
	}

	@Override
	public void bindView() {
		mViewPager = findViewById(R.id.viewPager);
		mTvEnter = findViewById(R.id.tvEnter);
		mSubscript = findViewById(R.id.subscriptLayout);
	}

	@Override
	public void bindData() {
		mGuideAdapter = new GuideViewPagerAdapter(getSupportFragmentManager(), mFragments);
		mViewPager.setAdapter(mGuideAdapter);
	}

	@Override
	public void bindListener() {
		mTvEnter.setOnClickListener(v -> {
			if (mCategory == CATEGORY_MAIN) {   // 做好标记
				SpUtil.put("extra_delay_time", 1024);
				SpUtil.put(PreferencesUtils.GUIDE_STATE_KEY, 1);
				startActivity(new Intent(this, ServerSettingActivity.class));
			}
			GuideActivity.this.finish();
		});
		mViewPager.addOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				mSubscript.showIndex(position);
				if (position == mFragments.size() - 1) {
					mSubscript.setVisibility(View.GONE);
					mTvEnter.setVisibility(View.VISIBLE);
				}
				else {
					mSubscript.setVisibility(View.VISIBLE);
					mTvEnter.setVisibility(View.GONE);
				}
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 如果是从更多模块进来的，可以按返回键
		if (mCategory == CATEGORY_MORE || mCategory == CATEGORY_ABOUT) {
			return super.onKeyDown(keyCode, event);
		}
		return false;
	}

	@Override
	protected void onPause() {
		super.onPause();
		FEUmengCfg.onActivityPauseUMeng(this, FEUmengCfg.GuideActivity);
	}

	@Override
	protected void onResume() {
		super.onResume();
		FEUmengCfg.onActivityResumeUMeng(this, FEUmengCfg.GuideActivity);
	}
}
