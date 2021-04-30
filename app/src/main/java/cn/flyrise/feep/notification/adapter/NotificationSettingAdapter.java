package cn.flyrise.feep.notification.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.flyrise.feep.R;
import com.hyphenate.chatui.utils.FeepPushManager;
import cn.flyrise.feep.core.base.views.UISwitchButton;
import cn.flyrise.feep.notification.NotificationSettingActivity;
import cn.flyrise.feep.notification.bean.ItemInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NotificationSettingAdapter extends RecyclerView.Adapter<NotificationSettingAdapter.MyViewHolder> {

	private Context mContext;
	private ArrayList<ItemInfo> mItems;
	private Map<String, Boolean> mNotifyState;

	/**
	 * 是否启用消息推送
	 */
	public NotificationSettingAdapter(Context context, ArrayList<ItemInfo> list, Map<String, Boolean> notificationStatus) {
		this.mContext = context;
		this.mItems = list;
		if (notificationStatus == null) {
			this.mNotifyState = new HashMap<>();
			initNotificationStatus();
		}
		else {
			this.mNotifyState = notificationStatus;
		}
	}

	private void initNotificationStatus() {
		if (mItems == null) {
			return;
		}
		for (ItemInfo item : mItems) {
			mNotifyState.put(item.notificationId, true);
		}
	}

	public ArrayList<ItemInfo> getDataList() {
		return mItems;
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		MyViewHolder viewHolder = new MyViewHolder(
				LayoutInflater.from(mContext).inflate(R.layout.notification_setting_item, parent, false));
		return viewHolder;
	}

	@Override
	public void onBindViewHolder(MyViewHolder holder, int position) {
		final ItemInfo itemInfo = mItems.get(position);
		holder.title.setText(itemInfo.title);
		holder.massage.setText(itemInfo.message);

		Boolean isChecked = mNotifyState.get(itemInfo.notificationId);
		holder.checkBox.setChecked(isChecked == null || isChecked);

		holder.checkBox.setOnCheckedChangeListener((buttonView, isCheckeds) -> {
			if (itemInfo.notificationId.equals(NotificationSettingActivity.OPEN_NOTIFICATION)) {
				updateNotifyState(isCheckeds);
				FeepPushManager.setNotificationOpen(isCheckeds);
				return;
			}
			modifyNotificationStatus(itemInfo.notificationId, isCheckeds);
		});
	}

	private void modifyNotificationStatus(String notificationId, boolean isChecked) {
		mNotifyState.put(notificationId, isChecked);
	}

	public Map<String, Boolean> getCurrentNotificationStatus() {
		return mNotifyState;
	}

	private void updateNotifyState(boolean isOpen) {
		for (String state : mNotifyState.keySet()) {
			boolean cond = !isOpen;
			if (mNotifyState.get(state) == cond) {
				mNotifyState.put(state, isOpen);
			}
		}
		notifyDataSetChanged();
	}

	@Override
	public int getItemCount() {
		return mItems == null ? 0 : mItems.size();
	}

	class MyViewHolder extends RecyclerView.ViewHolder {

		UISwitchButton checkBox;
		TextView title;
		TextView massage;

		public MyViewHolder(View itemView) {
			super(itemView);
			checkBox = (UISwitchButton) itemView.findViewById(R.id.notification_list_item_checkbox);
			title = (TextView) itemView.findViewById(R.id.setting_title);
			massage = (TextView) itemView.findViewById(R.id.setting_message);
		}
	}
}
