package cn.flyrise.feep.addressbook.utils;

import static cn.flyrise.feep.addressbook.processor.AddressBookProcessor.ADDRESS_BOOK_DOWNLOAD_FAILED;
import static cn.flyrise.feep.addressbook.processor.AddressBookProcessor.ADDRESS_BOOK_UNZIP_FAILED;
import static cn.flyrise.feep.addressbook.processor.AddressBookProcessor.ADDRESS_BOOK_UPDATE_FAILED;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import cn.flyrise.feep.K;
import cn.flyrise.feep.addressbook.model.ExtractInfo;
import cn.flyrise.feep.addressbook.processor.AddressBookProcessor;
import cn.flyrise.feep.addressbook.processor.AddressBookProcessor.IDisposeListener;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.FileUtil;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.services.model.NetworkInfo;
import cn.flyrise.feep.dbmodul.table.ContactsVerionsTable;
import cn.flyrise.feep.dbmodul.utils.ContactsVersionUtils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author ZYP
 * @since 2017-02-09 11:28
 * 仅负责通讯录的下载和解压，但不负责解密和初始化各种数据库。
 */
public class AddressBookDownloader {

	private IDisposeListener mDisposeListener;
	private String mUserId;
	private String mSmallVersion;
	private final Handler mHandler = new Handler(Looper.getMainLooper());

	public void setUserId(String userId) {
		this.mUserId = userId;
	}

	public void setSmallVersion(String smallVersion) {
		this.mSmallVersion = smallVersion;
	}

	public void setDownloadListener(IDisposeListener listener) {
		this.mDisposeListener = listener;
	}

	public void start() {
		new Thread(() -> {
			File zipFile = null;

			final int MAX_TRY_TIMES = 3;
			for (int i = 0; i < MAX_TRY_TIMES; i++) {
				zipFile = downloadAddressBookZip();
				if (zipFile != null && zipFile.length()>0) {
					break;
				}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			if (zipFile == null || zipFile.length()==0) {                                          // 下载失败
				int sourceType = AddressBookExceptionInvoker.tryRestoreOldVersion();   // 尝试恢复使用旧版本
				if (sourceType == -1) {                                     // 使用旧版本失败
					if (mDisposeListener != null) {
						mHandler.post(() -> mDisposeListener.onDisposeFailed(ADDRESS_BOOK_DOWNLOAD_FAILED));
						FELog.e("shujuku--下载失败-使用旧版本失败");
					}
				}
				else {
					if (mDisposeListener != null) {                         // 下载失败，然而存在可用数据库
						mHandler.post(() -> mDisposeListener.onDisposeSuccess(ADDRESS_BOOK_UPDATE_FAILED, sourceType));
						FELog.e("shujuku--下载失败-然而存在可用数据库");
					}
				}
				return;
			}

			ExtractInfo extractInfo = null;
			for (int i = 0; i < MAX_TRY_TIMES; i++) {
				extractInfo = unZipFile(zipFile);
				if (extractInfo != null) {
					break;
				}
			}

			if (zipFile.exists()) {
				zipFile.delete();
			}

			if (extractInfo == null) {                          // 解压失败，有可能下载下来的内容有问题
				int sourceType = AddressBookExceptionInvoker.tryRestoreOldVersion();
				if (sourceType == -1) {
					if (mDisposeListener != null) {             // 彻底没救
						mHandler.post(() -> mDisposeListener.onDisposeFailed(ADDRESS_BOOK_UNZIP_FAILED));
						FELog.e("shujuku--解压失败-彻底没救");
					}
				}
				else {
					if (mDisposeListener != null) {
						mHandler.post(() -> mDisposeListener.onDisposeSuccess(ADDRESS_BOOK_UPDATE_FAILED, sourceType));
					}
				}
				return;
			}

			AddressBookProcessor processor = AddressBookProcessor.build(extractInfo.type);
			processor.serUserId(mUserId);
			processor.setDisposeListener(mDisposeListener);
			processor.dispose(extractInfo);
		}).start();
	}

	/**
	 * 下载通讯录文件，并保存到 /feep/userId/TEMPFILE 目录下
	 * 该文件是一个 zip 压缩包，解压后会被删除
	 */
	private File downloadAddressBookZip() {
		String downloadFileDirPath = CoreZygote.getPathServices().getTempFilePath();
		File downloadFileDir = new File(downloadFileDirPath);

		if (!downloadFileDir.exists()) {
			downloadFileDir.mkdirs();
		}

		File targetZipFile = new File(downloadFileDirPath + File.separator + "addressbook.zip");
		if (targetZipFile.exists()) {
			targetZipFile.delete();
		}

		String downloadUrl = getDownloadUrl();
		if (TextUtils.isEmpty(downloadUrl)) {
			FELog.i("addressDownload","downloadUrl is Empty!!!");
			return null;
		}

		try {
			OkHttpClient client = FEHttpClient.getInstance().getOkHttpClient();
			Request request = new Request.Builder()
					.url(downloadUrl)
					.addHeader("User-Agent", CoreZygote.getUserAgent())
					.build();
			Response response = client.newCall(request).execute();
			if (response != null && response.isSuccessful()) {
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(targetZipFile));
				InputStream is = response.body().byteStream();
				int len;
				byte[] buf = new byte[2048];
				while ((len = is.read(buf)) != -1) {
					bos.write(buf, 0, len);
				}

				bos.flush();
				bos.close();
				is.close();
			}
			else {
				targetZipFile = null;
			}
		} catch (Exception exp) {
			exp.printStackTrace();
			targetZipFile = null;
		}
		return targetZipFile;
	}

