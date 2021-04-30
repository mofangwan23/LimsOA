package cn.flyrise.feep.location.util;

import android.text.TextUtils;
import cn.flyrise.android.library.utility.encryption.AESUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.location.bean.LocationSaveItem;
import com.google.gson.reflect.TypeToken;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * cm2016-11-10.
 * 文件存储
 */

public class LocationSaveFileUtil {

	private static final int ADDRESS_HINT = 1021;
	private static final int ADDRESS_CUSTOM = 1022;

	private Map<Integer, String> fileMaps;

	private static LocationSaveFileUtil instance;

	private String filePath;

	public static LocationSaveFileUtil getInstance() {
		if (instance == null) {
			synchronized (LocationSaveFileUtil.class) {
				if (instance == null) {
					instance = new LocationSaveFileUtil();
				}
			}
		}
		return instance;
	}

	private LocationSaveFileUtil() {
		if (TextUtils.isEmpty(filePath)) {
			filePath = getFilePath();
		}
		if (TextUtils.isEmpty(filePath)) {
			return;
		}
		fileMaps = getFile();
		if (fileMaps == null) {
			fileMaps = new HashMap<>();
		}
	}

	private String getFilePath() {
		if (CoreZygote.getLoginUserServices() == null) {
			return "";
		}
		String text = CoreZygote.getLoginUserServices().getServerAddress() + CoreZygote.getLoginUserServices().getUserId();
		return CoreZygote.getPathServices().getUserPath() + File.separator + CommonUtil.getMD5(text) + ".dt";
	}

	//离开修改界面需要调用保存
	private void saveFile() {
		if (fileMaps == null || fileMaps.size() == 0 || TextUtils.isEmpty(filePath)) {
			return;
		}
		File file = new File(filePath);
		if (file.exists()) {
			file.delete();
		}
		else {
			file.getParentFile().mkdirs();
		}
		try {
			String text = GsonUtil.getInstance().toJson(fileMaps);
			byte[] data = AESUtils.encrypt(text.getBytes("utf-8"));
			fileWrite(file, data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void fileWrite(File file, byte[] bytes) {
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(file));
			bos.write(bytes);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bos.flush();
				bos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private Map<Integer, String> getFile() {
		if (TextUtils.isEmpty(filePath)) {
			return null;
		}
		File file = new File(filePath);
		if (!file.exists()) {
			return null;
		}
		String text = "";
		try {
			byte[] bytes = AESUtils.decrypt(fileReader(file));
			text = new String(bytes);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (TextUtils.isEmpty(text)) {
			return null;
		}
		return GsonUtil.getInstance().fromJson(text
				, new TypeToken<Map<Integer, String>>() {
				}.getType());
	}

	private byte[] fileReader(File file) {
		BufferedInputStream bis = null;
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		byte[] bytes = new byte[1024];
		int bin;
		try {
			bis = new BufferedInputStream(new FileInputStream(file));
			while ((bin = bis.read(bytes, 0, bytes.length)) != -1) {
				bao.write(bytes, 0, bin);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return bao.toByteArray();
	}

	void saveAddressHint(Map<String, LocationSaveItem> addressHints) {
		if (addressHints == null) {
			return;
		}
		putMap(ADDRESS_HINT, addressHints);
	}

	void saveAddressCustom(Map<String, LocationSaveItem> addressCustoms) {
		if (addressCustoms == null) {
			return;
		}
		putMap(ADDRESS_CUSTOM, addressCustoms);
	}

	private void putMap(int key, Object object) {
		if (fileMaps == null) {
			return;
		}
		fileMaps.put(key, GsonUtil.getInstance().toJson(object));
		saveFile();
	}


	private String getSaveFile(int key) {
		return fileMaps == null ? "" : fileMaps.get(key);
	}

	Map<String, LocationSaveItem> getAddressHint() {
		return GsonUtil.getInstance().fromJson(getSaveFile(ADDRESS_HINT), new TypeToken<Map<String, LocationSaveItem>>() {
		}.getType());
	}

	Map<String, LocationSaveItem> getAddressCustom() {
		return GsonUtil.getInstance().fromJson(getSaveFile(ADDRESS_CUSTOM), new TypeToken<Map<String, LocationSaveItem>>() {
		}.getType());
	}

	public void onDestroy() {
		instance = null;
	}
}
