package cn.flyrise.feep.main.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.drop.DropCover;
import com.drop.WaterDrop;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessage.ChatType;
import com.hyphenate.chatui.utils.image.ImageSynthesisFatcory;
import com.hyphenate.easeui.EaseUiK;
import com.hyphenate.easeui.EaseUiK.EmChatContent;
import com.hyphenate.easeui.model.EaseAtMessageHelper;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.utils.EaseSmileUtils;
import java.util.List;

public class MainConversationAdapter extends BaseQuickAdapter<EMConversation, MainConversationAdapter.ViewHolder> {

	private OnDragCompeteListener mListener;

	public MainConversationAdapter(int layoutResId, List<EMConversation> conversationLists) {
		super(layoutResId, conversationLists);
	}

	@Override protected void convert(ViewHolder holder, EMConversation item) {
		if (TextUtils.isEmpty(item.getExtField())) {
			holder.rootLayout.setBackgroundColor(mContext.getResources().getColor(R.color.all_background_color));
		}
		else {
			holder.rootLayout.setBackgroundColor(Color.parseColor("#f3f3f7"));
		}
		String conversationId = item.conversationId();

		setAvatar(item, conversationId, holder.ivAvatar, holder.tvTitle, holder.tvAtMe);

		int unReadNumber = item.getUnreadMsgCount();
		String number = (unReadNumber > 0 && unReadNumber <= 99) ? String.valueOf(unReadNumber) : "99+";
		holder.badgeView.setVisibility(unReadNumber > 0 ? View.VISIBLE : View.INVISIBLE);
		holder.badgeView.setText(number);
		holder.badgeView.setOnDragCompeteListener(new DropCover.OnDragCompeteListener() {
			@Override public void onDrag() {//???????????????????????????
				if (mListener != null) mListener.onDragCompete(item);
			}

			@Override public void onDownDrag(boolean isDownDrag) {
				if (mListener != null) mListener.onDownDrag(isDownDrag);
			}
		});

		if (item.getAllMsgCount() != 0) {
			EMMessage lastMessage = item.getLastMessage();
			String moduleId = lastMessage.getStringAttribute("type", "");
			String content = lastMessage.getStringAttribute("title", "");
			boolean notShow = lastMessage.getBooleanAttribute(EmChatContent.MESSAGE_ATTR_NOT_SHOW_CONTENT, false);

			//???????????????????????????????????????????????????????????????????????????0???????????????????????????????????????????????????????????????????????????????????????
			if (notShow) {
				holder.tvTime.setText("");
				holder.tvMessage.setText("");
				holder.tvMessage.setVisibility(View.GONE);
			}

			holder.tvMessage.setVisibility(View.VISIBLE);
			if (TextUtils.isEmpty(moduleId)) {
				boolean systemMsg = lastMessage.getBooleanAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_IS_SYSTEM, false);
				if (lastMessage.getChatType() == ChatType.GroupChat && !systemMsg) {
					CoreZygote.getAddressBookServices().queryUserDetail(lastMessage.getFrom())
							.subscribe(addressBook -> {
								showNameMessage(CoreZygote.getContext(), addressBook == null ? "" : addressBook.name + ":"
										, lastMessage, holder.tvMessage);
							}, error -> {
								showNameMessage(CoreZygote.getContext(), "", lastMessage, holder.tvMessage);
							});
				}
				else {
					showNameMessage(CoreZygote.getContext(), "", lastMessage, holder.tvMessage);
				}
			}
			else {
				holder.tvMessage.setVisibility(TextUtils.isEmpty(content) ? View.GONE : View.VISIBLE);
				holder.tvMessage.setText(content);
			}
			holder.tvTime.setText(DateUtil.formatTimeForList(lastMessage.getMsgTime()));
		}
		else {
			holder.tvTime.setText("");
			holder.tvMessage.setText("");
			holder.tvMessage.setVisibility(View.GONE);
		}
	}

	private void setAvatar(EMConversation conversation, String id, ImageView ivAvatar, TextView title, TextView tvAtMe) {
		if (conversation.getType() == EMConversation.EMConversationType.GroupChat) {  // ??????
			if (!TextUtils.equals(id, (String) ivAvatar.getTag(R.id.ivIcon))) {
				ivAvatar.setTag(R.id.ivIcon, id);
				new ImageSynthesisFatcory.Builder(mContext).setGroupId(id).setImageView(ivAvatar).builder();
			}
			EMGroup group = EMClient.getInstance().groupManager().getGroup(id);
			if (group == null && CoreZygote.getConvSTServices() != null) {
				title.setText(CoreZygote.getConvSTServices().getCoversationName(id));
			}
			else if (group != null && !TextUtils.isEmpty(group.getGroupName())) {
				title.setText(group.getGroupName());
				EaseCommonUtils.saveConversationToDB(group.getGroupId(), group.getGroupName());
			}
			boolean hasAtMeMsg = EaseAtMessageHelper.get().hasAtMeMsg(id);
			tvAtMe.setVisibility(hasAtMeMsg ? View.VISIBLE : View.GONE);
		}
		else {
			tvAtMe.setVisibility(View.GONE);
			CoreZygote.getAddressBookServices().queryUserDetail(id)
					.subscribe(f -> {
						if (f == null) {
							FEImageLoader.load(mContext, ivAvatar, R.drawable.administrator_icon);
							title.setText(id);
						}
						else {
							String host = CoreZygote.getLoginUserServices().getServerAddress();
							FEImageLoader.load(mContext, ivAvatar, host + f.imageHref, f.userId, f.name);
							title.setText(f.name);
						}
					}, error -> {
						FEImageLoader.load(mContext, ivAvatar, R.drawable.administrator_icon);
						title.setText(id);
					});
		}
	}

	private void showNameMessage(Context context, String head, EMMessage lastMessage, TextView tv) {
		Spannable message = EaseSmileUtils.getSmallSmiledText(context, head + EaseCommonUtils.getMessageDigest(lastMessage, context));
		tv.setVisibility(TextUtils.isEmpty(message) ? View.GONE : View.VISIBLE);
		tv.setText(message, TextView.BufferType.SPANNABLE);
	}

	public class ViewHolder extends BaseViewHolder {

		public ImageView ivAvatar;
		public TextView tvTitle;
		public TextView tvTime;
		RelativeLayout rootLayout;
		WaterDrop badgeView;
		TextView tvMessage;
		TextView tvAtMe;

		public ViewHolder(View view) {
			super(view);
			this.rootLayout = itemView.findViewById(R.id.rootLayout);
			this.tvTime = itemView.findViewById(R.id.tvTime);
			this.tvTitle = itemView.findViewById(R.id.tvTitle);
			this.ivAvatar = itemView.findViewById(R.id.ivIcon);
			this.tvMessage = itemView.findViewById(R.id.tvMessage);
			this.tvAtMe = itemView.findViewById(R.id.tvAtMeInGroup);
			this.badgeView = itemView.findViewById(R.id.badge_view);
		}
	}

	@Override public long getItemId(int position) {
		return position;
	}

	public void setOnDragCompeteListener(OnDragCompeteListener listener) {
		this.mListener = listener;
	}

	public interface OnDragCompeteListener {

		void onDragCompete(EMConversation conversation);

		void onDownDrag(boolean isDownDrag);
	}
}
