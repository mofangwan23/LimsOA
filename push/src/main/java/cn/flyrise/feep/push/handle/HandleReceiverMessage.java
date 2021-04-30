package cn.flyrise.feep.push.handle;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.notification.NotificationMessage;
import cn.flyrise.feep.core.notification.ReceiverInfo;
import cn.flyrise.feep.push.Push;
import java.util.LinkedList;
import java.util.List;

/**
 * Create by cm132 on 2019/5/17 16:41.
 * Describe:所有透传消息统一回调
 */
public class HandleReceiverMessage {

	String push_content = "PUSH_CONTENT";
	String push_extra = "PUSH_EXTRA";
	private Context mContext = null;
	private List<ReceiverInfo> noticeDatas = new LinkedList();
	private int NOTICE_DATA = 1026;
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler(new Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			FELog.i("-->>>>noticeData:c-" + noticeDatas.size() + "--" + msg.what);
			if (NOTICE_DATA != msg.what || CommonUtil.isEmptyList(noticeDatas)) return false;
			noticeDatas = noticeDatas.size() > 3? noticeDatas.subList(noticeDatas.size() - 3, noticeDatas.size()) : noticeDatas;
			FELog.i("-->>>>noticeData:d-" + noticeDatas.size());
			for (ReceiverInfo data : noticeDatas) {
				sendHandlerMessage(data);
			}
			return false;
		}
	});

	private void sendHandlerMessage(ReceiverInfo data) {
		mHandler.postDelayed(() -> sendBroadcast(data), 1000);
	}


	public void handle(Context context, ReceiverInfo info) {
		mContext = context;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//8.0系统通知提示受通道影响，需要限制通知数量
			if (noticeDatas.size() > 3) noticeDatas.remove(0);
			FELog.i("-->>>>noticeData-a:run-" + noticeDatas.size());
			noticeDatas.add(info);
			mHandler.removeMessages(NOTICE_DATA);
			mHandler.sendEmptyMessageDelayed(NOTICE_DATA, 3 * 1000);
		}
		else {
			sendBroadcast(info);
		}
	}

	@SuppressLint("WrongConstant")
	private void sendBroadcast(ReceiverInfo info) {
		FELog.i("push-handle:title: " + info.title);
		FELog.i("push-handle:extra: " + info.extra);
		FELog.i("push-handle:content: " + info.content);
		FELog.i("push-handle:pushTarget: " + info.pushTarget);
		Intent intent = new Intent();
		if (info.infoType == Push.Receiver.notification) {
			intent.setAction("cn.flyrise.study.notification.NotificationReceiver.PUSH_NOTIFICATION");
		}
		else if (info.infoType == Push.Receiver.message) {
			intent.setAction("cn.flyrise.study.notification.NotificationReceiver.PUSH_MESSAGE");
		}
		else if (info.infoType == Push.Receiver.click) {
			intent.setAction("cn.flyrise.study.notification.NotificationReceiver.ACTION_PUSH_CLICK");
		}
		if (info.pushTarget == Push.Notification.JPUSH) {//极光
			intent.putExtra(push_content, info.content);
			intent.putExtra(push_extra, info.extra);
		}
		else if (info.pushTarget == Push.Notification.HUAWEI) {//华为
			intent.putExtra(push_content, formatJson(GsonUtil.getInstance().toJson(info.rawData)).title);
			intent.putExtra(push_extra, info.content);
		}
		else if (info.pushTarget == Push.Notification.XIAOMI) {//小米
			intent.putExtra(push_content, info.title);
			intent.putExtra(push_extra, info.content);
		}

		if(Build.VERSION.SDK_INT >= 26){
			ComponentName componentName=new ComponentName("cn.flyrise.study","cn.flyrise.feep.notification.NotificationReceiver");
			intent.setComponent(componentName);
			intent.addFlags(0x01000000);//解决在android8.0系统以上2个module之间发送广播接收不到的问题
		}

		mContext.sendBroadcast(intent);
	}

	private NotificationMessage formatJson(String jsonString) {
		try {
			return GsonUtil.getInstance().fromJson(jsonString, NotificationMessage.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}
