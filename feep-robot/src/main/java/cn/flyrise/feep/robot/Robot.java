package cn.flyrise.feep.robot;

/**
 * Created by Administrator on 2017-7-14.
 */

public interface Robot {

	interface input {

		int voice = 2010;//语音输入

		int text = 2011;//文本输入
	}

	interface operation { //操作类型

		String searchType = "search";  //语音搜索
		String openType = "open";     //语音打开
		String createType = "new";    //语音新建
		String invitaType = "invita"; //语音邀请
	}

	interface adapter { //适配器显示

		int ROBOT_INPUT_LEFT = 1230;            //在左侧显示的数据(LeftViewHodler)
		int ROBOT_INPUT_RIGHT = 1231;          //在右侧显示的数据，一般为用户自己说的话(RightViewHodler)
		int ROBOT_CONTENT_HINT = 1232;        //在中间显示的提示数据(ContentViewHodler
		int ROBOT_CONTENT_LIST = 1233;        //在中间显示的列表数据(MessageListViewHodler)
		int ROBOT_CONTENT_EMAIL = 1234;       //在中间显示的邮件(EmailViewHodler)
		int ROBOT_CONTENT_HINT_TITLE = 1235; //在中间显示的提示语标题,无背景(RollListViewHodler)
		int ROBOT_WEATHER_HINT_LIST = 1237;  //天气(WeatherViewHodler)
		int ROBOT_PLAY_VOICE = 1238;          //播放音频
		int ROBOT_CONTENT_TRAIN = 1239;       //火车
		int ROBOT_CONTENT_HOLIDAY = 1240;      //节假日查询
		int ROBOT_CONTENT_RIDDLE = 1241;       //猜谜语
	}

	interface process {

		int start = 0x006661; //开始
		int end = 0x006662; //结束
		int no = 0x006663; //否
		int yes = 0x006664; //是
		int content = 0x006665; //内容
	}

	interface schedule extends process { //新建日程

		int error = 0x006601; //语法错误提示
		int start_time = 0x006603; //开始日期
		int end_time = 0x006605; //结束日期
		int content_hint = 0x006606; //内容提示
		int content = 0x006607; //内容
		int share_hint = 0x006608; //分享他人提示
		int send_hint = 0x006611; //发送提示
	}

	interface search_message extends process {

		int error = 0x006701;//搜索消息异常
		int content_null = 0x006703;//搜索消息的内容为空
		int content = 0x006704;//搜索消息内容
		int content_start = 0x006705;//搜索消息内容开始
		int content_end = 0x006706;//搜索消息内容结束

	}

}
