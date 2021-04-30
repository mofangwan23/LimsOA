package cn.flyrise.feep.addressbook.processor;

import static cn.flyrise.feep.addressbook.processor.AddressBookProcessor.ADDRESS_BOOK_DOWNLOADING;
import static cn.flyrise.feep.addressbook.processor.AddressBookProcessor.ADDRESS_BOOK_DOWNLOADING_UPDATE;
import static cn.flyrise.feep.addressbook.processor.AddressBookProcessor.ADDRESS_BOOK_DOWNLOAD_ACTION;
import static cn.flyrise.feep.addressbook.processor.AddressBookProcessor.ADDRESS_BOOK_DOWNLOAD_FAILED;
import static cn.flyrise.feep.addressbook.processor.AddressBookProcessor.ADDRESS_BOOK_INIT_SUCCESS;
import static cn.flyrise.feep.addressbook.processor.AddressBookProcessor.ADDRESS_BOOK_UPDATE_FAILED;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.source.AddressBookRepository;
import cn.flyrise.feep.addressbook.utils.AddressBookCacheInvoker;
import cn.flyrise.feep.addressbook.utils.AddressBookDownloader;
import cn.flyrise.feep.addressbook.utils.AddressBookExceptionInvoker;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.FileUtil;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.request.RemoteRequest;
import cn.flyrise.feep.core.network.response.AddressBookVersionResponse;
import cn.flyrise.feep.core.services.ILoginUserServices;
import cn.flyrise.feep.location.service.LocationService;
import java.io.File;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2017-03-13 10:26
 * 通讯录异步下载
 */
public class AddressBookDownloadServices extends Service {

