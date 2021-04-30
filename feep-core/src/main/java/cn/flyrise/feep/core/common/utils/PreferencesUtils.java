/*
/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-8-27 下午01:35:04
 */
package cn.flyrise.feep.core.common.utils;

/**
 * 类功能描述：</br> 使用文件持久化一些用户使用信息
 * @author zms
 * @version 1.0 </p> 修改时间：</br> 修改备注：</br>
 */
public class PreferencesUtils {

	/**
	 * 用户是否已阅读操作指南，默认第一次登录后会自动显示操作指南
	 */
	public static final String GUIDE_STATE_KEY = "GUIDE_STATE";

	/**
	 * 应用模块的指导界面
	 */
	public static final String GUIDE_STATE_APP = "GUIDE_APP";

	/**
	 * 保存通知栏状态
	 */
	public static final String SETTING_NOTIFICATION_STATUS = "setting_notification_status";

	/**
	 * 手势登录
	 */
	public static final String LOGIN_GESTRUE_PASSWORD = "login_gestrue_password";

	/**
	 * 指纹解锁标记
	 */
	public static final String FINGERPRINT_IDENTIFIER = "fingerprint_identifier";

	/**
	 * 保存九宮格输入的密码
	 */
	public static final String NINEPOINT_SET_PASSWORD = "ninepoint_set_password";

	/**
	 * 并且保存用户信息用于手势登录
	 */
	public static final String NINEPOINT_USER_INFO = "ninepoint_user_info";

	/**
	 * 储存工作界面按钮
	 */
	public static final String EXTEND_ALL_MODULE = "extend_all_module";

	/**
	 * 识别用户是否第一次使用手势密码
	 */
	public static final String FIRST_USE_GESTURE_PASSWORD = "first_use_gesture_password";


	/**
	 * 标记是否启动考勤自动上报位置功能
	 */
	public static final String LOCATION_LOCUS_IS_REPORT = "LOCATION_LOCUS_IS_REPORT";

	/**
	 * 自动上传保存的json
	 */
	public static final String LOCATION_SERVICE_AUTOUPLOADTIME = "LOCATION_UPLOAD_TIME";

	/**
	 * 用户登录后的accessToken
	 */
	public static final String USER_ACCESSTOKEN = "USER_ACCESSTOKEN";

	/**
	 * 用户的IP地址
	 */
	public static final String USER_IP = "USER_IP";

	/**
	 * 用户的id
	 */
	public static final String USER_ID = "USER_ID";

	/**
	 * 保存用户踢线提示
	 */
	public static final String USER_KICK_PROMPT = "user_kick_prompt";

	/**
	 * 部门名称
	 */
	public static final String DEPERTMENT_NAME = "depertment_name";

	/**
	 * 是否有下属
	 */
	public static final String HAS_SUBORDINATES = "hasSubordinates";

	/**
	 * 是否有分管下属
	 */
	public static final String HAS_SUB_SUBORDINATES = "hasSubSubordinates";

	/**
	 * IM登录成功的用户id
	 */
	public static final String IM_SUCCESS_USER_ID = "im_success_user_id";

	/**
	 * 人脸识别登录
	 */
	public static final String LOGIN_FACE = "login_face";

	/**
	 * OA消息推送跳转详情的消息
	 * */
	public static final String PUSH_DETAIL_MSG="push_detail_msg";

	/**
	 * 存储回复框里面的数据
	 * */
	public static final String SAVE_REPLY_DATA="save_reply_data";
}
