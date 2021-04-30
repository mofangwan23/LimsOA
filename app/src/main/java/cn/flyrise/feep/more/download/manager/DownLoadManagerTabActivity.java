package cn.flyrise.feep.more.download.manager;

import static cn.flyrise.feep.R.id.tabLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.knowledge.view.NoScrollViewPager;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import cn.flyrise.feep.more.adapter.DownloadManagerViewPagerAdapter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Update By ZYP 2016-08-15
 */
public class DownLoadManagerTabActivity extends BaseActivity implements IDownloadManagerOperationListener {

	private FEToolbar mToolBar;
	private View mLayoutAttachmentEdit;
	private TextView mTvSelectedCount;
	private CheckBox mSelectedAllDeleteBtn;

	private TabLayout mTabLayout;
	private NoScrollViewPager mViewPager;

	public static void startDownLoadManagerTabActivity(Context context, int currentItem) {
		Intent intent = new Intent(context, DownLoadManagerTabActivity.class);
		intent.putExtra("currentItem", currentItem);
		context.startActivity(intent);
	}

	/**
	 * 打开 {@link DownLoadManagerTabActivity } 切换到 {下载中} 标签, 并强制下载
	 */
	public static void startAndSwitchToDownloadingView(Context context,
			ArrayList<String> prepareDownloadTasks, boolean forceDownload) {
		Intent intent = new Intent(context, DownLoadManagerTabActivity.class);
		intent.putExtra("currentItem", 0);
		intent.putExtra("forceDownload", forceDownload);
		intent.putExtra("prepareDownloadTasks", prepareDownloadTasks);
		context.startActivity(intent);
	}

