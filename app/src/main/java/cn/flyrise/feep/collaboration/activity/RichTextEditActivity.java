package cn.flyrise.feep.collaboration.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieManager;
import cn.flyrise.android.protocol.entity.CommonResponse;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.utility.RichStyle;
import cn.flyrise.feep.collaboration.utility.RichTextContentKeeper;
import cn.flyrise.feep.collaboration.utility.RichTextUtil;
import cn.flyrise.feep.collaboration.view.FontSizeSelectDialog;
import cn.flyrise.feep.collaboration.view.RichTextToolBar;
import cn.flyrise.feep.commonality.manager.XunFeiVoiceInput;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.NotTranslucentBarActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.common.utils.PhotoUtil;
import cn.flyrise.feep.core.dialog.FEColorDialog;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.dialog.FEMaterialDialog.Builder;
import cn.flyrise.feep.core.network.entry.RecordItem;
import cn.flyrise.feep.core.network.listener.OnProgressUpdateListenerImpl;
import cn.flyrise.feep.core.network.request.FileRequest;
import cn.flyrise.feep.core.network.request.FileRequestContent;
import cn.flyrise.feep.core.network.uploader.UploadManager;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.flyrise.feep.media.images.ImageSelectionActivity;
import cn.flyrise.feep.media.record.camera.CameraManager;
import cn.squirtlez.frouter.annotations.RequestExtras;
import cn.squirtlez.frouter.annotations.Route;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import jp.wasabeef.richeditor.RichEditor;
import org.json.JSONObject;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * @author ZYP
 * @since 2017-04-26 14:43 富文本编辑框
 */
@Route("/rich/editor")
@RequestExtras({"title"})
public class RichTextEditActivity extends NotTranslucentBarActivity {

	private static final String SAVE_INSTANCE_STATE = "save_instance_state";

	public String[] IMAGE_MODE;
	public final int CODE_PICTURE_SELECT = 1024;

	private ViewGroup mLayoutContentView;
	private RichEditor mRichEditor;
	private RichTextToolBar mRichTextToolBar;

	private int mEditorFontColor = Color.parseColor("#000000");
	private int mEditorFontSize = 0;
//	private String mCameraPhotoPath;

	//	private PhotoUtil mPhotoUtil;
	private CameraManager mCamera;
	private FELoadingDialog mLoadingDialog;
	private XunFeiVoiceInput mVoiceInput;
	@SuppressLint("HandlerLeak") private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 200) {
				mRichEditor.getInnerHtml(innerHtml -> {
					RichTextContentKeeper.getInstance().setRichTextContent(innerHtml);
					setResult(RESULT_OK);
					finish();
				});
			}
			else if (msg.what == 500) {
				FEToast.showMessage(getString(R.string.action_fail_again));
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		supportRequestWindowFeature(Window.FEATURE_ACTION_MODE_OVERLAY);
		setContentView(R.layout.activity_rich_text_edit);
		mCamera = new CameraManager(this);

	}

