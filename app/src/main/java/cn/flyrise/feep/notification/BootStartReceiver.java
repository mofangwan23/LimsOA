/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-9-27 下午3:04:35
 */
package cn.flyrise.feep.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import cn.flyrise.feep.notification.service.BoostStartService;

/**
 * 类功能描述：</br>
 * @author zms
 * @version 1.0 </p> 修改时间：</br> 修改备注：</br>
 */
public class BootStartReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		BoostStartService.start(context);
	}

}
