package cn.flyrise.feep.email.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.android.protocol.model.Mail;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2016/7/11 09:26
 */
public class MailBoxAdapter extends BaseExpandableListAdapter {

	private Context mContext;
	private boolean isDeleteModel;
	private View mEmptyView;
	private String mHost;
	private List<String> mDelMailIds = new ArrayList<>();
	private OnMailItemClickListener mOnItemClickListener;
	private OnMailItemLongClickListener mOnItemLongClickListener;
	private OnDeleteMailSizeChangeListener mDeleteMailSizeChangeListener;

	private List<String> mDateLists;
	private Map<String, List<Mail>> mMailMap;

	public MailBoxAdapter(Context context) {
		this.mContext = context;
		this.mHost = CoreZygote.getLoginUserServices().getServerAddress();
	}

	public void setOnMailItemClickListener(OnMailItemClickListener listener) {
		this.mOnItemClickListener = listener;
	}

	public void setOnMailItemLongClickListener(OnMailItemLongClickListener listener) {
		this.mOnItemLongClickListener = listener;
	}

	public void setOnDeleteMailSizeChangeListener(OnDeleteMailSizeChangeListener listener) {
		this.mDeleteMailSizeChangeListener = listener;
	}

	public void setEmptyView(View emptyView) {
		this.mEmptyView = emptyView;
	}

	public void setDeleteModel(boolean isDeleteModel) {
		mDelMailIds.clear();
		this.isDeleteModel = isDeleteModel;
		this.notifyDataSetChanged();
	}

	public boolean isDeleteModel() {
		return isDeleteModel;
	}

	public void setMailList(List<Mail> mailList) {
		if (mDateLists == null) {
			mDateLists = new ArrayList<>();
		}
		mDateLists.clear();

		if (mMailMap == null) {
			mMailMap = new HashMap<>();
		}
		mMailMap.clear();

		if (CommonUtil.nonEmptyList(mailList)) {
			for (Mail mail : mailList) {
				String date = mail.getDate();
				if (!mDateLists.contains(date)) {
					mDateLists.add(date);
				}

				List<Mail> mails = mMailMap.get(date);
				if (mails == null) {
					mails = new ArrayList<>();
					mails.add(mail);
					mMailMap.put(date, mails);
				}
				else {
					mails.add(mail);
				}
			}
		}
		this.notifyDataSetChanged();
		mEmptyView.setVisibility(CommonUtil.isEmptyList(mDateLists) ? View.VISIBLE : View.GONE);
	}

	public void addMailList(List<Mail> mailList) {
		if (this.mDateLists == null) {
			this.mDateLists = new ArrayList<>();
		}

		if (CommonUtil.nonEmptyList(mailList)) {
			for (Mail mail : mailList) {
				String date = mail.getDate();
				if (!mDateLists.contains(date)) {
					mDateLists.add(date);
				}

				if (mMailMap == null) {
					mMailMap = new HashMap<>();
				}

				List<Mail> mails = mMailMap.get(date);
				if (mails == null) {
					mails = new ArrayList<>();
					mails.add(mail);
					mMailMap.put(date, mails);
				}
				else {
					mails.add(mail);
				}
			}
		}
		this.notifyDataSetChanged();
	}

	public String getDelMailIds() {
		if (this.mDelMailIds == null || this.mDelMailIds.size() == 0) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		int position = 0;
		for (int len = mDelMailIds.size(); position < len - 1; position++) {
			sb.append(mDelMailIds.get(position)).append(",");
		}
		sb.append(mDelMailIds.get(position));
		return sb.toString();
	}

	private void changeMailItemState(ViewHolder boxHolder, String state, int groupPosision, int childPosition) {
		String key = mDateLists.get(groupPosision);
		List<Mail> mails = mMailMap.get(key);
		if (boxHolder.ivState.getVisibility() == View.VISIBLE) {
			if (TextUtils.equals(state, "2")) {
				mails.get(childPosition).status = "0";
			}
			else if (TextUtils.equals(state, "3")) {
				mails.get(childPosition).status = "1";
			}
			notifyDataSetChanged();
		}
	}

