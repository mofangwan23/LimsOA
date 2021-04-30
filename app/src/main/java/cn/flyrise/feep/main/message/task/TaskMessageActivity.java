package cn.flyrise.feep.main.message.task;

import static cn.flyrise.feep.core.common.X.RequestType.*;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import android.support.v4.view.ViewPager.OnPageChangeListener;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.function.AppSubMenu;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.utils.Patches;
import java.util.ArrayList;
import java.util.List;

import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.bean.MenuInfo;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.main.message.BaseMessageActivity;

/**
 * @author ZYP
 * @since 2017-03-30 14:36
 * FE 666 版本：任务消息
 */
public class TaskMessageActivity extends BaseMessageActivity {

	private List<Fragment> mFragments = new ArrayList<>();
	private List<String> mTabTexts = new ArrayList<>();
	private List<Integer> mTabIcons = new ArrayList<>();

	public static void start(Activity activity) {
		Intent intent = new Intent(activity, TaskMessageActivity.class);
		activity.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		toolbar.setTitle(R.string.message_misson_title);
	}

	@Override
	public void bindView() {
		super.bindView();
		List<AppSubMenu> menuInfoList = FunctionManager.getAppSubMenu(Func.Collaboration);
		mTabIcons.clear();
		mTabTexts.clear();
		mFragments.clear();
		if (CommonUtil.nonEmptyList(menuInfoList)) {
			for (AppSubMenu menuInfo : menuInfoList) {
				String tabText = menuInfo.menu;
				int tabIcon = -1;
				switch (menuInfo.menuId) {
					case ToDo:
						tabIcon = R.drawable.icon_msg_wait_selector;
						break;
					case Done:
						tabIcon = R.drawable.icon_msg_done_selector;
						break;
					case Sended:
						tabIcon = R.drawable.icon_msg_send_selector;
						break;
					case ToDoDispatch:
						tabIcon = R.drawable.icon_msg_jijian_selector;
						break;
					case ToDoNornal:
						tabIcon = R.drawable.icon_msg_pingjian_selector;
						break;
					case ToDoRead:
						tabIcon = R.drawable.icon_msg_yuejian_selector;
						break;
					default:
						break;
				}
				mTabTexts.add(tabText);
				mTabIcons.add(tabIcon);
				mFragments.add(TaskMessageFragment.newInstance(menuInfo));
			}
		}
	}

	@Override
	public void bindListener() {
		super.bindListener();
		mViewPager.addOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				if (!FunctionManager.hasPatch(Patches.PATCH_NO_REFRESH_LIST)) return;
				Fragment fragment = mFragments.get(position);
				if (fragment instanceof TaskMessageFragment) ((TaskMessageFragment) fragment).startRefreshList();
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});
	}

	@Override
	protected List<Fragment> getFragments() {
		return mFragments;
	}

	@Override
	protected List<String> getTabTexts() {
		return mTabTexts;
	}

	@Override
	protected int getTabIcon(int position) {
		return mTabIcons.get(position);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
