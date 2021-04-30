package cn.flyrise.feep.commonality.util;

import static cn.flyrise.feep.core.common.X.Func.Activity;
import static cn.flyrise.feep.core.common.X.Func.AddressBook;
import static cn.flyrise.feep.core.common.X.Func.Announcement;
import static cn.flyrise.feep.core.common.X.Func.CRM;
import static cn.flyrise.feep.core.common.X.Func.CircleNotice;
import static cn.flyrise.feep.core.common.X.Func.Done;
import static cn.flyrise.feep.core.common.X.Func.InBox;
import static cn.flyrise.feep.core.common.X.Func.Knowledge;
import static cn.flyrise.feep.core.common.X.Func.Meeting;
import static cn.flyrise.feep.core.common.X.Func.News;
import static cn.flyrise.feep.core.common.X.Func.Plan;
import static cn.flyrise.feep.core.common.X.Func.RemindPlan;
import static cn.flyrise.feep.core.common.X.Func.Salary;
import static cn.flyrise.feep.core.common.X.Func.Schedule;
import static cn.flyrise.feep.core.common.X.Func.Sended;
import static cn.flyrise.feep.core.common.X.Func.ToDo;
import static cn.flyrise.feep.core.common.X.Func.ToSend;
import static cn.flyrise.feep.core.common.X.Func.Trace;
import static cn.flyrise.feep.core.common.X.Func.Vote;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import cn.flyrise.android.protocol.model.EmailNumber;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.bean.FEListItem;
import cn.flyrise.feep.cordova.Activity.ActivityListActivity;
import cn.flyrise.feep.cordova.Activity.ScheduleActivity;
import cn.flyrise.feep.cordova.Activity.VoteActivity;
import cn.flyrise.feep.cordova.utils.CordovaShowUtils;
import cn.flyrise.feep.core.common.CordovaShowInfo;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.X;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.function.Module;
import cn.flyrise.feep.email.MailBoxActivity;
import cn.flyrise.feep.email.MailDetailActivity;
import cn.flyrise.feep.knowledge.util.KnowledgeUtil;
import cn.flyrise.feep.main.message.MessageVO;
import cn.flyrise.feep.particular.ParticularActivity;
import cn.flyrise.feep.particular.ParticularIntent;
import cn.flyrise.feep.particular.presenter.NewMeetingPresenter;
import cn.flyrise.feep.particular.presenter.ParticularPresenter;
import cn.flyrise.feep.salary.SalaryDetailActivity;
import cn.flyrise.feep.utils.Patches;
import cn.flyrise.feep.workplan7.PlanCreateMainActivity;
import cn.flyrise.feep.workplan7.PlanDetailActivity;
import cn.squirtlez.frouter.FRouter;
import cn.squirtlez.frouter.annotations.Route;

@Route("/util/message/detail")
public class IntentMessageDetail {

	private Context myContext;
	private int mRequestType;
	private String mMessageId;
	private String mId;
	private String mUrl;
	private String mTitle;
	private int type;

	public IntentMessageDetail(Context context, MessageVO data) {
		myContext = context;
		mRequestType = data.getRequestType();
		mId = data.getBusinessID();
		mMessageId = data.getMessageID();
		mUrl = data.getUrl();
		mTitle = data.getTitle();
		type = CommonUtil.parseInt(data.getType());
	}

	//AIUI反射调用
	public IntentMessageDetail(Context context, int type, String mId, String mMessageId, String mTitle, int moduleItemType) {//反射调用
		myContext = context;
		mRequestType = type;
		this.mId = mId;
		this.mMessageId = mMessageId;
		this.mTitle = mTitle;
		this.type = moduleItemType;
	}


