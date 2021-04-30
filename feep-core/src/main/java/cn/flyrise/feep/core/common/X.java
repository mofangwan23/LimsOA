package cn.flyrise.feep.core.common;

/**
 * Update By ZYP in 2018-07-18
 */
public interface X {

	// 功能模块 FFFF....我佛慈悲
	interface Func {

		int Unsupport = -1;             // 客户端不支持的消息
		int System = -2;                // 系统消息
		int ToDo = 0;                   // 待办
		int Done = 1;                   // 已办
		int Trace = 2;                  // 跟踪
		int ToSend = 3;                 // 待发
		int Sended = 4;                 // 已发
		int NewCollaboration = 8;       // 新建协同
		int NewForm = 10;               // 新建表单
		int HandWrite = 20;             // 手写签批
		int Collaboration = 42;         // 协同
		int Approval = 44;              // 审批

		int AddressBook = 7;            // 通讯录
		int AddressBookLetter = 30;     // 字母通讯录

		int Report = 13;                // 报表
		int NewNotice = 18;             // 最新消息
		int HistoryNotice = 19;         // 历史消息
		int CircleNotice = 23;          // 圈子消息

		int InBox = 16;                 // 邮件收件箱
		int OutBox = 17;                // 邮件发件箱
		int DraftBox = 26;              // 邮件草稿箱
		int Mail = 46;                  // 邮箱

		int WQT = 12;                   // 外勤通
		int Location = 21;              // 位置上报
		int LocationHistory = 22;       // 位置上报历史记录

		int News = 5;                   // 新闻
		int Announcement = 6;           // 公告
		int Meeting = 9;                // 会议
		int Plan = 14;                  // 计划
		int Knowledge = 35;             // 知识中心
		int Vote = 36;                  // 问卷调查(H5)
		int Schedule = 37;              // 日程
		int Activity = 38;              // 活动(H5)
		int Headline = 39;              // 头条(H5)
		int CRM = 43;                   // CRM(H5)
		int Dudu = 45;                  // 嘟嘟(H5)
		int Salary = 48;                // 工资
		int Associate = 47;             // 同事圈(H5)
		int RemindPlan= 67;             // 提醒新建计划
		int Default = 10011;            // 默认
	}

	interface Quick {

		int NewCollaboration = 44;              // 发起协同
		int NewMeeting = 9;                     // 发起会议
		int NewPlan = 14;                       // 发起计划
		int NewMail = 46;                       // 发起邮件
		int NewSchedule = 37;                   // 发起日程
		int Location = 21;                      // 签到
		int Hyphenate = 49;                     // 发起聊天
		int NewForm = 10;                       // 发起表单
	}

	// 列表请求类型
	interface RequestType {

		int System = -2;            // 系统消息（消息面板中使用）
		int ToDo = 0;               // 协同 待办
		int Done = 1;               // 协同 已办
		int Trace = 2;              // 协同 跟踪
		int ToSend = 3;             // 协同 待发
		int Sended = 4;             // 协同 已发
		int News = 5;               // 新闻
		int Announcement = 6;       // 公告
		int Meeting = 7;            // 会议
		int Report = 8;             // 报表
		int OthersWorkPlan = 14;    // 他人工作计划
		int WorkPlan = 15;          // 我的工作计划
		int LocationHistory = 22;   // 位置上报历史记录
		int ToDoDispatch = 23;      // 急件
		int ToDoNornal = 24;        // 平件
		int ToDoRead = 25;          // 阅件
		int Knowledge = 35;         // 知识中心
		int Vote = 36;              // 问卷调查
		int Schedule = 37;          // 日程备忘
		int Activity = 38;          // 活动
	}

	// 协同节点权限
	interface NodePermission {

		int Forward = 1;        // 转发
		int Rollback = 2;       // 回退
		int Termination = 4;    // 终止
		int AddSign = 8;        // 加签
	}

	// 节点状态
	interface NodeState {

		int Uncheck = 0;    // 未处理
		int Checked = 1;    // 已处理
	}

	// 协同请求类型
	interface CollaborationType {

