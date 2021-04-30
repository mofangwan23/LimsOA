package cn.flyrise.feep.media.attachments.bean;

import android.support.annotation.Keep;

/**
 * @author ZYP
 * @since 2017-10-27 10:01
 * 附件对照组：实际附件的名字、存储在 sdcard 上的名字
 */
@Keep
public class AttachmentControlGroup {

	public String taskId;           // attachment id
	public String realName;         // attachment name
	public String storageName;      // 保存在 sdcard 下的文件名（一串 MD5)

}