	public void startIntent() {
		if (type == -99) {
			return;
		}
		switch (type) {
			case ToDo:    // 协同 待办
			case Done:    // 协同 已办
			case Trace:   // 协同 跟踪
			case ToSend:  // 协同 待发
			case Sended:  // 协同 已发
				new ParticularIntent.Builder(myContext)
						.setBusinessId(mId)
						.setListRequestType(mRequestType)
						.setMessageId(mMessageId)
						.setParticularType(ParticularPresenter.PARTICULAR_COLLABORATION)
						.setTargetClass(ParticularActivity.class)
						.create()
						.start();
				break;
			case News: // 新闻
			case Announcement: // 公告
				FEListItem messageItem = new FEListItem();
				messageItem.setId(mId);
				int particularType = type == News
						? ParticularPresenter.PARTICULAR_NEWS
						: ParticularPresenter.PARTICULAR_ANNOUNCEMENT;

				new ParticularIntent.Builder(myContext)
						.setBusinessId(mId)
						.setMessageId(mMessageId)
						.setFEListItem(messageItem)
						.setListRequestType(mRequestType)
						.setParticularType(particularType)
						.setTargetClass(ParticularActivity.class)
						.create()
						.start();
				break;
			case AddressBook:
			case Meeting:
				if (FunctionManager.hasPatch(Patches.PATCH_MEETING_MANAGER)) {
					new NewMeetingPresenter(myContext, mId, mMessageId).start();
				}
				else {
					new ParticularIntent.Builder(myContext)
							.setBusinessId(mId)
							.setMessageId(mMessageId)
							.setParticularType(ParticularPresenter.PARTICULAR_MEETING)
							.setTargetClass(ParticularActivity.class)
							.create()
							.start();
				}
				break;
			case Plan:
				PlanDetailActivity.Companion.startActivity((Activity) myContext, "", mId);
//				if (FunctionManager.hasPatch(Patches.PATCH_PLAN)) {
//					FRouter.build(myContext, "/plan/detail")
//							.withString("EXTRA_BUSINESSID", mId)
//							.withString("EXTRA_MESSAGEID", mMessageId)
//							.go();
//				}
//				else {
//					new ParticularIntent.Builder(myContext)
//							.setBusinessId(mId)
//							.setMessageId(mMessageId)
//							.setRelatedUserId("")
//							.setParticularType(ParticularPresenter.PARTICULAR_WORK_PLAN)
//							.setTargetClass(ParticularActivity.class)
//							.create()
//							.start();
//				}
				break;
			case InBox:// 邮件收件箱.
				if (TextUtils.equals(mId, "0")) {    // 出现延迟的情况
					MailBoxActivity.startMailBoxActivity(myContext, CommonUtil.getString(R.string.mail_box), EmailNumber.INBOX_INNER);
				}
				else {
					MailDetailActivity.startMailDetailActivity(myContext, EmailNumber.INBOX_INNER, mId);
				}
				break;
			case Knowledge:// 知识管理
				KnowledgeUtil.openReceiverFileActivity(mMessageId, mId, myContext);
				break;
			case Vote:// 投票管理-问卷
//				FRouter.build(myContext, "/x5/browser")
//						.withString("businessId", mId)
//						.withString("messageId", mMessageId)
//						.withInt("moduleId", Vote)
//						.go();
				Intent voteintent = new Intent(myContext, VoteActivity.class);
				CordovaShowInfo voteShowInfo = new CordovaShowInfo();
				voteShowInfo.id = mId;
				voteShowInfo.msgId = mMessageId;
				voteShowInfo.type = Func.Vote;
				voteintent.putExtra(CordovaShowUtils.CORDOVA_SHOW_INFO, GsonUtil.getInstance().toJson(voteShowInfo));
				myContext.startActivity(voteintent);
				break;
			case Activity:// 活动管理
//				FRouter.build(myContext, "/x5/browser")
//						.withString("businessId", mId)
//						.withString("messageId", mMessageId)
//						.withInt("moduleId", Activity)
//						.go();

				Intent activityintent = new Intent(myContext, ActivityListActivity.class);
				CordovaShowInfo activityShowInfo = new CordovaShowInfo();
				activityShowInfo.id = mId;
				activityShowInfo.msgId = mMessageId;
				activityShowInfo.type = Func.Activity;
				activityintent.putExtra(CordovaShowUtils.CORDOVA_SHOW_INFO, GsonUtil.getInstance().toJson(activityShowInfo));
				myContext.startActivity(activityintent);
				break;
			case Schedule:// 日程管理
				Module module = FunctionManager.findModule(X.Func.Schedule);
				if (module != null && !FunctionManager.isNative(X.Func.Schedule) && TextUtils.isEmpty(module.url)) {//兼容65环境
					Intent scheduleintent = new Intent(myContext, ScheduleActivity.class);
					CordovaShowInfo scheduleShowInfo = new CordovaShowInfo();
					scheduleShowInfo.id = mId;
					scheduleShowInfo.msgId = mMessageId;
					scheduleShowInfo.type = X.Func.Schedule;
					scheduleintent.putExtra(CordovaShowUtils.CORDOVA_SHOW_INFO, GsonUtil.getInstance().toJson(scheduleShowInfo));
					myContext.startActivity(scheduleintent);
				}
				else {
					FRouter.build(myContext, "/x5/browser")
							.withString("businessId", mId)
							.withString("messageId", mMessageId)
							.withInt("moduleId", Schedule)
							.go();
				}
				break;
			case CircleNotice:
			case CRM:
				if (TextUtils.isEmpty(mId) || TextUtils.isEmpty(mUrl)) {
					FEToast.showMessage(myContext.getResources().getString(R.string.phone_does_not_support_message));
					return;
				}
				FRouter.build(myContext, "/x5/browser")
						.withString("appointURL", mUrl)
						.withInt("moduleId", Func.Default)
						.go();
				break;
			case Salary:  // 工资
				Intent salaryIntent = new Intent(myContext, SalaryDetailActivity.class);
				salaryIntent.putExtra(K.salary.show_verify_dialog, true);
				salaryIntent.putExtra(K.salary.request_month, mTitle.substring(0, 7));
				myContext.startActivity(salaryIntent);
				break;
			case RemindPlan://提醒新建计划
				PlanCreateMainActivity.Companion.start((Activity) myContext);
				break;
		}
	}

	public static boolean isShowMessage(MessageVO data, Context context) {
		int typeValue = -1;
		if (CommonUtil.isInteger(data.getType())) {
			typeValue = Integer.valueOf(data.getType());
		}
		if (typeValue < 0 || (typeValue == 23 && "0".equals(data.getBusinessID()))) {
			String contentText = data.getSendTime() + data.getAction() + "\n" + data.getTitle() + " " + data.getContent();
			String titleText = context.getString(R.string.phone_does_not_support_message);
			new FEMaterialDialog.Builder(context)
					.setTitle(titleText)
					.setMessage(contentText)
					.setPositiveButton(null, null)
					.build()
					.show();
			return false;
		}

		if (typeValue == 16 && "0".equals(data.getBusinessID())) {
			new FEMaterialDialog.Builder(context)
					.setTitle(null)
					.setMessage(context.getString(R.string.lbl_message_mail_not_exist))
					.setPositiveButton(null, null)
					.build()
					.show();
			return false;
		}
		return true;
	}
}
