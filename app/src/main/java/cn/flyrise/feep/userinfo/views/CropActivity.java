package cn.flyrise.feep.userinfo.views;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.PhotoUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.flyrise.feep.media.images.ImageSelectionActivity;
import cn.flyrise.feep.media.record.camera.CameraManager;
import cn.flyrise.feep.userinfo.contract.CropContract;
import cn.flyrise.feep.userinfo.presenter.CropPresenter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.hyphenate.easeui.widget.photoview.EasePhotoView;
import com.kevin.crop.UCrop;
import com.kevin.crop.util.BitmapLoadUtils;
import com.kevin.crop.view.CropImageView;
import com.kevin.crop.view.GestureCropImageView;
import com.kevin.crop.view.OverlayView;
import com.kevin.crop.view.TransformImageView;
import com.kevin.crop.view.UCropView;
import java.io.File;
import java.io.OutputStream;

public class CropActivity extends BaseActivity implements CropContract.View {

	private static final String SAVE_INSTANCE_STATE = "save_instance_state";
	private static final int CODE_IMAGE_SELECTION = 788;
	private String takePhoto = "";

	UCropView mUCropView;
	GestureCropImageView mGestureCropImageView;
	OverlayView mOverlayView;

	FEToolbar feToolbar;

	private Uri mOutputUri;

//	private PhotoUtil photoUtil;

	private FrameLayout cropView;

	private EasePhotoView photoView;

	private boolean isShowBeforeIcon = true;

	private FELoadingDialog mLoadingDialog;

	private CropContract.Presenter mPresenter;

	private CameraManager mCamera;//拍照管理

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_crop);
		bindViews();
		mCamera=new CameraManager(this);
	}

//	@Override
//	protected void onSaveInstanceState(Bundle outState) {
//		super.onSaveInstanceState(outState);
//		if (photoUtil != null) {
//			outState.putString(SAVE_INSTANCE_STATE, photoUtil.getPhotoPath());
//		}
//	}

