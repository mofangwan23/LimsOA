package cn.flyrise.feep.main.modules

import android.content.Context
import cn.flyrise.feep.R.drawable.*
import cn.flyrise.feep.collaboration.activity.NewCollaborationActivity
import cn.flyrise.feep.commonality.ApprovalCollaborationListActivity
import cn.flyrise.feep.core.common.X
import cn.flyrise.feep.core.common.X.Func.*
import cn.flyrise.feep.core.function.IPreDefinedModuleRepository
import cn.flyrise.feep.core.function.PreDefinedModule
import cn.flyrise.feep.core.function.PreDefinedShortCut
import cn.flyrise.feep.email.MailBoxHomeActivity
import cn.flyrise.feep.form.FormListActivity
import cn.flyrise.feep.location.SignInMainTabActivity
import cn.flyrise.feep.meeting.MeetingMainActivity
import cn.flyrise.feep.news.AnnouncementListActivity
import cn.flyrise.feep.news.NewsListActivity
import cn.flyrise.feep.report.ReportListActivity
import cn.flyrise.feep.salary.SalaryListActivity
import cn.flyrise.feep.workplan7.PlanMainActivity
import cn.flyrise.feep.x5.X5BrowserActivity

/**
 * @author 社会主义建设接班人
 * @since 2018-07-19 18:29
 * 富强、民主、文明、和谐
 * 自由、平等、公正、法治
 * 爱国、敬业、诚信、友善
 */
class PreDefinedModuleRepository(val c: Context) : IPreDefinedModuleRepository {


    private val r = mutableListOf<PreDefinedModule>()   // 标准公告
    private val q = mutableListOf<PreDefinedShortCut>()     // 应用快捷方式

    init {
        r.add(PreDefinedModule(News, ic_news_v7, NewsListActivity::class.java))                                     // 新闻
        r.add(PreDefinedModule(Announcement, ic_announcement_v7, AnnouncementListActivity::class.java))            // 公告
        r.add(PreDefinedModule(Location, ic_location_v7, SignInMainTabActivity::class.java))                       // 签到
        r.add(PreDefinedModule(Salary, ic_salary_v7, SalaryListActivity::class.java))                              // 工资
        r.add(PreDefinedModule(Meeting, ic_meeting_v7, MeetingMainActivity::class.java))                           // 会议
        r.add(PreDefinedModule(Plan, ic_plan_v7, PlanMainActivity::class.java))                                    // 计划
        r.add(PreDefinedModule(Approval, ic_approval_v7, ApprovalCollaborationListActivity::class.java))           // 审批
        r.add(PreDefinedModule(Mail, ic_mail_v7, MailBoxHomeActivity::class.java))                                 // 邮箱

        r.add(PreDefinedModule(Schedule, ic_schedule_v7, X5BrowserActivity::class.java))                                    // 日程
        r.add(PreDefinedModule(Knowledge, ic_knowledge_v7, X5BrowserActivity::class.java))                                 // 知识
        r.add(PreDefinedModule(Vote, ic_vote_v7, X5BrowserActivity::class.java))                                                // 投票
        r.add(PreDefinedModule(Activity, ic_activity_v7, X5BrowserActivity::class.java))                                // 活动
        r.add(PreDefinedModule(CRM, ic_crm_v7, X5BrowserActivity::class.java))                                                   // CRM
        r.add(PreDefinedModule(Dudu, ic_dudu_v7, X5BrowserActivity::class.java))                                                // 嘟嘟
        r.add(PreDefinedModule(Headline, ic_headline_v7, X5BrowserActivity::class.java))                                     // 头条
        r.add(PreDefinedModule(Default, ic_unknown, X5BrowserActivity::class.java))                                   // 默认

        r.add(PreDefinedModule(Report, ic_report_v7, ReportListActivity::class.java))                                      // 报表
        r.add(PreDefinedModule(NewForm, ic_new_form, FormListActivity::class.java))                                        // 发起表单
        r.add(PreDefinedModule(NewCollaboration, ic_new_collaboration, NewCollaborationActivity::class.java))              // 发起协同

        q.add(PreDefinedShortCut(X.Quick.NewCollaboration, ic_quick_approval, "协同"))
        q.add(PreDefinedShortCut(X.Quick.Location, ic_quick_location, "签到"))
        q.add(PreDefinedShortCut(X.Quick.NewSchedule, ic_quick_schedule, "日程"))
        q.add(PreDefinedShortCut(X.Quick.NewPlan, ic_quick_plan, "计划"))
        q.add(PreDefinedShortCut(X.Quick.NewMail, ic_quick_mail, "邮件"))
        q.add(PreDefinedShortCut(X.Quick.NewMeeting, ic_quick_meeting, "会议"))
        q.add(PreDefinedShortCut(X.Quick.Hyphenate, ic_quick_chat, "聊天"))
        q.add(PreDefinedShortCut(X.Quick.NewForm, ic_quick_flow, "流程"))
    }

    override fun getV7Icon(moduleId: Int) = r.find { it.moduleId == moduleId }?.icon ?: -1

    override fun getModuleClass(moduleId: Int) = r.find { it.moduleId == moduleId }?.moduleClass ?: X5BrowserActivity::class.java

    override fun getShortCut(quickId: Int) = q.find { it.quickId == quickId }

}