	private void setStateAndAttachmentVisible(ViewHolder holder, int stateVisible, int attachmentVisible) {
		holder.ivState.setVisibility(stateVisible);
		holder.ivAttachment.setVisibility(attachmentVisible);
	}

	@Override public int getGroupCount() {
		return CommonUtil.isEmptyList(mDateLists) ? 0 : mDateLists.size();
	}

	@Override public int getChildrenCount(int groupPosition) {
		String key = mDateLists.get(groupPosition);
		if (TextUtils.isEmpty(key)) {
			return 0;
		}

		List<Mail> mails = mMailMap == null ? null : mMailMap.get(key);
		return CommonUtil.isEmptyList(mails) ? 0 : mails.size();
	}

	@Override public Object getGroup(int groupPosition) {
		return CommonUtil.isEmptyList(mDateLists) ? null : mDateLists.get(groupPosition);
	}

	@Override public Object getChild(int groupPosition, int childPosition) {
		String key = CommonUtil.isEmptyList(mDateLists) ? null : mDateLists.get(groupPosition);
		if (key == null) {
			return null;
		}
		List<Mail> mails = mMailMap == null ? null : mMailMap.get(key);
		return CommonUtil.isEmptyList(mails) ? null : mails.get(childPosition);
	}

	@Override public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override public long getChildId(int groupPosition, int childPosition) {
		return groupPosition + childPosition;
	}

	@Override public boolean hasStableIds() {
		return false;
	}

	@Override public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		GroupViewHolder holder;
		if (convertView == null) {
			convertView = View.inflate(parent.getContext(), R.layout.item_mail_box_group, null);
			holder = new GroupViewHolder(convertView);
			convertView.setTag(holder);
		}
		else {
			holder = (GroupViewHolder) convertView.getTag();
		}

