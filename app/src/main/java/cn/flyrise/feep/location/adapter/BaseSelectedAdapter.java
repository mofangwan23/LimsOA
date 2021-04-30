package cn.flyrise.feep.location.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import cn.flyrise.feep.core.base.views.adapter.BaseRecyclerAdapter;

public class BaseSelectedAdapter extends BaseRecyclerAdapter {

	protected String id;
	protected OnSelectedClickeItemListener mListener;

	public void setListener(OnSelectedClickeItemListener listener) {
		this.mListener = listener;
	}

	@Override
	public int getDataSourceCount() {
		return 0;
	}

	@Override
	public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
	}

	@Override
	public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
		return null;
	}

	//点击了某一个人员，让其选中，刷新数据
	public void setCurrentPosition(String id) {
		this.id = id;
		notifyDataSetChanged();
	}

	public interface OnSelectedClickeItemListener {

		void onSelectedClickeItem(String id, int position);
	}
}
