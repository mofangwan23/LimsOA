package cn.flyrise.feep.cordova.Activity;

import static cn.flyrise.feep.knowledge.util.KnowKeyValue.EXTRA_RECEIVERMSAID;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.cordova.utils.CordovaShowUtils;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.common.CordovaShowInfo;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.knowledge.FileDetailActivity;
import cn.flyrise.feep.knowledge.NewKnowledgeActivity;
import cn.flyrise.feep.knowledge.RecFileListFormMsgActivity;
import cn.flyrise.feep.knowledge.util.KnowledgeUtil;
import cn.squirtlez.frouter.annotations.RequestExtras;
import cn.squirtlez.frouter.annotations.Route;

/**
 * 类描述：知识管理中心
 * 文档
 * @author 罗展健
 * @version FE6.0.3
 */
@Route("/knowledge/home")
@RequestExtras({"extra_message_id", "extra_business_id"})
public class KnowledgeActivity extends BaseActivity {

	private final String FEEP_UMENG = "KnowledgeActivity";

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
		if (KnowledgeUtil.isNewVersion()) {
			String businessId = getIntent().getStringExtra("extra_business_id");
			if (TextUtils.isEmpty(businessId)) {
				String messageId = getIntent().getStringExtra("extra_message_id");
				if (TextUtils.isEmpty(messageId)) {
					Intent intent = new Intent(KnowledgeActivity.this, NewKnowledgeActivity.class);
					startActivity(intent);
					finish();
					return;
				}

				Intent intent = new Intent(this, RecFileListFormMsgActivity.class);
				intent.putExtra(EXTRA_RECEIVERMSAID, messageId);
				startActivity(intent);
				finish();
				return;
			}

			Intent intent = new Intent(this, FileDetailActivity.class);
			intent.putExtra(FileDetailActivity.FILEID, businessId);
			startActivity(intent);
			finish();
			return;
		}

		Intent intent = CordovaShowUtils.getCordovaActivity(this, Func.Knowledge);
		String showInfoStr = getIntent().getStringExtra(CordovaShowUtils.CORDOVA_SHOW_INFO);
		if (!TextUtils.isEmpty(showInfoStr)) {
			intent.putExtra(CordovaShowUtils.CORDOVA_SHOW_INFO, showInfoStr);
			intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			startActivity(intent);
			finish();
			return;
		}

		CordovaShowInfo showInfo = new CordovaShowInfo();
		showInfo.id = getIntent().getStringExtra("extra_business_id");
		showInfo.msgId = getIntent().getStringExtra("extra_message_id");
		showInfo.type = Func.Knowledge;
		intent.putExtra(CordovaShowUtils.CORDOVA_SHOW_INFO, GsonUtil.getInstance().toJson(showInfo));
		startActivity(intent);
		finish();
	}

	@Override
	public void onResume() {
		super.onResume();
		FEUmengCfg.onActivityResumeUMeng(this, FEEP_UMENG);
	}

	@Override
	public void onPause() {
		super.onPause();
		FEUmengCfg.onActivityPauseUMeng(this, FEEP_UMENG);
	}
}
