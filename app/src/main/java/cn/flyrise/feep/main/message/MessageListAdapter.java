package cn.flyrise.feep.main.message;

import static cn.flyrise.feep.core.common.X.Func.Activity;
import static cn.flyrise.feep.core.common.X.Func.Announcement;
import static cn.flyrise.feep.core.common.X.Func.CRM;
import static cn.flyrise.feep.core.common.X.Func.CircleNotice;
import static cn.flyrise.feep.core.common.X.Func.Done;
import static cn.flyrise.feep.core.common.X.Func.InBox;
import static cn.flyrise.feep.core.common.X.Func.Knowledge;
import static cn.flyrise.feep.core.common.X.Func.Meeting;
import static cn.flyrise.feep.core.common.X.Func.News;
import static cn.flyrise.feep.core.common.X.Func.Plan;
import static cn.flyrise.feep.core.common.X.Func.Salary;
import static cn.flyrise.feep.core.common.X.Func.Schedule;
import static cn.flyrise.feep.core.common.X.Func.Sended;
import static cn.flyrise.feep.core.common.X.Func.ToDo;
import static cn.flyrise.feep.core.common.X.Func.ToSend;
import static cn.flyrise.feep.core.common.X.Func.Trace;
import static cn.flyrise.feep.core.common.X.Func.Vote;
import static cn.flyrise.feep.utils.Patches.PATCH_APPLICATION_BUBBLE;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.X;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.image.loader.FEImageLoader;

/**
 * @author ZYP
 * @since 2017-03-30 17:06
 */
public class MessageListAdapter extends BaseMessageAdapter<MessageVO> {

	private int mExtraCount;

	@Override
	public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		MessageListViewHolder messageHolder = (MessageListViewHolder) holder;
		final MessageVO msg = mDataSource.get(position);
		messageHolder.tvImportant.setVisibility(View.GONE);
		if(msg == null) return;
		String type = msg.getTypeDcrp();
		String action = msg.getAction();
		String title = msg.getTitle();
		String sendTime = msg.getSendTime();
		String readed = msg.getReaded();

		if (!TextUtils.isEmpty(type)) {
			if (type.contains("[")) {
				type = type.replace("[", "");
				type = type.replace("]", "");
			}
			messageHolder.tvType.setText(type);
		}
		else if (!TextUtils.isEmpty(msg.getType())) {
			messageHolder.tvType.setText(getMessageType(Integer.parseInt(msg.getType())));
		}

		//65 环境不显示消息类型
		if (!FunctionManager.hasPatch(PATCH_APPLICATION_BUBBLE)) {
			messageHolder.tvType.setVisibility(View.GONE);
		}

		if (!TextUtils.isEmpty(action)) {
			messageHolder.tvAction.setVisibility(View.VISIBLE);
			messageHolder.tvAction.setText(action);
		}
		else {
			messageHolder.tvAction.setVisibility(View.GONE);
		}

		if (!TextUtils.isEmpty(title)) {
			messageHolder.tvTitle.setVisibility(View.VISIBLE);
			messageHolder.tvTitle.setText(Html.fromHtml(title));
		}
		else {
			messageHolder.tvTitle.setVisibility(View.GONE);
		}

		if (!TextUtils.isEmpty(sendTime)) {
			messageHolder.tvTime.setText(DateUtil.formatTimeForList(sendTime));
		}

		if (!TextUtils.isEmpty(readed)) {
			messageHolder.ivMessageState.setBackgroundResource(R.drawable.core_badg_spot_background);
			messageHolder.ivMessageState.setVisibility(TextUtils.equals(readed, "false") ? View.VISIBLE : View.GONE);
		}
		String userID = msg.getSender();
		CoreZygote.getAddressBookServices().queryUserDetail(userID)
				.subscribe(userInfo -> {
					if (userInfo != null) {
						int userId = CommonUtil.parseInt(userInfo.userId);
						if (userId != 0 && userId != 1) {
							if (!TextUtils.isEmpty(userInfo.position)) {
								messageHolder.llName.setVisibility(View.VISIBLE);
								messageHolder.tvJob.setText(userInfo.position);
							}
							else {
								messageHolder.llNameNoJob.setVisibility(View.VISIBLE);
							}

							if (!TextUtils.isEmpty(userInfo.name)) {
								messageHolder.tvSystem.setVisibility(View.GONE);
								messageHolder.tvNameNoJob.setText(userInfo.name);
								messageHolder.tvName.setText(userInfo.name);
								FEImageLoader.load(CoreZygote.getContext(), messageHolder.ivAvatar,
										CoreZygote.getLoginUserServices().getServerAddress() + userInfo.imageHref,
										userInfo.userId, userInfo.name);
							}
							else {
								showSystemMessage(messageHolder);
							}
						}
						else {
							showSystemMessage(messageHolder);
						}
					}
					else {
						showSystemMessage(messageHolder);
					}
				}, error -> {
					showSystemMessage(messageHolder);
				});

