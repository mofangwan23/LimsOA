package cn.flyrise.feep.notification;

import static cn.flyrise.feep.core.common.X.Func.Activity;
import static cn.flyrise.feep.core.common.X.Func.Announcement;
import static cn.flyrise.feep.core.common.X.Func.CRM;
import static cn.flyrise.feep.core.common.X.Func.CircleNotice;
import static cn.flyrise.feep.core.common.X.Func.Done;
import static cn.flyrise.feep.core.common.X.Func.InBox;
import static cn.flyrise.feep.core.common.X.Func.Knowledge;
import static cn.flyrise.feep.core.common.X.Func.Meeting;
import static cn.flyrise.feep.core.common.X.Func.News;
import static cn.flyrise.feep.core.common.X.Func.Plan;
import static cn.flyrise.feep.core.common.X.Func.Salary;
import static cn.flyrise.feep.core.common.X.Func.Schedule;
import static cn.flyrise.feep.core.common.X.Func.Sended;
import static cn.flyrise.feep.core.common.X.Func.System;
import static cn.flyrise.feep.core.common.X.Func.ToDo;
import static cn.flyrise.feep.core.common.X.Func.ToSend;
import static cn.flyrise.feep.core.common.X.Func.Trace;
import static cn.flyrise.feep.core.common.X.Func.Vote;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import cn.flyrise.android.protocol.model.EmailNumber;
import cn.flyrise.feep.FEApplication;
import cn.flyrise.feep.K;
import cn.flyrise.feep.auth.unknown.NewLoginActivity;
import cn.flyrise.feep.auth.views.SplashActivity;
import cn.flyrise.feep.auth.views.gesture.GestureLoginActivity;
import cn.flyrise.feep.commonality.bean.FEListItem;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.request.NoticesManageRequest;
import cn.flyrise.feep.core.notification.NotificationMessage;
import cn.flyrise.feep.core.services.ILoginUserServices;
import cn.flyrise.feep.email.MailBoxActivity;
import cn.flyrise.feep.email.MailDetailActivity;
import cn.flyrise.feep.event.EventCircleMessageRead;
import cn.flyrise.feep.main.message.MessageConstant;
import cn.flyrise.feep.main.message.other.SystemMessageActivity;
import cn.flyrise.feep.main.message.toberead.ToBeReadMessageActivity;
import cn.flyrise.feep.particular.ParticularActivity;
import cn.flyrise.feep.particular.ParticularIntent;
import cn.flyrise.feep.particular.presenter.NewMeetingPresenter;
import cn.flyrise.feep.particular.presenter.ParticularPresenter;
import cn.flyrise.feep.salary.SalaryDetailActivity;
import cn.flyrise.feep.utils.Patches;
import cn.squirtlez.frouter.FRouter;
import com.dk.view.badge.BadgeUtil;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;


/**
 * @author CWW
 * @version 1.0 <br/>
 * 创建时间: 2013-6-4 下午1:49:39 <br/>
 * 类说明 :
 */
public class NotificationController {

	private static Context mContext;

