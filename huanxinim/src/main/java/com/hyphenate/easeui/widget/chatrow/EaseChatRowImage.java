package com.hyphenate.easeui.widget.chatrow;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.BitmapUtil;
import cn.flyrise.feep.core.common.utils.PixelUtil;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMFileMessageBody;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.ChatType;
import com.hyphenate.chatui.R;
import com.hyphenate.easeui.adapter.EaseMessageAdapter;
import com.hyphenate.easeui.model.EaseImageCache;
import com.hyphenate.easeui.ui.EaseShowBigImageActivity;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.utils.EaseImageUtils;
import java.io.File;


public class EaseChatRowImage extends EaseChatRowFile {

	protected ImageView imageView;
	private EMImageMessageBody imgBody;

	public static EaseChatRowImage create(ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View view = inflater.inflate(viewType == EaseMessageAdapter.MESSAGE_TYPE_SENT_IMAGE ? R.layout.ease_row_sent_picture :
				R.layout.ease_row_received_picture, parent, false);
		return new EaseChatRowImage(view);
	}

	public EaseChatRowImage(View view) {
		super(view);
	}

	@Override
	protected void findView() {
		tvPercent = (TextView) itemView.findViewById(R.id.percentage);
		imageView = (ImageView) itemView.findViewById(R.id.image);
	}

	@Override
	protected void setUpView() {
		imgBody = (EMImageMessageBody) message.getBody();
		if (message.direct() == EMMessage.Direct.RECEIVE) {
			imageView.setImageResource(R.drawable.ease_default_image);
			if (imgBody.thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.DOWNLOADING ||
					imgBody.thumbnailDownloadStatus() == EMFileMessageBody.EMDownloadStatus.PENDING) {
				setMsgCallBack();
			}
			else {
				progressBar.setVisibility(View.GONE);
				tvPercent.setVisibility(View.GONE);
				String thumbPath = imgBody.thumbnailLocalPath();
				if (!new File(thumbPath).exists()) {
					thumbPath = EaseImageUtils.getThumbnailImagePath(imgBody.getLocalUrl());
				}
				showImageView(thumbPath, imageView, imgBody.getLocalUrl(), message);
			}
			return;
		}

		String filePath = imgBody.getLocalUrl();
		String thumbPath = EaseImageUtils.getThumbnailImagePath(imgBody.getLocalUrl());
		showImageView(thumbPath, imageView, filePath, message);
		handleSendMessage();
	}



	@Override
	protected void onBubbleClick() {
		Intent intent = new Intent(activity, EaseShowBigImageActivity.class);
		File file = new File(imgBody.getLocalUrl());
		if (file.exists()) {
			Uri uri = Uri.fromFile(file);
			intent.putExtra("uri", uri);
		}
		else {
			String msgId = message.getMsgId();
			intent.putExtra("messageId", msgId);
			intent.putExtra("localUrl", imgBody.getLocalUrl());
		}
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
	 * load image into image view
	 * @return the image exists or not
	 */
	private boolean showImageView(final String thumbernailPath, final ImageView iv, final String localFullSizePath,
			final EMMessage message) {
		Bitmap saveBitmap = EaseImageCache.getInstance().get(thumbernailPath);
		if (saveBitmap != null) {
			Bitmap bitmap;
			if (message.direct() == EMMessage.Direct.RECEIVE) {
				bitmap = BitmapUtil.fillet(getBitmap(saveBitmap), true);
			}
			else {
				bitmap = BitmapUtil.fillet(getBitmap(saveBitmap), false);
			}
			iv.setImageBitmap(bitmap);
			return true;
		}
		else {
			new AsyncTask<Object, Void, Bitmap>() {
				@Override
				protected Bitmap doInBackground(Object... args) {
					File file = new File(thumbernailPath);
					if (file.exists()) {
						return EaseImageUtils.decodeScaleImage(thumbernailPath, 350, 350);
					}
					else if (new File(imgBody.thumbnailLocalPath()).exists()) {
						return EaseImageUtils.decodeScaleImage(imgBody.thumbnailLocalPath(), 350, 350);
					}
					else {
						if (message.direct() == EMMessage.Direct.SEND) {
							if (localFullSizePath != null && new File(localFullSizePath).exists()) {
								return EaseImageUtils.decodeScaleImage(localFullSizePath, 350, 350);
							}
							else {
								return null;
							}
						}
						else {
							return null;
						}
					}
				}

				protected void onPostExecute(Bitmap image) {
					if (image != null) {
						FELog.i("EaseChatRowImage", "-->>>>imageView222:--w:" + image.getWidth() + "-->>h:" + image.getHeight());
						Bitmap bt;
						if (message.direct() == EMMessage.Direct.RECEIVE) {
							bt = BitmapUtil.fillet(getBitmap(image), true);
						}
						else {
							bt = BitmapUtil.fillet(getBitmap(image), false);
						}
						if (bt != null) {
							iv.setImageBitmap(bt);
							if (mListener != null) {
								mListener.imageLoadComplete();
							}
						}
						EaseImageCache.getInstance().put(thumbernailPath, image);
					}
					else {
						if (message.status() == EMMessage.Status.FAIL) {
							if (EaseCommonUtils.isNetWorkConnected(activity)) {
								new Thread(() -> {
									EMClient.getInstance().chatManager().downloadThumbnail(message);
								}).start();
							}
						}

					}
				}
			}.execute();

			return true;
		}
	}

	//固定图片宽度
	private Bitmap getBitmap(Bitmap bitmap) {
		final int bitmapWidth = PixelUtil.dipToPx(120);
		return BitmapUtil.fitBitmap(bitmap, bitmapWidth);
	}
}