	/**
	 * 将下载好的通讯录文件解压到 /feep/userId/addressbook 目录下
	 * 解压完成后将 zip 压缩包删除
	 */
	private ExtractInfo unZipFile(File zFile) {
		ExtractInfo extractInfo = null;
		try{
			ZipFile zipFile = new ZipFile(zFile);
			Enumeration<? extends ZipEntry> entries = zipFile.entries();
			if (entries.hasMoreElements()) {
				extractInfo = new ExtractInfo();
				ZipEntry zipEntry = entries.nextElement();
				String targetFileName = zipEntry.getName();
				extractInfo.name = targetFileName;

				if (targetFileName.endsWith(".db")) {
					extractInfo.type = ExtractInfo.TYPE_DB;
					extractInfo.path = CoreZygote.getPathServices().getAddressBookPath() + File.separator + targetFileName;
				}
				else if (targetFileName.endsWith(".sql")) {
					extractInfo.type = ExtractInfo.TYPE_SQL;
					extractInfo.path = CoreZygote.getPathServices().getAddressBookPath() + File.separator + targetFileName;
				}
				else {
					extractInfo.type = ExtractInfo.TYPE_JSON;
					extractInfo.path = CoreZygote.getPathServices().getAddressBookPath() + File.separator + "addressbook.dat";
				}

				File targetFile = new File(extractInfo.path);
				if (targetFile.exists()) {
					targetFile.delete();
				}
				FileUtil.newFile(targetFile);

				InputStream inputStream = zipFile.getInputStream(zipEntry);

				BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(targetFile));

				int len = 0;
				byte[] buf = new byte[2048];
				while ((len = inputStream.read(buf)) != -1) {
					outputStream.write(buf, 0, len);
				}
				outputStream.flush();
				outputStream.close();
				inputStream.close();
			}
		} catch (Exception exp) {
			extractInfo = null;
			exp.printStackTrace();
		}


		return extractInfo;
	}

	/**
	 * 获取 AddressBook 的下载地址，需要考虑新旧机制的兼容，以及傻逼 databaseVersion 值的获取。
	 */
	private String getDownloadUrl() {
		NetworkInfo networkInfo = null;
		try {
			networkInfo = CoreZygote.getLoginUserServices().getNetworkInfo();
		} catch (Exception exp) {
			exp.printStackTrace();
		}

		if (networkInfo == null) {
			return null;
		}

		StringBuilder urlBuilder = new StringBuilder(networkInfo.isHttps ? "https" : "http");
		urlBuilder.append("://").append(networkInfo.serverAddress);
		if (!TextUtils.isEmpty(networkInfo.serverPort)) urlBuilder.append(":").append(networkInfo.serverPort);
		urlBuilder.append(CoreZygote.getPathServices().getAddressBookUrl());
		urlBuilder.append(mUserId);

		if (!TextUtils.isEmpty(mSmallVersion)) {
			urlBuilder.append("&new=1");
		}

		ContactsVerionsTable table = ContactsVersionUtils.select();
		if (table != null) {
			if (TextUtils.isEmpty(mSmallVersion)) {     // 特别旧的机制
				urlBuilder.append("&orgCount=").append(table.departmentNums);
				urlBuilder.append("&userCount=").append(table.personNums);
			}
			else {  // 稍微新一点的机制
				urlBuilder.append("&orgCount=").append(TextUtils.isEmpty(table.allVersion) ? "0" : table.allVersion);
				urlBuilder.append("&userCount=").append(TextUtils.isEmpty(table.personsVersion) ? "0" : table.personsVersion);
			}
		}

		urlBuilder.append("&userMax=").append(databaseVersion());
		return urlBuilder.toString();
	}

	/**
	 * 获取数据库版本
	 * 1. 从 sp 文件中获取，如果 sp 文件没有，返回 0
	 * 2. sp 文件中获取到了，根据获取到的内容到 /feep/userId/addressbook/ 目录下查找
	 * 2.1 找到，返回名字
	 * 2.2 找不到，清空该目录，同时重置 sp 文件下对应的值，并返回 0
	 */
	private String databaseVersion() {
		String spDBName = SpUtil.get(K.preferences.address_book_version, "");
		if (TextUtils.isEmpty(spDBName)) {
			return "0";
		}

		File addressBookDir = new File(CoreZygote.getPathServices().getAddressBookPath());
		if (!addressBookDir.exists()) {
			SpUtil.put(K.preferences.address_book_version, "");
			return "0";
		}

		File[] files = addressBookDir.listFiles();
		if (files != null && files.length == 0) {
			SpUtil.put(K.preferences.address_book_version, "");
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

		String version = targetName.substring(0, targetName.lastIndexOf("."));
		return version;
	}

}
