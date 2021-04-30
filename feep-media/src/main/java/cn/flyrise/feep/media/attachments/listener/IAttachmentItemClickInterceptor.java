package cn.flyrise.feep.media.attachments.listener;

import cn.flyrise.feep.media.attachments.bean.Attachment;

/**
 * @author ZYP
 * @since 2017-11-08 11:51
 * 是否拦截附件点击事件
 */
public interface IAttachmentItemClickInterceptor {

	/**
	 * 是否拦截附件打开操作
	 */
	boolean isInterceptAttachmentClick(Attachment attachment);

}
