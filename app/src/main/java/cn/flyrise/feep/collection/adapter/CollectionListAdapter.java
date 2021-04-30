package cn.flyrise.feep.collection.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collection.bean.Favorite;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.views.adapter.BaseRecyclerAdapter;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.X.Func;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.component.LargeTouchCheckBox;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.media.common.FileCategoryTable;
import java.util.ArrayList;
import java.util.List;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2018-05-21 11:35
 */
public class CollectionListAdapter extends BaseRecyclerAdapter {

	private static final int VIEW_TYPE_COMMON = 10;
	private static final int VIEW_TYPE_FILES = 11;

	private final String mHost;
	private View mEmptyView;
	private List<Favorite> mDataSources;
	private List<Favorite> clickFavoriteList;
	private OnItemClickListener mItemClickListener;
	private OnCheckBoxClickListener mCheckBoxClickListener;
	private OnChoiceListener mChoiceListener;
	private boolean canChoice;
	private Context mContext;

	public CollectionListAdapter(Context context) {
		this.mContext= context;
		mHost = CoreZygote.getLoginUserServices().getServerAddress();
		clickFavoriteList = new ArrayList<>();
	}

	public void setEmptyView(View emptyView) {
		this.mEmptyView = emptyView;
	}

	public void setDataSources(List<Favorite> favorites) {
		this.mDataSources = favorites;
		if (mEmptyView != null) {
			mEmptyView.setVisibility(CommonUtil.isEmptyList(mDataSources) ? View.VISIBLE : View.GONE);
		}
		this.notifyDataSetChanged();
	}

	public void setCanChoice(boolean canChoice) {
		this.canChoice = canChoice;
		if (!canChoice) {
			clickFavoriteList.clear();
			mChoiceListener.onChoiceListener(clickFavoriteList);
			for (Favorite item : mDataSources) {
				item.isChoice = false;
			}
			FELog.d("tag", "mDataSources: " + mDataSources);
		}
		notifyDataSetChanged();
	}

	public List<Favorite> getSelectionFavoriteList() {
		return clickFavoriteList;
	}

	public boolean canChoice() {
		return canChoice;
	}

	public void appendDataSources(List<Favorite> favorites) {
		if (CommonUtil.isEmptyList(mDataSources)) {
			mDataSources = new ArrayList<>();
		}

		if (CommonUtil.nonEmptyList(favorites)) {
			mDataSources.addAll(favorites);
		}

		if (mEmptyView != null) {
			mEmptyView.setVisibility(CommonUtil.isEmptyList(mDataSources) ? View.VISIBLE : View.GONE);
		}
		this.notifyDataSetChanged();
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		mItemClickListener = listener;
	}

	public void setOnCheckBoxClickListener(OnCheckBoxClickListener listener) {
		mCheckBoxClickListener = listener;
	}

	public void setOnChoiceListener(OnChoiceListener listener) {
		mChoiceListener = listener;
	}

	@Override
	public int getDataSourceCount() {
		return CommonUtil.isEmptyList(mDataSources) ? 0 : mDataSources.size();
	}

	@Override
	public int getItemViewType(int position) {
		Favorite favorite = mDataSources.get(position);
		if (favorite != null) {
			int favoriteType = CommonUtil.parseInt(favorite.type);
			if (favoriteType == Func.Knowledge) {
				return VIEW_TYPE_FILES;
			}
			else if (favoriteType == Func.Done
					|| favoriteType == Func.Sended
					|| favoriteType == Func.News
					|| favoriteType == Func.Announcement
					|| favoriteType == Func.Plan) {
				return VIEW_TYPE_COMMON;
			}
		}

		return super.getItemViewType(position);
	}

	@Override
	public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		Favorite favorite = mDataSources.get(position);

