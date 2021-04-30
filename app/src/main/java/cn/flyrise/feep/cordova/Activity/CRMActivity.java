package cn.flyrise.feep.cordova.Activity;

import android.content.Intent;
import android.text.TextUtils;
import cn.flyrise.feep.cordova.utils.CordovaShowUtils;
import cn.flyrise.feep.cordova.view.ParticularCordovaActivity;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.common.CordovaShowInfo;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.squirtlez.frouter.annotations.RequestExtras;
import cn.squirtlez.frouter.annotations.Route;

@Route("/crm/home")
@RequestExtras({"extra_message_url"})
public class CRMActivity extends BaseActivity {

	@Override
	protected boolean optionStatusBar() {
		return false;
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		Intent intent = new Intent(CRMActivity.this, ParticularCordovaActivity.class);
		String showInfoStr = getIntent().getStringExtra(CordovaShowUtils.CORDOVA_SHOW_INFO);
		if (!TextUtils.isEmpty(showInfoStr)) {
			intent.putExtra(CordovaShowUtils.CORDOVA_SHOW_INFO, showInfoStr);
			intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			startActivity(intent);
			finish();
			return;
		}

		CordovaShowInfo showInfo = new CordovaShowInfo();
		showInfo.url = getIntent().getStringExtra("extra_message_url");
		showInfo.type = Func.CRM;
		intent.putExtra(CordovaShowUtils.CORDOVA_SHOW_INFO, GsonUtil.getInstance().toJson(showInfo));
		startActivity(intent);
		finish();
	}
}
