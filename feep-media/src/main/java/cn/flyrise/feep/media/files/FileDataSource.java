package cn.flyrise.feep.media.files;

import cn.flyrise.feep.core.common.utils.CommonUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Subscriber;

/**
 * @author ZYP
 * @since 2017-10-23 10:51
 */
public class FileDataSource {

	private FileSelectionSpec mSelectionSpec;
	private List<FileItem> mSelectedFiles;
	private List<String> mSelectedFilePath;

	public FileDataSource(FileSelectionSpec selectionSpec) {
		this.mSelectionSpec = selectionSpec;
		mSelectedFiles = new ArrayList<>();
		mSelectedFilePath = new ArrayList<>();
		if(CommonUtil.nonEmptyList(selectionSpec.getSelectedFiles())) {
			mSelectedFilePath.addAll(selectionSpec.getSelectedFiles());
		}
	}

	/**
	 * 加载指定目录下的子文件夹和文件
	 */
	public Observable<List<FileItem>> loadFiles(final String path) {
		File rootPath = new File(path);
		File[] files = rootPath.listFiles();
		if (files == null || files.length == 0) {
			return Observable.create(new OnSubscribe<List<FileItem>>() {
				@Override public void call(Subscriber<? super List<FileItem>> subscriber) {
					subscriber.onNext(null);
					subscriber.onCompleted();
				}
			});
		}

		return Observable.from(files)
				.filter(file -> file.length() > 0)                  // 0. 过滤 size = 0 的文件
				.map(FileItem::convertFile)                         // 1. 将 File 转换成 FileItem
				.filter(mSelectionSpec::isExpectFile)               // 2. 过滤文件后缀、排除指定路径后的文件
				.doOnNext(fileItem -> {                             // 3. 检查 FileItem 是否之前已经被用户选过
					if (mSelectionSpec.isFileSelected(fileItem.path)) {
						if (!mSelectedFiles.contains(fileItem)) {
							mSelectedFiles.add(fileItem);
						}
					}
				})
				.toSortedList((fileItem, fileItem2) -> {        // 4. 排序，按文件夹(a-z)、文件(a-z)
					if (fileItem.isDir() && !fileItem2.isDir()) return -1;
					if (fileItem2.isDir() && !fileItem.isDir()) return 1;
					return fileItem.name.toLowerCase().compareTo(fileItem2.name.toLowerCase());
				});
	}

	/**
	 * 执行文件操作，添加 or 移除，返回对象的状态码
	 * return 1：添加成功; -1: 移除成功; 0: 无法添加或已达到最大上限
	 */
	public int executeFileCheckedChange(FileItem fileItem) {
		if (mSelectedFiles.contains(fileItem)) {
			mSelectedFiles.remove(fileItem);
			mSelectedFilePath.remove(fileItem.path);
			return -1;
		}

		if (mSelectedFiles.size() >= mSelectionSpec.getMaxSelectCount()) {
			return 0;
		}

		mSelectedFiles.add(fileItem);
		mSelectedFilePath.add(fileItem.path);
		return 1;
	}

	public boolean isSingleChoice() {
		return mSelectionSpec.isSingleChoice();
	}

	public List<FileItem> getSelectedFiles() {
		return mSelectedFiles;
	}

	public List<String> getSelectedFilePaths() {
		return mSelectedFilePath;
	}

	public List<String> getLastTimeSelectedFiles() {
		return mSelectionSpec.getSelectedFiles();
	}
}
