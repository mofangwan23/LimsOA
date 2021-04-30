package cn.flyrise.feep.main.adapter;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.PixelUtil;
import cn.flyrise.feep.core.common.utils.PreferencesUtils;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.utils.Patches;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-02-13 10:34
 */
public class MainContactAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	public static final int TYPE_COMPANY = 1;       // 公司
	public static final int TYPE_DEPARTMENT = 2;    // 我的部门
	public static final int TYPE_ALL = 3;           // 全部联系人
	public static final int TYPE_ATTENTION = 4;     // 我的关注
	public static final int TYPE_COMMON_USE = 5;    // 常用联系人
	public static final int TYPE_GROUP_CHAT = 6;    // 群聊
	public static final int TYPE_IM_APPLY_FOR = 7;  // 申请与通知
	public static final int TYPE_CUSTOM_CONTACT = 8;// 客户联系人

	private static final int VIEW_TYPE_HEADER = 1;
	private static final int VIEW_TYPE_TAG = 2;
	private static final int VIEW_TYPE_CONTACT = 3;
	private OnItemClickListener mItemClickListener;
	private OnPartTimeItemClickListener mPartTimeItemClickListener;
	private OnContactTitleClickListener mContactTitleClicklistener;
	private List<MainContactModel> mModels;
	private Context mContext;
	private boolean isShowPartTimeDepartment;
	private boolean hasClick;
	private int itemCount;

	public MainContactAdapter(Context context) {
		this.mContext = context;
	}

	public void setMainContactModels(List<MainContactModel> models) {
		this.mModels = models;
	}

	public void addMainContactModels(List<MainContactModel> models) {
		if (CommonUtil.isEmptyList(models)) {
			return;
		}

		this.mModels.addAll(models);
		this.notifyDataSetChanged();
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.mItemClickListener = listener;
	}

	public void setOnPartTimeDepartmentItemClickLister(OnPartTimeItemClickListener lister) {
		this.mPartTimeItemClickListener = lister;
	}

	public void setIsShowPartTimeDepartment(boolean isShowPartTimeDepartment) {
		this.isShowPartTimeDepartment = isShowPartTimeDepartment;
		hasClick = false;
	}

	public void setContactTitleClicklistener(OnContactTitleClickListener listener) {
		this.mContactTitleClicklistener = listener;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		RecyclerView.ViewHolder holder = null;
		if (viewType == VIEW_TYPE_TAG) {
			View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_contact_tag, parent, false);
			holder = new MainContactTagViewHolder(itemView);
		}
		else if (viewType == VIEW_TYPE_CONTACT) {
			View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_contact, parent, false);
			holder = new MainContactViewHolder(itemView);
		}
		else {
			View item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main_contact_header, parent, false);
			holder = new MainContactHeaderViewHolder(item);
		}
		holder.setIsRecyclable(false);
		return holder;
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		MainContactModel model = mModels.get(position);
		if (TextUtils.equals("header", model.tag)) {
			MainContactHeaderViewHolder headerHolder = (MainContactHeaderViewHolder) holder;
			if (SpUtil.get(PreferencesUtils.HAS_SUBORDINATES, true)) {
				headerHolder.tvSubordinates.setVisibility(View.VISIBLE);
			}
			headerHolder.tvAll.setOnClickListener(v -> {
				mContactTitleClicklistener.onAllContactClick();
			});
			headerHolder.tvGroupChat.setVisibility(FunctionManager.hasPatch(Patches.PATCH_HUANG_XIN) ? View.VISIBLE : View.GONE);
			headerHolder.tvGroupChat.setOnClickListener(v -> {
				mContactTitleClicklistener.onGroupChatClick();
			});
			headerHolder.tvSubordinates.setOnClickListener(v -> {
				mContactTitleClicklistener.onSubordinatesClick();
			});
			headerHolder.tvFollow.setOnClickListener(v -> {
				mContactTitleClicklistener.onFollowClick();
			});
			return;
		}
		if (!TextUtils.isEmpty(model.tag)) {        // tag
			MainContactTagViewHolder tagHolder = (MainContactTagViewHolder) holder;
			tagHolder.headerLine.setVisibility(position == 0 ? View.VISIBLE : View.GONE);
			tagHolder.tvTag.setText(model.tag);
			return;
		}

		MainContactViewHolder contactHolder = (MainContactViewHolder) holder;
		LayoutParams layoutParams = contactHolder.ivIcon.getLayoutParams();
		if (!TextUtils.isEmpty(model.iconUrl)) {
			int size = PixelUtil.dipToPx(45);
			layoutParams.width = size;
			layoutParams.height = size;
			contactHolder.ivIcon.setLayoutParams(layoutParams);
			if (TextUtils.isEmpty(model.userId)) {
				FEImageLoader.load(mContext, contactHolder.ivIcon, model.iconUrl);
			}
			else {
				FEImageLoader.load(mContext, contactHolder.ivIcon, model.iconUrl, model.userId, model.name);
			}
		}
		else {
			int size = PixelUtil.dipToPx(25);
			layoutParams.width = size;
			layoutParams.height = size;
			contactHolder.ivIcon.setLayoutParams(layoutParams);
			FEImageLoader.load(mContext, contactHolder.ivIcon, model.iconRes);
		}

		contactHolder.ivArrow.setVisibility(model.hasArrow ? View.VISIBLE : View.GONE);
		contactHolder.llPartTimeDepartment.setVisibility(model.departmentSize >= 1 ? View.VISIBLE : View.GONE);
		contactHolder.tvName.setText(model.name);

		if (model.hasLongSpliteLine) {
			contactHolder.spliteLineLong.setVisibility(View.VISIBLE);
			contactHolder.spliteLineShort.setVisibility(View.GONE);
			contactHolder.divider.setVisibility(View.GONE);
		}
		else if (model.hasDivider) {
			contactHolder.divider.setVisibility(View.VISIBLE);
			contactHolder.spliteLineLong.setVisibility(View.GONE);
			contactHolder.spliteLineShort.setVisibility(View.GONE);
		}
		else {
			contactHolder.spliteLineShort.setVisibility(View.VISIBLE);
			contactHolder.spliteLineLong.setVisibility(View.GONE);
			contactHolder.divider.setVisibility(View.GONE);
		}

		if (TextUtils.isEmpty(model.subName)) {
			contactHolder.tvSubName.setVisibility(View.GONE);
		}
		else {
			contactHolder.tvSubName.setVisibility(View.VISIBLE);
			contactHolder.tvSubName.setText(model.subName);
		}
		if (isShowPartTimeDepartment) {
			contactHolder.tvExpend.setText(mContext.getString(R.string.contacts_retract_part_time_department));
		}
		else {
			contactHolder.tvExpend.setText(mContext.getString(R.string.contacts_extent_part_time_department));
		}
		ObjectAnimator animator;
		if (hasClick) {
			if (isShowPartTimeDepartment) {
				animator = ObjectAnimator.ofFloat(contactHolder.ivDownUp, "rotation", 0.0f, 180.0f);
			}
			else {
				animator = ObjectAnimator.ofFloat(contactHolder.ivDownUp, "rotation", 180.0f, 360.0f);
			}
			animator.setDuration(500);
			animator.start();
		}
		contactHolder.itemView.setOnClickListener(v -> {
			FELog.i("onClick");
			if (mItemClickListener != null) {
				mItemClickListener.onItemClick(model);
			}
		});

		contactHolder.llPartTimeDepartment.setOnClickListener(v -> {
			if (!isShowPartTimeDepartment) {
				isShowPartTimeDepartment = true;
			}
			else {
				isShowPartTimeDepartment = false;
			}
			mPartTimeItemClickListener.onShowPartTimeDepartmentClick(isShowPartTimeDepartment);
			hasClick = true;
		});

	}

	@Override
	public int getItemCount() {
		return mModels == null ? 0 : mModels.size();
	}

	@Override
	public int getItemViewType(int position) {
		super.getItemViewType(position);
		MainContactModel model = mModels.get(position);
		if (TextUtils.isEmpty(model.tag)) {
			return VIEW_TYPE_CONTACT;
		}
		else if (TextUtils.equals("header", model.tag)) {
			return VIEW_TYPE_HEADER;
		}
		return VIEW_TYPE_TAG;
	}

	public void addData(int position) {
		notifyItemInserted(position);
	}


	public interface OnItemClickListener {

		void onItemClick(MainContactModel model);
	}

	public interface OnPartTimeItemClickListener {

		void onShowPartTimeDepartmentClick(boolean isShow);
	}

	public interface OnContactTitleClickListener {

		void onAllContactClick();

		void onGroupChatClick();

		void onSubordinatesClick();

		void onFollowClick();
	}


	public MainContactModel getItem(int pos) {
		return mModels.get(pos);
	}

}
