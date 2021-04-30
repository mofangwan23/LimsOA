package cn.flyrise.feep.main.message.task;

import static cn.flyrise.feep.utils.Patches.PATCH_APPLICATION_BUBBLE;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import cn.flyrise.android.library.utility.SubTextUtility;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.bean.FEListItem;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.function.AppSubMenu;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.main.message.BaseMessageAdapter;
import cn.flyrise.feep.main.message.MessageListViewHolder;
import cn.flyrise.feep.utils.Patches;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2017-03-30 16:30
 */
public class TaskMessageAdapter extends BaseMessageAdapter<FEListItem> {

	private final String mHost;
	private AppSubMenu menuInfo;

	public TaskMessageAdapter(AppSubMenu menuInfo) {
		mHost = CoreZygote.getLoginUserServices().getServerAddress();
		this.menuInfo = menuInfo;
	}

	public void removeMessage(String messageId) {
		if (CommonUtil.isEmptyList(mDataSource)) return;
		FEListItem item = null;
		int position = -1;
		for (int i = 0; i < mDataSource.size(); i++) {
			item = mDataSource.get(i);
			if (item != null && TextUtils.equals(messageId, item.getId())) {
				position = i;
				break;
			}
		}
		if (position < 0) return;
		mDataSource.remove(item);
		notifyItemRemoved(position);
	}

	public void markupMessageRead(String messageId) {
		if (CommonUtil.isEmptyList(mDataSource)) return;
		FEListItem item = null;
		int position = -1;
		for (int i = 0; i < mDataSource.size(); i++) {
			item = mDataSource.get(i);
			if (item != null && TextUtils.equals(messageId, item.getId())) {
				position = i;
				break;
			}
		}
		if (position < 0) return;
		item.setNews(false);
		notifyItemChanged(position);
	}

	@Override
	public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		MessageListViewHolder messageHolder = (MessageListViewHolder) holder;
		final FEListItem feListItem = mDataSource.get(position);
		messageHolder.tvTitle.setVisibility(View.GONE);
		//7.0以上
		if (FunctionManager.hasPatch(Patches.PATCH_NEW_APPLICATION)) {
			//查找通讯录人的名字
			CoreZygote.getAddressBookServices().queryUserDetail(feListItem.getSendUserId())
					.subscribe(userInfo -> {
						if (userInfo != null) {
							int userId = CommonUtil.parseInt(userInfo.userId);
							if (userId != 0 && userId != 1) {
								messageHolder.tvSystem.setVisibility(View.GONE);
								if (!TextUtils.isEmpty(userInfo.name)) {
									if (!TextUtils.isEmpty(userInfo.position)) {
										messageHolder.llNameNoJob.setVisibility(View.GONE);
										messageHolder.llName.setVisibility(View.VISIBLE);
										messageHolder.tvJob.setText(userInfo.position);
										messageHolder.tvName.setText(userInfo.name);
									}
									else {
										messageHolder.llName.setVisibility(View.GONE);
										messageHolder.llNameNoJob.setVisibility(View.VISIBLE);
										messageHolder.tvNameNoJob.setText(userInfo.name);
									}
									FEImageLoader.load(CoreZygote.getContext(), messageHolder.ivAvatar, mHost + userInfo.imageHref,
											userInfo.userId,
											feListItem.getSendUser());
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
		}
		else {
			messageHolder.llNameNoJob.setVisibility(View.VISIBLE);
			messageHolder.tvSystem.setVisibility(View.GONE);
			if (!TextUtils.isEmpty(feListItem.getSendUser())) {
				messageHolder.tvNameNoJob.setText(feListItem.getSendUser());
			}
			FEImageLoader.load(CoreZygote.getContext(), messageHolder.ivAvatar, mHost + feListItem.getImageHerf(), "10086",
					feListItem.getSendUser());
		}
		String title = feListItem.getTitle();
		if (SubTextUtility.isTextBook(title)) {
			title = SubTextUtility.subTextString(title);
		}
		if (!TextUtils.isEmpty(title)) {
			messageHolder.tvAction.setVisibility(View.VISIBLE);
			messageHolder.tvAction.setText(title);
		}
		else {
			messageHolder.tvAction.setVisibility(View.GONE);
		}

		if (!TextUtils.isEmpty(feListItem.getSendTime())) {
			messageHolder.tvTime.setVisibility(View.VISIBLE);
			messageHolder.tvTime.setText(DateUtil.formatTimeForList(feListItem.getSendTime()));
		}

		if (!TextUtils.isEmpty(feListItem.getImportant())
				&& !"平急".equals(feListItem.getImportant())
				&& !"平件".equals(feListItem.getImportant())) {
			messageHolder.tvImportant.setVisibility(View.VISIBLE);
			messageHolder.tvImportantNoJob.setVisibility(View.VISIBLE);
			messageHolder.tvImportant.setText(feListItem.getImportant());
			messageHolder.tvImportantNoJob.setText(feListItem.getImportant());
		}
		else {
			messageHolder.tvImportant.setVisibility(View.GONE);
			messageHolder.tvImportantNoJob.setVisibility(View.GONE);
		}

		if (feListItem.isNews()) {
			messageHolder.ivMessageState.setVisibility(View.VISIBLE);
			messageHolder.ivMessageState.setImageResource(cn.flyrise.feep.R.drawable.core_badg_spot_background);
		}
		else {
			messageHolder.ivMessageState.setVisibility(View.GONE);
		}

		// 65 环境 不显示消息类型
		if(!FunctionManager.hasPatch(PATCH_APPLICATION_BUBBLE)){
			messageHolder.tvType.setVisibility(View.GONE);
		}else {
			if(!TextUtils.isEmpty(feListItem.getMsgType())){
				messageHolder.tvType.setVisibility(View.VISIBLE);
				messageHolder.tvType.setText(feListItem.getMsgType());
			}
		}

		messageHolder.cardView.setOnClickListener(v -> {
			if (mMessageClickListener != null) {
				mMessageClickListener.onMessageClick(feListItem, position);
			}
		});

		messageHolder.cardView.setOnLongClickListener(v -> {
			if (onItemLongClickListener != null)
				onItemLongClickListener.onItemLongClick(v, feListItem);
			return true;
		});

//        if (TextUtils.isEmpty(feListItem.getLevel())) {
//            messageHolder.tvTitle.setTextColor(Color.parseColor("#04121A"));
//            messageHolder.tvAction.setTextColor(Color.parseColor("#04121A"));
////            messageHolder.tvReadAll.setTextColor(Color.parseColor("#04121A"));
//        }
//        else {
//            messageHolder.tvTitle.setTextColor(Color.RED);
//            messageHolder.tvAction.setTextColor(Color.RED);
////            messageHolder.tvReadAll.setTextColor(Color.RED);
//        }

	}

	@Override public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext())
				.inflate(cn.flyrise.feep.core.R.layout.core_common_msg_list_card, parent, false);
		return new MessageListViewHolder(itemView);
	}

	private void showSystemMessage(MessageListViewHolder messageHolder) {
		messageHolder.ivAvatar.setImageResource(R.mipmap.icon_system_message);
		messageHolder.tvSystem.setVisibility(View.VISIBLE);
		messageHolder.llNameNoJob.setVisibility(View.GONE);
		messageHolder.llName.setVisibility(View.GONE);
	}
}
