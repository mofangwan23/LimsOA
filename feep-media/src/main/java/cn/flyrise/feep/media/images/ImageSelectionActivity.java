package cn.flyrise.feep.media.images;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.media.R;
import cn.flyrise.feep.media.images.AlbumSelectionWindow.OnImageAlbumOperatedListener;
import cn.flyrise.feep.media.images.adapter.GridDivideDecoration;
import cn.flyrise.feep.media.images.adapter.ImageSelectionAdapter;
import cn.flyrise.feep.media.images.adapter.ImageSelectionAdapter.OnImageOperatedListener;
import cn.flyrise.feep.media.images.bean.Album;
import cn.flyrise.feep.media.images.bean.ImageItem;
import cn.flyrise.feep.media.images.repository.ImageSelectionSpec;
import cn.squirtlez.frouter.annotations.RequestExtras;
import cn.squirtlez.frouter.annotations.ResultExtras;
import cn.squirtlez.frouter.annotations.Route;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static cn.flyrise.feep.media.common.SelectionSpec.EXTRA_SELECTED_FILES;

/**
 * @author ZYP
 * @since 2010-11-12 13:14
 * 图片选择器，支持多选、单选、各种乱七八糟配置
 */
@Route("/media/image/select")
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
 * 2. 单选：String             【eg: intent.getStringExtra("SelectionData")】
 */
