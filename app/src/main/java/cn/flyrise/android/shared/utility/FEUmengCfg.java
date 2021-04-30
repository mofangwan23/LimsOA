package cn.flyrise.android.shared.utility;

import android.content.Context;
import android.text.TextUtils;

import com.umeng.analytics.MobclickAgent;

import java.util.HashMap;
import java.util.Map;

import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;

/**
 * 这个是友盟分析统计各个模块名称的类<br>
 *
 * @author Robert
 */
public class FEUmengCfg {

    /**
     * 协同详情
     */
    public static final String CollaborationDetail = "CollaborationDetail";
    /**
     * 锁屏
     */
    public static final String NinepointLoginActivity = "NinepointLoginActivity";
    /**
     * 设置
     */
    public static final String NetIPSettingActivity = "NetIPSettingActivity";
    /**
     * 快捷按钮
     */
    public static final String MainMenuRecyclerViewActivity = "MainMenuRecyclerViewActivity";
    /**
     * 我的界面
     */
    public static final String SettingActivity = "SettingActivity";
    /**
     * 新建协同
     **/
    public static final String NewCollaboration = "NewCollaboration";
    /**
     * 添加附件
     */
    public static final String AddAttachment = "addAttachment";
    /**
     * 新建流程
     */
    public static final String NewWorkFlow = "NewWorkFlow";
    /**
     * 协同处理
     */
    public static final String CollaborationDispose = "CollaborationDispose";
    /**
     * 通讯录
     */
    public static final String AddressBookList = "AddressBookList";
    /**
     * 通讯录详情
     */
    public static final String AddressBookDetail = "AddressBookDetail";
    /**
     * 会议管理
     */
    public static final String MeetingList = "MeetingList";
    /**
     * 会议详情
     */
    public static final String MeetingDetail = "MeetingDetail";
    /**
     * 报表
     */
    public static final String ReportList = "ReportList";
    /**
     * 报表详情
     */
    public static final String ReportDeatil = "ReportDeatil";
    /**
     * 工作计划
     */
    public static final String WorkPlanList = "WorkPlanList";
    /**
     * 新建工作计划
     */
    public static final String NewWorkPlan = "NewWorkPlan";
    /**
     * 工作计划详情
     */
    public static final String WorkPlanDetail = "WorkPlanDetail";
    /**
     * 位置上报
     */
    public static final String LocationChoose = "LocationChoose";
    /**
     * 历史位置
     */
    public static final String LocationHistory = "LocationHistory";
    /** 新闻公告列表 */
    // public static final String NewsList = "NewsList";
    /**
     * 新闻公告详情
     */
    public static final String NewsDeatil = "NewsDeatil";
    /**
     * 推送消息设置
     */
    public static final String NotificationSetting = "NotificationSetting";
    /**
     * 新建邮件
     */
    public static final String NewEmail = "NewEmail";
    /**
     * 邮件列表
     */
    public static final String EmailList = "EmailList";
    /**
     * 邮件详情
     */
    public static final String EmainDetail = "EmainDetail";
    /**
     * 表单处理
     */
    public static final String FormHandle = "FormHandle";
    /**
     * 表单送办或退回
     */
    public static final String FormSendToDispose = "FormSendToDispose";
    /**
     * 表单人员选择
     */
    public static final String FormPersonChoose = "FormPersonChoose";
    /**
     * 表单列表
     */
    public static final String FormList = "FormList";
    /**
     * 表单新建
     */
    public static final String NewForm = "NewForm";
    /**
     * 表单加签
     */
    public static final String FormAddSign = "FormAddSign";
    /**
     * 关于我们
     */
    public static final String AboutUS = "AboutUS";
    /**
     * 下载管理
     */
    public static final String DownloadManager = "DownloadManager";
    /**
     * 附件管理
     */
    public static final String AttachmentManager = "AttachmentManager";
    /**
     * 观看帮助
     */
    public static final String Help = "Help";
    /** 消息中心列表 */
    // public static final String NoticesList = "NoticesList";
    /**
     * 录音
     */
    public static final String Record = "Record";
    /**
     * 现场签到
     */
    public static final String LocationOnSite = "LocationOnSite";
    /**
     * 考勤设置
     */
    public static final String LocationSetting = "LocationSetting";
    /**
     * 考勤轨迹
     */
    public static final String LocationLocus = "Locus";
    /**
     * 企业活动
     */
    public static final String Activity = "Activity";
    /**
     * 企业头条
     */
    public static final String Yunger = "Yunger";
    /**
     * 日程管理
     */
    public static final String Schedule = "Schedule";
    /**
     * 问卷
     */
    public static final String Vote = "Vote";
    /**
     * 知识管理
     */
    public static final String Knowledge = "Knowledge";
    /**
     * 消息主界面
     */
    public static final String MainMessageActivity = "MainMessageFragment";
    /**
     * 消息列表界面
     */
    public static final String MessageListActivity = "MessagesListActivity";
    /**
     * 联系人人员列表
     */
    public static final String AddressTreePersonnelFragment = "AddressTreePersonnelFragment";

