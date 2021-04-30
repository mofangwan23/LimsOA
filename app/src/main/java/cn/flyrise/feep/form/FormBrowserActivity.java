package cn.flyrise.feep.form;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;

import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.x5.X5BrowserFragment;
import org.apache.cordova.Config;

import cn.flyrise.android.library.view.RockerView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.cordova.view.CordovaFragment;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;

public abstract class FormBrowserActivity extends BaseActivity {

	public static final String TITLE_DATA_KEY = "TITLE_DATA_KEY";
	public static final String URL_DATA_KEY = "URL_DATA_KEY";
	protected String title;

	protected X5BrowserFragment fragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_form_layout);
		Intent intent = getIntent();
		String url = intent.getStringExtra(URL_DATA_KEY);
		title = intent.getStringExtra(TITLE_DATA_KEY);

		String host = CoreZygote.getLoginUserServices().getServerAddress();
		Bundle args = new Bundle();
		args.putString("homeLink", host + url);
		args.putInt("formIntent", formIntent());
		fragment = new X5BrowserFragment();
		fragment.setArguments(args);

		getSupportFragmentManager().beginTransaction()
				.add(R.id.frame_layout, fragment)
				.show(fragment)
				.commit();
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

	@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (fragment != null) {
			fragment.onActivityResult(requestCode, resultCode, data);
		}
	}

	protected abstract int formIntent();


}
