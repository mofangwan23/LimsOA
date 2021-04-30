package cn.flyrise.feep.media.attachments.listener;

import cn.flyrise.feep.media.attachments.bean.Attachment;
import cn.flyrise.feep.media.attachments.bean.DownloadProgress;

/**
 * @author ZYP
 * @since 2017-11-07 14:32
 */
public interface IDownloadProgressCallback {

	DownloadProgress downloadProgress(Attachment attachment);

}
