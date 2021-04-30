package com.hyphenate.easeui.widget.chatrow;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMFileMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.ChatType;
import com.hyphenate.chat.EMVideoMessageBody;
import com.hyphenate.chatui.R;
import com.hyphenate.easeui.adapter.EaseMessageAdapter;
import com.hyphenate.easeui.model.EaseImageCache;
import com.hyphenate.easeui.ui.EaseShowVideoActivity;
import com.hyphenate.easeui.utils.DateUtils;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.util.EMLog;
import com.hyphenate.util.ImageUtils;
import com.hyphenate.util.TextFormater;
import java.io.File;

public class EaseChatRowVideo extends EaseChatRowFile {

	private ImageView imageView;
	private TextView sizeView;
	private TextView timeLengthView;

	public static EaseChatRowVideo create(ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View view = inflater.inflate(viewType == EaseMessageAdapter.MESSAGE_TYPE_SENT_VIDEO ? R.layout.ease_row_sent_video :
				R.layout.ease_row_received_video, parent, false);
		return new EaseChatRowVideo(view);
	}

	public EaseChatRowVideo(View itemView) {
		super(itemView);
	}

	@Override
	protected void findView() {
		imageView = ((ImageView) itemView.findViewById(R.id.chatting_content_iv));
		sizeView = (TextView) itemView.findViewById(R.id.chatting_size_iv);
		timeLengthView = (TextView) itemView.findViewById(R.id.chatting_length_iv);
		tvPercent = (TextView) itemView.findViewById(R.id.percentage);
	}

	@Override
	protected void setUpView() {

		EMVideoMessageBody videoBody = (EMVideoMessageBody) message.getBody();
		String localThumb = videoBody.getLocalThumb();

		if (localThumb != null) {

			showVideoThumbView(localThumb, imageView, videoBody.getThumbnailUrl(), message);
		}
		if (videoBody.getDuration() > 0) {
			String time = DateUtils.toTime(videoBody.getDuration());
			timeLengthView.setText(time);
		}

		if (message.direct() == EMMessage.Direct.RECEIVE) {
			if (videoBody.getVideoFileLength() > 0) {
				String size = TextFormater.getDataSize(videoBody.getVideoFileLength());
				sizeView.setText(size);
			}
		}
		else {
			if (videoBody.getLocalUrl() != null && new File(videoBody.getLocalUrl()).exists()) {
				String size = TextFormater.getDataSize(new File(videoBody.getLocalUrl()).length());
				sizeView.setText(size);
			}
		}

		EMLog.d(TAG, "video thumbnailStatus:" + videoBody.thumbnailDownloadStatus());
		if (message.direct() == EMMessage.Direct.RECEIVE) {
			if (videoBody.thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.DOWNLOADING ||
					videoBody.thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.PENDING) {
				imageView.setImageResource(R.drawable.ease_default_image);
				setMsgCallBack();
			}
			else {
				// System.err.println("!!!! not back receive, show image directly");
				imageView.setImageResource(R.drawable.ease_default_image);
				if (localThumb != null) {
					showVideoThumbView(localThumb, imageView, videoBody.getThumbnailUrl(), message);
				}
			}
			return;
		}
		//handle sending message
		handleSendMessage();
	}

	@Override
	protected void onBubbleClick() {
		EMLog.d(TAG, "video view is on click");
		Intent intent = new Intent(activity, EaseShowVideoActivity.class);
		intent.putExtra("msg", message);
		if (message != null && message.direct() == EMMessage.Direct.RECEIVE && !message.isAcked()
				&& message.getChatType() == ChatType.Chat) {
			try {
				EMClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		activity.startActivity(intent);
	}

	/**
	 * show video thumbnails
	 * @param localThumb local path for thumbnail
	 * @param thumbnailUrl Url on server for thumbnails
	 */
	private void showVideoThumbView(final String localThumb, final ImageView iv, String thumbnailUrl, final EMMessage message) {
		// first check if the thumbnail image already loaded into cache
		Bitmap bitmap = EaseImageCache.getInstance().get(localThumb);
		if (bitmap != null) {
			// thumbnail image is already loaded, reuse the drawable
			iv.setImageBitmap(bitmap);
		}
		else {
			new AsyncTask<Void, Void, Bitmap>() {
				@Override
				protected Bitmap doInBackground(Void... params) {
					if (new File(localThumb).exists()) {
						return ImageUtils.decodeScaleImage(localThumb, 160, 160);
					}
					else {
						return null;
					}
				}

				@Override
				protected void onPostExecute(Bitmap result) {
					super.onPostExecute(result);
					if (result != null) {
						EaseImageCache.getInstance().put(localThumb, result);
						iv.setImageBitmap(result);

					}
					else {
						if (message.status() == EMMessage.Status.FAIL) {
							if (EaseCommonUtils.isNetWorkConnected(activity)) {
								EMClient.getInstance().chatManager().downloadThumbnail(message);
							}
						}

					}
				}
			}.execute();
		}
	}


}
