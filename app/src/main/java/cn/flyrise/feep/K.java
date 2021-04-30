package cn.flyrise.feep;

/**
 * @author ZYP
 * @since 2016-12-20 18:17
 */
public interface K {

	interface schedule {

		int detail_result_code = 1002;                  // 处理完日程详情的 result code.
		int detail_request_code = 1001;                 // 请求日程详情的 request code.
		int share_result_code = 1005;                   // 处理完日程分享的 result code.
		int share_person_request_code = 1006;           // 请求选择分享对象的 request code.
		int share_other_request_code = 1007;            // 分享日程
		int modify_schedule_code = 1008;                // 修改日程

		int new_request_code = 1003;                    // 新建日程的 request code.
		int new_result_code = 1004;                     // 新建完日程的 result code.

		String event_source = "EXTRA_EVENT_SOURCE";
		String event_source_id = "EXTRA_EVENT_SOURCE_ID";                       // 获取日程详情，使用该 id
		String schedule_id = "EXTRA_SCHEDULE_ID";
		String schedule_default_date = "EXTRA_SCHEDULE_DEFAULT_DATE";           // 创建日程的默认日期

		String ERROR_CODE_REPLY_NO_PERMISSION = "16001";
	}

	interface addressBook {

		int search_result_code = 2048;                    // ContactSearchActivity 返回码
		String select_mode = "select_mode";               // 开启 AddressBookActivity 的模式，true 选人模式，false 普通预览方式
		String data_keep = "data_keep";                   // 开启 AddressBookActivity 的时候传递当前界面的 hashcode，用于界面数据共享
		String company_only = "company_only";             // 开启 AddressBookActivity 的时候是否只使用公司进行筛选
		String with_position = "with_position";           // 开启 AddressBookActivity 的时候是否使用岗位进行筛选
		String start_chat = "start_chat";                 // 开启 AddressBookActivity 是否以为目的。
		String only_user_company = "only_user_company";   // 开启 AddressBookActivity 的时候是否只显示用户所在的公司
		String except_selected = "except_selected";       // 开启 AddressBookActivity 的时候不能再操作已选择的人员
		String address_title = "address_title";           // AddressBookActivity 显示的 title
		String default_department = "default_department"; // 开启 AddressBookActivity 的时候设置默认的部门
		String user_id = "user_id";                       // userId
		String department_id = "department_id";           // department id
		String department_name = "department_name";       // department name
		String cannot_selected = "cannot_selected";
		String except_self = "except_self";
		String user_name = "user_name";
		String single_select = "single_select";           // 单选
		String except_own_select = "except_own_select";//不能选择用户自己
	}

	interface email {

		int receiver_request_code = 3001;                       // 邮件接收人
		int copy_to_request_code = 3002;                        // 邮件抄送人
		int blind_to_request_code = 3003;                       // 邮件密送人

		String EXTRA_UNREAD = "extra_unread";//是否为未读邮件
		String EXTRA_TYPE = "extra_type";
		String box_name = "extra_box_name";
		String mail_id = "extra_mail_id";
		String b_transmit = "extra_bTransmit";
		String mail_account = "extra_mail_account";
		String recipient_type = "extra_recipient_type";
		String attachment_json = "extra_attachment_json";
		String recipient_ids = "extra_recipient_ids";
		String mail_select_persons = "extra_mail_sel_person";

		String mail_account_id = "extra_mail_account_id";
		String mail_tto = "extra_mail_tto";
		String mail_cc = "extra_mail_cc";
		String mail_tto_id = "extra_mail_tto_id";
		String mail_cc_id = "extra_mail_cc_id";

		String mail_tto_list = "extra_mail_tto_list";
		String mail_cc_list = "extra_mail_cc_list";
		String mail_search_text = "mail_search_text";
	}

	interface plan {

		int receiver_request_code = 4001;                       // 计划主送人
		int copy_to_request_code = 4002;                        // 计划抄送人
		int notify_request_code = 4003;                         // 计划知会人

		int PLAN_TYPE_DAY = 1;
		int PLAN_TYPE_WEEK = 2;
		int PLAN_TYPE_MONTH = 3;
		int PLAN_TYPE_OTHER = 4;

		int PLAN_FREQUENCY_DAT = 1;
		int PLAN_FREQUENCY_WEEK = 2;
		int PLAN_FREQUENCY_MONTH = 3;