		holder.tvDate.setText(mDateLists.get(groupPosition));
		holder.ivGroupIndicator.setImageResource(isExpanded
				? R.drawable.address_tree_department_ex
				: R.drawable.address_tree_department_ec);
		return convertView;
	}

	@Override public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = View.inflate(parent.getContext(), R.layout.item_mail_box_child, null);
			viewHolder = new ViewHolder(convertView);
			convertView.setTag(viewHolder);
		}
		else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		final ViewHolder holder = viewHolder;
		String key = mDateLists.get(groupPosition);
		List<Mail> mails = mMailMap.get(key);
		final Mail mail = mails.get(childPosition);

		if (TextUtils.isEmpty(mail.sendMan)) {
			mail.sendMan = CoreZygote.getLoginUserServices().getUserName();
		}

		if (TextUtils.isEmpty(mail.sendUserId)) {
			mail.sendUserId = CoreZygote.getLoginUserServices().getUserId();
		}

		holder.tvTitle.setText(mail.title);
		holder.tvSender.setText(mail.sendMan);
		holder.tvTime.setText(DateUtil.formatTimeToHm(mail.sendTime));
		holder.tvContent.setText(TextUtils.isEmpty(mail.summary.trim()) ? "" : mail.summary);

		final String mailId = mail.mailId;
		holder.checkBox.setVisibility(isDeleteModel ? View.VISIBLE : View.GONE);
		holder.checkBox.setChecked(mDelMailIds.contains(mailId));

		switch (mail.status) {
			case "0":   // "0":已收带有附件
				setStateAndAttachmentVisible(holder, View.GONE, View.VISIBLE);
				break;
			case "1":   // "1":已收没有附件
				setStateAndAttachmentVisible(holder, View.GONE, View.GONE);
				break;
			case "2":   // "2":未收带有附件
				setStateAndAttachmentVisible(holder, View.VISIBLE, View.VISIBLE);
				break;
			case "3":   // "3":未收没有附件
				setStateAndAttachmentVisible(holder, View.VISIBLE, View.GONE);
				break;
		}

		String userId = mail.sendUserId;
		if (TextUtils.isEmpty(userId) || TextUtils.equals(userId, "-1")) {
			userId = Math.abs(UUID.randomUUID().toString().hashCode()) + "";
			mails.get(childPosition).sendUserId = userId;
		}
		final String id = userId;
		CoreZygote.getAddressBookServices().queryUserDetail(id)
				.subscribe(it -> {
					if (it != null) {
						FEImageLoader.load(mContext, holder.ivAvatar, mHost + it.imageHref, id, mail.sendMan);
					}
					else {
						FEImageLoader.load(mContext, holder.ivAvatar, mHost + "/helloworld", id, mail.sendMan);
					}
				}, error -> {
					FEImageLoader.load(mContext, holder.ivAvatar, mHost + "/helloworld", id, mail.sendMan);
				});

		if (mOnItemClickListener != null) {
			holder.rootView.setOnClickListener(new View.OnClickListener() {
				@Override public void onClick(View v) {
					if (isDeleteModel) {
						if (mDelMailIds.contains(mailId)) {
							mDelMailIds.remove(mailId);
							holder.checkBox.setChecked(false);
						}
						else {
							mDelMailIds.add(mailId);
							holder.checkBox.setChecked(true);
						}

						if (mDeleteMailSizeChangeListener != null) {
							mDeleteMailSizeChangeListener.onDeleteMailSizeChange(mDelMailIds.size());
						}

						return;
					}

					mOnItemClickListener.onMailItemClick(mail);
					changeMailItemState(holder, mail.status, groupPosition, childPosition);
				}
			});
		}

		if (mOnItemLongClickListener != null) {
			holder.rootView.setOnLongClickListener(v -> {
				if (isDeleteModel) {
					return false;
				}
				setDeleteModel(true);
				mDelMailIds.add(mailId);
				holder.checkBox.setChecked(true);
				mOnItemLongClickListener.onMailItemLongClick(mail);
				return true;
			});
		}

		return convertView;
	}

	@Override public boolean isChildSelectable(int groupPosition, int childPosition) {
		return false;
	}

	public class ViewHolder {

		public View rootView;
		public ImageView ivState;
		public ImageView ivAvatar;
		public ImageView ivAttachment;
		public TextView tvSender;
		public TextView tvTime;
		public TextView tvTitle;
		public TextView tvContent;
		public CheckBox checkBox;

		public ViewHolder(View itemView) {
			rootView = itemView;
			ivState = (ImageView) itemView.findViewById(R.id.ivMailState);
			ivAvatar = (ImageView) itemView.findViewById(R.id.ivMailIcon);
			ivAttachment = (ImageView) itemView.findViewById(R.id.ivMailAttachment);
			tvSender = (TextView) itemView.findViewById(R.id.tvMailSender);
			tvTime = (TextView) itemView.findViewById(R.id.tvMailTime);
			tvTitle = (TextView) itemView.findViewById(R.id.tvMailTitle);
			tvContent = (TextView) itemView.findViewById(R.id.tvMailContent);
			checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
		}
	}

	public class GroupViewHolder {

		public TextView tvDate;
		public ImageView ivGroupIndicator;

		public GroupViewHolder(View convertView) {
			tvDate = (TextView) convertView.findViewById(R.id.tvMailSendTime);
			ivGroupIndicator = (ImageView) convertView.findViewById(R.id.ivGroupIndicator);
		}
	}

	public interface OnMailItemClickListener {

		void onMailItemClick(Mail mail);
	}

	public interface OnMailItemLongClickListener {

		void onMailItemLongClick(Mail mail);
	}

	public interface OnDeleteMailSizeChangeListener {

		void onDeleteMailSizeChange(int afterDeleteSize);
	}
}
