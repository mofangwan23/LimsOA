package com.hyphenate.easeui.widget.chatrow;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMFileMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.ChatType;
import com.hyphenate.chatui.R;
import com.hyphenate.easeui.adapter.EaseMessageAdapter;
import com.hyphenate.easeui.ui.EaseShowBigImageActivity;
import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class EaseChatRowGif extends EaseChatRowFile {

	protected ImageView imageView;
	EMFileMessageBody fileMessageBody;

	private static final int SUCCESS = 10010;
	private static final int ERROR = 10011;
	private static final int PROGRESS = 10012;

	private Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (SUCCESS == msg.what) {
				File file = (File) msg.obj;
				showImageView(file, imageView);
				if (tvPercent != null) {
					tvPercent.setVisibility(View.GONE);
				}
			}
			if (PROGRESS == msg.what) {
				int progress = (Integer) msg.obj;
				if (tvPercent != null) {
					tvPercent.setVisibility(View.VISIBLE);
					tvPercent.setText(progress + "%");
				}
			}
			if (ERROR == msg.what) {
				File file = (File) msg.obj;
				if (file != null && file.exists() && file.isFile())
					file.delete();
				if (tvPercent != null) {
					tvPercent.setVisibility(View.GONE);
				}
			}
		}
	};


	public static EaseChatRowGif create(ViewGroup parent, int viewType) {
		LayoutInflater inflater = LayoutInflater.from(parent.getContext());
		View view = inflater.inflate(viewType == EaseMessageAdapter.MESSAGE_TYPE_SENT_GIF ? R.layout.ease_row_sent_gif :
				R.layout.ease_row_received_gif, parent, false);
		return new EaseChatRowGif(view);
	}

	public EaseChatRowGif(View view) {
		super(view);
	}

	@Override
	protected void findView() {
		tvPercent = (TextView) itemView.findViewById(R.id.percentage);
		imageView = (ImageView) itemView.findViewById(R.id.image);
	}

	@Override
	protected void setUpView() {
		fileMessageBody = (EMFileMessageBody) message.getBody();
		if (imageView != null) {
			imageView.setImageResource(R.drawable.ease_default_image);
		}
		downLoadGif(fileMessageBody);
		if (progressBar != null) {
			progressBar.setVisibility(View.GONE);
		}
	}


	@Override
	protected void onBubbleClick() {
		Intent intent = new Intent(activity, EaseShowBigImageActivity.class);
		File file = new File(fileMessageBody.getLocalUrl());
		if (file.exists()) {
			Uri uri = Uri.fromFile(file);
			intent.putExtra("uri", uri);
		}
		else {
			String msgId = message.getMsgId();
			intent.putExtra("messageId", msgId);
			intent.putExtra("localUrl", fileMessageBody.getLocalUrl());
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
	private void showImageView(final File path, final ImageView mImageView) {
		if (mImageView == null) {
			return;
		}
		RequestBuilder<GifDrawable> requestBuilder = Glide.with(activity).asGif();
		requestBuilder.load(path)
				.apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA).placeholder(R.drawable.ease_default_image))
				.listener(new RequestListener<GifDrawable>() {

					@Override public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<GifDrawable> target,
							boolean isFirstResource) {
						return false;
					}

					@Override
					public boolean onResourceReady(GifDrawable resource, Object model, Target<GifDrawable> target, DataSource dataSource,
							boolean isFirstResource) {
						if (imageView == null) {
							return false;
						}
						if (imageView.getScaleType() != ImageView.ScaleType.FIT_XY) {
							imageView.setScaleType(ImageView.ScaleType.FIT_XY);
						}
						ViewGroup.LayoutParams params = imageView.getLayoutParams();
						int vw = imageView.getWidth() - imageView.getPaddingLeft() - imageView.getPaddingRight();
						float scale = (float) vw / (float) resource.getIntrinsicWidth();
						int vh = Math.round(resource.getIntrinsicHeight() * scale);
						params.height = vh + imageView.getPaddingTop() + imageView.getPaddingBottom();
						imageView.setLayoutParams(params);
						return false;
					}
				}).into(mImageView);
	}

	private void downLoadGif(EMFileMessageBody messageBody) {
		final File file = new File(messageBody.getLocalUrl());
		if (file.exists()) {
			Message ms = myHandler.obtainMessage();
			ms.what = SUCCESS;
			ms.obj = file;
			myHandler.sendMessage(ms);
			return;
		}
		final Map<String, String> maps = new HashMap<>();
		if (!TextUtils.isEmpty(messageBody.getSecret())) {
			maps.put("share-secret", messageBody.getSecret());
		}

		EMClient.getInstance().chatManager().downloadFile(messageBody.getRemoteUrl(), messageBody.getLocalUrl(), maps,
				new EMCallBack() {

					@Override
					public void onSuccess() {
						Message ms = myHandler.obtainMessage();
						ms.what = SUCCESS;
						ms.obj = file;
						myHandler.sendMessage(ms);
					}

					@Override
					public void onProgress(final int progress, String status) {
						Message ms = myHandler.obtainMessage();
						ms.what = PROGRESS;
						ms.obj = progress;
						myHandler.sendMessage(ms);
					}

					@Override
					public void onError(int error, final String msg) {
						Message ms = myHandler.obtainMessage();
						ms.what = ERROR;
						ms.obj = file;
						myHandler.sendMessage(ms);
					}
				});
	}
}
