package cn.flyrise.feep.media.attachments.listener;

import cn.flyrise.feep.media.attachments.bean.Attachment;

/**
 * @author ZYP
 * @since 2017-11-08 16:04
 */
public interface ILocalAttachmentItemHandleListener {

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

}