		String EXTRA_MESSAGEID = "EXTRA_MESSAGEID";
		String EXTRA_BUSINESSID = "EXTRA_BUSINESSID";
	}

	interface knowledge {

		int publish_request_code = 5001;                        // 文档发布选人
		int choosefolder_request_code = 5002;                     //选择返回文件夹
		String EXTRA_CHOOSEFOLDER = "EXTRA_CHOOSE_FOLDER";        //是否为选择文件夹
		String EXTRA_CHOOSEFOLDER_ID = "EXTRA_CHOOSE_FOLDER_ID";  //返回的文件夹ID
		String EXTRA_IS_PIC_FOLDER = "EXTRA_IS_PIC_FOLDER";  //   是否是图片文件夹
	}

	interface salary {

		int gesture_verify_request_code = 6001;                 // 手势校验的请求码
		int fingerprint_verify_request_code = 6002;             // 指纹解锁的请求码
		String show_verify_dialog = "EXTRA_SHOW_VERIFY";        // 是否显示验证密码
		String request_month = "EXTRA_REQUEST_MONTH";           // 具体请求哪个月份的工资 yyyy-MM
	}

	interface preferences {

		String auto_sync_calendar = "AutoSyncCalendar";         // 是否自动同步日历
		String address_book_version = "AddressBookVersion";     // 新机制的数据库版本，userMax
		String address_frist_common = "firstCommonData";        //常用联系人加载防重复标识
	}

	interface form {

		String TITLE_DATA_KEY = "TITLE_DATA_KEY";
		String URL_DATA_KEY = "URL_DATA_KEY";
		String LOAD_KEY = "LOAD_KEY";
		String EXTRA_ID = "Form_ID";
		String CURRENT_NODE_ID = "Current_Node_Id";
	}

	interface location {

		int LOCATION_DETAIL = 601;  //查看地图详情
		int LOCATION_SEARCH = 602;  //地图搜索
		int LOCATION_SEND = 603;//地图发送位置
		int LOCATION_SIGN = 604;//地图签到
		int LOCATION_CUSTOM_SEARCH = 605;//设置自定义位置设置(搜索)
		int LOCATION_CUSTOM = 606;//自定义签到
		int LOCATION_CUSTOM_SETTING = 607;//设置自定义位置设置(选位置)
	}

	interface sign {//考勤组签到类型

		int STYLE_LIST = 100;//列表签到，默认
		int STYLE_ATT = 101;//考勤点签到（不允许超范围签到）
		int STYLE_LIST_ATT = 102;//考勤点+列表（允许超范围签到）
		int STYLE_MANY = 103;//考勤组，多次签到
	}

	interface userInfo {

		int DETAIL_ICON = 701;              //用户头像
		int DETAIL_LOGIN_PASSWORD = 702;         //登录密码
		int DETIAL_DEPERTMENT = 703;             //部门
		int DETAIL_PHONE = 704;                  //移动电话
		int DETAIL_TEL = 705;                    //办公电话
		int DETAIL_EMAIL = 706;                 //邮箱
		int DETAIL_LOCATION = 707;             //联系地址
		int DETAIL_BIRTHDAY = 708;             //生日
	}

	interface collaboration {

		String Extra_Collaboration_ID = "collaborationId";

		//到新建协同界面的类型
		String EXTRA_FORM_TYPE = "fromType";
		//到新建协同界面的类型ID
		int EXTRA_FORM_TYPE_IM = 100;
		int EXTRA_FORM_TYPE_WAITSEND = 101;
		int EXTRA_FORM_TYPE_WORKPLAN = 102;
		int EXTRA_FROM_TYPE_SCHEDULE = 103;
		//新建协同的变量值
		String EXTRA_NEW_COLLABORATION_TITLE = "EXTRA_NEW_COLLABORATION_TITLE";
		String EXTRA_NEW_COLLABORATION_CONTENT = "EXTRA_NEW_COLLABORATION_CONTENT";
		String EXTRA_NEW_COLLABORATION_ATTACHMENT = "EXTRA_NEW_COLLABORATION_ATTACHMENT";

	}

	interface picture {

		String SINGLE = "single";  //单选
		String LIMITED = "limited";//限制数量
		String CHECKBOX = "checkbox"; //多选
	}

	interface ChatContanct {

		String EXTRA_FORWARD_MSG_ID = "forward_msg_id";
	}

}
