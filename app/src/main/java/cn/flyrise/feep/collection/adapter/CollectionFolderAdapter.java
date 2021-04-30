package cn.flyrise.feep.collection.adapter;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collection.CollectionFolderFragment;
import cn.flyrise.feep.collection.bean.FavoriteFolder;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import java.util.List;

/**
 * @author ZYP
 * @since 2018-05-21 10:09
 */
public class CollectionFolderAdapter extends BaseAdapter {

	private int mMode;
	private FavoriteFolder mSelectedFolder;
	private List<FavoriteFolder> mDataSources;

	public void setFavoriteFolders(List<FavoriteFolder> favoriteFolders) {
		this.mDataSources = favoriteFolders;
		this.notifyDataSetChanged();
	}

	public CollectionFolderAdapter(int mode) {
		this.mMode = mode;
	}

	@Override public int getCount() {
		return CommonUtil.isEmptyList(mDataSources) ? 0 : mDataSources.size();
	}

	@Override public Object getItem(int position) {
		return CommonUtil.isEmptyList(mDataSources) ? null : mDataSources.get(position);
	}

	@Override public long getItemId(int position) {
		return position;
	}

	@Override public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_collection_folder, parent, false);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}

		FavoriteFolder folder = mDataSources.get(position);
		holder.tvName.setText(folder.favoriteName);
		holder.itemView.setBackgroundColor(folder.isEdit ? Color.WHITE : Color.parseColor("#FCFCFC"));

		if (mMode == CollectionFolderFragment.MODE_DISPLAY) {
			holder.ivCheck.setVisibility(View.GONE);
			holder.ivMore.setVisibility(View.VISIBLE);
		}
		else {
			holder.ivMore.setVisibility(View.GONE);
			holder.ivCheck.setVisibility(View.VISIBLE);

			if (mSelectedFolder != null && TextUtils.equals(mSelectedFolder.favoriteId, folder.favoriteId)) {
				holder.ivCheck.setImageResource(R.drawable.ic_collect_checked);
			}
			else {
				holder.ivCheck.setImageResource(R.drawable.ic_collect_uncheck);
			}
		}
		return convertView;
	}

	public void setSelectedFolder(FavoriteFolder favoriteFolder) {
		this.mSelectedFolder = favoriteFolder;
		this.notifyDataSetChanged();
	}

	public FavoriteFolder getSelectedFolder() {
		return mSelectedFolder;
	}

	private static class ViewHolder {

		private View itemView;
		private TextView tvName;
		private ImageView ivCheck;
		private ImageView ivMore;

		public ViewHolder(View itemView) {
			this.itemView = itemView;
			this.tvName = itemView.findViewById(R.id.tvCollectionName);
			this.ivCheck = itemView.findViewById(R.id.ivCollectionSelect);
			this.ivMore = itemView.findViewById(R.id.ivCollectionMore);
		}
	}
}