		if (holder instanceof ViewHolder) {
			ViewHolder vHolder = (ViewHolder) holder;
			vHolder.tvTitle.setText(favorite.title);
			CoreZygote.getAddressBookServices().queryUserDetail(favorite.userId)
					.subscribe(it -> {
						if (it != null) {
							FEImageLoader.load(mContext, vHolder.ivAvatar, mHost + it.imageHref, it.userId, it.name);
						}
						else {
							vHolder.ivAvatar.setImageResource(R.drawable.administrator_icon);
						}
					}, error -> {
						vHolder.ivAvatar.setImageResource(R.drawable.administrator_icon);
					});

			if (!TextUtils.isEmpty(favorite.userName)) {
				vHolder.tvUserName.setVisibility(View.GONE);
				vHolder.tvUserName.setText(favorite.userName);
			}
			else {
				vHolder.tvUserName.setVisibility(View.VISIBLE);
			}
			String publishTime = favorite.publishTime;
			vHolder.tvTime.setText(publishTime);
			//TODO
//            boolean isCanChoice = isCanChoice();
			if (canChoice) {
				vHolder.checkBox.setVisibility(View.VISIBLE);
				vHolder.checkBox.setChecked(favorite.isChoice);
			}
			else {
				vHolder.checkBox.setVisibility(View.GONE);
			}

			vHolder.cardView.setOnClickListener(v -> {
				if (mItemClickListener != null && !canChoice) {
					mItemClickListener.onItemClick(favorite);
				}
				else {
					onCheckBoxClickEvent(favorite, vHolder);
				}
			});
			vHolder.cardView.setOnLongClickListener(v -> {
				onCheckBoxClickEvent(favorite, holder);
				if (onItemLongClickListener != null) {
					onItemLongClickListener.onItemLongClick(vHolder.checkBox, favorite);
					return true;
				}
				return false;
			});

			vHolder.checkBox.setOnClickListener(v -> {
				onCheckBoxClickEvent(favorite, vHolder);
			});

		}
		else {
			FileViewHolder fHolder = (FileViewHolder) holder;
			fHolder.tvFileName.setText(favorite.title);
			FEImageLoader.load(CoreZygote.getContext(), fHolder.ivFileIcon,
					FileCategoryTable.getIcon(FileCategoryTable.getType(favorite.title)));
			fHolder.tvFileTime.setText(DateUtil.formatTimeForList(favorite.publishTime));
			fHolder.tvFileSize.setText(favorite.fileSize);
			fHolder.cardView.setOnClickListener(v -> {
				if (mItemClickListener != null && !canChoice) {
					mItemClickListener.onItemClick(favorite);
				}
				else {
					onCheckBoxClickEvent(favorite, fHolder);
				}
			});
			if (canChoice) {
				fHolder.checkBox.setVisibility(View.VISIBLE);
				fHolder.checkBox.setChecked(favorite.isChoice);
			}
			else {
				fHolder.checkBox.setVisibility(View.GONE);
			}
			fHolder.cardView.setOnLongClickListener(v -> {
				onCheckBoxClickEvent(favorite, holder);
				if (onItemLongClickListener != null) {
					onItemLongClickListener.onItemLongClick(fHolder.cardView, favorite);
					return true;
				}
				return false;
			});

			fHolder.checkBox.setOnClickListener(v -> {
				onCheckBoxClickEvent(favorite, fHolder);
			});

			if (canChoice)
				fHolder.checkBox.setVisibility(View.VISIBLE);
			else
				fHolder.checkBox.setVisibility(View.GONE);
		}


	}


	@Override
	public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
		if (VIEW_TYPE_FILES == viewType) {
			View contentView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_collection_list_file, parent, false);
			return new FileViewHolder(contentView);
		}
		else {
			View contentView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_collection_list, parent, false);
			return new ViewHolder(contentView);
		}
	}

	public class ViewHolder extends RecyclerView.ViewHolder {

		private View cardView;
		private LargeTouchCheckBox checkBox;
		private TextView tvTitle;
		private TextView tvTime;
		private TextView tvUserName;
		private ImageView ivAvatar;

		public ViewHolder(View itemView) {
			super(itemView);
			cardView = itemView.findViewById(R.id.layoutContentView);
			checkBox = itemView.findViewById(R.id.checkbox);
			tvTitle = itemView.findViewById(R.id.tvCollectionTitle);
			tvTime = itemView.findViewById(R.id.tvCollectionTime);
			tvUserName = itemView.findViewById(R.id.tvUserName);
			ivAvatar = itemView.findViewById(R.id.ivCollectionAvatar);
		}
	}

	public class FileViewHolder extends RecyclerView.ViewHolder {

		private View cardView;
		private LargeTouchCheckBox checkBox;
		private ImageView ivFileIcon;
		private TextView tvFileName;
		private TextView tvFileSize;
		private TextView tvFileTime;

		public FileViewHolder(View itemView) {
			super(itemView);
			cardView = itemView.findViewById(R.id.layoutContent);
			checkBox = itemView.findViewById(R.id.fileCheckbox);
			ivFileIcon = itemView.findViewById(R.id.ivFileType);
			tvFileName = itemView.findViewById(R.id.tvFileName);
			tvFileSize = itemView.findViewById(R.id.tvFileSize);
			tvFileTime = itemView.findViewById(R.id.tvFileTime);
		}
	}

	public interface OnItemClickListener {

		void onItemClick(Favorite favorite);
	}

	public interface OnCheckBoxClickListener {

		void onCheckBoxClickListener(List<Favorite> favoriteList);
	}

	public interface OnChoiceListener {

		void onChoiceListener(List<Favorite> favoriteList);
	}

	private void onCheckBoxClickEvent(Favorite favorite, RecyclerView.ViewHolder holder) {
		boolean isChoice = !favorite.isChoice;
		favorite.isChoice = isChoice;
		if (isChoice) {
			clickFavoriteList.add(favorite);
		}
		else {
			clickFavoriteList.remove(favorite);
		}
		if (holder instanceof ViewHolder) {
			ViewHolder vHolder = (ViewHolder) holder;
			vHolder.checkBox.setChecked(isChoice);
		}
		else {
			FileViewHolder fHolder = (FileViewHolder) holder;
			fHolder.checkBox.setChecked(isChoice);
		}
		if (mCheckBoxClickListener != null) {
			mCheckBoxClickListener.onCheckBoxClickListener(clickFavoriteList);
		}
	}

}
