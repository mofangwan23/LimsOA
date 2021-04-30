package cn.flyrise.feep.main.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import cn.flyrise.feep.R;

/**
 * @author ZYP
 * @since 2017-02-13 11:11
 */
public class MainContactHeaderViewHolder extends RecyclerView.ViewHolder {

	public TextView tvAll;
	public TextView tvGroupChat;
	public TextView tvSubordinates;
	public TextView tvFollow;

	public MainContactHeaderViewHolder(View itemView) {
		super(itemView);
		tvAll = itemView.findViewById(R.id.item_main_contacts_all);
		tvGroupChat = itemView.findViewById(R.id.item_main_contacts_group_chat);
		tvSubordinates = itemView.findViewById(R.id.item_main_contacts_subordinates);
		tvFollow = itemView.findViewById(R.id.item_main_contacts_follow);
	}
}
