package cn.flyrise.feep.more.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import cn.flyrise.feep.R;
import cn.flyrise.feep.more.ShareActivity;

public class ShareAdapter extends BaseAdapter {

    private final Context context;
    private final List<Map<String, Object>> listOs;
    int currentIndex = 0;

    public ShareAdapter(Context context, List<Map<String, Object>> listos2) {
        this.context = context;
        this.listOs = listos2;
    }

    @Override
    public int getCount() {
        return listOs.size();
    }

    @Override
    public Object getItem(int position) {
        return listOs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder vh;
        if (convertView == null) {
            vh = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.share_listview_item, null);
            vh.imgIcon = (ImageView) convertView.findViewById(R.id.ivUserIcon);
            vh.tvTitle = (TextView) convertView.findViewById(R.id.share_item_tv);
            vh.shareTv = (TextView) convertView.findViewById(R.id.share_tv);
            vh.shareItemLayout = (RelativeLayout) convertView.findViewById(R.id.share_item_layout);
            vh.tvBottom = (TextView) convertView.findViewById(R.id.share_item_bottom);
            convertView.setTag(vh);
        } else {
            vh = (ViewHolder) convertView.getTag();
        }
        if (listOs == null || listOs.size() <= 0) {
            return convertView;
        }
        if (listOs.get(position) != null) {
            String userName = (String) listOs.get(position).get(ShareActivity.NAME);
            vh.tvBottom.setVisibility(View.VISIBLE);
            vh.tvTitle.setText(userName);
            vh.imgIcon.setImageResource((Integer) listOs.get(position).get(ShareActivity.ICON));
        }
        if (position == (listOs.size() - 1)) {
            vh.shareTv.setVisibility(View.GONE);
        }
        return convertView;
    }

    class ViewHolder {
        ImageView imgIcon;
        TextView tvTitle;
        TextView tvBottom;
        TextView shareTv;
        RelativeLayout shareItemLayout;
    }
}