    /**
     * 附件管理附件下载中
     */
    public static final String DownLoadManagerActivity = "DownLoadManagerActivity";

    /**
     * 附件管理附件已完成
     */
    public static final String CompletedAddAttachmentsActivity = "CompletedAddAttachmentsActivity";

    /**
     * 帮助中心
     */
    public static final String GuideActivity = "GuideActivity";

    public static void onActivityResumeUMeng(Context context, String activityName) {
        MobclickAgent.onPageStart(activityName);
        MobclickAgent.onResume(context);
    }

    public static void onActivityPauseUMeng(Context context, String activityName) {
        MobclickAgent.onPageEnd(activityName);
        MobclickAgent.onPause(context);
    }

    public static void onFragmentActivityResumeUMeng(Context context) {
        MobclickAgent.onResume(context);
    }

    public static void onFragmentActivityPauseUMeng(Context context) {
        MobclickAgent.onPause(context);
    }

    public static void onFragmentResumeUMeng(String fragmentName) {
        MobclickAgent.onPageStart(fragmentName);
    }

    public static void onFragmentPauseUMeng(String fragmentName) {
        MobclickAgent.onPageEnd(fragmentName);
    }


    public static void cordovaPause(Context context, int type) {
        switch (type) {
            case 0:
                onActivityPauseUMeng(context, Knowledge);
                break;
            case 1:
                onActivityPauseUMeng(context, Vote);
                break;
            case 2:
                onActivityPauseUMeng(context, Schedule);
                break;
            case 3:
                onActivityPauseUMeng(context, Activity);
                break;
            case 4:
                onActivityPauseUMeng(context, Yunger);
                break;
            default:
                break;
        }
    }

    public static void cordovaResume(Context context, int type) {
        switch (type) {
            case 0:
                onActivityResumeUMeng(context, Knowledge);
                break;
            case 1:
                onActivityResumeUMeng(context, Vote);
                break;
            case 2:
                onActivityResumeUMeng(context, Schedule);
                break;
            case 3:
                onActivityResumeUMeng(context, Activity);
                break;
            case 4:
                onActivityResumeUMeng(context, Yunger);
                break;
            default:
                break;
        }
    }

    public static void sendDepertmentName(Context context, String deptName) {
        String spName = SpUtil.get(PreferencesUtils.DEPERTMENT_NAME, "");
        if (!TextUtils.isEmpty(spName)) {
            return;
        }
        SpUtil.put(PreferencesUtils.DEPERTMENT_NAME, deptName);
        Map<String, String> map_value = new HashMap<>();
        map_value.put("deptName", deptName);
        MobclickAgent.onEvent(context, "6050003", map_value);
    }

}
