package cn.flyrise.feep.core.base.views.adapter;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.feep.core.R;

/**
 * RecyclerView加载状态
 */
public abstract class BaseMessageRecyclerAdapter<T> extends FEListAdapter<T> {

	private final int TYPE_ITEM = 1;// 普通布局
	private final int TYPE_FOOTER = 2;// 脚布局
	private int loadState = 2;// 当前加载状态，默认为加载完成
	public static final int LOADING = 1;// 正在加载
	public static final int LOADING_COMPLETE = 2;// 加载完成
	public static final int LOADING_END = 3;// 加载到底

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == TYPE_FOOTER) {
			return new FootViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.core_refresh_message_bottom_loading
					, parent, false));
		}
		return onChildCreateViewHolder(parent, viewType);
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (!(holder instanceof BaseMessageRecyclerAdapter.FootViewHolder)) {
			onChildBindViewHolder(holder, position);
		}
		else {
			FootViewHolder footViewHolder = (FootViewHolder) holder;
			footViewHolder.layout.setBackgroundColor(getLoadBackgroundColor());
			switch (loadState) {
				case LOADING: // 正在加载
					footViewHolder.pbLoading.setVisibility(View.VISIBLE);
					footViewHolder.tvLoading.setVisibility(View.VISIBLE);
					footViewHolder.llEnd.setVisibility(View.GONE);
					break;

				case LOADING_COMPLETE: // 加载完成
					footViewHolder.pbLoading.setVisibility(View.INVISIBLE);
					footViewHolder.tvLoading.setVisibility(View.INVISIBLE);
					footViewHolder.llEnd.setVisibility(View.GONE);
					break;

				case LOADING_END: // 加载到底
					footViewHolder.pbLoading.setVisibility(View.GONE);
					footViewHolder.tvLoading.setVisibility(View.GONE);
					footViewHolder.llEnd.setVisibility(View.VISIBLE);
					break;

				default:
					break;
			}
		}
	}

	@Override
	public int getItemCount() {
		return getDataSourceCount() + 1;
	}

	@Override
	public int getItemViewType(int position) {
		if (position + 1 == getItemCount()) return TYPE_FOOTER;
		else return TYPE_ITEM;
	}

	public abstract int getDataSourceCount();

	public int getLoadBackgroundColor() {//背景色，部分布局需要透明色
		return Color.parseColor("#ffffff");
	}

	public abstract void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position);

	public abstract RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType);

	private class FootViewHolder extends RecyclerView.ViewHolder {

		ProgressBar pbLoading;
		TextView tvLoading;
		TextView llEnd;
		RelativeLayout layout;

		FootViewHolder(View itemView) {
			super(itemView);
			layout = itemView.findViewById(R.id.layout);
			pbLoading = itemView.findViewById(R.id.ivLoading);
			tvLoading = itemView.findViewById(R.id.tvHint);
			llEnd = itemView.findViewById(R.id.tvNoMore);
		}
	}

	/**
	 * 设置上拉加载状态
	 * @param loadState 0.正在加载 1.加载完成 2.加载到底
	 */
	public void setLoadState(int loadState) {
		this.loadState = loadState;
		notifyDataSetChanged();
	}
}