	private DownloadingFragment mDownloadingFragment;
	private DownloadCompletedFragment mDownloadCompletedFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_download_manager);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		mToolBar = toolbar;
		mToolBar.setTitle(R.string.reside_menu_item_downloadmanager);
		mToolBar.setLineVisibility(View.GONE);
	}

	@Override
	public void bindView() {
		Intent intent = getIntent();
		boolean forceDownload = intent.getBooleanExtra("forceDownload", false);
		int currentItem = intent.getIntExtra("currentItem", 0);

		List<String> titles = Arrays.asList(
				getResources().getString(R.string.lbl_text_download),
				getResources().getString(R.string.lbl_text_completed));
		List<Fragment> fragments = new ArrayList<>();
		fragments.add(mDownloadingFragment = DownloadingFragment.newInstance(this, forceDownload));
		fragments.add(mDownloadCompletedFragment = DownloadCompletedFragment.newInstance(this));
		mDownloadingFragment.setPrepareDownloadTasks(intent.getStringArrayListExtra("prepareDownloadTasks"));

		mTabLayout = (TabLayout) findViewById(tabLayout);
		mTabLayout.newTab().setText(titles.get(0));
		mTabLayout.newTab().setText(titles.get(1));

		mViewPager = (NoScrollViewPager) findViewById(R.id.viewPager);
		DownloadManagerViewPagerAdapter adapter = new DownloadManagerViewPagerAdapter(getSupportFragmentManager(), fragments);
		adapter.setTitles(titles);

		mViewPager.setAdapter(adapter);
		mViewPager.setCurrentItem(currentItem);
		mTabLayout.setupWithViewPager(mViewPager);

		mLayoutAttachmentEdit = findViewById(R.id.layoutAttachmentEdit);
		mTvSelectedCount = (TextView) findViewById(R.id.tvDeleteSelectedCount);
		mSelectedAllDeleteBtn = (CheckBox) findViewById(R.id.cbSelectedAll);
	}

	@Override
	public void bindListener() {
		mToolBar.setNavigationOnClickListener(v -> {
			if (isEditMode()) {
				notifyEditModeChange(getViewCode(), false);
				return;
			}
			finish();
		});

		mToolBar.setRightTextClickListener(v -> {
			int viewCode = getViewCode();
			int toDeleteAttachmentSize = getToDeleteAttachmentSize(viewCode);
			if (toDeleteAttachmentSize != 0) {
				List<Attachment> toDeleteAttachments = getToDeleteAttachments(viewCode);
				deleteAttachments(viewCode, toDeleteAttachments);
				clearToDeleteAttachments(viewCode);
			}
			notifyEditModeChange(viewCode, false);
		});

		mSelectedAllDeleteBtn.setOnClickListener(v -> {
			int viewCode = getViewCode();
			if (viewCode == DOWNLOAD_COMPLETED_VIEW) {
				mDownloadCompletedFragment.getAttachmentListView().notifyAllAttachmentDeleteState(mSelectedAllDeleteBtn.isChecked());
			}
			else {
				mDownloadingFragment.getAdapter().notifyAllAttachmentDeleteState(mSelectedAllDeleteBtn.isChecked());
			}
			notifyEditModeChange(getViewCode(), true);
		});
	}


	@Override
	public void onBackPressed() {
		if (isEditMode()) {
			notifyEditModeChange(getViewCode(), false);
			return;
		}
		super.onBackPressed();
	}

	private void setViewPagerScrollEnable(boolean enableScroll) {
		mViewPager.setCanScroll(enableScroll);
		LinearLayout tabStrip = (LinearLayout) mTabLayout.getChildAt(0);
		for (int i = 0; i < tabStrip.getChildCount(); i++) {
			View tabView = tabStrip.getChildAt(i);
			tabView.setOnTouchListener((v, event) -> !enableScroll);
		}
	}

	private boolean isEditMode() {
		return mDownloadCompletedFragment.isEditMode() || mDownloadingFragment.isEidtMode();
	}

	@Override
	public void notifyEditModeChange(int viewCode, boolean isEditMode) {
		if (isEditMode != isViewInEditMode(viewCode)) {
			setEditMode(viewCode, isEditMode);
		}

		if (isEditMode) {
			setViewPagerScrollEnable(false);
			mLayoutAttachmentEdit.setVisibility(View.VISIBLE);
			int toDeleteAttachmentSize = getToDeleteAttachmentSize(viewCode);
			mToolBar.getRightTextView().setVisibility(View.VISIBLE);
			mToolBar.setRightText(toDeleteAttachmentSize == 0 ? "取消" : String.format("删除(%d)", toDeleteAttachmentSize));

			int totalItemCount = getItemCount(viewCode);
			mTvSelectedCount.setText("已选：" + toDeleteAttachmentSize);                   // 待删除的个数
			mSelectedAllDeleteBtn.setChecked(toDeleteAttachmentSize == totalItemCount);
			mSelectedAllDeleteBtn.setText(mSelectedAllDeleteBtn.isChecked() ? "全不选" : "全选");
			return;
		}

		setViewPagerScrollEnable(true);
		clearToDeleteAttachments(viewCode);
		mToolBar.getRightTextView().setVisibility(View.GONE);
		mLayoutAttachmentEdit.setVisibility(View.GONE);
	}

	@Override
	public void refreshDownloadList() {
		if (mDownloadCompletedFragment != null) {
			mDownloadCompletedFragment.refreshDownloadCompletedAttachments();
		}

		if (mDownloadingFragment != null) {
			mDownloadingFragment.refreshDownloadingAttachments();
		}
	}

	private int getViewCode() {
		return mViewPager.getCurrentItem() == 0 ? DOWNLOADING_VIEW : DOWNLOAD_COMPLETED_VIEW ;
	}

	private boolean isViewInEditMode(int viewCode) {
		return viewCode == DOWNLOAD_COMPLETED_VIEW ? mDownloadCompletedFragment.isEditMode() : mDownloadingFragment.isEidtMode();
	}

	private void setEditMode(int viewCode, boolean isEditMode) {
		if (viewCode == DOWNLOAD_COMPLETED_VIEW) {
			mDownloadCompletedFragment.getAttachmentListView().setEditMode(isEditMode);
			mDownloadCompletedFragment.getAttachmentListView().getAdapter().notifyDataSetChanged();
			return;
		}

		mDownloadingFragment.getAdapter().setEditMode(isEditMode);
		mDownloadingFragment.getAdapter().notifyDataSetChanged();
	}

	private int getToDeleteAttachmentSize(int viewCode) {
		if (viewCode == DOWNLOAD_COMPLETED_VIEW) {
			return mDownloadCompletedFragment.getAttachmentListView().getToDeleteAttachmentSize();
		}

		return mDownloadingFragment.getAdapter().getToDeleteAttachmentSize();
	}

	private int getItemCount(int viewCode) {
		if (viewCode == DOWNLOAD_COMPLETED_VIEW) {
			return mDownloadCompletedFragment.getAttachmentListView().getAdapter().getItemCount();
		}
		return mDownloadingFragment.getAdapter().getItemCount();
	}

	private void clearToDeleteAttachments(int viewCode) {
		if (viewCode == DOWNLOAD_COMPLETED_VIEW) {
			mDownloadCompletedFragment.getAttachmentListView().clearToDeleteAttachments();
			return;
		}

		mDownloadingFragment.getAdapter().clearToDeleteAttachments();
	}

	private List<Attachment> getToDeleteAttachments(int viewCode) {
		if (viewCode == DOWNLOAD_COMPLETED_VIEW) {
			return mDownloadCompletedFragment.getAttachmentListView().getToDeleteAttachments();
		}

		return mDownloadingFragment.getAdapter().getToDeleteAttachments();
	}

	private void deleteAttachments(int viewCode, List<Attachment> toDeleteAttachments) {
		if (viewCode == DOWNLOAD_COMPLETED_VIEW) {
			mDownloadCompletedFragment.deleteAttachments(toDeleteAttachments);
			return;
		}

		mDownloadingFragment.deleteAttachments(toDeleteAttachments);
	}
}
