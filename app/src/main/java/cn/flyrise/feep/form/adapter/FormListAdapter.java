package cn.flyrise.feep.form.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import cn.flyrise.feep.core.base.views.adapter.FEListAdapter;
import cn.flyrise.android.protocol.model.FormTypeItem;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.image.loader.FEImageLoader;

/**
 * 类功能描述：</br>
 * @author 钟永健
 * @version 1.0</br> 修改时间：2012-9-10</br> 修改备注：</br>
 */
public class FormListAdapter extends FEListAdapter<FormTypeItem> {

    private final Context mContext;
    private final String baseUrl;

    public FormListAdapter(Context context) {
        this.mContext = context;
        this.baseUrl = CoreZygote.getLoginUserServices().getServerAddress();
    }

    @Override
    public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        viewHolder.tvName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        viewHolder.ivHead.setVisibility(View.VISIBLE);
        viewHolder.ivArrow.setVisibility(View.VISIBLE);
        viewHolder.view.setBackgroundResource(R.drawable.listview_item_bg);
        final FormTypeItem item = dataList.get(position);
        viewHolder.view.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(viewHolder.view, item);
            }
        });
        String imageUrl = item.getFormIconUrl();
        String type = item.getFormType();
        viewHolder.tvName.setText(item.getFormName());
        viewHolder.ivHead.setVisibility(TextUtils.equals(type, "0") ? View.GONE : View.VISIBLE);
        FEImageLoader.load(mContext, viewHolder.ivHead, baseUrl + imageUrl, R.drawable.icon_table_fe);
    }

    @Override
    public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.form_list_item, null);
        return new ItemViewHolder(view);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        View view;
        ImageView ivHead;
        TextView tvName;
        ImageView ivArrow;

        public ItemViewHolder(View itemView) {
            super(itemView);
            this.view = itemView;
            this.ivHead = (ImageView) itemView.findViewById(R.id.form_list_item_headimage);
            this.tvName = (TextView) itemView.findViewById(R.id.form_list_item_name);
            this.ivArrow = (ImageView) itemView.findViewById(R.id.form_list_item_arrow);
        }
    }
}


/**
 * ViewHolder holder;
 * LayoutInflater inflater = LayoutInflater.from(mContext);
 * if (convertView == null) {
 * holder = new ViewHolder();
 * convertView = inflater.inflate(R.layout.form_list_item, null);
 * holder.nameTV = (TextView) convertView.findViewById(R.id.form_list_item_name);
 * holder.nameTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
 * holder.headImageIV = (ImageView) convertView.findViewById(R.id.form_list_item_headimage);
 * holder.headImageIV.setVisibility(View.VISIBLE);
 * convertView.findViewById(R.id.form_list_item_arrow).setVisibility(View.VISIBLE);
 * convertView.setTag(holder);
 * }
 * else {
 * holder = (ViewHolder) convertView.getTag();
 * }
 * convertView.setBackgroundResource(R.drawable.listview_item_bg);
 * final FormTypeItem item = formItemInfos.get(position).getFormTypeItem();
 * if (item != null) {
 * holder.nameTV.setText(item.getFormName());
 * String imageUrl = item.getFormIconUrl();
 * String type = item.getFormType();
 * holder.headImageIV.setVisibility(TextUtils.equals(type, "0") ? View.GONE : View.VISIBLE);
 * FEImageLoader.load(mContext, holder.headImageIV, baseUrl + imageUrl, R.drawable.icon_table_fe);
 * }
 * return convertView;
 */
