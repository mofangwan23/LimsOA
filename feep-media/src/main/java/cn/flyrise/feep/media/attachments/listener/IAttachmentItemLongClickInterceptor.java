package cn.flyrise.feep.media.attachments.listener;

import cn.flyrise.feep.media.attachments.bean.Attachment;

/**
 * @author ZYP
 * @since 2017-11-08 15:35
 */
public interface IAttachmentItemLongClickInterceptor {

	/**
	 * 是否拦截附件长按事件
	 */
	boolean isInterceptAttachmentLongClick(Attachment attachment);

}
