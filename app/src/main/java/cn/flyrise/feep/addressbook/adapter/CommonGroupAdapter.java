package cn.flyrise.feep.addressbook.adapter;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import cn.flyrise.android.protocol.model.CommonGroup;
import cn.flyrise.feep.R;

/**
 * @author ZYP
 * @since 2018-03-23 16:24
 */
public class CommonGroupAdapter extends OrgBaseAdapter<CommonGroup> {

	@Override public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			convertView = View.inflate(parent.getContext(), R.layout.item_address_book_position, null);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}
		CommonGroup p = mOrgDatas.get(position);
		if (mDefault != null && TextUtils.equals(p.groupId, mDefault.groupId)) {
			holder.ivChecked.setVisibility(View.VISIBLE);
			holder.tvPosition.setTextColor(parent.getContext().getResources().getColor(R.color.defaultColorAccent));
		}
		else {
			holder.ivChecked.setVisibility(View.INVISIBLE);
			holder.tvPosition.setTextColor(parent.getContext().getResources().getColor(R.color.text_bright_color));
		}
		holder.tvPosition.setText(p.groupName);
		return convertView;
	}
}
