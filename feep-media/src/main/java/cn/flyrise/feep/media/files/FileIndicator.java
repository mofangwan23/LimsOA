package cn.flyrise.feep.media.files;

import android.os.Environment;
import android.text.TextUtils;

/**
 * @author ZYP
 * @since 2017-10-24 10:46
 */
public class FileIndicator {

	public String name;
	public String path;

	private FileIndicator() {
	}

	private FileIndicator(String name, String path) {
		this.name = name;
		this.path = path;
	}

	public static FileIndicator create(String name, String path) {
		return new FileIndicator(name, path);
	}

	/**
	 * 创建根路径的指示器
	 */
	public static FileIndicator createRootIndicator() {
		return new FileIndicator("设备存储", Environment.getExternalStorageDirectory().toString());
	}

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		FileIndicator that = (FileIndicator) o;

		if (!name.equals(that.name)) return false;
		return path.equals(that.path);

	}

	@Override public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + path.hashCode();
		return result;
	}

	public boolean isRootIndicator() {
		return TextUtils.equals(Environment.getExternalStorageDirectory().toString(), path);
	}
}