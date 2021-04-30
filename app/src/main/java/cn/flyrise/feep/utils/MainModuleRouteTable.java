package cn.flyrise.feep.utils;

import cn.flyrise.feep.addressbook.AddressBookActivity;
import cn.flyrise.feep.addressbook.AddressBookDetailActivity;
import cn.flyrise.feep.addressbook.ContactSearchActivity;
import cn.flyrise.feep.auth.views.SplashActivity;
import cn.flyrise.feep.chat.ChatContactActivity;
import cn.flyrise.feep.collaboration.activity.NewCollaborationActivity;
import cn.flyrise.feep.collaboration.activity.RichTextEditActivity;
import cn.flyrise.feep.collaboration.search.ApprovalSearchActivity;
import cn.flyrise.feep.commonality.ApprovalCollaborationListActivity;
import cn.flyrise.feep.commonality.MessageSearchActivity;
import cn.flyrise.feep.commonality.TheContactPersonSearchActivity;
import cn.flyrise.feep.commonality.util.IntentMessageDetail;
import cn.flyrise.feep.cordova.view.FECordovaActivity;
import cn.flyrise.feep.email.MailBoxActivity;
import cn.flyrise.feep.email.MailDetailActivity;
import cn.flyrise.feep.email.MailSearchActivity;
import cn.flyrise.feep.email.NewAndReplyMailActivity;
import cn.flyrise.feep.form.FormListActivity;
import cn.flyrise.feep.knowledge.FileDetailActivity;
import cn.flyrise.feep.knowledge.KnowledgeSearchActivity;
import cn.flyrise.feep.knowledge.NewKnowledgeActivity;
import cn.flyrise.feep.knowledge.RecFileListFormMsgActivity;
import cn.flyrise.feep.location.SignInMainTabActivity;
import cn.flyrise.feep.location.views.LocationSendActivity;
import cn.flyrise.feep.location.views.LocationSendDetailActivity;
import cn.flyrise.feep.location.views.SignInSearchActivity;
import cn.flyrise.feep.main.message.other.SystemMessageActivity;
import cn.flyrise.feep.main.message.toberead.ToBeReadMessageActivity;
import cn.flyrise.feep.meeting.MeetingQRCodeActivity;
import cn.flyrise.feep.meeting.MeetingSearchActivity;
import cn.flyrise.feep.meeting7.ui.MeetingDetailActivity;
import cn.flyrise.feep.meeting7.ui.MeetingRoomActivity;
import cn.flyrise.feep.particular.ParticularActivity;
import cn.flyrise.feep.salary.SalaryDetailActivity;
import cn.flyrise.feep.schedule.NativeScheduleActivity;
import cn.flyrise.feep.schedule.NewScheduleActivity;
import cn.flyrise.feep.schedule.ScheduleDetailActivity;
import cn.flyrise.feep.schedule.ScheduleSearchActivity;
import cn.flyrise.feep.workplan7.PlanCreateMainActivity;
import cn.flyrise.feep.workplan7.PlanDetailActivity;
import cn.flyrise.feep.workplan7.WorkPlanSearchActivity;
import cn.flyrise.feep.x5.FileDisplayActivity;
import cn.flyrise.feep.x5.X5BrowserActivity;
import cn.squirtlez.frouter.IRouteTable;
import cn.squirtlez.frouter.RouteManager;

/**
 * @author ZYP
 * @since 2017-12-06 11:51
 */
public class MainModuleRouteTable implements IRouteTable {

	@Override public void registerTo(RouteManager manager) {
		manager.register("/addressBook/list", AddressBookActivity.class);
		manager.register("/mail/home", MailBoxActivity.class);
		manager.register("/message/toberead", ToBeReadMessageActivity.class);
		manager.register("/message/search", MessageSearchActivity.class);
		manager.register("/plan/create", PlanCreateMainActivity.class);
		manager.register("/mail/create", NewAndReplyMailActivity.class);
		manager.register("/location/selected", LocationSendActivity.class);
		manager.register("/location/detail", LocationSendDetailActivity.class);
		manager.register("/location/main", SignInMainTabActivity.class);
		manager.register("/location/search", SignInSearchActivity.class);
		manager.register("/rich/editor", RichTextEditActivity.class);
		manager.register("/salary/detail", SalaryDetailActivity.class);
		manager.register("/schedule/native/new", NewScheduleActivity.class);
		manager.register("/cordova/old/page", FECordovaActivity.class);
		manager.register("/x5/browser", X5BrowserActivity.class);
		manager.register("/auth/splash", SplashActivity.class);
		manager.register("/particular/detail", ParticularActivity.class);
		manager.register("/flow/list", FormListActivity.class);
		manager.register("/message/system", SystemMessageActivity.class);
		manager.register("/im/forward", ChatContactActivity.class);
		manager.register("/collaboration/create", NewCollaborationActivity.class);
		manager.register("/collaboration/list", ApprovalCollaborationListActivity.class);
		manager.register("/collaboration/search", ApprovalSearchActivity.class);
		manager.register("/addressBook/detail", AddressBookDetailActivity.class);
		manager.register("/mail/detail", MailDetailActivity.class);
		manager.register("/knowledge/native/RecFileFromMsg", RecFileListFormMsgActivity.class);
		manager.register("/knowledge/native/FileDetail", FileDetailActivity.class);
		manager.register("/knowledge/native/home", NewKnowledgeActivity.class);
		manager.register("/meeting/search", MeetingSearchActivity.class);
		manager.register("/knowledge/search", KnowledgeSearchActivity.class);
		manager.register("/mail/search", MailSearchActivity.class);
		manager.register("/contact/search", ContactSearchActivity.class);
		manager.register("/contact/search/network", TheContactPersonSearchActivity.class);
		manager.register("/schedule/detail", ScheduleDetailActivity.class);
		manager.register("/schedule/search", ScheduleSearchActivity.class);
		manager.register("/schedule/native", NativeScheduleActivity.class);
		manager.register("/plan/search", WorkPlanSearchActivity.class);
		manager.register("/meeting/qrcode", MeetingQRCodeActivity.class);       // 这两个原本是不应该在这里的~
		manager.register("/meeting/room", MeetingRoomActivity.class);
		manager.register("/util/message/detail", IntentMessageDetail.class);
		manager.register("/plan/detail", PlanDetailActivity.class);//计划
		manager.register("/meeting/detail", MeetingDetailActivity.class);//会议
		manager.register("/x5/fileDisplay", FileDisplayActivity.class);  //附件展示
	}
}
