package cn.flyrise.feep.addressbook.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.core.services.model.AddressBook;
import java.util.List;

/**
 * @author ZYP
 * @since 2017-02-16 23:00
 */
public class OrganizationPersonsAdapter extends BaseAdapter {

	public List<AddressBook> mAddressBooks;
	public String mHost;

	public OrganizationPersonsAdapter(String host) {
		this.mHost = host;
	}

	public void setAddressBooks(List<AddressBook> addressBooks) {
		this.mAddressBooks = addressBooks;
		this.notifyDataSetChanged();
	}

	@Override public int getCount() {
		return CommonUtil.isEmptyList(mAddressBooks) ? 0 : mAddressBooks.size();
	}

	@Override public Object getItem(int position) {
		return CommonUtil.isEmptyList(mAddressBooks) ? 0 : mAddressBooks.get(position);
	}

	@Override public long getItemId(int position) {
		return position;
	}

	@Override public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_organization_persons, parent, false);
			holder = new ViewHolder(convertView);
			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}
		AddressBook addressBook = mAddressBooks.get(position);
		String username = addressBook.name;
		holder.tvUsername.setText(username);
		holder.tvPosition.setText(addressBook.position);

		FEImageLoader.load(parent.getContext(), holder.ivUserIcon, mHost + addressBook.imageHref,
				addressBook.userId, username);
		return convertView;
	}

	private class ViewHolder {

		private TextView tvUsername;
		private TextView tvPosition;
		private ImageView ivUserIcon;

		public ViewHolder(View itemView) {
			tvUsername = (TextView) itemView.findViewById(R.id.tvUserName);
			tvPosition = (TextView) itemView.findViewById(R.id.tvPosition);
			ivUserIcon = (ImageView) itemView.findViewById(R.id.ivUserIcon);
		}
	}
}
