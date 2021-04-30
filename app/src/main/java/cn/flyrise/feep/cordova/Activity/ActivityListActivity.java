package cn.flyrise.feep.cordova.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.cordova.utils.CordovaShowUtils;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.common.CordovaShowInfo;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.squirtlez.frouter.annotations.RequestExtras;
import cn.squirtlez.frouter.annotations.Route;

/**
 * 类描述：活动列表
 * @author 罗展健
 * @version 1.0
 */
@Route("/activity/home")
@RequestExtras({"extra_message_id", "extra_business_id"})
public class ActivityListActivity extends BaseActivity {

	private final String FEEP_UMENG = "ActivityListActivity";

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
		Intent intent = CordovaShowUtils.getCordovaActivity(this, Func.Activity);
		String showInfoStr = getIntent().getStringExtra(CordovaShowUtils.CORDOVA_SHOW_INFO);
		if (!TextUtils.isEmpty(showInfoStr)) {
			intent.putExtra(CordovaShowUtils.CORDOVA_SHOW_INFO, showInfoStr);
			intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			startActivity(intent);
			finish();
			return;
		}

		CordovaShowInfo activityShowInfo = new CordovaShowInfo();
		activityShowInfo.id = getIntent().getStringExtra("extra_business_id");
		activityShowInfo.msgId = getIntent().getStringExtra("extra_message_id");
		activityShowInfo.type = Func.Activity;
		intent.putExtra(CordovaShowUtils.CORDOVA_SHOW_INFO, GsonUtil.getInstance().toJson(activityShowInfo));
		startActivity(intent);
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		FEUmengCfg.onActivityResumeUMeng(this, FEEP_UMENG);
	}

	@Override
	protected void onPause() {
		super.onPause();
		FEUmengCfg.onActivityPauseUMeng(this, FEEP_UMENG);
	}
}