//	@Override
//	protected void onRestoreInstanceState(Bundle savedInstanceState) {
//		super.onRestoreInstanceState(savedInstanceState);
//		if (photoUtil == null) {
//			takePhoto = savedInstanceState.getString(SAVE_INSTANCE_STATE);
//		}
//	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		feToolbar = toolbar;
	}

	private void bindViews() {
		mPresenter = new CropPresenter(this);
		mUCropView = (UCropView) this.findViewById(R.id.weixin_act_ucrop);

		cropView = (FrameLayout) this.findViewById(R.id.crop_view);
		photoView = (EasePhotoView) this.findViewById(R.id.photo_view);

		mGestureCropImageView = mUCropView.getCropImageView();
		mOverlayView = mUCropView.getOverlayView();

		feToolbar.setTitle(getResources().getString(R.string.photograph_icon));
		feToolbar.setRightButtonText(getResources().getString(R.string.photograph_selected));
		initCropView();
		bindListeners();
	}

	private void initShowIcon() {
		if (isShowBeforeIcon) {
			cropView.setVisibility(View.GONE);
			photoView.setVisibility(View.VISIBLE);
		}
		else {
			cropView.setVisibility(View.VISIBLE);
			photoView.setVisibility(View.GONE);
		}
	}

	/**
	 * 初始化裁剪View
	 */
	private void initCropView() {
		// 设置允许缩放
		mGestureCropImageView.setScaleEnabled(true);
		// 设置禁止旋转
		mGestureCropImageView.setRotateEnabled(false);
		// 设置周围阴影是否为椭圆(如果false则为矩形)
		mOverlayView.setOvalDimmedLayer(false);
		// 设置显示裁剪边框
		mOverlayView.setShowCropFrame(true);
		// 设置不显示裁剪网格
		mOverlayView.setShowCropGrid(false);

		final Intent intent = getIntent();
		setImageData(intent);
	}

	private void setImageData(Intent intent) {
		Uri inputUri = intent.getParcelableExtra(UCrop.EXTRA_INPUT_URI);
		mOutputUri = intent.getParcelableExtra(UCrop.EXTRA_OUTPUT_URI);
		try {
			initShowIcon();
			Glide.with(this).load(inputUri)
					.apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA)
							.error(R.drawable.userinfo_default_icon))
					.into(photoView);
		} catch (Exception e) {
			setResultException(e);
		}
		// 设置裁剪宽高比
		if (intent.getBooleanExtra(UCrop.EXTRA_ASPECT_RATIO_SET, false)) {
			float aspectRatioX = intent.getFloatExtra(UCrop.EXTRA_ASPECT_RATIO_X, 0);
			float aspectRatioY = intent.getFloatExtra(UCrop.EXTRA_ASPECT_RATIO_Y, 0);

			if (aspectRatioX > 0 && aspectRatioY > 0) {
				mGestureCropImageView.setTargetAspectRatio(aspectRatioX / aspectRatioY);
			}
			else {
				mGestureCropImageView.setTargetAspectRatio(CropImageView.SOURCE_IMAGE_ASPECT_RATIO);
			}
		}

		// 设置裁剪的最大宽高
		if (intent.getBooleanExtra(UCrop.EXTRA_MAX_SIZE_SET, false)) {
			int maxSizeX = intent.getIntExtra(UCrop.EXTRA_MAX_SIZE_X, 0);
			int maxSizeY = intent.getIntExtra(UCrop.EXTRA_MAX_SIZE_Y, 0);

			if (maxSizeX > 0 && maxSizeY > 0) {
				mGestureCropImageView.setMaxResultImageSizeX(maxSizeX);
				mGestureCropImageView.setMaxResultImageSizeY(maxSizeY);
			}
		}
	}

	private void bindListeners() {
		feToolbar.setRightButtonListener(v -> {
			setFeToolbar();
		});
		mGestureCropImageView.setTransformImageListener(mImageListener);
	}

	private void setFeToolbar() {
		if (isShowBeforeIcon) {
			selectedPictrue();
		}
		else {
			cropAndSaveImage();
		}
	}

	private void selectedPictrue() {
		String[] title = {getResources().getString(R.string.photograph), getResources().getString(R.string.picture_selected)};
		new FEMaterialDialog.Builder(this)
				.setWithoutTitle(true)
				.setItems(title, (dialog, view, position) -> onClickItem(dialog, position))
				.build()
				.show();
	}

	private void onClickItem(AlertDialog dialog, int position) {
		dialog.dismiss();
		switch (position) {
			case 0:
				// "拍照"按钮被点击了
				FePermissions.with(CropActivity.this)
						.permissions(new String[]{Manifest.permission.CAMERA})
						.rationaleMessage(getResources().getString(R.string.permission_rationale_camera))
						.requestCode(PermissionCode.CAMERA)
						.request();
				break;
			case 1:
				Intent intent = new Intent(CropActivity.this, ImageSelectionActivity.class);
				intent.putExtra("extra_single_choice", true);
				intent.putExtra("extra_except_path", new String[]{CoreZygote.getPathServices().getUserPath()});
				startActivityForResult(intent, CODE_IMAGE_SELECTION);
				break;
		}
	}

	private void cropAndSaveImage() {
		OutputStream outputStream = null;
		try {
			final Bitmap croppedBitmap = mGestureCropImageView.cropImage();
			if (croppedBitmap != null) {
				outputStream = getContentResolver().openOutputStream(mOutputUri);
				croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream);
				croppedBitmap.recycle();

				setResultUri(mOutputUri);
			}
			else {
				setResultException(new NullPointerException("CropImageView.cropImage() returned null."));
			}
		} catch (Exception e) {
			setResultException(e);
			FEToast.showMessage(getString(R.string.crop_error));
		} finally {
			BitmapLoadUtils.close(outputStream);
		}
	}

	private TransformImageView.TransformImageListener mImageListener = new TransformImageView.TransformImageListener() {
		@Override
		public void onRotate(float currentAngle) {
		}

		@Override
		public void onScale(float currentScale) {
		}

		@Override
		public void onLoadComplete() {
			Animation fadeInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.crop_fade_in);
			fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					mUCropView.setVisibility(View.VISIBLE);
					mGestureCropImageView.setImageToWrapCropBounds();
				}

				@Override
				public void onAnimationEnd(Animation animation) {

				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}
			});
			mUCropView.startAnimation(fadeInAnimation);
		}

		@Override
		public void onLoadFailure(Exception e) {
			setResultException(e);
		}

	};

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		switch (requestCode) {
			case CameraManager.TAKE_PHOTO_RESULT:// 调用相机拍照
				if (mCamera.isExistPhoto()) {
					setImage(mCamera.getUri());
				}
				break;
			case CODE_IMAGE_SELECTION:
				String imagePath = data.getStringExtra("SelectionData");
				if (!TextUtils.isEmpty(imagePath)) {
					File file = new File(imagePath);
					setImage(Uri.fromFile(file));
				}
				break;
		}
	}

	private void setImage(Uri inputUri) {
		if (inputUri != null && mOutputUri != null) {
			try {
				mGestureCropImageView.setImageUri(inputUri);
				isShowBeforeIcon = false;
				initShowIcon();
				feToolbar.setRightButtonText(getResources().getString(R.string.photograph_submit));
				feToolbar.setTitle(getResources().getString(R.string.photograph_crop));
			} catch (Exception e) {
				setResultException(e);
			}
		}
	}

	@PermissionGranted(PermissionCode.CAMERA)
	public void onCameraPermissionGrated() {
		mCamera.start(CameraManager.TAKE_PHOTO_RESULT);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}

	private void setResultUri(Uri uri) {
		mPresenter.handleCropResult(uri);
	}

	private void setResultException(Throwable throwable) {
		setResult(UCrop.RESULT_ERROR, new Intent().putExtra(UCrop.EXTRA_ERROR, throwable));
	}

	@Override
	public void showLoading() {
		hideLoading();
		mLoadingDialog = new FELoadingDialog.Builder(CropActivity.this)
				.setCancelable(true)
				.create();
		mLoadingDialog.show();
		mLoadingDialog.setOnDismissListener(() -> mPresenter.cancleUploader());
	}

	@Override
	public void showProgress(int progress) {
		if (mLoadingDialog != null) {
			mLoadingDialog.updateProgress(progress);
		}
	}

	@Override
	public void hideLoading() {
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
		}
	}

	@Override
	public void modifySuccess() {
		hideLoading();
		finish();
	}

	@Override
	public void modifyFailure() {
		hideLoading();
		feToolbar.setTitle(getResources().getString(R.string.photograph_icon));
		feToolbar.setRightButtonText(getResources().getString(R.string.photograph_selected));
		isShowBeforeIcon = true;
		initShowIcon();
	}

}