@ResultExtras({"SelectionData"})
public class ImageSelectionActivity extends BaseActivity
		implements ImageSelectionView, OnImageOperatedListener, OnImageAlbumOperatedListener {

	private RecyclerView mRecyclerView;
	private ImageSelectionAdapter mPickerAdapter;
	private ImageSelectionPresenter mPickerPresenter;

	private View mBottomView;
	private View mTranslucenceView;
	private TextView mTvPreview;
	private TextView mTvCurrentFolder;
	private AlbumSelectionWindow mAlbumSelectionWindow;
	private FEToolbar mToolbar;

	private FELoadingDialog mLoadingDialog;
	private List<ImageItem> imageItemsList;
	private List<Integer> selectedIndexs;
	public static final int EXTRA_SELECTED_IMAGE = 1001;
	public static final int EXTRA_SELECTED_PREVIEW_IMAG = 1002;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPickerPresenter = new ImageSelectionPresenter(this, getIntent());
		setContentView(R.layout.ms_activity_image_selection);
	}

	@Override protected void toolBar(FEToolbar toolbar) {
		mToolbar = toolbar;
		toolbar.setTitle("选择图片");
		toolbar.setRightText("确定");
		toolbar.setRightTextColor(Color.parseColor("#CFD0D1"));
		toolbar.setRightTextClickListener(v -> {
			ArrayList<ImageItem> selectedImages = (ArrayList<ImageItem>) mPickerPresenter.getSelectedImages();
			if (CommonUtil.isEmptyList(selectedImages)) {
				return;
			}
			Intent data = new Intent();
			ArrayList<String> selectImages = (ArrayList<String>) mPickerPresenter.getSelectedImagePath();
			data.putStringArrayListExtra("SelectionData", selectImages);
			setResult(Activity.RESULT_OK, data);
			finish();
		});
	}

	@Override public void bindView() {
		mRecyclerView = (RecyclerView) findViewById(R.id.msRecyclerView);
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
		mRecyclerView.addItemDecoration(new GridDivideDecoration(this));

		final int widthPixels = getResources().getDisplayMetrics().widthPixels;
		mPickerAdapter = new ImageSelectionAdapter(widthPixels / 3,
				mPickerPresenter.isSingleChoice(), mPickerPresenter.getSelectedImages());
		mPickerAdapter.setOnImageOperatedListener(this);
		mRecyclerView.setAdapter(mPickerAdapter);

		mBottomView = findViewById(R.id.msLayoutBottom);
		mTranslucenceView = findViewById(R.id.msTranslucence);
		mTvCurrentFolder = (TextView) findViewById(R.id.msTvCurrentFolder);
		mTvCurrentFolder.setText("全部图片");
		mTvCurrentFolder.setOnClickListener(v -> showImageAlbumList());

		mTvPreview = (TextView) findViewById(R.id.msTvPreview);
		if (mPickerPresenter.isSingleChoice()) {
			mTvPreview.setVisibility(View.GONE);
		}

		mTvPreview.setOnClickListener(new OnClickListener() {
			@Override public void onClick(View v) {
				ArrayList<ImageItem> selectedImages = (ArrayList<ImageItem>) mPickerPresenter.getSelectedImages();
				if (CommonUtil.isEmptyList(selectedImages)) {
					return;
				}
				Intent intent = new Intent(ImageSelectionActivity.this, ImageBrowserActivity.class);
				intent.putExtra(ImageSelectionSpec.EXTRA_SELECTED_FILES, selectedImages);
				startActivityForResult(intent,EXTRA_SELECTED_PREVIEW_IMAG);
				selectedIndexs = new ArrayList<>();
				for(int i=0;i<imageItemsList.size();i++){
	            	if(imageItemsList.get(i).isHasSelected()){
	            		selectedIndexs.add(i);
					}
				}
			}
		});

		if (mLoadingDialog == null) {
			mLoadingDialog = new FELoadingDialog.Builder(this).setCancelable(false).create();
		}
		mLoadingDialog.show();
		mPickerPresenter.start();
	}

	@Override public void onImageLoad(List<ImageItem> imageItems) {
		if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
		mPickerAdapter.setImages(imageItems);
		mRecyclerView.scrollToPosition(0);
		imageItemsList = imageItems;
	}

	@Override public void onImageAlbumLoad(List<Album> imageAlbums) {
		if (imageAlbums == null) {
			return;
		}

		mAlbumSelectionWindow = new AlbumSelectionWindow(this, imageAlbums);
		mAlbumSelectionWindow.setOnImageAlbumOperatedListener(this);
		mAlbumSelectionWindow.showImageAlbums(mBottomView);
	}

	@Override protected void onDestroy() {
		super.onDestroy();
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}

		if (mPickerPresenter != null) {
			mPickerPresenter.onDestroy();
		}
	}

	@Override public int onImageCheckChange(ImageItem imageItem, int position) {
		int executeResult = mPickerPresenter.executeImageCheckChange(imageItem);   // 操作结果...
		if (executeResult == 0) {
			FEToast.showMessage(String.format("最多只能选择 %d 张图片", mPickerPresenter.getSelectedImages().size()));
		}
        imageItemsList = mPickerPresenter.getAllImagesInCludeSelected(imageItemsList,position,executeResult);
		return executeResult;
	}

	@Override public void onImageClick(ImageItem imageItem, int position) {
		if (mPickerPresenter.isSingleChoice()) {
			Intent data = new Intent();
			data.putExtra("SelectionData", imageItem.path);
			setResult(Activity.RESULT_OK, data);
			finish();
		}
		else {
			Intent intent = new Intent(ImageSelectionActivity.this, ImageBrowserActivity.class);
			intent.putExtra(ImageSelectionSpec.EXTRA_SELECTED_FILES, (Serializable) imageItemsList);
			intent.putExtra(ImageSelectionSpec.EXTRA_POSITION_SELECTED,position);
			startActivityForResult(intent,EXTRA_SELECTED_IMAGE);
		}
	}

	@Override
	public void onImageCheckListenr() {
		ArrayList<ImageItem> selectedImages = (ArrayList<ImageItem>) mPickerPresenter.getSelectedImages();
		if (CommonUtil.isEmptyList(selectedImages)) {
			mToolbar.setRightTextColor(Color.parseColor("#CFD0D1"));
			mTvPreview.setTextColor(Color.parseColor("#CFD0D1"));
		}else {
			mToolbar.setRightTextColor(Color.parseColor("#565656"));
			mTvPreview.setTextColor(Color.parseColor("#FFFFFF"));
		}
	}

	private void showImageAlbumList() {
		if (mAlbumSelectionWindow == null) {
			mPickerPresenter.loadImageAlbums();
			return;
		}

		mAlbumSelectionWindow.showImageAlbums(mBottomView);
	}

	@Override public void onAlbumPrepareDisplay() {
		mTranslucenceView.setVisibility(View.VISIBLE);
	}

	@Override public void onAlbumDismiss() {
		mTranslucenceView.setVisibility(View.GONE);
	}

	@Override public void onAlbumClick(Album album) {
		mTvCurrentFolder.setText(album.name);
		mPickerPresenter.loadImages(album.id);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			List selectedImages;
			switch (requestCode){
				case EXTRA_SELECTED_IMAGE://从所有图片的预览页面过来的
					imageItemsList = (List<ImageItem>) data.getSerializableExtra(EXTRA_SELECTED_FILES);
					mPickerAdapter.setImages(imageItemsList);
					selectedImages = mPickerPresenter.getSelectedImageList(imageItemsList);
					mPickerPresenter.setSelectedImages(selectedImages);
					setTextColor(selectedImages);
					break;
				case EXTRA_SELECTED_PREVIEW_IMAG://从选好的预览页面过来的
					List<ImageItem> imageItemsListPreview = (List<ImageItem>) data.getSerializableExtra(EXTRA_SELECTED_FILES);
					int i=0;
					for(Integer index:selectedIndexs){
						ImageItem imageItem = imageItemsListPreview.get(i);
						imageItemsList.set(index,imageItem);
						i++;
					}
					mPickerAdapter.setImages(imageItemsList);
					selectedImages = mPickerPresenter.getSelectedImageList(imageItemsList);
					mPickerPresenter.setSelectedImages(selectedImages);
					setTextColor(selectedImages);
					break;
			}
		}
	}

	private void setTextColor(List<ImageItem> selectedImages){
		if (CommonUtil.isEmptyList(selectedImages)) {
			mToolbar.setRightTextColor(Color.parseColor("#CFD0D1"));
			mTvPreview.setTextColor(Color.parseColor("#CFD0D1"));
		}else {
			mToolbar.setRightTextColor(Color.parseColor("#565656"));
			mTvPreview.setTextColor(Color.parseColor("#FFFFFF"));
		}
	}
}
