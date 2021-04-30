package cn.flyrise.feep.robot.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.robot.R;
import java.util.ArrayList;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2017-12-7-10:24.
 */

public class MoreDetailAdapter extends RecyclerView.Adapter<MoreDetailAdapter.ViewHodler> {

	private List<String> datas = new ArrayList<>();

	private OnClickeItemListener mListener;

	public MoreDetailAdapter(OnClickeItemListener mListener) {
		this.mListener = mListener;
	}

	public void setData(List<String> list) {
		this.datas = list;
		notifyDataSetChanged();
	}

	@Override
	public ViewHodler onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.robot_more_detil_item_layout, parent, false);
		return new ViewHodler(view);
	}

	@Override
	public void onBindViewHolder(ViewHodler holder, int position) {
		holder.mTvTitle.setText(datas.get(position));
		holder.view.setOnClickListener(v -> {
			if (mListener != null) {
				mListener.moreDetailClickeItem(datas.get(position));
			}
		});
	}

	@Override
	public int getItemCount() {
		return CommonUtil.isEmptyList(datas) ? 0 : datas.size();
	}

	class ViewHodler extends RecyclerView.ViewHolder {

		TextView mTvTitle;
		View view;

		ViewHodler(View itemView) {
			super(itemView);
			mTvTitle = itemView.findViewById(R.id.text_title);
			view = itemView;
		}
	}

	public interface OnClickeItemListener {

		void moreDetailClickeItem(String title);
	}

}
