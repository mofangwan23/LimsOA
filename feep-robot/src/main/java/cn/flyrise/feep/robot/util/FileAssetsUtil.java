package cn.flyrise.feep.robot.util;

import android.content.Context;
import android.content.res.AssetManager;
import java.io.IOException;
import java.io.InputStream;

/**
 * 新建：陈冕;
 * 日期： 2017-11-30-17:09.
 */

public class FileAssetsUtil {

	public String getAssetsFileText(Context context, String path) {
		return new String(getAssetsFileBytes(context, path));
	}

	private byte[] getAssetsFileBytes(Context context, String path) {
		AssetManager assetManager = context.getResources().getAssets();
		byte[] buffer = null;
		InputStream ins = null;
		try {
			ins = assetManager.open(path);
			buffer = new byte[ins.available()];

			ins.read(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (ins != null) {
				try {
					ins.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return buffer;
	}
}
