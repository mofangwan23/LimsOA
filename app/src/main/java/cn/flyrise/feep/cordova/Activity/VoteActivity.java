package cn.flyrise.feep.cordova.Activity;

import android.content.Intent;
import android.text.TextUtils;
import cn.flyrise.feep.cordova.utils.CordovaShowUtils;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.common.CordovaShowInfo;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.squirtlez.frouter.annotations.RequestExtras;
import cn.squirtlez.frouter.annotations.Route;

/**
 * 类描述：调查问卷
 * @author 罗展健
 * @version FE6.0.3
 */
@Route("/vote/home")
@RequestExtras({"extra_message_id", "extra_business_id"})
public class VoteActivity extends BaseActivity {

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		//判断要跳到旧的FECordovaActivity 还是 ParticularCordovaActivity
		Intent intent = CordovaShowUtils.getCordovaActivity(this, Func.Vote);
		String showInfoStr = getIntent().getStringExtra(CordovaShowUtils.CORDOVA_SHOW_INFO);
		if (!TextUtils.isEmpty(showInfoStr)) {
			intent.putExtra(CordovaShowUtils.CORDOVA_SHOW_INFO, showInfoStr);
			intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			startActivity(intent);
			finish();
			return;
		}

		CordovaShowInfo voteShowInfo = new CordovaShowInfo();
		voteShowInfo.id = getIntent().getStringExtra("extra_business_id");
		voteShowInfo.msgId = getIntent().getStringExtra("extra_message_id");
		voteShowInfo.type = Func.Vote;
		intent.putExtra(CordovaShowUtils.CORDOVA_SHOW_INFO, GsonUtil.getInstance().toJson(voteShowInfo));
		startActivity(intent);
		finish();
	}

	@Override
	protected boolean optionStatusBar() {
		return false;
	}
}
