package cn.flyrise.feep.cordova.Activity;

import android.content.Intent;
import android.text.TextUtils;
import cn.flyrise.feep.K;
import cn.flyrise.feep.cordova.utils.CordovaShowUtils;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.common.CordovaShowInfo;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.schedule.NativeScheduleActivity;
import cn.flyrise.feep.schedule.NewScheduleActivity;
import cn.flyrise.feep.schedule.ScheduleDetailActivity;
import cn.squirtlez.frouter.annotations.RequestExtras;
import cn.squirtlez.frouter.annotations.Route;
import java.util.ArrayList;

/**
 * 类描述：日程管理
 * @author 罗展健
 * @version 1.0
 */
@Route("/schedule/create")
@RequestExtras({"userIds", "extra_business_id", "extra_message_id"})
public class ScheduleActivity extends BaseActivity {

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		if (FunctionManager.isNative(Func.Schedule)) {
			CordovaShowInfo mShowInfo = null;
			String shoInfo = getIntent().getStringExtra(CordovaShowUtils.CORDOVA_SHOW_INFO);
			if (!TextUtils.isEmpty(shoInfo)) {
				mShowInfo = GsonUtil.getInstance().fromJson(shoInfo, CordovaShowInfo.class);
			}
			else {
				String businessId = getIntent().getStringExtra("extra_business_id");
				String messageId = getIntent().getStringExtra("extra_message_id");
				if (!TextUtils.isEmpty(businessId) && !TextUtils.isEmpty(messageId)) {
					mShowInfo = new CordovaShowInfo();
					mShowInfo.id = businessId;
					mShowInfo.msgId = messageId;
					mShowInfo.type = Func.Schedule;
				}
			}

			Intent intent;
			if (mShowInfo == null || TextUtils.isEmpty(mShowInfo.id)) {
				ArrayList<String> userIds = getIntent().getStringArrayListExtra("userIds");
				intent = new Intent(this, CommonUtil.isEmptyList(userIds)
						? NativeScheduleActivity.class : NewScheduleActivity.class);
				intent.putStringArrayListExtra("userIds", userIds);
			}
			else {
				intent = new Intent(this, ScheduleDetailActivity.class);
				intent.putExtra(K.schedule.event_source_id, mShowInfo.id);
				intent.putExtra(K.schedule.event_source, "fe.do?SYS.ACTION=viewevent&SYS.ID=017-001-000");
				intent.putExtra(K.schedule.schedule_id, mShowInfo.msgId);
			}
			startActivity(intent);
			finish();
			return;
		}

		Intent startIntent = CordovaShowUtils.getCordovaActivity(this, Func.Schedule);
		String showInfoStr = getIntent().getStringExtra(CordovaShowUtils.CORDOVA_SHOW_INFO);
		if (!TextUtils.isEmpty(showInfoStr)) {
			startIntent.putExtra(CordovaShowUtils.CORDOVA_SHOW_INFO, showInfoStr);
			startIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			ScheduleActivity.this.startActivity(startIntent);
			finish();
			return;
		}

		CordovaShowInfo scheduleShowInfo = new CordovaShowInfo();
		scheduleShowInfo.id = getIntent().getStringExtra("extra_business_id");
		scheduleShowInfo.msgId = getIntent().getStringExtra("extra_message_id");
		scheduleShowInfo.type = Func.Schedule;
		startIntent.putExtra(CordovaShowUtils.CORDOVA_SHOW_INFO, GsonUtil.getInstance().toJson(scheduleShowInfo));
		startActivity(startIntent);
		finish();
	}

	@Override
	protected boolean optionStatusBar() {
		return false;
	}
}
