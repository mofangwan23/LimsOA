package cn.flyrise.feep.cordova.Activity;

import android.content.Intent;
import android.os.Bundle;
import cn.flyrise.feep.cordova.utils.CordovaShowUtils;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.common.X.Func;
import cn.squirtlez.frouter.annotations.RequestExtras;
import cn.squirtlez.frouter.annotations.Route;

@Route("/dudu/home")
@RequestExtras({"cordova_show_info"})
public class DuduActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected boolean optionStatusBar() {
		return false;
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		Intent intent = CordovaShowUtils.getCordovaActivity(this, Func.Vote);
		if (getIntent() != null) {
			String shoInfo = getIntent().getStringExtra(CordovaShowUtils.CORDOVA_SHOW_INFO);
			intent.putExtra(CordovaShowUtils.CORDOVA_SHOW_INFO, shoInfo);
		}
		intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
		startActivity(intent);
		this.finish();
	}
}
