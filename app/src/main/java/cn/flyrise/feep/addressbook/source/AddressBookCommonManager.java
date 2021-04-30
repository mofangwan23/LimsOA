package cn.flyrise.feep.addressbook.source;

import android.text.TextUtils;
import cn.flyrise.android.protocol.entity.CommonTagResponse;
import cn.flyrise.feep.K;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.FileUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.request.RemoteRequest;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.event.CompanyChangeEvent;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import rx.Observable;
import rx.Subscriber;

/**
 * Create by cm132 on 2019/3/8.
 * Describe:常用联系人管理
 * 1、首次进入加载常用联系人，并缓存到本地；
 * 2、后面优先使用缓存的数据；
 * 3、在超过一小时时，更新缓存数据。
 */
class AddressBookCommonManager {

	private IAddressBookDataSource mDataSources;
	private Subscriber<? super List<AddressBook>> f;

	private List<String> lastData;

	AddressBookCommonManager(IAddressBookDataSource mDataSources) {
		this.mDataSources = mDataSources;
	}

	Observable<List<AddressBook>> loadCommonAddress() {//加载
		return Observable.unsafeCreate(subscriber -> {
			f = subscriber;
			File file = getCommonFile();
			if (!file.exists()) {
				FELog.i("-->>>>common:第一次网络下载");
				downloadCommon(true);
				return;
			}

			userIdsToAddressBooks(getCacheCommon());

			if (!SpUtil.get(K.preferences.address_frist_common, false)) {
				SpUtil.put(K.preferences.address_frist_common, true);
				downloadCommon(false);
			}
		});
	}

//	private boolean isUpdataCacheCommon(File file) {//使用最后修改文件的时间，判断是否更新缓存（默认更新）
//		@SuppressLint("SimpleDateFormat")
//		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");//获取目录下所有文件
//		Date currDate = new Date(System.currentTimeMillis());//获取当前时间
//		try {
//			currDate = dateFormat.parse(dateFormat.format(new Date(System.currentTimeMillis())));
//		} catch (Exception e) {
//			FELog.d("dataformat exeption e " + e.toString());
//		}
//		try {
//			//文件时间减去当前时间
//			Date fileDate = dateFormat.parse(dateFormat.format(new Date(file.lastModified())));
//			long diff = currDate.getTime() - fileDate.getTime();
//			long minute = diff / (1000 * 60);
//			FELog.i("-->>>>common:最后更新时间相距：" + minute + "分钟");
//			return minute > 60;//大于60分钟更新缓存
//		} catch (Exception e) {
//			FELog.d("dataformat exeption e " + e.toString());
//		}
//		return false;
//	}

	private File getCommonFile() {
		return new File(CoreZygote.getPathServices().getCommonUserId() + File.separator + "common.txt");
	}

	private void saveCommon(List<String> user) {//缓存
		FELog.i("-->>>>common:开始缓存");
		String text = GsonUtil.getInstance().toJson(user);
		File file = getCommonFile();
		if (file.exists()) file.delete();
		FileUtil.newFile(file);
		try {
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
			bos.write(text.getBytes());
			bos.flush();
			bos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<String> getCacheCommon() {//获取缓存
		FELog.i("-->>>>common:使用缓存数据");
		File file = getCommonFile();
		if (!file.exists()) {
			f.onError(new RuntimeException("Query user by type failed."));
			f.onCompleted();
			return null;
		}
		;
		BufferedInputStream bis = null;
		StringBuilder sb = new StringBuilder();
		byte[] chat = new byte[1024];
		int tmp;
		try {
			bis = new BufferedInputStream(new FileInputStream(file));
			while ((tmp = bis.read(chat)) != -1) {
				sb.append(new String(chat, 0, tmp));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (bis != null) bis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (TextUtils.isEmpty(sb)) {
			f.onError(new RuntimeException("Query user by type failed."));
			f.onCompleted();
			return null;
		}
		try {
			lastData = GsonUtil.getInstance().fromJson(sb.toString(), new TypeToken<List<String>>() {}.getType());
			return lastData;
		} catch (Exception e) {
			f.onError(new RuntimeException("Query user by type failed."));
			f.onCompleted();
		}
		return null;
	}

	private void downloadCommon(boolean isFirstLoad) {//网络获取
		FELog.i("-->>>>common:网络下载");
		String method = RemoteRequest.METHOD_GET_COMMON_PERSONS;
		RemoteRequest request = RemoteRequest.buildRequest(method);
		FEHttpClient.getInstance().post(request, new ResponseCallback<CommonTagResponse>() {
			@Override
			public void onCompleted(CommonTagResponse response) {
				if (response.getErrorCode().equals("0")) {
					List<String> userIds = response.result;
					if (CommonUtil.isEmptyList(userIds)) {
						f.onError(new RuntimeException("Query user by type failed."));
						f.onCompleted();
						return;
					}
					FELog.i("-->>>>common:完成:" + isFirstLoad);
					if (!isUserModify(userIds, lastData)) {//为修改
						return;
					}
					saveCommon(userIds);
					if (isFirstLoad) {
						userIdsToAddressBooks(userIds);
						f.onCompleted();
					}
					else {
						EventBus.getDefault().post(new CompanyChangeEvent());
					}
				}
				else {
					f.onError(new Throwable("get data error"));
					f.onCompleted();
				}
			}

			@Override
			public void onFailure(RepositoryException repositoryException) {
				f.onError(repositoryException != null && repositoryException.exception() != null
						? repositoryException.exception()
						: new RuntimeException("Query user by type failed."));
				f.onCompleted();
			}
		});
	}

	private void userIdsToAddressBooks(List<String> userIds) {//转换成用户详情，并发送
		List<AddressBook> dbUsers = mDataSources.obtainUserByIds(userIds);
		if (CommonUtil.isEmptyList(userIds) || CommonUtil.isEmptyList(dbUsers)) {
			f.onError(new RuntimeException("Query user by type failed."));
			f.onCompleted();
			return;
		}
		for (int i = 0; i < userIds.size(); i++) {
			for (int j = i; j < dbUsers.size(); j++) {
				if (dbUsers.get(j).userId.equals(userIds.get(i))) {
					Collections.swap(dbUsers, i, j);
					break;
				}
			}
		}
		f.onNext(dbUsers);
		f.onCompleted();
	}

	private boolean isUserModify(List<String> curr, List<String> last) {
		if (CommonUtil.isEmptyList(last) || CommonUtil.isEmptyList(curr)) {
			return true;
		}
		return !curr.containsAll(last) || !last.containsAll(curr);
	}
}
