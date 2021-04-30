package cn.flyrise.feep.utils;

import android.text.TextUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.FileUtil;
import java.io.File;

/**
 * @author ZYP
 * @since 2018-05-08 09:13
 */
public class TempDirCleaner {

	public static void clear() {
		new Thread(() -> {
			if (CoreZygote.getPathServices() == null) return;
			String tempDirPath = CoreZygote.getPathServices().getTempFilePath();
			if (TextUtils.isEmpty(tempDirPath)) return;
			File tempDir = new File(tempDirPath);
			if (!tempDir.exists()) return;// 文件不存在，不清理
			FileUtil.deleteFolderFile(tempDirPath, false);

		}).start();
	}
}
