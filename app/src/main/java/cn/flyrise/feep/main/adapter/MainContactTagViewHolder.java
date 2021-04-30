package cn.flyrise.feep.main.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import cn.flyrise.feep.R;

/**
 * @author ZYP
 * @since 2017-02-13 11:11
 */
public class MainContactTagViewHolder extends RecyclerView.ViewHolder {

	public TextView tvTag;
	public View headerLine;

	public MainContactTagViewHolder(View itemView) {
		super(itemView);
		tvTag = (TextView) itemView.findViewById(R.id.tvMainTagName);
		headerLine = itemView.findViewById(R.id.viewSplitLine16);
	}
}
