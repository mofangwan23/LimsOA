package cn.flyrise.feep.more.download.manager;

import android.content.Context;
import cn.flyrise.feep.media.attachments.bean.Attachment;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-11-10 14:40
 */
public interface DownloadingView {

	void showDownloadingAttachments(List<Attachment> attachments);

	void onDownloadProgressChange(int position);

	Context getContext();

	void onDownloadCompleted();

}
