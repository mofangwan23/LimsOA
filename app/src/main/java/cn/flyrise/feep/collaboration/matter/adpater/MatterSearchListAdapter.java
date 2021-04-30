package cn.flyrise.feep.collaboration.matter.adpater;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.matter.MatterListActivity;
import cn.flyrise.feep.collaboration.matter.MatterViewHolder;
import cn.flyrise.feep.collaboration.matter.model.Matter;
import cn.flyrise.feep.core.base.views.adapter.FEListAdapter;

/**
 * @author ZYP
 * @since 2017-05-12 10:43
 */
public class MatterSearchListAdapter extends FEListAdapter<Matter> {

    private List<Matter> selectAssociations;

    public void setSelectAssociations(List<Matter> selectAssociations) {
        this.selectAssociations = selectAssociations;
    }

    @Override
    public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MatterViewHolder matterHolder = (MatterViewHolder) holder;
        Matter association = dataList.get(position);
        matterHolder.tvMatterType.setVisibility(View.GONE);
        if (association.matterType == MatterListActivity.MATTER_KNOWLEDGE) {
            matterHolder.tvMatterTitle.setText(association.title + association.fileType);
        }
        else {
            matterHolder.tvMatterTitle.setText(association.title);
        }
        if (association.matterType == MatterListActivity.MATTER_FLOW) {
            matterHolder.ivUser.setVisibility(View.VISIBLE);
        }
        else {
            matterHolder.ivUser.setVisibility(View.GONE);
        }
        matterHolder.tvMatterSendTime.setText(association.time);
        String sendUser = association.name;
        matterHolder.tvMatterSendUser.setVisibility(TextUtils.isEmpty(sendUser) ? View.GONE : View.VISIBLE);
        if (association.matterType == MatterListActivity.MATTER_MEETING) {
            matterHolder.tvMatterSendUser.setText(association.meetingDeal + "/" + association.name);
        }
        else {
            matterHolder.tvMatterSendUser.setText(association.name);
        }
        matterHolder.ivCheck.setImageResource(selectAssociations.contains(association)
                ? R.drawable.node_current_icon : R.drawable.no_select_check);
        matterHolder.contentView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(matterHolder.ivCheck, association);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_matter_result, parent, false);
        return new MatterViewHolder(convertView);
    }


}
