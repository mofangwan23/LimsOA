package cn.flyrise.feep.media.files;

import cn.flyrise.feep.media.R;
import cn.flyrise.feep.media.common.FileCategoryTable;
import java.io.File;

/**
 * @author ZYP
 * @since 2017-10-23 11:17
 */
public class FileItem {

	public String name;
	public String path;
	public int thumbnailRes;

	public static FileItem convertFile(File file) {
		FileItem fileItem = new FileItem();
		fileItem.name = file.getName();
		fileItem.path = file.getPath();
		if (file.isDirectory()) {
			fileItem.thumbnailRes = R.mipmap.ms_icon_thumbnail_dir;
			return fileItem;
		}

		int lastDotIndex = fileItem.path.lastIndexOf(".");
		if (lastDotIndex == -1) {
			fileItem.thumbnailRes = R.mipmap.ms_icon_thumbnail_unknow;
			return fileItem;
		}

		String suffix = fileItem.path.substring(lastDotIndex + 1, fileItem.path.length());
		fileItem.thumbnailRes = FileCategoryTable.getIcon(suffix);
		return fileItem;
	}

	public boolean isDir() {
		return this.thumbnailRes == R.mipmap.ms_icon_thumbnail_dir;
	}

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		FileItem fileItem = (FileItem) o;

		return path.equals(fileItem.path);

	}

	@Override public int hashCode() {
		return path.hashCode();
	}
}
