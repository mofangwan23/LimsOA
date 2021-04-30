package cn.flyrise.feep.knowledge;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import cn.flyrise.feep.FEApplication;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.MainMenuRecyclerViewActivity;
import cn.flyrise.feep.commonality.adapter.BaseFragmentPagerAdapter;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.utils.UIUtil;
import cn.flyrise.feep.knowledge.model.FolderManager;
import cn.flyrise.feep.knowledge.model.KnowledgeListEvent;
import cn.flyrise.feep.knowledge.util.KnowKeyValue;
import cn.flyrise.feep.knowledge.view.FolderFragment;
import cn.flyrise.feep.knowledge.view.NoScrollViewPager;
import cn.squirtlez.frouter.annotations.Route;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by klc on 2016/12/7.
 */
@Route("/knowledge/native/home")
public class NewKnowledgeActivity extends BaseActivity {

	private FEToolbar mToolBar;
	private TabLayout mTabLayout;
	private NoScrollViewPager mViewPager;
	private FloatingActionsMenu mDetailMenu;
	private FloatingActionButton mNewFolderIcon;

	private List<String> mTitles;
	private List<Fragment> mFragments;
	private int mPageIndex;
	private int searchType;
	private boolean isChooseFolder;
	public static final String SEARCH_TYPE = "search_TYPE";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_knowledge_root_folder_list);
		EventBus.getDefault().register(this);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		isChooseFolder = getIntent().getBooleanExtra(K.knowledge.EXTRA_CHOOSEFOLDER, false);
		mToolBar = toolbar;
		toolbar.setLineVisibility(View.GONE);
		toolbar.setLeftText(getString(R.string.dialog_default_cancel_button_text));
		toolbar.showNavigationIcon();
		toolbar.setTitle(R.string.know_document);
		toolbar.setRightText(R.string.know_more_control);
		toolbar.setLeftTextClickListener(v -> onBackPressed());
		toolbar.setNavigationOnClickListener(v -> onBackPressed());
		toolbar.setRightTextClickListener(v -> {
			if (getString(R.string.know_more_control).equals(toolbar.getRightText())) {
				Intent intent = new Intent(NewKnowledgeActivity.this, MainMenuRecyclerViewActivity.class);
				intent.putExtra(MainMenuRecyclerViewActivity.MENU_TYPE, MainMenuRecyclerViewActivity.KNOWLEDGE_MENU);
				if (mPageIndex == 0) {
					intent.putExtra(MainMenuRecyclerViewActivity.SEARCH_TYPE, KnowKeyValue.FOLDERTYPE_PERSON);
				}
				else if (mPageIndex == 1) {
					intent.putExtra(MainMenuRecyclerViewActivity.SEARCH_TYPE, KnowKeyValue.FOLDERTYPE_UNIT);
				}
				else {
					intent.putExtra(MainMenuRecyclerViewActivity.SEARCH_TYPE, KnowKeyValue.FOLDERTYPE_GROUP);
				}
				startActivity(intent);
			}
			else {
				((FolderFragment) mFragments.get(mPageIndex)).selectCancel();
				mDetailMenu.setVisibility(View.VISIBLE);
			}

		});

	}

	@Override
	public void bindView() {
		super.bindView();
		mViewPager = (NoScrollViewPager) findViewById(R.id.viewPager);
		mTabLayout = (TabLayout) findViewById(R.id.tabLayout);
		mDetailMenu = (FloatingActionsMenu) findViewById(R.id.moreaction_menu);
		mNewFolderIcon = (FloatingActionButton) findViewById(R.id.newfloder_icon);
	}

	@Override
	public void bindData() {
		super.bindData();
		mTitles = new ArrayList<>();
		mFragments = new ArrayList<>();
		if (!isChooseFolder) {
			List<TabLayout.Tab> mTabs = new ArrayList<>();
			initFragment(getString(R.string.know_person_folder), KnowKeyValue.FOLDERTYPE_PERSON);
			initFragment(getString(R.string.know_unit_folder), KnowKeyValue.FOLDERTYPE_UNIT);
			if (((FEApplication) getApplication()).isGroupVersion()) {
				initFragment(getString(R.string.know_group_folder), KnowKeyValue.FOLDERTYPE_GROUP);
			}
			for (String title : mTitles) {
				TabLayout.Tab tab = mTabLayout.newTab();
				tab.setText(title);
				mTabs.add(tab);
			}
		}
		else {
			mTabLayout.setVisibility(View.GONE);
			initFragment(getString(R.string.know_person_folder), KnowKeyValue.FOLDERTYPE_PERSON);
		}
		final BaseFragmentPagerAdapter adapter = new BaseFragmentPagerAdapter(getSupportFragmentManager(), mFragments);
		adapter.setTitles(mTitles);
		mViewPager.setAdapter(adapter);
		mViewPager.setOffscreenPageLimit(3);
		mTabLayout.setupWithViewPager(mViewPager);
		UIUtil.fixTabLayoutIndicatorWidth(mTabLayout);
		Intent intent = getIntent();
		if (intent != null) {
			searchType = intent.getIntExtra(SEARCH_TYPE, -1);
		}
	}

	@Override
	public void bindListener() {
		super.bindListener();

		mNewFolderIcon.setOnClickListener(v -> {
			mDetailMenu.collapse();
			((FolderFragment) mFragments.get(mPageIndex)).createFolder();
		});

		mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				mPageIndex = position;
				mDetailMenu.collapse();
			}

			@Override
			public void onPageScrollStateChanged(int state) {
			}
		});

	}

	private void initFragment(String titleName, int folderType) {
		mTitles.add(titleName);
		FolderManager manager = new FolderManager(folderType);
		FolderFragment fragment = FolderFragment.newInstance();
		fragment.setActivityView(manager, mToolBar, mDetailMenu);
		mFragments.add(fragment);
	}

	@Override
	public void onBackPressed() {
		((FolderFragment) mFragments.get(mPageIndex)).onBackPressed();
		mDetailMenu.setVisibility(View.VISIBLE);
	}


	@Subscribe(threadMode = ThreadMode.MAIN) //在ui线程执行
	public void onItemLongClickEvent(KnowledgeListEvent event) {
		if (event.eventID == KnowledgeListEvent.SETTABLAYOUTENABLE) {
			setTabLayoutEnabled(event.tabEnable);
			mViewPager.setCanScroll(event.tabEnable);
		}
	}

	private void setTabLayoutEnabled(boolean enabled) {
		if (mTabLayout.getChildCount() == 0) return;
		LinearLayout childView = (LinearLayout) mTabLayout.getChildAt(0);
		if (childView == null) return;
		int childCount = childView.getChildCount();
		for (int i = 0; i < childCount; i++) {
			LinearLayout view = (LinearLayout) childView.getChildAt(i);
			view.setEnabled(enabled);
		}
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == KnowKeyValue.STARTMOVECODE && resultCode == RESULT_OK) {
			FolderFragment fragment = (FolderFragment) mFragments.get(mPageIndex);
			fragment.refreshListByNet();
			fragment.onBackPressed();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}

}
