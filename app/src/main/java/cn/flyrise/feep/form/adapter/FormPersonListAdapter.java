/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-9-18 上午10:44:12
 */

package cn.flyrise.feep.form.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import cn.flyrise.android.library.view.DeleteButton;
import cn.flyrise.android.library.view.DeleteButton.OnConfirmClickListener;
import cn.flyrise.android.library.view.addressbooklistview.adapter.AddressBookBaseAdapter;
import cn.flyrise.android.library.view.addressbooklistview.been.AddressBookListItem;
import cn.flyrise.android.protocol.model.AddressBookItem;
import cn.flyrise.feep.core.common.X.AddressBookType;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.view.ThreeStateCheckBox;
import cn.flyrise.feep.commonality.view.ThreeStateCheckBox.onCheckStateListener;
import cn.flyrise.feep.form.FormPersonChooseActivity.CheckPersonObject;

/**
 * 类功能描述：</br>
 * @author 钟永健
 * @version 1.0</   br> 修改时间：2012-9-18</br> 修改备注：</br>
 */
public class FormPersonListAdapter extends AddressBookBaseAdapter {

	public static final int ShowListAdapterType = 0x00;

	public static final int CheckedListAdapterType = 0x01;

	private int adapterType = ShowListAdapterType;

	private final Context context;

	private final boolean isMultiSelect;

	private final ArrayList<Boolean> checkStates = new ArrayList<>();

	private ArrayList<AddressBookListItem> addressListItems = new ArrayList<>();

	private final int personType;

	private final CheckPersonObject checkObject;

	class ViewHolder {

		TextView nameTV;

		ThreeStateCheckBox nodeCBox;

		DeleteButton deleteBnt;
	}

	public FormPersonListAdapter(Context context, int adapterType, CheckPersonObject checkObject) {
		this(context, adapterType, false, AddressBookType.Staff, checkObject);
	}

	public FormPersonListAdapter(Context context, int adapterType, boolean isMultiSelect, CheckPersonObject checkObject) {
		this(context, adapterType, isMultiSelect, AddressBookType.Staff, checkObject);
	}

	public FormPersonListAdapter(Context context, int adapterType, boolean isMultiSelect, int personType, CheckPersonObject checkObject) {
		this.context = context;
		this.adapterType = adapterType;
		this.isMultiSelect = isMultiSelect;
		this.personType = personType;
		this.checkObject = checkObject;
	}

	@Override
	public int getCount() {
		if (addressListItems == null) {
			return 0;
		}
		return addressListItems.size();
	}

	@Override
	public AddressBookListItem getItem(int position) {
		return addressListItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.form_list_item, null);
			holder.nameTV = (TextView) convertView.findViewById(R.id.form_list_item_name);
			holder.nodeCBox = (ThreeStateCheckBox) convertView.findViewById(R.id.form_list_item_checkbox);
			holder.deleteBnt = (DeleteButton) convertView.findViewById(R.id.form_list_item_delete);

			convertView.setTag(holder);
		}
		else {
			holder = (ViewHolder) convertView.getTag();
		}
		final AddressBookItem item = addressListItems.get(position).getAddressBookItem();
		if (item != null) {
			holder.nameTV.setText(item.getName());
		}
		convertView.setBackgroundResource(R.drawable.listview_item_bg);
		if (adapterType == ShowListAdapterType) {
			if ((personType == (item != null ? item.getType() : null)) || !isPersonOrPosition()) {
				holder.nodeCBox.setVisibility(View.VISIBLE);
				holder.deleteBnt.setVisibility(View.INVISIBLE);
			}
			else {
				holder.deleteBnt.setVisibility(View.GONE);
				holder.nodeCBox.setVisibility(View.GONE);
			}
			final boolean isChecked = checkStates.get(position);
			holder.nodeCBox.setCheckStateType(isChecked ? ThreeStateCheckBox.ALL_CHECK_STATE_TYPE : ThreeStateCheckBox.NO_CHECK_STATE_TYPE);
			holder.nodeCBox.setOnCheckStateListener(new onCheckStateListener() {
				@Override
				public void onCheckState(View v, int state) {
					checkObject.check(addressListItems.get(position), adapterType);
				}
			});
		}
		else if (adapterType == CheckedListAdapterType) {
			holder.deleteBnt.setVisibility(View.VISIBLE);
			holder.nodeCBox.setVisibility(View.INVISIBLE);
			holder.deleteBnt.setOnConfirmClickListener(new OnConfirmClickListener() {
				@Override
				public void setOnConfirmClickListener(View v) {
					checkObject.check(addressListItems.get(position), adapterType);
				}
			});
		}
		return convertView;
	}

	@Override
	public void refreshAdapter(ArrayList<AddressBookListItem> listDatas) {
		this.addressListItems = listDatas;
		if (adapterType == ShowListAdapterType) {
			initCheckedState();
			/*---判断当前列表数据中是否存在已经选中的数据，有就把它的状态改为true--*/
			if (addressListItems != null) {
				for (int i = 0; i < addressListItems.size(); i++) {
					final AddressBookListItem listItem = listDatas.get(i);
					final ArrayList<AddressBookListItem> checkedItems = checkObject.getCheckedDatas();
					if (checkedItems != null) {
						for (final AddressBookListItem checkedItem : checkedItems) {
							if (checkObject.isEqualsObjects(checkedItem, listItem)) {
								checkStates.set(i, true);
							}
						}
					}
				}
			}
		}
		notifyDataSetChanged();
	}

	private void initCheckedState() {
		if (addressListItems != null) {
			checkStates.clear();
			for (int i = 0; i < addressListItems.size(); i++) {
				checkStates.add(false);
			}
		}
	}

	/**
	 * 选择人员
	 */
	public void checkPerson(AddressBookListItem listItem) {
		final int index = checkObject.containsObject(addressListItems, listItem);
		if (index != -1) {
			final boolean isChecked = checkStates.get(index);
			if (!isMultiSelect) {
				initCheckedState();
			}
			checkStates.set(index, !isChecked);
		}
		else if (checkStates != null) {
			final int stateIndex = checkStates.indexOf(true);
			if (stateIndex != -1) {
				checkStates.set(stateIndex, false);
			}
		}
		notifyDataSetChanged();
	}

	/**
	 * 是否是人员或者岗位
	 */
	private boolean isPersonOrPosition() {
		return personType == AddressBookType.Staff || personType == AddressBookType.Position;
	}

}
