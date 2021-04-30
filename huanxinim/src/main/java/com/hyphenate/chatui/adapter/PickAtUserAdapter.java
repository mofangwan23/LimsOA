package com.hyphenate.chatui.adapter;

import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hyphenate.chatui.R;

import java.util.ArrayList;
import java.util.List;

import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.core.services.model.AddressBook;

/**
 * @author ZYP
 * @since 2017-03-27 13:42
 */
public class PickAtUserAdapter extends BaseAdapter {

    private final String mHost;
    private List<AddressBook> mAddressBook;
    private final SparseArray<Integer> mLetterSelection = new SparseArray<>();  // 英文字母索引
    private final List<String> mLetterList = new ArrayList<>();        //英文字母集合

    public PickAtUserAdapter() {
        this.mHost = CoreZygote.getLoginUserServices().getServerAddress();
    }

    public void setAddressBook(List<AddressBook> addressBook) {
        this.mAddressBook = addressBook;
        this.buildSelection(mAddressBook);
        this.notifyDataSetChanged();
    }

    @Override public int getCount() {
        return CommonUtil.isEmptyList(mAddressBook) ? 0 : mAddressBook.size();
    }

    @Override public Object getItem(int position) {
        return CommonUtil.isEmptyList(mAddressBook) ? null : mAddressBook.get(position);
    }

    @Override public long getItemId(int position) {
        return position;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.ease_row_contact, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        AddressBook addressBook = mAddressBook.get(position);
        holder.tvName.setText(addressBook.name);
        FEImageLoader.load(parent.getContext(), holder.ivAvatar, mHost
                + addressBook.imageHref, addressBook.userId, addressBook.name);
        return convertView;
    }

    private class ViewHolder {
        TextView tvName;
        ImageView ivAvatar;

        public ViewHolder(View itemView) {
            tvName = (TextView) itemView.findViewById(R.id.name);
            ivAvatar = (ImageView) itemView.findViewById(R.id.avatar);
        }
    }

    public int getPositionBySelection(int selection) {
        return mLetterSelection.indexOfKey(selection) >= 0 ? mLetterSelection.get(selection) : -1;
    }

	public List<String> getLetterList() {
		return mLetterList;
	}

	public void buildSelection(List<AddressBook> addressBooks) {
        if (CommonUtil.isEmptyList(addressBooks)) return;
        this.mLetterSelection.clear();
	    this. mLetterList.clear();
        for (int i = 0, n = addressBooks.size(); i < n; i++) {
            AddressBook addressBook = addressBooks.get(i);
            int ch = addressBook.pinyin.charAt(0);
            if (mLetterSelection.indexOfKey(ch) < 0) {  // 保存姓首字母
                mLetterSelection.put(ch, i);
                char letter = addressBook.pinyin.charAt(0);
                mLetterList.add(String.valueOf(letter).toUpperCase());
            }
        }
    }
}
