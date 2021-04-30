package cn.flyrise.feep.qrcode;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.DevicesUtil;

/**
 * Created by klc on 2018/3/9.
 */

public class QRCodeErrorActivity extends BaseActivity {


	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_qrcode);
		String content = getIntent().getStringExtra("content");
		TextView tvContent = (TextView) findViewById(R.id.tvContent);
		tvContent.setText(content);
		findViewById(R.id.btCopy).setOnClickListener(v -> {
			ClipboardManager cmb = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
			cmb.setText(tvContent.getText().toString());
			FEToast.showMessage(getResources().getString(R.string.lbl_text_copy_success));
		});
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		toolbar.setNavigationIcon(R.drawable.back_left_icon);
		toolbar.setNavigationOnClickListener(v -> finish());
		toolbar.setTitle(R.string.dialog_default_title);
	}
}
