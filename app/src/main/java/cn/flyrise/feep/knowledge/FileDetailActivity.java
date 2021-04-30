package cn.flyrise.feep.knowledge;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.knowledge.contract.FileDetailContract;
import cn.flyrise.feep.knowledge.model.FileDetail;
import cn.flyrise.feep.knowledge.model.PubAndRecFile;
import cn.flyrise.feep.knowledge.presenter.FileDetailPresenter;
import cn.flyrise.feep.knowledge.util.SizeUtil;
import cn.flyrise.feep.media.common.FileCategoryTable;
import cn.squirtlez.frouter.annotations.Route;

/**
 * Created by klc
 */
@Route("/knowledge/native/FileDetail")
public class FileDetailActivity extends BaseActivity implements FileDetailContract.View {

	public final static String FILEID = "fileId";
	public final static String FILE = "file";

	private ImageView mFileIconIv;
	private TextView mFileNameTv;
	private TextView mSendUserTv;
	private TextView mFileSizeTv;
	private TextView mStartTimeTv;
	private TextView mEndTimeTv;

	public LinearLayout mDownLayout;
	public LinearLayout mOpenLayout;

	private FileDetailContract.Presenter presenter;
	private FileDetail shareFile;
	private Handler mHandler = new Handler(Looper.getMainLooper());

	public static void startFileDetailActivity(Context context, PubAndRecFile receiverAndPublishFile) {
		Intent intent = new Intent(context, FileDetailActivity.class);
		FileDetail fileDetail = new FileDetail();
		if (receiverAndPublishFile.enddate.equals(context.getString(R.string.know_infinite))) {
			fileDetail.setExpiredtimelong(0);
		}
		else {
			fileDetail
					.setExpiredtimelong(DateUtil.strToDate(receiverAndPublishFile.enddate.substring(0, 16), "yyyy-MM-dd HH:mm").getTime());
		}
		fileDetail.setFileid(receiverAndPublishFile.id);
		fileDetail.setFilesize(SizeUtil.formatSize(receiverAndPublishFile.filesize));
		fileDetail.setFiletype(receiverAndPublishFile.filetype);
		fileDetail.setPubTimeLong(DateUtil.strToDate(receiverAndPublishFile.startdate.substring(0, 16), "yyyy-MM-dd HH:mm").getTime());
		fileDetail.setPubUserName(receiverAndPublishFile.publishuser);
		fileDetail.setTitle(receiverAndPublishFile.title);
		intent.putExtra(FILE, fileDetail);
		context.startActivity(intent);
	}

	public static void startFileDetailActivity(Context context, FileDetail fileDetail) {
		Intent intent = new Intent(context, FileDetailActivity.class);
		intent.putExtra(FILE, fileDetail);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.knowledge_show_public);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		toolbar.setTitle(R.string.know_file_detail);
	}

	@Override
	public void bindView() {
		super.bindView();
		findViewById(R.id.share_layout).setVisibility(View.GONE);
		findViewById(R.id.rename_layout).setVisibility(View.GONE);
		findViewById(R.id.move_layout).setVisibility(View.GONE);
		findViewById(R.id.delete_layout).setVisibility(View.GONE);
		mDownLayout = findViewById(R.id.down_layout);
		mOpenLayout = findViewById(R.id.open_layout);
		mFileIconIv = findViewById(R.id.file_icon);
		mFileNameTv = findViewById(R.id.file_name);
		mSendUserTv = findViewById(R.id.send_user);
		mFileSizeTv = findViewById(R.id.file_size);
		mStartTimeTv = findViewById(R.id.public_time);
		mEndTimeTv = findViewById(R.id.valid_time);
	}

	@Override
	public void bindData() {
		super.bindData();
		String mFileId = getIntent().getStringExtra(FILEID);
		this.shareFile = getIntent().getParcelableExtra(FILE);
		presenter = new FileDetailPresenter(this, this);
		if (mFileId == null) {
			showFileDetail(shareFile);
		}
		else {
			presenter.getFileDetailById(mFileId);
		}
	}

	@Override
	public void bindListener() {
		super.bindListener();
		mDownLayout.setOnClickListener(v -> presenter.openFile(shareFile));
		mOpenLayout.setOnClickListener(v -> presenter.openFile(shareFile));
	}

	@Override
	public void showFileDetail(FileDetail file) {
		this.shareFile = file;
//		FEImageLoader.load(this, mFileIconIv, FileCategoryTable.getIcon(file.getFiletype()));
		FEImageLoader.load(this, mFileIconIv, FileCategoryTable.getIcon(FileCategoryTable.getType(file.getFiletype())));
		mFileNameTv.setText(file.getTitle() + file.getFiletype());
		mSendUserTv.setText(file.getPubUserName());
		mFileSizeTv.setText(file.getFilesize());
		mStartTimeTv.setText(DateUtil.formatTimeForDetail(file.getPubTimeLong()));
		if (file.getExpiredtimelong() == 0) {
			mEndTimeTv.setText(getString(R.string.know_infinite));
		}
		else {
			mEndTimeTv.setText(DateUtil.formatTimeForDetail(file.getExpiredtimelong()));
		}
		showDownLayout(!presenter.haveDownloaded(file));
	}

	@Override
	public void showProgress(int resourceID, int progress) {
		LoadingHint.showProgress(progress, getString(resourceID));
	}


	@Override
	public void showConfirmDialog(int resourceID, FEMaterialDialog.OnClickListener onClickListener) {
		new FEMaterialDialog.Builder(this).setMessage(getString(resourceID))
				.setPositiveButton(null, onClickListener)
				.setNegativeButton(null, null)
				.build()
				.show();
	}

	@Override
	public void showDownLayout(boolean show) {
		if (show) {
			mDownLayout.setVisibility(View.VISIBLE);
			mOpenLayout.setVisibility(View.GONE);
		}
		else {
			mDownLayout.setVisibility(View.GONE);
			mOpenLayout.setVisibility(View.VISIBLE);
		}
	}

	@Override public void openFile(Intent intent) {
		if (intent == null) {
			FEToast.showMessage("暂不支持查看此文件类型");
			return;
		}

		try {
			startActivity(intent);
		} catch (Exception exp) {
			FEToast.showMessage("无法打开，建议安装查看此类型文件的软件");
		}
	}

	@Override
	public void showDealLoading(boolean show) {
		if (show)
			LoadingHint.show(this);
		else
			LoadingHint.hide();
	}

	@Override
	public void showMessage(int resourceID) {
		if (this.isFinishing()) return;
		mHandler.post(() -> FEToast.showMessage(getString(resourceID)));
	}


}
