package cn.flyrise.feep.addressbook.adapter;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.model.Position;

/**
 * @author ZYP
 * @since 2016-12-07 17:08
 */
public class PositionAdapter extends OrgBaseAdapter<Position> {

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
        Position p = mOrgDatas.get(position);
        if (mDefault != null && TextUtils.equals(p.position, mDefault.position)) {
            holder.ivChecked.setVisibility(View.VISIBLE);
            holder.tvPosition.setTextColor(parent.getContext().getResources().getColor(R.color.defaultColorAccent));
        }
        else {
            holder.ivChecked.setVisibility(View.INVISIBLE);
            holder.tvPosition.setTextColor(parent.getContext().getResources().getColor(R.color.text_bright_color));
        }
        holder.tvPosition.setText(p.position);
        return convertView;
    }
}
