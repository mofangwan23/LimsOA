package cn.flyrise.feep.addressbook.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.flyrise.feep.R;
import cn.flyrise.feep.core.common.utils.CommonUtil;

/**
 * @author ZYP
 * @since 2016-12-08 14:06
 */
public abstract class OrgBaseAdapter<T> extends BaseAdapter {

    protected List<T> mOrgDatas;

    protected T mDefault;

    public void setData(List<T> data) {
        this.mOrgDatas = data;
    }

    public void setDefault(T defaultD) {
        this.mDefault = defaultD;
    }

    @Override public int getCount() {
        return CommonUtil.isEmptyList(mOrgDatas) ? 0 : mOrgDatas.size();
    }

    @Override public Object getItem(int position) {
        return CommonUtil.isEmptyList(mOrgDatas) ? null : mOrgDatas.get(position);
    }

    @Override public long getItemId(int position) {
        return position;
    }

    @Override public abstract View getView(int position, View convertView, ViewGroup parent);

    public class ViewHolder {
        public ImageView ivChecked;
        public TextView tvPosition;

        public ViewHolder(View convertView) {
            ivChecked = (ImageView) convertView.findViewById(R.id.ivPositionCheck);
            tvPosition = (TextView) convertView.findViewById(R.id.tvPositionName);
        }
    }
}
