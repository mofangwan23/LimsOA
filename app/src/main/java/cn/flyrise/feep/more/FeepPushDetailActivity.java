package cn.flyrise.feep.more;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import cn.flyrise.feep.FEMainActivity;
import cn.flyrise.feep.auth.unknown.NewLoginActivity;
import cn.flyrise.feep.auth.views.SplashActivity;
import cn.flyrise.feep.auth.views.fingerprint.NewFingerprintLoginActivity;
import cn.flyrise.feep.auth.views.gesture.GestureLoginActivity;
import cn.flyrise.feep.commonality.util.IntentMessageDetail;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.main.message.MessageVO;

/**
 * Create by cm132 on 2019/5/22 16:11.
 * Describe:推送详情的过度也，判断是进入1、消息详情、2、启动页、3、登录页
 */
public class FeepPushDetailActivity extends AppCompatActivity {

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FELog.i("-->>>>push:FeepPushDetailActivity-onCreate");
		if (getIntent() != null && getIntent().getData() != null) {
			startSpecificActivity(getIntent().getData());
		}
		finish();
	}

	private void startSpecificActivity(Uri uri) {
		MessageVO vo = toMessageVo(uri);
		if (vo == null) return;
		SpUtil.put(PreferencesUtils.PUSH_DETAIL_MSG, GsonUtil.getInstance().toJson(vo));
		if (CoreZygote.getApplicationServices() == null) {//为空代表应用没有启动
			FELog.i("-->>>>feepPush-1-启动");
			startActivity(new Intent(this, SplashActivity.class));
		}
		else if (isExistActivity(FEMainActivity.class)) {//存在主界面，直接进入详情页
			FELog.i("-->>>>feepPush-3-main");
			SpUtil.put(PreferencesUtils.PUSH_DETAIL_MSG, "");
			new IntentMessageDetail(this, vo).startIntent();
		}
		else if (!isExistLoginActivity()) {//不存在主界面并且不在登录页，应用重新未启动
			FELog.i("-->>>>feepPush-2-login");
			startActivity(new Intent(this, SplashActivity.class));
		}
		else {//在登录页的情况
			FELog.i("-->>>>feepPush-4");
		}
	}

	private boolean isExistLoginActivity() {
		return isExistActivity(NewLoginActivity.class)//默认登录页
				|| isExistActivity(NewFingerprintLoginActivity.class)//指纹登录页
//				|| isExistActivity(FingerprintLoginActivity.class)//指纹登录页
				|| isExistActivity(GestureLoginActivity.class);//手势密码登录页
	}

	private boolean isExistActivity(Class<? extends Activity> clss) {
		return CoreZygote.getApplicationServices().activityInStacks(clss);
	}

	private MessageVO toMessageVo(Uri uri) {
		MessageVO vo = new MessageVO();
		vo.setBusinessID(uri.getQueryParameter("businessID"));
		vo.setMessageID(uri.getQueryParameter("messageID"));
		vo.setUrl(uri.getQueryParameter("url"));
		vo.setType(uri.getQueryParameter("type"));
		vo.setSendTime(System.currentTimeMillis() + "");
		return vo;
	}
}
