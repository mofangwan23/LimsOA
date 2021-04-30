package cn.flyrise.feep.media.attachments.listener;

import cn.flyrise.feep.media.attachments.bean.Attachment;

/**
 * @author ZYP
 * @since 2017-11-07 14:30
 */
public interface INetworkAttachmentItemHandleListener {

	/**
	 * 附件点击，打开 or 下载
	 */
	void onAttachmentItemClick(int positition, Attachment attachment);

	/**
	 * 附件暂停下载
	 */
	void onAttachmentDownloadStopped(Attachment attachment);

	/**
	 * 附件继续下载
	 */
	void onAttachmentDownloadResume(Attachment attachment);
}
