package cn.flyrise.feep.particular.views;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.flyrise.android.protocol.model.Reply;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.core.network.entry.AttachmentBean;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import java.util.List;

/**
 * @author ZYP
 * @since 2016-10-26 17:41
 */
public class ReplyRelativeElegantAdapter extends RelativeElegantAdapter<Reply> {

	private boolean showReplyButton;
	private OnAttachmentItemClickListener mAttachmentItemClickListener;
	private OnReplyButtonClickListener mReplyButtonClickListener;
	private final String mHost;

	public ReplyRelativeElegantAdapter(Context context, int layoutId, List<Reply> data) {
		super(context, layoutId, data);
		mHost = CoreZygote.getLoginUserServices().getServerAddress();
	}

	public void showReplyButton(boolean showReplyButton) {
		this.showReplyButton = showReplyButton;
	}

	public void setOnAttachmentItemClickListener(OnAttachmentItemClickListener listener) {
		this.mAttachmentItemClickListener = listener;
	}

	public void setOnReplyButtonClickListener(OnReplyButtonClickListener listener) {
		this.mReplyButtonClickListener = listener;
	}

	@Override
	public void initItemViews(View view, int position, final Reply reply) {
		String userId = reply.getSendUserID();
		String userName = reply.getSendUser();

		TextView tvUserName = view.findViewById(R.id.tvUserName);
		ImageView ivAvatar = view.findViewById(R.id.ivAvatar);
		setUserAvatar(ivAvatar, tvUserName, userId, userName);
		TextView tvReplyTime = view.findViewById(R.id.tvReplyTime);
		tvReplyTime.setText(DateUtil.formatTimeForDetail(reply.getSendTime()));
		tvReplyTime.setVisibility(TextUtils.isEmpty(reply.getSendTime()) ? View.GONE : View.VISIBLE);

		TextView tvReplyTips = view.findViewById(R.id.tvReplyTips);
		tvReplyTips.setText(reply.getTips());
		tvReplyTips.setVisibility(TextUtils.isEmpty(reply.getTips()) ? View.GONE : View.VISIBLE);

		TextView tvReplyAttachment = view.findViewById(R.id.tvReplyAttachment);
		int attachmentSize = CommonUtil.isEmptyList(reply.getAttachments()) ? 0 : reply.getAttachments().size();
		tvReplyAttachment.setVisibility(attachmentSize == 0 ? View.GONE : View.VISIBLE);
		tvReplyAttachment.setText(mContext.getString(R.string.collaboration_attachment) + "(" + attachmentSize + ")");
		tvReplyAttachment.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mAttachmentItemClickListener != null) {
					mAttachmentItemClickListener.onAttachmentItemClick(v, reply.getAttachments());
				}
			}
		});

		View replyBtn = view.findViewById(R.id.layoutReply);
		replyBtn.setVisibility(showReplyButton ? View.VISIBLE : View.GONE);
		replyBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mReplyButtonClickListener != null) {
					mReplyButtonClickListener.onReplyButtonClick(reply.getId());
				}
			}
		});

		EditText etReplyContent = view.findViewById(R.id.etReplyContent);
		etReplyContent.setText(contentReplaceNewline(reply.getContent()));

		etReplyContent.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				((EditText) v).setTextIsSelectable(true);
				return false;
			}
		});

		ImageView handWritingView = view.findViewById(R.id.ivHandWritting);
		if (reply.getWrittenContentHref().size() > 0 && reply.getWrittenContentHref().get(0) != null) {
			AttachmentBean bean = reply.getWrittenContentHref().get(0);
			String href = bean.href;
			if (!TextUtils.isEmpty(href)) {
				handWritingView.setVisibility(View.VISIBLE);
				final String baseUrl = CoreZygote.getLoginUserServices().getServerAddress();
				if ((mContext instanceof Activity && !(((Activity) mContext).isFinishing()))) {
					Glide.with(mContext).load(baseUrl + href)
							.apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.DATA))
							.into(handWritingView);
				}
			}
		}
		else {
			handWritingView.setVisibility(View.GONE);
		}

		List<Reply> subReplies = reply.getSubReplies();
		if (CommonUtil.nonEmptyList(subReplies)) {
			ViewStub viewStub = view.findViewById(R.id.viewStubSubReply);
			View subReplyView = viewStub.inflate();
			subReplyView.findViewById(R.id.tvParticularLabel).setVisibility(View.GONE);
			RelativeElegantLayout subRelativeElegantLayout = subReplyView.findViewById(R.id.relativeElegantLayout);
			ReplyRelativeElegantAdapter subAdapter = new ReplyRelativeElegantAdapter(mContext, mLayoutId, subReplies);
			subAdapter.showReplyButton(false);
			subAdapter.setOnAttachmentItemClickListener(mAttachmentItemClickListener);
			subRelativeElegantLayout.setAdapter(subAdapter);
		}

		LinearLayout bottomLayout = view.findViewById(R.id.layoutReplyItemBottom);
		if (TextUtils.isEmpty(reply.getTips()) && attachmentSize == 0) {
			bottomLayout.setVisibility(View.GONE);
		}

		if (TextUtils.isEmpty(userId)) {
			tvUserName.setTextColor(mContext.getResources().getColor(R.color.text_menu_text_color));
			etReplyContent.setTextColor(mContext.getResources().getColor(R.color.text_bright_color));
		}

		if (TextUtils.isEmpty(reply.getContent())) {
			etReplyContent.setVisibility(View.GONE);
		}
	}

	public interface OnAttachmentItemClickListener {

		void onAttachmentItemClick(View parentView, List<AttachmentBean> attachments);
	}

	public interface OnReplyButtonClickListener {

		void onReplyButtonClick(String replyId);
	}

	private void setUserAvatar(ImageView imageView, TextView tvUserName, final String userId, final String userName) {
		if (TextUtils.isEmpty(userId)) {
			imageView.setVisibility(View.GONE);
			tvUserName.setText(TextUtils.isEmpty(userName) ? mContext.getResources().getString(R.string.admin) : userName);
		}
		else if (TextUtils.equals("0", userId) || TextUtils.equals("1", userId)) {
			imageView.setImageResource(R.drawable.administrator_icon);
			tvUserName.setText(mContext.getResources().getString(R.string.admin));
		}
		else {
			CoreZygote.getAddressBookServices().queryUserDetail(userId)
					.subscribe(f -> {
						if (f != null) {
							FEImageLoader.load(mContext, imageView, mHost + f.imageHref, userId, f.name);
							tvUserName.setText(TextUtils.isEmpty(f.name) ? mContext.getResources().getString(R.string.admin) : f.name);
						}
						else {
							settingUserInfoError(imageView, tvUserName, userName);
						}
					}, error -> {
						settingUserInfoError(imageView, tvUserName, userName);
					});
		}
	}

	private void settingUserInfoError(ImageView imageView, TextView tvUserName, String userName) {
		if (!TextUtils.isEmpty(userName)) {
			tvUserName.setText(userName);
			FEImageLoader.load(mContext, imageView, "", "", userName);
		}
		else {
			imageView.setImageResource(R.drawable.administrator_icon);
			tvUserName.setText(mContext.getResources().getString(R.string.admin));
		}
	}

	private String contentReplaceNewline(String content) {
		if (TextUtils.isEmpty(content)) {
			return content;
		}
		String text = "";
		if (content.contains("<br>")) {
			text = content.replaceAll("<br>", "\n");
		}
		if (!TextUtils.isEmpty(text)) {
			return text;
		}
		return content;
	}

}
