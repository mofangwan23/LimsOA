package cn.flyrise.feep.main.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
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
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * @author ZYP
 * @since 2017-04-01 14:51
 */
public class NewMainConversationListAdapter extends BaseAdapter {

	private List<EMConversation> mConversationLists;

	private Context mContext;

	private OnDragCompeteListener mListener;

	public NewMainConversationListAdapter(Context context) {
		this.mContext = context;
	}

	public void setConversationLists(List<EMConversation> conversationLists) {
		this.mConversationLists = conversationLists;
		this.notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return CommonUtil.isEmptyList(mConversationLists) ? 0 : mConversationLists.size();
	}

	@Override
	public Object getItem(int position) {
		return CommonUtil.isEmptyList(mConversationLists) ? null : mConversationLists.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = View.inflate(mContext, R.layout.view_new_main_message_head_item, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}
		EMConversation conversation = mConversationLists.get(position);
		if (TextUtils.isEmpty(conversation.getExtField())) {
			holder.rootLayout.setBackgroundColor(mContext.getResources().getColor(R.color.all_background_color));
		}
		else
			holder.rootLayout.setBackgroundColor(Color.parseColor("#f3f3f7"));
		String conversationId = conversation.conversationId();

		setAvatar(conversation, conversationId, holder.ivAvatar, holder.tvTitle, holder.tvAtMe);

		int unReadNumber = conversation.getUnreadMsgCount();
		String number = (unReadNumber > 0 && unReadNumber <= 99) ? String.valueOf(unReadNumber) : "99+";
		holder.badgeView.setVisibility(unReadNumber > 0 ? View.VISIBLE : View.INVISIBLE);
		holder.badgeView.setText(number);
		holder.badgeView.setOnDragCompeteListener(new DropCover.OnDragCompeteListener() {
			@Override public void onDrag() {//拖动气泡标记为已读
				if (mListener != null) mListener.onDragCompete(conversation);
			}

			@Override public void onDownDrag(boolean isDownDrag) {
				if (mListener != null) mListener.onDownDrag(isDownDrag);
			}
		});

		if (conversation.getAllMsgCount() != 0) {
			EMMessage lastMessage = conversation.getLastMessage();
			String moduleId = lastMessage.getStringAttribute("type", "");
			String content = lastMessage.getStringAttribute("title", "");
			boolean notShow = lastMessage.getBooleanAttribute(EmChatContent.MESSAGE_ATTR_NOT_SHOW_CONTENT, false);

			//清空聊天消息后，再次登录，环信那边会过滤掉消息数为0的会话，所以我们人为添加一条消息，在消息主界面不显示出来。
			if (notShow) {
				holder.tvTime.setText("");
				holder.tvMessage.setText("");
				holder.tvMessage.setVisibility(View.GONE);
				return convertView;
			}

			holder.tvMessage.setVisibility(View.VISIBLE);
			if (TextUtils.isEmpty(moduleId)) {
				boolean systemMsg = lastMessage.getBooleanAttribute(EaseUiK.EmChatContent.MESSAGE_ATTR_IS_SYSTEM, false);
				if (lastMessage.getChatType() == ChatType.GroupChat && !systemMsg) {
					CoreZygote.getAddressBookServices().queryUserDetail(lastMessage.getFrom())
							.subscribe(addressBook -> {
								showNameMessage(parent.getContext(), addressBook == null ? "" : addressBook.name + ":"
										, lastMessage, holder.tvMessage);
							}, error -> {
								showNameMessage(parent.getContext(), "", lastMessage, holder.tvMessage);
							});
				}
				else {
					showNameMessage(parent.getContext(), "", lastMessage, holder.tvMessage);
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
		return convertView;
	}

	private void setAvatar(EMConversation conversation, String id, ImageView ivAvatar, TextView title, TextView tvAtMe) {

		if (conversation.getType() == EMConversation.EMConversationType.GroupChat) {  // 群聊
			if (!TextUtils.equals(id, (String) ivAvatar.getTag(R.id.ivIcon))) {
				ivAvatar.setTag(R.id.ivIcon, id);
				new ImageSynthesisFatcory.Builder(mContext).setGroupId(id).setImageView(ivAvatar).builder();
			}
			EMGroup group = EMClient.getInstance().groupManager().getGroup(id);
			if (group == null) {
				title.setText(CoreZygote.getConvSTServices().getCoversationName(id));
			}
			else {
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
							if (!TextUtils.equals(id, (String) ivAvatar.getTag(R.id.ivIcon))) {
								ivAvatar.setTag(R.id.ivIcon, id);
								FEImageLoader.load(mContext, ivAvatar, R.drawable.administrator_icon);
							}
							title.setText(id);
						}
						else {
							if (!TextUtils.equals(id, (String) ivAvatar.getTag(R.id.ivIcon))) {
								ivAvatar.setTag(R.id.ivIcon, id);
								String host = CoreZygote.getLoginUserServices().getServerAddress();
								FEImageLoader.load(mContext, ivAvatar, host + f.imageHref, f.userId, f.name);
							}
							title.setText(f.name);
						}
					}, error -> {
						if (!TextUtils.equals(id, (String) ivAvatar.getTag(R.id.ivIcon))) {
							ivAvatar.setTag(R.id.ivIcon, id);
							FEImageLoader.load(mContext, ivAvatar, R.drawable.administrator_icon);
						}
						title.setText(id);
					});
		}
	}

	private void showNameMessage(Context context, String head, EMMessage lastMessage, TextView tv) {
		Spannable message = EaseSmileUtils.getSmallSmiledText(context, head + EaseCommonUtils.getMessageDigest(lastMessage, context));
		tv.setVisibility(TextUtils.isEmpty(message) ? View.GONE : View.VISIBLE);
		tv.setText(message, TextView.BufferType.SPANNABLE);
	}


	public class ViewHolder {

		public ImageView ivAvatar;
		public TextView tvTitle;
		public TextView tvTime;
		RelativeLayout rootLayout;
		WaterDrop badgeView;
		TextView tvMessage;
		TextView tvAtMe;

		public ViewHolder(View itemView) {
			this.rootLayout = itemView.findViewById(R.id.rootLayout);
			this.tvTime = itemView.findViewById(R.id.tvTime);
			this.tvTitle = itemView.findViewById(R.id.tvTitle);
			this.ivAvatar = itemView.findViewById(R.id.ivIcon);
			this.tvMessage = itemView.findViewById(R.id.tvMessage);
			this.tvAtMe = itemView.findViewById(R.id.tvAtMeInGroup);
			this.badgeView = itemView.findViewById(R.id.badge_view);
		}
	}

	public void setOnDragCompeteListener(OnDragCompeteListener listener) {
		this.mListener = listener;
	}

	public interface OnDragCompeteListener {

		void onDragCompete(EMConversation conversation);

		void onDownDrag(boolean isDownDrag);
	}
}
