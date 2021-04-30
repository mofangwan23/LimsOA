package cn.flyrise.feep.workplan7.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.views.adapter.FEListAdapter;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.utils.RandomSources;
import cn.flyrise.feep.workplan7.model.WorkPlanListItemBean;
import java.util.List;

/**
 * @author CWW
 * @version 1.0 <br/>
 *          创建时间: 2013-3-27 上午9:52:44 <br/>
 *          类说明 :
 */
public class WorkPlanSearchAdapter extends FEListAdapter<WorkPlanListItemBean> {

    private final Context context;
    private String userId;

    public WorkPlanSearchAdapter(Context context) {
        this.context = context;
        userId = CoreZygote.getLoginUserServices().getUserId();
    }

    @Override
    public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        WorkPlanListItemBean itemBean = dataList.get(position);
        viewHolder.layout.setBackgroundColor(context.getResources().getColor(R.color.all_background_color));
        String iconWeek = DateUtil.getMMWeek(context, itemBean.getSendTime());
        String id = itemBean.getId();
        String sendUserId = itemBean.getSendUserId();
        if (!TextUtils.isEmpty(id)) {
            viewHolder.tvIconName.setBackgroundResource(RandomSources.getSourceById(id));
        } else {
            viewHolder.tvIconName.setBackgroundResource(R.drawable.fe_listview_item_icon_bg_e);
        }
        if (!TextUtils.isEmpty(iconWeek)) {
            viewHolder.tvIconName.setText(iconWeek);
        } else {
            viewHolder.tvIconName.setText(R.string.other);
        }
        viewHolder.view.setBackgroundResource(R.drawable.listview_item_bg);
        viewHolder.tvTitle.setText(itemBean.getTitle());
        viewHolder.tvName.setText(itemBean.getSendUser());
        viewHolder.tvTime.setText(DateUtil.formatTimeForList(itemBean.getSendTime()));
        viewHolder.ivArrow.setVisibility(View.GONE);
        viewHolder.tvName.setVisibility(View.VISIBLE);
        viewHolder.tvTime.setVisibility(View.VISIBLE);
        if (TextUtils.isEmpty(itemBean.getBadge())) {
            viewHolder.layoutNum.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.layoutNum.setVisibility(View.VISIBLE);
        }
        if (!TextUtils.isEmpty(itemBean.getContent())) {
            viewHolder.tvContent.setVisibility(View.VISIBLE);
            viewHolder.tvContent.setText(itemBean.getContent());
        } else {
            viewHolder.tvContent.setVisibility(View.GONE);
        }
        final String status = itemBean.getStatus();
        if ("0".equals(status)) {
            viewHolder.ivRead.setVisibility(View.VISIBLE);
        } else if ("1".equals(status)) {
            viewHolder.ivRead.setVisibility(View.GONE);
        }
        if (!TextUtils.isEmpty(sendUserId) && sendUserId.equals(userId))
            viewHolder.ivRead.setVisibility(View.GONE);
        viewHolder.view.setOnClickListener(v -> {
            if (onItemClickListener != null)
                onItemClickListener.onItemClick(viewHolder.view, itemBean);
        });
    }

    @Override
    public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.fe_list_item, null);
        return new ItemViewHolder(convertView);
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView tvIconName;
        TextView tvTitle;
        TextView tvName;
        TextView tvTime;
        ImageView ivRead;
        ImageView ivArrow;
        TextView tvContent;
        RelativeLayout layout;
        LinearLayout layoutNum;

        ItemViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            tvIconName = (TextView) itemView.findViewById(R.id.category_name);
            tvTitle = (TextView) itemView.findViewById(R.id.fe_list_item_title);
            tvName = (TextView) itemView.findViewById(R.id.fe_list_item_name);
            tvTime = (TextView) itemView.findViewById(R.id.fe_list_item_time);
            ivRead = (ImageView) itemView.findViewById(R.id.read);
            ivArrow = (ImageView) itemView.findViewById(R.id.fe_list_item_icon_arrow);
            tvContent = (TextView) itemView.findViewById(R.id.fe_list_item_content);
            layout = (RelativeLayout) itemView.findViewById(R.id.relativeLayout1);
            layoutNum = (LinearLayout) itemView.findViewById(R.id.fe_list_item_nums_layout);
        }

    }

    public void addDataList(List<WorkPlanListItemBean> dataList) {
        String SectionName;
        if (this.dataList.size() != 0) {
            SectionName = this.dataList.get(this.dataList.size() - 1).getSectionName();
            if (dataList != null && dataList.size() != 0) {
                if (SectionName.equals(dataList.get(0).getSectionName())) {
                    dataList.remove(0);
                }
                this.dataList.addAll(dataList);
            }
        } else {
            this.dataList.addAll(dataList);
        }
        this.notifyDataSetChanged();
    }

    public void markReplay(String id) {
        if (id == null) {
            return;
        }
        final int size = dataList.size();
        for (int i = 0; i < size; i++) {
            if (id.equals(dataList.get(i).getId())) {
                dataList.get(i).setStatus("1");
                this.notifyDataSetChanged();
                break;
            }
        }
    }

}