		messageHolder.cardView.setOnClickListener(v -> {
			if (messageHolder.ivMessageState.getVisibility() == View.VISIBLE) {
				messageHolder.ivMessageState.setVisibility(View.INVISIBLE);
			}
			if (mMessageClickListener != null) {
				mMessageClickListener.onMessageClick(msg, position);
			}
		});

		messageHolder.cardView.setOnLongClickListener(v -> {
			if (onItemLongClickListener != null) {
				onItemLongClickListener.onItemLongClick(v, msg);
			}
			return true;
		});
	}


	public void updateMessageState(int position) {
		try {
			mDataSource.get(position).setReaded();
		} catch (Exception exp) {

		}
	}

	public void removeMessageVO(MessageVO messageVO) {
		if (messageVO == null) return;
		mDataSource.remove(messageVO);
		if (mEmptyView != null) {
			mEmptyView.setVisibility(CommonUtil.isEmptyList(mDataSource) ? View.VISIBLE : View.GONE);
		}
		this.notifyDataSetChanged();
	}

	public void addMessageVO(MessageVO messageVO) {
		if (messageVO == null) return;
		if (!mDataSource.contains(messageVO)) {
			mDataSource.add(messageVO);
			this.notifyDataSetChanged();
		}

		if (mEmptyView != null) {
			mEmptyView.setVisibility(CommonUtil.isEmptyList(mDataSource) ? View.VISIBLE : View.GONE);
		}
	}

	public void setExtraCount(int extraCount) {
		this.mExtraCount = extraCount;
	}

	private void showSystemMessage(MessageListViewHolder messageHolder) {
		messageHolder.llName.setVisibility(View.GONE);
		messageHolder.llNameNoJob.setVisibility(View.GONE);
		messageHolder.ivAvatar.setImageResource(R.mipmap.icon_system_message);
		messageHolder.tvSystem.setVisibility(View.VISIBLE);
	}

	@Override
	public boolean needAddFooter(int totalSize) {
		return !CommonUtil.isEmptyList(mDataSource) && mDataSource.size() + mExtraCount < totalSize;
	}

	@Override public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext())
				.inflate(cn.flyrise.feep.core.R.layout.core_common_msg_list_card, parent, false);
		return new MessageListViewHolder(itemView);
	}

	private String getMessageType(int type) {
		String type_str = "";
		switch (type) {
			case ToDo:    // 协同 待办
			case Done:    // 协同 已办
			case Trace:   // 协同 跟踪
			case ToSend:  // 协同 待发
			case Sended:  // 协同 已发
				type_str = "协同";
				break;
			case News: // 新闻
				type_str = "新闻";
				break;
			case Announcement: // 公告
				type_str = "公告";
				break;
			case X.Func.AddressBook:
				type_str = "联系人";
				break;
			case Meeting:
				type_str = "会议";
				break;
			case Plan:
				type_str = "计划";
				break;
			case InBox:// 邮件收件箱.
				type_str = "邮箱";
				break;
			case Knowledge:// 知识管理
				type_str = "文档";
				break;
			case Vote:// 投票管理
				type_str = "投票管理";
				break;
			case Activity:// 活动管理
				type_str = "活动";
				break;
			case Schedule:// 日程管理
				type_str = "日程管理";
				break;
			case CircleNotice:
			case CRM:
				break;
			case Salary:  // 工资
				type_str = "工资";
				break;
		}
		return type_str;
	}

}
