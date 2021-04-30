package cn.flyrise.feep.retrieval.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import cn.flyrise.feep.R;

/**
 * @author ZYP
 * @since 2018-04-28 14:40
 */
public class RetrievalHeadHolder extends RecyclerView.ViewHolder {

	public TextView mTvHeader;

	public RetrievalHeadHolder(View itemView) {
		super(itemView);
		mTvHeader = itemView.findViewById(R.id.drTvHeader);
	}
}
