package cn.flyrise.feep.media.attachments.bean;

import static cn.flyrise.feep.media.attachments.downloader.AttachmentDownloadTask.STATE_COMPLETED;
import static cn.flyrise.feep.media.attachments.downloader.AttachmentDownloadTask.STATE_RUNNING;
import static cn.flyrise.feep.media.attachments.downloader.AttachmentDownloadTask.STATE_STOP;

/**
 * @author ZYP
 * @since 2017-11-01 09:44
 */
public class DownloadProgress {

	private volatile int state;
	private int progress;

	public boolean isRunning() {
		return STATE_RUNNING == state;
	}

	public boolean isCompleted() {
		return STATE_COMPLETED == state;
	}

	public boolean isStop() {
		return STATE_STOP == state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public void setProgress(int progress) {
		this.progress = progress;
	}

	public int getProgress() {
		return progress;
	}

	@Override public String toString() {
		return "DownloadProgress{" +
				"state=" + state +
				", progress=" + progress +
				'}';
	}
}
