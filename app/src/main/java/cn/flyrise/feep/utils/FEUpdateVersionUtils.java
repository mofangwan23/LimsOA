package cn.flyrise.feep.utils;

import android.app.NotificationManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.FEUpdateVersionDialog;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.NetworkUtil;
import cn.flyrise.feep.event.EventIgnoreVersion;
import com.baidu.autoupdatesdk.AppUpdateInfo;
import com.baidu.autoupdatesdk.AppUpdateInfoForInstall;
import com.baidu.autoupdatesdk.BDAutoUpdateSDK;
import com.baidu.autoupdatesdk.CPUpdateDownloadCallback;
import com.baidu.autoupdatesdk.utils.PreferenceUtils;
import org.greenrobot.eventbus.EventBus;

/**
 * 新建：陈冕;
 * 日期： 2018-1-16-9:31.
 */

public class FEUpdateVersionUtils implements FEUpdateVersionDialog.OnClickeUpdateListener {

	private boolean isHttpsUpdateVersion = false;

	private Context context;

	private UpdateVersionListener mUpdateListener;

	private IgnoreVersionListener mIgnoreListener;

	private NotificationManager notifyManager;

	private NotificationCompat.Builder builder;

	private Handler mHander = new Handler(Looper.getMainLooper());

	public FEUpdateVersionUtils(Context context, UpdateVersionListener listener) {
		this.context = context;
		this.mUpdateListener = listener;
	}

	public FEUpdateVersionUtils(Context context, IgnoreVersionListener listener) {
		this.context = context;
		this.mIgnoreListener = listener;
	}

	public void detectionUpdateVerson() { //检测更新版本
		BDAutoUpdateSDK.cpUpdateCheck(context, (info, infoForInstall) -> {
			if (mUpdateListener == null) {
				return;
			}
			mUpdateListener.onUpdateVersion((info != null || infoForInstall != null)//存在需要更新的版本
					, info != null
							&& PreferenceUtils.getIgnoreVersionCode(context) != info.getAppVersionCode()//非忽略此版本
			);

		}, isHttpsUpdateVersion);
	}

	public void showUpdateVersionDialog() {//检查更新并显示更新提示框
		LoadingHint.show(context);
		BDAutoUpdateSDK.cpUpdateCheck(context, this::showUpdateDialog, isHttpsUpdateVersion);
		new Thread(() -> {
			if (!NetworkUtil.ping()) {
				mHander.post(() -> {
					FEToast.showMessage(context.getResources().getString(R.string.lbl_retry_network_connection));
				});
			}
		}).start();
	}

	private void showUpdateDialog(AppUpdateInfo info, AppUpdateInfoForInstall infoForInstall) {
		if (LoadingHint.isLoading()) {
			LoadingHint.hide();
		}
		if (infoForInstall != null && !TextUtils.isEmpty(infoForInstall.getInstallPath())) {
			if (CoreZygote.getContext() == null) {
				return;
			}
			BDAutoUpdateSDK.cpUpdateInstall(CoreZygote.getContext(), infoForInstall.getInstallPath());
		}
		else if (info != null) {
			new FEUpdateVersionDialog()
					.setAppUpdateInfo(info)
					.setOnClickeUpdateListener(this)
					.show(((AppCompatActivity) context).getFragmentManager(), "update_version");
		}
	}

	@Override
	public void onUpdate(AppUpdateInfo info) {
		BDAutoUpdateSDK.cpUpdateDownload(CoreZygote.getContext(), info, new UpdateDownloadCallback());
	}

	@Override
	public void onIgnoreVersion(int appVersion) {
		PreferenceUtils.setIgnoreVersionCode(context, appVersion);
		EventBus.getDefault().post(new EventIgnoreVersion());
		if (mIgnoreListener == null) {
			return;
		}
		mIgnoreListener.onIgnoreVersion();
	}

	private class UpdateDownloadCallback implements CPUpdateDownloadCallback {

		@Override
		public void onDownloadComplete(String apkPath) {
			if (CoreZygote.getContext() == null) {
				return;
			}
			BDAutoUpdateSDK.cpUpdateInstall(CoreZygote.getContext(), apkPath);
			if (notifyManager != null) {
				notifyManager.cancel(1001);
			}
		}

		@Override
		public void onStart() {
		}

		@Override
		public void onPercent(int percent, long rcvLen, long fileSize) {
			setNotification(percent);
		}

		@Override
		public void onFail(Throwable error, String content) {
			if (notifyManager != null) {
				notifyManager.cancel(1001);
			}
		}

		@Override
		public void onStop() {
		}
	}

	private void setNotification(int progress) {
		if (CoreZygote.getContext() == null) {
			return;
		}
		if (notifyManager == null) {
			notifyManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			builder = new NotificationCompat.Builder(CoreZygote.getContext(), "updateMessage");
			builder.setSmallIcon(R.drawable.notification_small_icon);
			builder.setLargeIcon(BitmapFactory.decodeResource(CoreZygote.getContext().getResources(), R.drawable.notification_large_icon));
			//禁止用户点击删除按钮删除
			builder.setAutoCancel(false);
			//禁止滑动删除
			builder.setOngoing(true);
			//取消右上角的时间显示
			builder.setShowWhen(false);
			builder.setOngoing(true);
			builder.setShowWhen(false);
		}
		builder.setContentTitle(String.format(CoreZygote.getContext().getResources()
				.getString(R.string.update_version_progress), progress) + "%");
		builder.setProgress(100, progress, false);
		notifyManager.notify(1001, builder.build());
	}


	public interface UpdateVersionListener {

		void onUpdateVersion(boolean needUpdate, boolean isNoIgnoreVersion);
	}

	public interface IgnoreVersionListener {

		void onIgnoreVersion();
	}
}
