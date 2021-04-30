package cn.flyrise.feep.utils;

/**
 * @author ZYP
 * @since 2017-05-23 10:14
 */
public interface Patches {

	int PATCH_APPLICATION_BUBBLE = 1;                // 应用中心红点
	int PATCH_FLOW_CURRENT_NODE = 2;                 // 流程审核增加当前节点显示
	int PATCH_UNREAD_MESSAGE = 3;                    // 待阅消息分栏
	int PATCH_COLLABORATION_SUPPLEMENT = 4;          // 协同补充正文
	int PATCH_COLLABORATION_WAIT_SEND = 5;           // 待发协同
	int PATCH_COLLABORATION_TRANSMIT = 6;            // 协同转发
	int PATCH_COLLABORATION_REVOCATION = 7;          // 协同撤销
	int PATCH_FLOW_REVOCATION = 8;                   // 流程撤销
	int PATCH_RELATED_MATTERS = 9;                   // 关联事项
	int PATCH_ATTENDANCE_MULTI = 10;                 // 考勤组多次签到
	int PATCH_PART_TIME_DEPARTMENT = 11;             // 兼职部门
	int PATCH_USER_INFO_MODIFY = 12;                 // 用户信息修改
	int PATCH_EXTERNAL_CONTACT = 13;                 // 外部联系人
	int PATCH_HUANG_XIN = 14;                         // 环信
	int PATCH_CIRCULATE = 15;                         // 传阅
	int PATCH_COLLABORATION_EMERGENCY_DEGREE = 16;  // 协同紧急程度
	int PATCH_FLOW_SUPPLEMENT = 17;                  // 流程表单补充正文
	int PATCH_KNOWLEDGE_FILTER = 18;                 // 知识中心上传限制
	int PATCH_ROBOT_UNDERSTANDER = 19;               // 语音助手
	int PATCH_WATERMARK = 20;                         // 水印
	int PATCH_FORM_MORE_PERSON = 21;                 // 多环节允许送办多人
	int PATCH_WATER_DROP_READ_MESSAGE = 22;          // 拖动气泡标记消息已读
	int PATH_COLLABORATION_SENDBACK = 23;            // 协同【退回到发起人】、【重新提交后直接返回本节点】
	int PATCH_SCHEDULE_REPLY = 24;                   // 日程回复
	int PATCH_DATA_RETRIEVAL = 25;                   // 信息检索

	int PATCH_SIGN_IN_STATICS = 26;                  // 考勤统计
	int PATCH_PLAN = 27;                              // 计划
	int PATCH_MEETING_MANAGER = 28;                  // 会议管理

	int PATCH_COLLECTIONS = 29;                      // 收藏
	int PATCH_NEW_APPLICATION = 30;                  // 新版应用中心

	int PATCH_NO_REFRESH_LIST = 31;                  // 审批处理后不刷新列表

	int PATCH_FORM_INPUT_ATTACHMENT = 32;           //表单没有必填项处理时，添加附件

	int PATCH_REQUEST_USER_DETAIL = 33;             //获取人员详情（包含停用（离职）人员）

	int PATCH_GROUP_MESSAGE = 34;                   //消息主界面，圈子消息

	int PATCH_SHOW_NUM_BUBBL = 35;                  //审批功能待办列表显示汇总数量
}
