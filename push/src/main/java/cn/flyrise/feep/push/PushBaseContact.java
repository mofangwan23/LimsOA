package cn.flyrise.feep.push;

import android.content.Context;

/**
 * Create by cm132 on 2019/5/16 10:22.
 * Describe:初始化推送的基类
 */
public abstract class PushBaseContact {

	protected abstract void resumePush(Context context);//重启推送

	protected abstract void stopPush(Context context);//暂停推送

	protected abstract void deleteAlias(Context context);//清空推送token

	protected abstract String getAppId(Context context);//注册的appid

	protected abstract String getAppKey(Context context);//注册的appkey

	protected abstract void getAlias(Context context);//获取当前已注册的alias
}