//	@Override
//	protected void onSaveInstanceState(Bundle outState) {
//		super.onSaveInstanceState(outState);
//		if (mPhotoUtil != null) {
//			outState.putString(SAVE_INSTANCE_STATE, mPhotoUtil.getPhotoPath());
//		}
//	}
//
//	@Override
//	protected void onRestoreInstanceState(Bundle savedInstanceState) {
//		super.onRestoreInstanceState(savedInstanceState);
//		if (mPhotoUtil == null) {
//			mCameraPhotoPath = savedInstanceState.getString(SAVE_INSTANCE_STATE);
//		}
//	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		String title = getIntent().getStringExtra("title");
		if (TextUtils.isEmpty(title)) {
			title = getString(R.string.lbl_text_edit);
		}
		toolbar.setTitle(title);
		toolbar.setRightText(cn.flyrise.feep.core.R.string.core_btn_positive);
		toolbar.setRightTextClickListener(view -> {
			mRichEditor.getContentLength(contentLength -> {
				int length = CommonUtil.parseInt(contentLength);
				if (length > 2000) {
					new Builder(RichTextEditActivity.this)
							.setMessage(R.string.richedit_over_hint)
							.setPositiveButton(null, null).build().show();
				}
				else {
					if (RichTextContentKeeper.getInstance().isAllImageUpload()) {               // 所有图片上传完毕...
						mRichEditor.getInnerHtml(innerHtml -> {
							RichTextContentKeeper.getInstance().setRichTextContent(innerHtml);
							setResult(RESULT_OK);
							finish();
						});
					}
					else {
						tryToUploadImage();
					}
				}
			});
		});

		toolbar.setNavigationOnClickListener(view -> askForExist());
	}

	@Override
	public void bindData() {
		super.bindData();
		mVoiceInput = new XunFeiVoiceInput(this);
		IMAGE_MODE = new String[]{getString(R.string.attach_take_pic), getString(R.string.know_from_pic)};
	}

	@Override
	public void bindView() {
		mLayoutContentView = (ViewGroup) findViewById(R.id.layoutContentView);
		mRichEditor = (RichEditor) findViewById(R.id.richEditor);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			try {
				CookieManager.getInstance().setAcceptThirdPartyCookies(mRichEditor, true);
			} catch (Exception exp) {
			}
		}

		mRichTextToolBar = (RichTextToolBar) findViewById(R.id.richTextToolBar);
		mRichTextToolBar.setRichEditor(mRichEditor);
		mRichEditor.setFontSize(4);
		mRichEditor.setEditorFontColor(mEditorFontColor);
		mRichEditor.setPlaceholder(getResources().getString(R.string.collaboration_content));
		mRichEditor.setBold();
		mRichEditor.focusEditor();
		if (RichTextContentKeeper.getInstance().hasContent()) {
			mRichEditor.setHtml(RichTextContentKeeper.getInstance().getRichTextContent());
		}
		else {
			Observable.timer(500, TimeUnit.MILLISECONDS)
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(time -> DevicesUtil.showKeyboard(mRichEditor), exception -> exception.printStackTrace());
		}
	}

	@Override
	public void bindListener() {
		mRichEditor.setOnClickListener(view -> mRichEditor.focusEditor());
		mRichEditor.setOnDecorationChangeListener(this::onDecorationChange);
		mRichEditor.setOnContentEmptyListener(() -> {
			mRichEditor.setTextColorExtend(mEditorFontColor = Color.parseColor("#000000"));
			mEditorFontSize = FontSizeSelectDialog.FONT_SIZE_DEFAULT;
			mRichEditor.setFontSize(4);
			mRichTextToolBar.resetAllStyle();
		});

		// 选择图片
		mRichTextToolBar.setImageMenuClickListener(view -> {
			new FEMaterialDialog.Builder(RichTextEditActivity.this)
					.setWithoutTitle(true)
					.setItems(IMAGE_MODE, (dialog, v, position) -> {
						if (position == 0) {                                                            // 拍照
							FePermissions.with(RichTextEditActivity.this)
									.permissions(new String[]{Manifest.permission.CAMERA})
									.rationaleMessage(getResources().getString(R.string.permission_rationale_camera))
									.requestCode(PermissionCode.CAMERA)
									.request();
						}
						else {                                                                          // 从手机相册选择
							Intent intent = new Intent(RichTextEditActivity.this, ImageSelectionActivity.class);
							intent.putExtra("extra_except_path", new String[]{CoreZygote.getPathServices().getUserPath()});
							startActivityForResult(intent, CODE_PICTURE_SELECT);
						}
						dialog.dismiss();
					})
					.build()
					.show();
		});

		// 字体颜色
		mRichTextToolBar.setFontColorMenuClickListener(view -> {                                               // 插入超链接
			FEColorDialog dialogFragment = FEColorDialog.newInstance(mEditorFontColor);
			dialogFragment.setOnColorSelectedListener(color -> {
				mEditorFontColor = color;
				mRichEditor.setTextColor(mEditorFontColor);
				mRichTextToolBar.setFontColorBtnColor(mEditorFontColor);
				Observable.timer(200, TimeUnit.MILLISECONDS)
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(time -> DevicesUtil.showKeyboard(mRichEditor), exception -> exception.printStackTrace());
			});
			dialogFragment.show(getSupportFragmentManager(), "colorDialog");
		});

		// 字体大小
		mRichTextToolBar.setFontSizeMenuClickListener(view -> {
			// 甩个 dialog 进行选择
			FontSizeSelectDialog dialog = new FontSizeSelectDialog();
			dialog.setDefaultSize(mEditorFontSize);
			dialog.setOnFontSizeSelectedListener(fontSize -> {
				mEditorFontSize = fontSize;
				switch (fontSize) {
					case FontSizeSelectDialog.FONT_SIZE_BIG:
						mRichEditor.setFontSize(5);
						break;
					case FontSizeSelectDialog.FONT_SIZE_DEFAULT:
						mRichEditor.setFontSize(4);
						break;
					case FontSizeSelectDialog.FONT_SIZE_SMALL:
						mRichEditor.setFontSize(2);
						break;
				}
				Observable.timer(200, TimeUnit.MILLISECONDS)
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(time -> DevicesUtil.showKeyboard(mRichEditor), exception -> exception.printStackTrace());
			});
			dialog.show(getSupportFragmentManager(), "fontSizeDialog");
		});

		// 语音输入
		mRichTextToolBar.setVoiceMenuClickListener(view -> {
			FePermissions.with(RichTextEditActivity.this)
					.permissions(new String[]{Manifest.permission.RECORD_AUDIO})
					.rationaleMessage(getResources().getString(R.string.permission_rationale_record))
					.requestCode(PermissionCode.RECORD)
					.request();
		});

		mVoiceInput.setOnRecognizerDialogListener(result -> {
			String htmlResult = RichTextUtil.buildVoiceHtml(result, mRichTextToolBar.isBold(),
					mRichTextToolBar.isUnderLine(), mEditorFontColor, mEditorFontSize);
			FELog.i("HTML result = " + htmlResult);
			mRichEditor.appendText(htmlResult);
			Observable.timer(400, TimeUnit.MILLISECONDS)
					.subscribeOn(Schedulers.io())
					.observeOn(AndroidSchedulers.mainThread())
					.subscribe(time -> {
						DevicesUtil.showKeyboard(mRichEditor);
					}, exception -> exception.printStackTrace());
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CODE_PICTURE_SELECT) {                           // 从相册中选择
			if (data != null) {
				ArrayList<String> selectedImages = data.getStringArrayListExtra("SelectionData");
				if (CommonUtil.nonEmptyList(selectedImages)) {
					handleImageAfterSelect(selectedImages);
				}
			}
		}
		else if (requestCode == CameraManager.TAKE_PHOTO_RESULT) {             // 拍照
			if (resultCode == RESULT_OK) {
				if (mCamera.isExistPhoto())
					handleImageAfterSelect(Arrays.asList(mCamera.getAbsolutePath()));
			}
		}
	}

	private void handleImageAfterSelect(List<String> imagesPath) {
		Observable
				.from(imagesPath)
				.map(RichTextUtil::compressImageByRichEditor)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(compressImagePath -> {
					if (TextUtils.isEmpty(compressImagePath)) {
						return;
					}
					RichTextContentKeeper.getInstance().addCompressImagePath(compressImagePath);
					Uri uri = Uri.fromFile(new File(compressImagePath));
					mRichEditor.insertImage(uri.getPath(), "Unknown");
					DevicesUtil.showKeyboard(mRichEditor);
				}, exception -> exception.printStackTrace());
	}

	@PermissionGranted(PermissionCode.CAMERA)
	public void onCameraPermissionGrated() {
		mCamera.start(CameraManager.TAKE_PHOTO_RESULT);
	}

	@PermissionGranted(PermissionCode.RECORD)
	public void onRecordPermissionGranted() {
		if (mVoiceInput != null) mVoiceInput.show();
	}

	private void onDecorationChange(String text, List<RichEditor.Type> types) {
		if (!TextUtils.isEmpty(text)) {
			String[] decorations = text.split(";");
			RichStyle richStyle = RichTextUtil.buildRichStyle(decorations);
			mRichTextToolBar.setMenuStyle(richStyle);
			mEditorFontSize = richStyle.fontSize;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mRichEditor != null) {
			mLayoutContentView.removeAllViews();
			mRichEditor.removeAllViews();
			mRichEditor.destroy();
			mRichEditor = null;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
			grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}

	/**
	 * 尝试提交 Image
	 */
	private void tryToUploadImage() {
		List<String> compressImagePaths = RichTextContentKeeper.getInstance().getCompressImagePaths();
		FileRequest fileRequest = new FileRequest();
		FileRequestContent fileRequestContent = new FileRequestContent();
		fileRequestContent.setAttachmentGUID(UUID.randomUUID().toString());
		fileRequestContent.setFiles(compressImagePaths);
		fileRequest.setFileContent(fileRequestContent);

		new UploadManager(this)
				.fileRequest(fileRequest)
				.progressUpdateListener(new OnProgressUpdateListenerImpl() {
					@Override
					public void onPreExecute() {
						showLoading();
					}

					@Override
					public void onProgressUpdate(long currentBytes, long contentLength, boolean done) {
					}

					@Override
					public void onPostExecute(String jsonBody) {
						hideLoading();
						FELog.i("onPostExecute : " + Thread.currentThread().getName());
						try {
							JSONObject properties = new JSONObject(jsonBody);
							JSONObject iq = properties.getJSONObject("iq");
							String query = iq.get("query").toString();

							final CommonResponse commonResponse
									= GsonUtil.getInstance().fromJson(query, CommonResponse.class);

							runOnUiThread(() -> {
								List<RecordItem> attachmentItems = commonResponse.getAttaItems();
								List<String> localImagePath = RichTextContentKeeper.getInstance().getCompressImagePaths();
								for (int i = 0; i < localImagePath.size(); i++) {
									RecordItem recordItem = attachmentItems.get(i);
									String path = localImagePath.get(i);
									RichTextContentKeeper.getInstance().addLocalAndGUID(path, recordItem.getMaster_key());
								}
								mHandler.sendEmptyMessage(200);
							});
						} catch (Exception exp) {
							FELog.i("onPostExecute : ");
							exp.printStackTrace();
							mHandler.sendEmptyMessage(500);
						}
					}

					@Override
					public void onFailExecute(Throwable ex) {
						FELog.i("onFailExecute : ");
						ex.printStackTrace();
					}
				})
				.execute();
	}

	private void showLoading() {
		hideLoading();
		mLoadingDialog = new FELoadingDialog.Builder(this)
				.setCancelable(false)
				.setLoadingLabel(getString(R.string.core_loading_wait))
				.create();
		mLoadingDialog.show();
	}

	private void hideLoading() {
		if (mLoadingDialog != null) {
			mLoadingDialog.hide();
			mLoadingDialog = null;
		}
	}

	@Override
	public void onBackPressed() {
		askForExist();
	}

	private void askForExist() {
		List<String> compressImagePaths = RichTextContentKeeper.getInstance().getCompressImagePaths();
		if (CommonUtil.nonEmptyList(compressImagePaths)) {
			showConfirmDialog();
			return;
		}

		mRichEditor.getContentLength(contentLength -> {
			int length = CommonUtil.parseInt(contentLength);
			if (length == 0) {
				setResult(RESULT_CANCELED);
				finish();
				return;
			}
			showConfirmDialog();
		});
	}

	private void showConfirmDialog() {
		new FEMaterialDialog.Builder(this)
				.setTitle(null)
				.setMessage(getString(cn.flyrise.feep.core.R.string.exit_edit_tig))
				.setPositiveButton(null, dialog -> {
					setResult(RESULT_CANCELED);
					finish();
				})
				.setNegativeButton(null, null)
				.build()
				.show();
	}
}
