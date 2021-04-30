package cn.flyrise.feep.push;

/**
 * 新建：陈冕;
 * 日期： 2018-9-21-16:24.
 */
public interface Push {

	interface Phone {//手机类型

		String huawei = "HUAWEI";//华为
		String xiaomi = "Xiaomi"; //小米
		String jpush = "jiguang"; //极光类型（非手机）
	}

	interface Notification {//通道类型
		int JPUSH = 100; //极光
		int HUAWEI = 101;//华为
		int XIAOMI = 102;//小米
	}

	interface Receiver {//广播类型
		int notification = 1;//通知栏消息
		int message = 2;// 2：自定义消息
		int click = 3;// 3：点击事件
	}

}
