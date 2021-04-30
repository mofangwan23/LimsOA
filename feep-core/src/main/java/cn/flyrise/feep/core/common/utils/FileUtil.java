package cn.flyrise.feep.core.common.utils;

import android.text.TextUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.R;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-02-07 09:37
 * 文件操作工具类
 */
public class FileUtil {

	private static final String[] ERROR_CHARS = {"/", "\\", "*", "?", "<", ">", "\"", "|"};

	/**
	 * 创建文件目录
	 */
	public static void newDir(File f) {
		if (!f.exists()) {
			f.mkdir();
		}
	}

	public static boolean isFileExists(String filePath){
		File file = new File(filePath);
		if (file.exists()){
			return true;
		}else {
			return false;
		}
	}

	public static long getFolderSize(File file) throws Exception {
		long size = 0;
		try {
			File[] fileList = file.listFiles();
			if (CommonUtil.isEmptyList(fileList)) return 0;
			for (File aFileList : fileList) {
				if (aFileList.isDirectory()) {
					size = size + getFolderSize(aFileList);
				}
				else {
					size = size + aFileList.length();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return size;
	}

	public static void newFile(File f) {
		if (!f.exists()) {
			File parentFile = f.getParentFile();
			if (!parentFile.exists()) {
				parentFile.mkdirs();
			}

			try {
				f.createNewFile();
			} catch (final IOException e) {

				e.printStackTrace();
			}
		}
	}


	public static long getFileByteSize(String path) {
		final File file = new File(path);
		if (file.exists()) {
			return file.length();
		}
		else {
			return 0;
		}
	}

	public static String getFileSize(long size) {
		String strSize = CoreZygote.getContext().getString(R.string.core_unknow_size);
		if (size >= 1024 * 1024) {
			double dousize = (double) size / (1024 * 1024);
			DecimalFormat df = new DecimalFormat("#.00");
			strSize = df.format(dousize) + "Mb";
		}
		else if (size >= 1024) {
			final double dousize = size / 1024;
			strSize = (int) dousize + "Kb";
		}
		else if (size > 0) {
			strSize = size + "b";
		}
		return strSize;
	}

	public static double getSize(List<String> filePaths) {
		return (double) getSizeUnitByte(filePaths) / (1024 * 1024);
	}

	/**
	 * 计算文件的大小，单位是字节
	 */
	public static long getSizeUnitByte(List<String> filePaths) {
		long allfilesize = 0;
		for (int i = 0; i < filePaths.size(); i++) {
			final File newfile = new File(filePaths.get(i));
			if (newfile.exists() && newfile.isFile()) {
				allfilesize = allfilesize + newfile.length();
			}
		}
		return allfilesize;
	}

	/**
	 * 根据文件名获取文件类型
	 */
	public static String getFileType(String name) {
		if (name.lastIndexOf(".") != -1) {
			return name.substring(name.lastIndexOf("."), name.length());
		}
		return CoreZygote.getContext().getString(R.string.core_unknow_type);
	}

	/**
	 * 复制文件
	 */
	public static boolean copyFile(String oldPath, String newPath) {
		boolean iscopy = false;
		InputStream inStream = null;
		FileOutputStream fs = null;
		try {
			int byteread;
			final File oldfile = new File(oldPath);
			if (oldfile.exists()) { // 文件存在时
				inStream = new FileInputStream(oldPath); // 读入原文件
				fs = new FileOutputStream(newPath);
				final byte[] buffer = new byte[1024];
				while ((byteread = inStream.read(buffer)) != -1) {
					fs.write(buffer, 0, byteread);
				}
				iscopy = true;
			}
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (inStream != null) {
					inStream.close();
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
			try {
				if (fs != null) {
					fs.close();
				}
			} catch (final IOException e) {
				e.printStackTrace();
			}
		}
		return iscopy;
	}

	public static void deleteAllFiles(File root) {
		if (root == null || !root.exists()) {
			return;
		}
		File files[] = root.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isDirectory()) {
					deleteAllFiles(f);
					try {
						f.delete();
					} catch (Exception ignored) {
					}
				}
				else {
					if (f.exists()) {
						deleteAllFiles(f);
						try {
							f.delete();
						} catch (Exception ignored) {
						}
					}
				}
			}
		}
	}

	public static void moveToDir(String srcPath, String targetPath) {
		File folder = new File(srcPath);
		if (folder.exists()) {
			File[] files = folder.listFiles();
			for (File file : files) {
				if (file.isFile()) {
					String filename = file.getPath();
					filename = filename.substring(filename.lastIndexOf("/"), filename.length());
					File newFile = new File(targetPath + File.separator + filename);
					file.renameTo(newFile);
				}
			}
			folder.delete();
		}
	}

	/**
	 * 剔除文件名中的非法字符
	 */
	public static String fixFileName(String fileName) {
		if (fileName != null) {
			for (final String c : ERROR_CHARS) {
				if (fileName.contains(c)) {
					fileName = fileName.replaceAll(c, "");
				}
			}
		}
		return fileName;
	}

	/**
	 * 获取文件名称中的括号内容 如：
	 * input: (hello_world)file_name.pdf
	 * output: hello_world
	 */
	public static String getSpecialFileName(String fileName) {
		if (TextUtils.isEmpty(fileName)) {
			return fileName;
		}

		int index = fileName.indexOf(")");
		if (fileName.startsWith("(") && index != -1) {
			final int startIndex = index + 1;
			final int endIndex = fileName.length();
			if (startIndex < endIndex) {
				return fileName.substring(startIndex, endIndex);
			}
		}
		return fileName;
	}

	/**
	 * 读取全部文件内容
	 */
	public static String readAll(File file) {
		if (file == null || !file.exists()) {
			return null;
		}

		StringBuffer buffer = null;
		try {
			String line = null;
			buffer = new StringBuffer();
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			while ((line = reader.readLine()) != null) {
				buffer.append(line);
			}
			reader.close();
		} catch (Exception exp) {
			exp.printStackTrace();
		}
		return buffer == null ? null : buffer.toString();
	}


	public static boolean writeData(File file, String data) {
		if (file != null && file.exists()) {
			try {
				FileWriter writer = new FileWriter(file);
				writer.append(data);
				writer.flush();
				writer.close();
				return true;
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return false;
	}


	/**
	 * 清除对应目录下的文件，但不包括文件自身
	 * @param path 文件夹路径
	 * @return 清楚文件数量
	 */
	public static int deleteFiles(String path) {
		int deletedFiles = 0;
		final File needDeleteFile = new File(path);
		if (needDeleteFile.exists()) {
			try {
				if (needDeleteFile.isFile()) {
					if (needDeleteFile.delete()) {
						deletedFiles++;
					}
				}
				else {
					for (final File child : needDeleteFile.listFiles()) {
						if (child.isDirectory()) {
							deletedFiles += deleteFiles(child.getPath());
						}
						if (child.delete()) {
							deletedFiles++;
						}
					}
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		return deletedFiles;
	}

	public static void deleteFolderFile(String filePath, boolean deleteThisPath) {
		if (!TextUtils.isEmpty(filePath)) {
			try {
				File file = new File(filePath);
				if (file.isDirectory()) {
					File files[] = file.listFiles();
					for (File file1 : files) {
						deleteFolderFile(file1.getAbsolutePath(), true);
					}
				}
				if (deleteThisPath) {
					if (!file.isDirectory()) {
						file.delete();
					}
					else {// 目录
						if (file.listFiles().length == 0) {
							file.delete();
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
