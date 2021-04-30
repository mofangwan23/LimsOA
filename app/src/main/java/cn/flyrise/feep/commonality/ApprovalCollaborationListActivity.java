package cn.flyrise.feep.commonality;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.search.ApprovalSearchActivity;
import cn.flyrise.feep.commonality.adapter.BaseFragmentPagerAdapter;
import cn.flyrise.feep.commonality.fragment.ApprovalCollaborationFragment;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.common.X.RequestType;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.function.AppSubMenu;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.utils.Patches;
import java.util.ArrayList;
import java.util.List;

/**
 * 类功能描述:审批菜单
 * Update By ZYP：2016-08-16 看不爽，重写...
 * @author cm
 * @version 1.0 2015-11-30
 */
public class ApprovalCollaborationListActivity extends BaseActivity implements ApprovalCollaborationFragment.OnApprovalListener {

	private int mSearchType;
	private TabLayout tabLayout;
	private ViewPager viewPager;
	private BaseFragmentPagerAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_approval_collaboration_list);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		toolbar.setLineVisibility(View.GONE);
		toolbar.setRightIcon(R.drawable.add_btn);
		toolbar.setTitle(R.string.approval_title_string);
		toolbar.setRightImageViewSearchVisible();
		toolbar.setRightImageSearchClickListener(v -> {
			if (isExistFunction(Patches.PATCH_NEW_APPLICATION) && isExistFunction(Patches.PATCH_DATA_RETRIEVAL)) { //有信息检索
				startActivity(new Intent(this, ApprovalSearchActivity.class));
			}
			else {
				Intent searchIntent = new Intent(this, MessageSearchActivity.class);
				searchIntent.putExtra(MessageSearchActivity.REQUESTTYPE, mSearchType);
				searchIntent.putExtra(MessageSearchActivity.REQUESTNAME, getResources().getString(R.string.approval_title_string));
				startActivity(searchIntent);
			}
		});
		toolbar.setRightImageClickListener(v -> {
			Intent intent = new Intent(ApprovalCollaborationListActivity.this, MainMenuRecyclerViewActivity.class);
			intent.putExtra(MainMenuRecyclerViewActivity.MENU_TYPE, MainMenuRecyclerViewActivity.APPROVAL_MENU);
			startActivity(intent);
		});
	}

	private boolean isExistFunction(int patches) {
		return FunctionManager.hasPatch(patches);
	}

	@Override
	public void bindData() {
		List<String> titles = new ArrayList<>();
		tabLayout = findViewById(R.id.tabLayout);
		List<Fragment> fragments = new ArrayList<>();
		List<AppSubMenu> menus = FunctionManager.getAppSubMenu(Func.Collaboration);
		int n = 0;
		if (CommonUtil.nonEmptyList(menus)) {
			mSearchType = menus.get(0).menuId;
			n = menus.size();
		}

		int requestType = getIntent().getIntExtra("request_type", -1);
		int index = 0;
		AppSubMenu menu;
		ApprovalCollaborationFragment fragment;
		for (int i = 0; i < n; i++) {
			menu = menus.get(i);
			titles.add(menu.menu);
			fragment = ApprovalCollaborationFragment.newInstance(menu);
			if (menu.menuId == RequestType.ToDo) {
				fragment.setListener(this);
			}
			fragments.add(fragment);

			if (menu.menuId == requestType) {
				index = i;
			}
		}

		viewPager = findViewById(R.id.viewPager);
		adapter = new BaseFragmentPagerAdapter(getSupportFragmentManager(), fragments);
		adapter.setTitles(titles);
		viewPager.setAdapter(adapter);
		if (!CommonUtil.isEmptyList(menus))
			viewPager.setOffscreenPageLimit(menus.size());
		tabLayout.setupWithViewPager(viewPager);

		viewPager.setCurrentItem(index);

		for (int i = 0; i < titles.size(); i++) {
			setTabLayoutCustomItem(titles.get(i), i);
		}
		setListener();
	}

	private void setListener() {

		viewPager.addOnPageChangeListener(new ApprovalCollaborationListener() {
			@Override public void onPageSelected(int position) {
				ApprovalCollaborationFragment fragment = (ApprovalCollaborationFragment) adapter.getItem(position);
				mSearchType = fragment.getSearchType();
				if (FunctionManager.hasPatch(Patches.PATCH_NO_REFRESH_LIST)) {
					fragment.startRefreshList();
				}
			}
		});

		tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				updateTabTextView(tab, true);
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {
				updateTabTextView(tab, false);
			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {

			}
		});
	}

	private void setTabLayoutCustomItem(String menu, int index) {
		View view = LayoutInflater.from(this).inflate(R.layout.tab_layout_custom_title_item, null, false);
		TextView title = view.findViewById(R.id.title);
		title.setText(menu);
		if (index == 0) {
			title.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
			title.setTextColor(Color.parseColor("#28B9FF"));
		}
		tabLayout.getTabAt(index).setCustomView(view);
	}

	@Override
	public void messageSize(int size) {
		modifySelectedNumBadge(0, getShowNum(size));
	}

	private void updateTabTextView(TabLayout.Tab tab, boolean isSelect) {
		if (isSelect) {
			TextView tabSelect = tab.getCustomView().findViewById(R.id.title);
			tabSelect.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
			tabSelect.setTextColor(Color.parseColor("#28B9FF"));
			tabSelect.setText(tab.getText());
		}
		else {
			TextView tabUnSelect = tab.getCustomView().findViewById(R.id.title);
			tabUnSelect.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
			tabUnSelect.setTextColor(Color.parseColor("#80000000"));
			tabUnSelect.setText(tab.getText());
		}
	}

	private void modifySelectedNumBadge(int index, String text) {
		if (!FunctionManager.hasPatch(Patches.PATCH_SHOW_NUM_BUBBL)) return;
		if (tabLayout == null) return;
		View customView = tabLayout.getTabAt(index).getCustomView();
		if (customView == null) return;
		TextView viewById = customView.findViewById(R.id.coreTvAttachmentSize);
		if (viewById != null) viewById.setText(text);
	}

	private String getShowNum(int size) {
		String text;
		if (size > 99) {
			text = 99 + "+";
		}
		else if (size < 0) {
			text = 0 + "";
		}
		else {
			text = size + "";
		}
		return text;
	}

	private class ApprovalCollaborationListener implements ViewPager.OnPageChangeListener {

		@Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

		@Override public void onPageSelected(int position) { }

		@Override public void onPageScrollStateChanged(int state) { }
	}
}
