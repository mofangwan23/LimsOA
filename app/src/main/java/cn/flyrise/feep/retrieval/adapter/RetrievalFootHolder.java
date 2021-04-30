package cn.flyrise.feep.retrieval.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.flyrise.feep.R;

/**
 * @author ZYP
 * @since 2018-04-28 14:42
 */
public class RetrievalFootHolder extends RecyclerView.ViewHolder {

	public TextView mTvSearchMore;
	public ViewGroup mLayoutSearchMore;

	public RetrievalFootHolder(View itemView) {
		super(itemView);
		mTvSearchMore = itemView.findViewById(R.id.drTvSearchMore);
		mLayoutSearchMore = itemView.findViewById(R.id.drLayoutSearchMore);
	}
}