		int SendDo = 0;                 // 【协同】0:发起协同
		int DealWith = 1;               // 【协同】1:正常处理
		int Forwarding = 2;             // 【协同】2:转发
		int Additional = 3;             // 【协同】3:协同加签
		int Return = 4;                 // 【协同】4:协同退回
		int ReplyOthers = 5;            // 【协同】5:回复他人意见
		int ToggleState = 6;            // 【协同】6:改变跟踪状态 = 增加跟踪,取消跟踪)
		int SendedAdditional = 7;       // 【协同】7:协同已发加签
		int TempStorage = 10;           // 【协同】10:保存协同暂存
		int AddBody = 11;               // 【协同】补充正文
	}

	// 表单退回请求类型
	interface FormExitType {

		int SendDo = 0;         // 送办（响应时才有处理类型）
		int Return = 1;         // 退回
		int DealLatter = 2;     // 暂存待办
		int NewForm = 3;        // 发起表单
	}

	// 表单送办请求类型
	interface FormRequestType {

		int SendDo = 0;         // 送办（响应时才有处理类型）
		int Return = 1;         // 退回
		int DealLatter = 2;     // 暂存待办
		int Additional = 3;     // 加签
		int ToggleState = 4;    // 改变跟踪状态(增加跟踪,取消跟踪)
		int NewForm = 5;        // 发起表单
	}

	// 表单处理类型：0:正常办理，1:加签，2:传阅【重要】
	interface FormNode {

		int Normal = 0;         // 0:正常办理
		int Additional = 1;     // 加签
		int Circulated = 2;     // 传阅【重要】
		int CopyTo = 3;         // 抄送
	}

	// 表单事件类型
	interface JSControlType {

		int Error = -1;             // 登录失效
		int Break = -2;             // 表单返回
		int Date = 0;               // 日期（默认）
		int Person = 1;             // 人员选择
		int Attachment = 2;         // 附件
		int Linked = 3;             // 关联表单，协同
		int MultiAttachment = 4;    // 多媒体附件
		int Reference = 5;          // 参考项
		int MeetingBoard = 6;       // 会议看板
		int CommonWords = 7;        // 常用语
		int Record = 8;             // 录音
		int Contacts = 9;           // 系统通讯录
		int Download = 10;          // 下载浏览除图片外的附件
		int FetchFormData = 12;     // 流程请求表单数据
		int TakePhoto = 13;         // 拍照
		int WrittingCombo = 14;     // 手写签批
		int ZXing = 15;              // 扫描二维码
	}

	// 表单和报表处理类型
	interface JSActionType {

		int Error = -1;         // 非空检查有错误
		int Check = 0;          // 检查非空性
		int Send = 1;           // sendForm表单业务表提交
		int Search = 3;         // 获取搜索条件（用于报表交互）
		int FetchData = 4;      // 获取页面数据json
		int PushData = 5;       // 表单数据
	}

	// 表单非空性检查结果
	interface FormNullCheck {

		int Null = 0;        // 仍存在必需值示输入，未通过检查
		int NonNull = 1;     // 已输入所有必需值
		int DataExist = 2;   // 数据已存在
		int NonFormID = 3;   // 没有设置表单ID
	}

	// 通讯录
	interface AddressBookType {

		int Group = 0;                      // 集团
		int Staff = 1;                      // 人
		int Department = 2;                 // 部门
		int Position = 3;                   // 岗位
		int Company = 4;                    // 公司
		int SourceDepartment = 5;           // 数据源类型中的部门
	}

	// 通讯录过滤类型
	interface AddressBookFilterType {

		int Register = 0;           // 花名册数据
		int Organization = 1;       // 组织机构数据
		int Authority = 2;          // 带权限组织机构数据
	}

	// 回复类型
	interface ReplyType {

		int Collaboration = 0;
		int Meeting = 1;
		int WorkPlan = 2;
	}

	// 签到类型
	interface LocationType {

		String Locus = "0";
		String Person = "1";
		String LocationDate = "2";
		String WorkingTime = "3";
	}

	// 首页底部菜单类型
	interface MainMenu {
		String Message = "1001";        // 首页-消息
		String Associate = "1002";      // 首页-同事圈
		String Application = "1003";    // 首页-应用
		String Contact = "1004";        // 首页-联系人
		String Mine = "1005";           // 首页-我的
		String Study = "1006";          // 首页-在线学习
		String EXAM = "1007";           // 首页-在线考试
	}
}
