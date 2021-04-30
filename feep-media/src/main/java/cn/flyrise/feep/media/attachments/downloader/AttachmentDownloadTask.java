package cn.flyrise.feep.media.attachments.downloader;

import android.text.TextUtils;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.services.ILoginUserServices;
import cn.flyrise.feep.media.attachments.bean.TaskInfo;
import cn.flyrise.feep.media.attachments.listener.IDownloadListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author ZYP
 * @since 2017-10-31 11:07
 * 一个正在执行的下载任务
 */
public class AttachmentDownloadTask implements Runnable {

	public static final int STATE_RUNNING = 1;
	public static final int STATE_COMPLETED = 2;
	public static final int STATE_STOP = 3;

	private volatile int mDownloadState;
	private final TaskInfo mTaskInfo;
	private final IDownloadListener mDownloadListener;

	public AttachmentDownloadTask(TaskInfo taskInfo, IDownloadListener listener) {
		this.mTaskInfo = taskInfo;
		this.mDownloadListener = listener;
	}

	public String getTaskId() {
		return mTaskInfo.taskID;
	}

	public TaskInfo getTaskInfo() {
		return mTaskInfo;
	}

	public boolean isRunning() {
		return mDownloadState == STATE_RUNNING;
	}

	public boolean isCompleted() {
		return mDownloadState == STATE_COMPLETED;
	}

	public void stop() {
		mDownloadState = STATE_STOP;
		if (mDownloadListener != null) {
			mDownloadListener.onStop(this);
		}
	}

	@Override public void run() {
		RandomAccessFile attachmentFile = null;
		try {
			File attachment = new File(mTaskInfo.filePath);
			final File parentFile = attachment.getParentFile();
			if (!parentFile.exists()) {
				parentFile.mkdirs();
			}

			attachmentFile = new RandomAccessFile(attachment, "rwd");
			URL url = new URL(mTaskInfo.url);   // 这个 url 是完整的
			HttpURLConnection connection;
			connection = (HttpURLConnection) url.openConnection();

			connection.setConnectTimeout(1000 * 5);
			connection.setReadTimeout(1000 * 15);

			connection.setRequestProperty("User-agent",CoreZygote.getUserAgent());

			boolean hasDownloadSize = false;
			if (mTaskInfo.downloadSize > 0) {
				if (attachment.exists()) {
					hasDownloadSize = true;
					attachmentFile.seek(mTaskInfo.downloadSize);
					connection.setRequestProperty("Range", "bytes=" + mTaskInfo.downloadSize + "-");
				}
			}

			ILoginUserServices userServices = CoreZygote.getLoginUserServices();
			if (userServices != null && !TextUtils.isEmpty(userServices.getAccessToken())) {
				connection.setRequestProperty("token", userServices.getAccessToken());
			}

			mDownloadState = STATE_RUNNING;
			if (mDownloadListener != null) {
				mDownloadListener.onStart(mTaskInfo);
			}

			int len = 0;
			byte[] buff = new byte[2048];
			if (!hasDownloadSize) {
				mTaskInfo.fileSize = connection.getContentLength();
			}
			InputStream inputStream = connection.getInputStream();
			while ((len = inputStream.read(buff)) > 0 && isRunning()) {
				attachmentFile.write(buff, 0, len);
				mTaskInfo.downloadSize += len;
				if (mDownloadListener != null) {
					mDownloadListener.onProgress(this);
				}
			}

			if (isRunning()) {  // 下载成功
				if (mDownloadListener != null) {
					mDownloadListener.onCompleted(this);
				}
			}
			mDownloadState = STATE_COMPLETED;

		} catch (Exception exp) {
			exp.printStackTrace();
			if (mDownloadListener != null) {
				mDownloadListener.onFailed(this);
			}
		} finally {
			if (attachmentFile != null) {
				try {
					attachmentFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
