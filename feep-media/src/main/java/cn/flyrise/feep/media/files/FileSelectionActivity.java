package cn.flyrise.feep.media.files;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.media.R;
import cn.flyrise.feep.media.files.adapter.FileIndicatorAdapter;
import cn.flyrise.feep.media.files.adapter.FileIndicatorAdapter.OnFileIndicatorClickListener;
import cn.flyrise.feep.media.files.adapter.FileSelectionAdapter;
import cn.flyrise.feep.media.files.adapter.FileSelectionAdapter.OnFileItemClickListener;
import cn.squirtlez.frouter.annotations.RequestExtras;
import cn.squirtlez.frouter.annotations.ResultExtras;
import cn.squirtlez.frouter.annotations.Route;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-10-23 10:45
 * 文件选择器，支持单选、多选
 */
@Route("/media/file/select")
@RequestExtras({
		"extra_single_choice",      // true 为单选，默认 false【boolean 类型】
		"extra_expect_type",        // 只显示期待的文件类型【string[] 类型】
		"extra_except_path",        // 不加载指定路径下的文件【string[] 类型】
		"extra_selected_files",     // 已选择的文件【ArrayList<String> 类型】
		"extra_max_select_count"    // 能选择的最大上限【int 类型】
})

/**
 * 返回选择文件 #路径# 的 Key, 目前支持两种类型
 * 1. 多选：ArrayList<String>  【eg：intent.getStringArrayListExtra("SelectionData")】
 * 2. 单选：String             【eg: intent.getString("SelectionData")】
 */
@ResultExtras({"SelectionData"})
public class FileSelectionActivity extends BaseActivity
		implements FileSelectionView, OnFileItemClickListener, OnFileIndicatorClickListener {

	private FileSelectionPresenter mSelectionPresenter;
	private RecyclerView mIndicatorView;
	private FileIndicatorAdapter mIndicatorAdapter;

	private RecyclerView mRecyclerView;
	private FileSelectionAdapter mSelectionAdapter;

	private FileIndicator mCurrentIndicator;

	@Override protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSelectionPresenter = new FileSelectionPresenter(this, getIntent());
		setContentView(R.layout.ms_activity_file_selection);
		mSelectionPresenter.start(Environment.getExternalStorageDirectory().getPath());
	}

	@Override protected void toolBar(FEToolbar toolbar) {
		toolbar.setLineVisibility(View.GONE);
		toolbar.setTitle("选择附件");
		toolbar.setRightText("确定");
		toolbar.setRightTextClickListener(v -> {
			Intent data = new Intent();
			ArrayList<String> selectedFiles = (ArrayList<String>) mSelectionPresenter.getSelectedFilePath();
			data.putStringArrayListExtra("SelectionData", selectedFiles);
			setResult(Activity.RESULT_OK, data);
			finish();
		});
	}

	@Override public void bindView() {
		mIndicatorView = (RecyclerView) findViewById(R.id.msFileIndicator);
		mIndicatorView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
		mIndicatorView.setItemAnimator(new DefaultItemAnimator());
		mIndicatorView.setAdapter(mIndicatorAdapter = new FileIndicatorAdapter());
		mIndicatorAdapter.addIndicator(mCurrentIndicator = FileIndicator.createRootIndicator());
		mIndicatorAdapter.setOnFileIndicatorClickListener(this);

		mRecyclerView = (RecyclerView) findViewById(R.id.msRecyclerView);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());

		mSelectionAdapter = new FileSelectionAdapter(mSelectionPresenter.getSelectedFiles(), mSelectionPresenter.isSingleChoice());
		mRecyclerView.setAdapter(mSelectionAdapter);
		mSelectionAdapter.setOnFileItemClickListener(this);
	}

	@Override public void onFileLoad(List<FileItem> files) {
		mSelectionAdapter.setFiles(files);
	}

	@Override public int onFileItemClick(FileItem fileItem) {
		if (fileItem.isDir()) {
			mCurrentIndicator = FileIndicator.create(fileItem.name, fileItem.path);
			mIndicatorAdapter.addIndicator(mCurrentIndicator);
			mSelectionPresenter.loadFiles(fileItem.path);
			return -2;
		}

		if (mSelectionPresenter.isSingleChoice()) {
			Intent data = new Intent();
			data.putExtra("SelectionData", fileItem.path);
			setResult(Activity.RESULT_OK, data);
			finish();
			return 0;
		}

		int resultCode = mSelectionPresenter.executeImageCheckChange(fileItem);
		if (resultCode == 0) {
			FEToast.showMessage(String.format("最多只能选择 %d 个文件", mSelectionPresenter.getSelectedFilePath().size()));
		}
		return resultCode;
	}

	@Override public void onFileIndicatorClick(FileIndicator indicator) {
		if (mCurrentIndicator.equals(indicator)) {
			return;
		}
		mCurrentIndicator = indicator;
		mSelectionPresenter.loadFiles(indicator.path);
		mIndicatorAdapter.removeIndicatorAfter(indicator);
	}

	@Override public void onBackPressed() {
		if (!mCurrentIndicator.isRootIndicator()) {
			mCurrentIndicator = mIndicatorAdapter.forwardIndicator();
			mSelectionPresenter.loadFiles(mCurrentIndicator.path);
			return;
		}
		super.onBackPressed();
	}
}