	public static boolean startDetailActivity(Context context, NotificationMessage message) {
		mContext = context;
		Intent openIntent = new Intent();
		boolean hasStarted = true;
		ILoginUserServices services = CoreZygote.getLoginUserServices();
		if (services != null && !TextUtils.isEmpty(services.getUserId())) {// 检验用户是否已登录
			final int moduleType = CommonUtil.parseInt(message.getType());
			if (moduleType == -99) return false;
			FEApplication feApplication = (FEApplication) context.getApplicationContext();
			int num = feApplication.getCornerNum() - 1;
			BadgeUtil.setBadgeCount(context, num);//角标
			feApplication.setCornerNum(num);
			switch (moduleType) {
				case ToDo:    // 协同 待办
				case Done:    // 协同 已办
				case Trace:   // 协同 跟踪
				case ToSend:  // 协同 待发
				case Sended:  // 协同 已发
					new ParticularIntent.Builder(context)
							.setTargetClass(ParticularActivity.class)
							.setParticularType(ParticularPresenter.PARTICULAR_COLLABORATION)
							.setFromNotification(true)
							.setBusinessId(message.getId())
							.setMessageId(message.getMsgId())
							.setListRequestType(CommonUtil.parseInt(message.getType()))
							.create()
							.start();
					break;
				case News: // 新闻
				case Announcement: // 公告
					final FEListItem newsDetailsItem = new FEListItem();
					newsDetailsItem.setId(message.getId());
					int particularType = moduleType == 5
							? ParticularPresenter.PARTICULAR_NEWS
							: ParticularPresenter.PARTICULAR_ANNOUNCEMENT;
					new ParticularIntent.Builder(context)
							.setTargetClass(ParticularActivity.class)
							.setParticularType(particularType)
							.setFromNotification(true)
							.setBusinessId(message.getId())
							.setFEListItem(newsDetailsItem)
							.setListRequestType(moduleType)
							.create()
							.start();
					break;
				case Meeting: // 会议
					if (FunctionManager.hasPatch(Patches.PATCH_MEETING_MANAGER)) {
						new NewMeetingPresenter(context, message.getId(), message.getMsgId()).start();
					}
					else {
						new ParticularIntent.Builder(context)
								.setTargetClass(ParticularActivity.class)
								.setParticularType(ParticularPresenter.PARTICULAR_MEETING)
								.setFromNotification(true)
								.setBusinessId(message.getId())
								.setMessageId(message.getMsgId())
								.create()
								.start();
					}
					break;
				case Plan: // 计划
					if (FunctionManager.hasPatch(Patches.PATCH_PLAN)) {
						FRouter.build(context, "/plan/detail")
								.withString("EXTRA_BUSINESSID", message.getId())
								.withString("EXTRA_MESSAGEID", message.getMsgId())
								.go();
					}
					else {
						new ParticularIntent.Builder(context)
								.setTargetClass(ParticularActivity.class)
								.setParticularType(ParticularPresenter.PARTICULAR_WORK_PLAN)
								.setFromNotification(true)
								.setBusinessId(message.getId())
								.setMessageId(message.getMsgId())
								.setRelatedUserId("")
								.create()
								.start();
					}
					break;
				case InBox:// 邮件收件箱
					if (TextUtils.equals(message.getId(), "0")) {
						openIntent.setClass(context, MailBoxActivity.class);
						openIntent.putExtra(K.email.EXTRA_TYPE, "收件箱");
						openIntent.putExtra(K.email.box_name, EmailNumber.INBOX_INNER);
					}
					else {
						messageReaded(context, message.getMsgId());
						openIntent.putExtra(K.email.mail_id, message.getId());
						openIntent.putExtra(K.email.box_name, EmailNumber.INBOX_INNER);
						openIntent.setClass(context, MailDetailActivity.class);
					}
					startActivity(context, openIntent);
					break;
				case Activity:// 活动
					FRouter.build(context, "/x5/browser")
							.withString("businessId", message.getId())
							.withString("messageId", message.getMsgId())
							.withInt("moduleId", Activity)
							.go();
					break;
				case Knowledge:// 知识管理
					FRouter.build(context, "/x5/browser")
							.withString("businessId", message.getId())
							.withString("messageId", message.getMsgId())
							.withInt("moduleId", Knowledge)
							.go();
					break;
				case Vote:// 投票
					FRouter.build(context, "/x5/browser")
							.withString("businessId", message.getId())
							.withString("messageId", message.getMsgId())
							.withInt("moduleId", Vote)
							.go();
					break;
				case Schedule:// 日程备忘
					FRouter.build(context, "/x5/browser")
							.withString("businessId", message.getId())
							.withString("messageId", message.getMsgId())
							.withInt("moduleId", Schedule)
							.go();
					break;
				case System:  // 系统消息
					SystemMessageActivity.startForNotification(context, MessageConstant.SYSTEM, message);
					break;
				case CircleNotice:
					gotoWebView(message, moduleType, openIntent, context);
					List<String> messageIds = new ArrayList<>();
					messageIds.add(message.getMsgId());
					EventCircleMessageRead event = new EventCircleMessageRead();
					event.setValue("hasReadCircleMessage");
					event.setMessageIds(messageIds);
					EventBus.getDefault().post(event);
					break;
				case CRM:
					gotoWebView(message, moduleType, openIntent, context);
					break;
				case Salary:
					openIntent.setClass(context, SalaryDetailActivity.class);
					openIntent.putExtra(K.salary.request_month, message.getUrl());
					openIntent.putExtra(K.salary.show_verify_dialog, true);
					startActivity(context, openIntent);
					break;
				default:
					if (!TextUtils.isEmpty(message.getMsgId())) {
						messageReaded(context, message.getMsgId());
					}
					hasStarted = false;
			}
		}
		else {
			FEApplication.sNotificationMessage = message;
			openIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			openIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			if (isExsitMianActivity(NewLoginActivity.class)) {
				openIntent.setClass(context, NewLoginActivity.class);
				startActivity(context, openIntent);
				return hasStarted;
			}
			else if (isExsitMianActivity(GestureLoginActivity.class)) {
				openIntent.setClass(context, GestureLoginActivity.class);
				startActivity(context, openIntent);
				return hasStarted;
			}
			else {
				openIntent.setClass(context, SplashActivity.class);
				startActivity(context, openIntent);
			}
		}
		return hasStarted;
	}

	/**
	 * 判断某一个类是否存在任务栈里面
	 */
	private static boolean isExsitMianActivity(Class<?> cls) {
		Intent intent = new Intent(mContext, cls);
		ComponentName cmpName = intent.resolveActivity(mContext.getPackageManager());
		boolean flag = false;
		if (cmpName != null) { // 说明系统中存在这个activity
			ActivityManager am = (ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);
			List<ActivityManager.RunningTaskInfo> taskInfoList = am.getRunningTasks(10);
			for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
				if (taskInfo.baseActivity.equals(cmpName)) { // 说明它已经启动了
					flag = true;
					break;  //跳出循环，优化效率
				}
			}
		}
		return flag;
	}

	/**
	 * 标记消息已读
	 */
	public static void messageReaded(Context context, String id) {
		if (TextUtils.isEmpty(id)) {
			return;
		}
		final List<String> ids = new ArrayList<>();
		final NoticesManageRequest reqContent = new NoticesManageRequest();
		ids.add(id);
		reqContent.setMsgIds(ids);
		reqContent.setUserId(CoreZygote.getLoginUserServices().getUserId());
		FEHttpClient.getInstance().post(reqContent, null);
	}

	public static void startActivity(Context context, Intent openIntent) {
		if (context instanceof Activity) {
			context.startActivity(openIntent);
		}
		else {
			openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(openIntent);
		}
	}

	public static void gotoWebView(NotificationMessage message, int moduleType, Intent openIntent, Context context) {
		if (TextUtils.isEmpty(message.getUrl())) {
			if (moduleType == CircleNotice) {
				SystemMessageActivity.startForNotification(context, MessageConstant.SYSTEM, message);
			}
			else {
				openIntent.setClass(context, ToBeReadMessageActivity.class);
			}
			startActivity(context, openIntent);
		}
		else {
			FRouter.build(context, "/x5/browser")
					.withString("appointURL", message.getUrl())
					.withInt("moduleId", Func.Default)
					.go();
		}
	}
}
