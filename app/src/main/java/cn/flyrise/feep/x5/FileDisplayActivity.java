package cn.flyrise.feep.x5;

import android.os.Bundle;
import android.text.TextUtils;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.FileUtil;
import cn.flyrise.feep.x5.view.FileReaderView;
import cn.squirtlez.frouter.annotations.RequestExtras;
import cn.squirtlez.frouter.annotations.Route;
import java.io.File;

@Route("/x5/fileDisplay")
@RequestExtras("filePath")
public class FileDisplayActivity extends BaseActivity {

	private FileReaderView mDocumentReaderView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_display);
	}

	@Override protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		toolbar.setTitle("文件详情");
	}

	@Override public void bindView() {
		super.bindView();
		mDocumentReaderView = findViewById(R.id.documentReaderView);
	}

	@Override public void bindData() {
		super.bindData();
		String filePath = getIntent().getStringExtra("filePath");
		if (TextUtils.isEmpty(filePath) || !FileUtil.isFileExists(filePath)){
			FEToast.showMessage("文件不存在");
		}
		mDocumentReaderView.show(filePath);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mDocumentReaderView != null) {
			mDocumentReaderView.stop();
		}
	}
}
