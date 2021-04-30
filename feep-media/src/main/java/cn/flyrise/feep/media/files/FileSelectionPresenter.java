package cn.flyrise.feep.media.files;

import android.content.Intent;
import java.util.ArrayList;
import java.util.List;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2017-10-23 19:43
 */
public class FileSelectionPresenter {

	private FileSelectionView mSelectionView;
	private FileDataSource mDataSource;

	public FileSelectionPresenter(FileSelectionView selectionView, Intent intent) {
		this.mSelectionView = selectionView;
		this.mDataSource = new FileDataSource(new FileSelectionSpec(intent));
	}

	public void start(String rootPath) {
		loadFiles(rootPath);
	}

	public void loadFiles(String path) {
		mDataSource.loadFiles(path)
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(mSelectionView::onFileLoad, throwable-> mSelectionView.onFileLoad(null));
	}

	/**
	 * 未添加的会执行添加操作，已添加的执行移除操作.
	 */
	public int executeImageCheckChange(FileItem fileItem) {
		return mDataSource.executeFileCheckedChange(fileItem);
	}

	public boolean isSingleChoice() {
		return mDataSource.isSingleChoice();
	}

	public List<FileItem> getSelectedFiles() {
		return mDataSource.getSelectedFiles();
	}

	public List<String> getSelectedFilePath() {
		return mDataSource.getSelectedFilePaths();
	}

}
