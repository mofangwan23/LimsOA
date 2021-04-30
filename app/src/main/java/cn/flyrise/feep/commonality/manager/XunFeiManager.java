package cn.flyrise.feep.commonality.manager;

import android.content.Context;
import cn.flyrise.feep.IflytekManager;

/**
 * 新建：陈冕;
 * 日期： 2018-8-28-13:49.
 * 讯飞模块管理
 */
public class XunFeiManager {

	//初始化讯飞语音
	public static void init(Context context) {
		IflytekManager.init(context);
	}

	//开启小飞助手
	public static void startRobot(Context context) {
		IflytekManager.startRobot(context);
	}
}
