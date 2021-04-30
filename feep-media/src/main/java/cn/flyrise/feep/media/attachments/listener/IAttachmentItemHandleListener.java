package cn.flyrise.feep.media.attachments.listener;

import cn.flyrise.feep.media.attachments.bean.Attachment;

/**
 * @author ZYP
 * @since 2017-10-31 10:39
 */
public interface IAttachmentItemHandleListener {

	/**
	 * 附件点击，打开 or 下载
	 */
	void onAttachmentItemClick(int position, Attachment attachment);

	/**
	 * 附件长按，进入编辑状态
	 */
	void onAttachmentItemLongClick(Attachment attachment);

	/**
	 * 附件删除复选框切换时
	 */
	void onAttachmentItemToBeDeleteCheckChange();

	/**
	 * 附件暂停下载
	 */
	void onAttachmentDownloadStopped(Attachment attachment);

	/**
	 * 附件继续下载
	 */
	void onAttachmentDownloadResume(Attachment attachment);

}
