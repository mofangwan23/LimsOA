package cn.flyrise.feep.addressbook.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;

/**
 * @author ZYP
 * @since 2016-12-11 21:58
 */
public class SurnameAdapter extends BaseAdapter {

    private List<String> mSurnames;

    public void notifyChange(List<String> surnames) {
        this.mSurnames = surnames;
        this.notifyDataSetChanged();
    }

    @Override public int getCount() {
        return CommonUtil.isEmptyList(mSurnames) ? 0 : mSurnames.size();
    }

    @Override public Object getItem(int position) {
        return CommonUtil.isEmptyList(mSurnames) ? null : mSurnames.get(position);
    }

    @Override public long getItemId(int position) {
        return position;
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.item_address_book_surname, null);
        }
        ((TextView) convertView).setText(mSurnames.get(position));
        return convertView;
    }
}
