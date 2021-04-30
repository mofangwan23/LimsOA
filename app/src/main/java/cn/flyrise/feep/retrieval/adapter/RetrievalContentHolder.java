package cn.flyrise.feep.retrieval.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.R;

/**
 * @author ZYP
 * @since 2018-04-28 14:43
 */
public class RetrievalContentHolder extends RecyclerView.ViewHolder {

	public ImageView ivIcon;
	public TextView tvTitle;
	public TextView tvSubTitle;
	public ViewGroup layoutContent;

	public RetrievalContentHolder(View itemView) {
		super(itemView);
		ivIcon = itemView.findViewById(R.id.drIvIcon);
		tvTitle = itemView.findViewById(R.id.drTvTitle);
		tvSubTitle = itemView.findViewById(R.id.drTvSubTitle);
		layoutContent = itemView.findViewById(R.id.drLayoutContent);
	}
}
