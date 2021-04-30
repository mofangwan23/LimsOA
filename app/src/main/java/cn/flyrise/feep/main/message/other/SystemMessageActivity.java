package cn.flyrise.feep.main.message.other;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import cn.flyrise.feep.R;
import cn.flyrise.feep.cordova.utils.CordovaShowUtils;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.notification.NotificationMessage;
import cn.flyrise.feep.main.message.BaseMessageActivity;
import cn.flyrise.feep.main.message.MessageConstant;
import cn.squirtlez.frouter.annotations.Route;
import java.util.Arrays;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-03-30 14:41
 * FE 666 版本：系统消息
 */
@Route("/message/system")
public class SystemMessageActivity extends BaseMessageActivity {

	public static void start(Activity activity, String category) {
		Intent intent = new Intent(activity, SystemMessageActivity.class);
		intent.putExtra("category", category);
		activity.startActivity(intent);
	}

	public static void startForNotification(Context context, String category, NotificationMessage message) {
		Intent intent = new Intent(context, SystemMessageActivity.class);
		intent.putExtra("category", category);
		intent.putExtra(CordovaShowUtils.MSGID, message.getMsgId());
		intent.putExtra("isSystemMessage", true);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		String category = getIntent().getStringExtra("category");
		if (MessageConstant.MISSION.equals(category)) {
			toolbar.setTitle(R.string.message_misson_title);
		}
		else if (MessageConstant.CIRCLE.equals(category)) {
			toolbar.setTitle(R.string.top_associate);
		}
		else if (MessageConstant.SYSTEM.equals(category)) {
			toolbar.setTitle(R.string.message_system_title);
		}
		else if (MessageConstant.NOTIFY.equals(category)) {
			toolbar.setTitle(R.string.message_nofity_title);
		}
	}

	@Override
	protected List<Fragment> getFragments() {
		String category = getIntent().getStringExtra("category");
		return Arrays.asList(SystemMessageFragment.newInstance(category));
	}

	@Override
	protected List<String> getTabTexts() {
		return null;
	}

	@Override
	protected int getTabIcon(int position) {
		return 0;
	}
}