	public static void start(Context context) {
		Intent downloadIntent = new Intent(context, AddressBookDownloadServices.class);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			context.startForegroundService(downloadIntent);
		} else {
			context.startService(downloadIntent);
		}
	}

	@Nullable @Override public IBinder onBind(Intent intent) {
		return null;
	}

	@Override public void onCreate() {
		super.onCreate();
		setForeground();
		initDownloadAddressBook();
	}

	private void setForeground() {
		Context context = CoreZygote.getContext();
		if (context == null) {
			startForeground(2, new Notification());
			return;
		}
		//适配8.0service
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		NotificationChannel mChannel;
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
			mChannel = new NotificationChannel("service_01", getString(R.string.app_name),
					NotificationManager.IMPORTANCE_LOW);
			notificationManager.createNotificationChannel(mChannel);
			Notification notification = new Notification.Builder(getApplicationContext(), "service_01").build();
			startForeground(2, notification);
		}
	}

	private void initDownloadAddressBook() {
		ILoginUserServices loginUserService = CoreZygote.getLoginUserServices();
		if (loginUserService == null) {
			stopSelf();
			return;
		}

		String userId = loginUserService.getUserId();
		if (TextUtils.isEmpty(userId)) {
			stopSelf();
			return;
		}

		int oldSourceType = AddressBookExceptionInvoker.tryRestoreOldVersion();
		FELog.e("initDataSource--oldSourceType" + oldSourceType);

		if (oldSourceType == -1) {
			CoreZygote.getLoginUserServices().setAddressBookState(ADDRESS_BOOK_DOWNLOADING);
		}
		else {
			CoreZygote.getLoginUserServices().setAddressBookState(ADDRESS_BOOK_DOWNLOADING_UPDATE);
			AddressBookRepository.get().initDataSource(oldSourceType);
		}

		String clearMark = CommonUtil.getMD5(userId);
		String addressBookPath = CoreZygote.getPathServices().getAddressBookPath();
		if (AddressBookCacheInvoker.hasClearMark(addressBookPath, clearMark)) {
			FELog.i("Clear Mark...");
			FileUtil.deleteFolderFile(addressBookPath, false);
			SpUtil.put(K.preferences.address_book_version, "");
		}
		getServiceCurrentSqlVersion(databaseVersion(), oldSourceType)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(it -> startDownloadAddressBook(loginUserService.getSmallVersion(), userId));
	}

	/**
	 * 检查数据是否需要更新
	 */
	private Observable<Integer> getServiceCurrentSqlVersion(String version, int oldSourceType) {
		return Observable.unsafeCreate(f -> {
			RemoteRequest request = RemoteRequest.buildRequest("getBetweenCount", version);
			FEHttpClient.getInstance().post(request, new ResponseCallback<AddressBookVersionResponse>() {
				@Override
				public void onCompleted(AddressBookVersionResponse response) {
					if ((response.getErrorCode().equals("0") && response.result > 0) || oldSourceType == -1) {
						f.onNext(response.result);
						f.onCompleted();
					}
					else {
						disposeAddressBookResult();
						FELog.i("no need to update!");
					}
				}

				@Override
				public void onFailure(RepositoryException repositoryException) {
					f.onNext(56);//请求失败去下载数据库
					f.onCompleted();
				}
			});
		});
	}

	/**
	 * 开始下载联系人数据
	 */
	private void startDownloadAddressBook(String smallVersion, String userId) {
		AddressBookDownloader addressBookDownloader = new AddressBookDownloader();
		addressBookDownloader.setUserId(userId);
		addressBookDownloader.setSmallVersion(smallVersion);
		addressBookDownloader.setDownloadListener(new AddressBookProcessor.IDisposeListener() {
			@Override public void onDisposeSuccess(int resultCode, int sourceType) {
				if (CoreZygote.getLoginUserServices() != null) {
					CoreZygote.getLoginUserServices().setAddressBookState(
							resultCode == ADDRESS_BOOK_UPDATE_FAILED
									? ADDRESS_BOOK_UPDATE_FAILED : ADDRESS_BOOK_INIT_SUCCESS);

					if (resultCode == ADDRESS_BOOK_UPDATE_FAILED) {
						Activity activity = CoreZygote.getApplicationServices().getFrontActivity();
						if (activity != null) {
							FELog.i("AddressBookDownloadService : " + activity.getComponentName().getShortClassName());
							new FEMaterialDialog.Builder(activity)
									.setMessage(CommonUtil.getString(R.string.lbl_text_contact_update_error))
									.setCancelable(true)
									.setPositiveButton(null, null)
									.build()
									.show();
						}
					}
					AddressBookRepository.get().initDataSource(sourceType);
					disposeAddressBookResult();
				}
			}

			@Override public void onDisposeFailed(int errorCode) {                                                // 通讯录处理失败，同=
				FELog.e("address book download dispose failed. error code is : " + errorCode);
				if (CoreZygote.getLoginUserServices() != null) {
					CoreZygote.getLoginUserServices().setAddressBookState(ADDRESS_BOOK_DOWNLOAD_FAILED);
					File rootFile = new File(CoreZygote.getPathServices().getUserPath());
					FileUtil.deleteAllFiles(rootFile);
					disposeAddressBookResult();
				}
			}
		});
		addressBookDownloader.start();
	}

	/**
	 * 联系人数据加载完成
	 */
	private void disposeAddressBookResult() {
		sendBroadcast(new Intent(ADDRESS_BOOK_DOWNLOAD_ACTION));
		stopSelf();
	}

	/**
	 * 本地数据库版本
	 */
	private String databaseVersion() {
		String spDBName = SpUtil.get(K.preferences.address_book_version, "");
		if (TextUtils.isEmpty(spDBName)) {
			return "0";
		}

		File addressBookDir = new File(CoreZygote.getPathServices().getAddressBookPath());
		if (!addressBookDir.exists()) {
			return "0";
		}

		File[] files = addressBookDir.listFiles();
		if (files != null && files.length == 0) {
			return "0";
		}

		String targetName = null;
		for (File file : files) {
			String fileName = file.getName();
			if (TextUtils.equals(spDBName, fileName)) {
				targetName = fileName;
				break;
			}
		}

		if (TextUtils.isEmpty(targetName)) {
			SpUtil.put(K.preferences.address_book_version, "");
			return "0";
		}

		return targetName.substring(0, targetName.lastIndexOf("."));
	}
}
