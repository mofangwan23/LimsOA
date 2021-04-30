package cn.flyrise.feep;

import android.content.Context;
import android.content.Intent;
import cn.flyrise.feep.robot.view.RobotUnderstanderActivity;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

/**
 * 新建：陈冕;
 * 日期： 2018-8-28-15:09.
 */
public class IflytekManager {

	//初始化讯飞语音
	public static void init(Context context) {
		SpeechUtility.createUtility(context, SpeechConstant.APPID + "=" + "56e2682f");//讯飞语音
	}

	//开启小飞助手
	public static void startRobot(Context context) {
		Intent robotUnderstanderIntent = new Intent(context, RobotUnderstanderActivity.class);
		context.startActivity(robotUnderstanderIntent);
	}
}
