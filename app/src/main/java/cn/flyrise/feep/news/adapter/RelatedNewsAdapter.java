package cn.flyrise.feep.news.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.news.bean.RelatedNews;

/**
 * 类描述：
 * 
 * @author 罗展健
 * @date 2015年4月28日 上午11:06:16
 * @version 1.0
 */
public class RelatedNewsAdapter extends BaseAdapter {

    private List<RelatedNews> relatedNews;
    private final Context           context;

    public RelatedNewsAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return relatedNews == null ? 0 : relatedNews.size();
    }

    @Override
    public Object getItem(int arg0) {
        return relatedNews == null ? null : relatedNews.get(arg0);
    }

    @Override
    public long getItemId(int position) {
        return relatedNews == null ? 0 : position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyViewHolder holder;
        if (convertView == null) {
            holder = new MyViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_related_news, null);
            holder.time = (TextView) convertView.findViewById(R.id.related_item_time);
            holder.title = (TextView) convertView.findViewById(R.id.related_item_title);
            convertView.setTag(holder);
        } else {
            holder = (MyViewHolder) convertView.getTag();
        }

        final RelatedNews news = relatedNews.get(position);
//        FELog.i("dd-->>", "---title:" + news.getTitle() + "--time:" + news.getSendTime());
        holder.time.setText(news.getSendTime());
        holder.title.setText(news.getTitle());
        return convertView;
    }

    private class MyViewHolder {
        public TextView title;
        public TextView time;
    }

    public void refreshList(List<RelatedNews> relatedNews) {
        this.relatedNews = relatedNews;
        this.notifyDataSetChanged();
    }

}
