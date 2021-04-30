package cn.flyrise.feep.more.download.manager;

/**
 * @author ZYP
 * @since 2017-11-09 17:27
 */
public interface IDownloadManagerOperationListener {

	int DOWNLOAD_COMPLETED_VIEW = 1;
	int DOWNLOADING_VIEW = 2;

	void notifyEditModeChange(int view, boolean isEditMode);

	void refreshDownloadList();

}
