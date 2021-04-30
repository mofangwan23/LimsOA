package cn.flyrise.feep.form;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import cn.flyrise.android.library.view.RockerView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.cordova.view.CordovaFragment;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import org.apache.cordova.Config;

/**
 * Created by Administrator on 2016-12-23.
 */

public class FormCordovaActivity extends BaseActivity {

	public static final String TITLE_DATA_KEY = "TITLE_DATA_KEY";
	public static final String URL_DATA_KEY = "URL_DATA_KEY";
	public static final String LOAD_KEY = "LOAD_KEY";

	protected CordovaFragment fragment;

	protected FEToolbar mToolbar;

	protected String webViewUrl;

	protected String title;

	protected String baseUrl;

	protected RockerView rockerView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			setContentView(R.layout.new_form_layout);
			final FragmentTransaction fragmentTransaction = this.getSupportFragmentManager().beginTransaction();
			fragment = new CordovaFragment();
			fragmentTransaction.add(R.id.frame_layout, fragment).commit();
			Config.init(this);
			rockerView = (RockerView) this.findViewById(R.id.rocker_view);
			rockerView.setMoveSpeed(20);
			getIntentData(getIntent());
			if (mToolbar != null && !TextUtils.isEmpty(title)) {
				mToolbar.setTitle(title);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		rockerView.setOnShakingListener(new RockerView.onShakingListener() {
			@Override
			public void onGyroScrolling(double distanceX, double distanceY) {
				fragment.setScrollBy(distanceX, distanceY);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (fragment != null) {
			fragment.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	/**
	 * 获取intent中的数据
	 */
	private void getIntentData(Intent intent) {
		if (intent != null) {
			webViewUrl = intent.getStringExtra(URL_DATA_KEY);
			title = intent.getStringExtra(TITLE_DATA_KEY);
		}
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		super.toolBar(toolbar);
		mToolbar = toolbar;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			new FEMaterialDialog.Builder(this)
					.setTitle(null)
					.setMessage(getString(R.string.form_exit_edit_tig))
					.setPositiveButton(null, dialog -> finish())
					.setNegativeButton(null, null)
					.build()
					.show();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}


}
