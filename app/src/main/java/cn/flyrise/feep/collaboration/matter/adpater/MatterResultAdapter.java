package cn.flyrise.feep.collaboration.matter.adpater;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.matter.MatterListActivity;
import cn.flyrise.feep.collaboration.matter.MatterViewHolder;
import cn.flyrise.feep.collaboration.matter.model.Matter;
import cn.flyrise.feep.core.common.utils.CommonUtil;

/**
 * @author ZYP
 * @since 2017-05-15 15:40
 */
public class MatterResultAdapter extends MatterListAdapter {

    @Override
    public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MatterViewHolder matterHolder = (MatterViewHolder) holder;
        matterHolder.mLayoutOutTile.setVisibility(View.GONE);

        Matter association = mAssociations.get(position);
        matterHolder.ivCheck.setImageResource(mSelectedAssociations.contains(association)
                ? R.drawable.node_current_icon : R.drawable.no_select_check);
        matterHolder.tvMatterTitle.setText(association.title);
        matterHolder.contentView.setOnClickListener(v -> {
            if (mSelectedAssociations.contains(association)) {
                mSelectedAssociations.remove(association);
                matterHolder.ivCheck.setImageResource(R.drawable.no_select_check);
                if (mItemClickListener != null) {
                    mItemClickListener.onAssociationDelete(association);
                }
            }
            else {
                mSelectedAssociations.add(association);
                matterHolder.ivCheck.setImageResource(R.drawable.node_current_icon);
                if (mItemClickListener != null) {
                    mItemClickListener.onAssociationAdd(association);
                }
            }
        });

        if (position == 0) {
            matterHolder.tvMatterType.setVisibility(View.VISIBLE);
            matterHolder.tvMatterType.setText(getMatterType(association.matterType));
        }
        else {
            Matter preAssociation = mAssociations.get(position - 1);
            if (preAssociation.matterType == association.matterType) {
                matterHolder.tvMatterType.setVisibility(View.GONE);
            }
            else {
                matterHolder.tvMatterType.setVisibility(View.VISIBLE);
                matterHolder.tvMatterType.setText(getMatterType(association.matterType));
            }
        }
    }

    private String getMatterType(int matterType) {
        String matter;
        switch (matterType) {
            case MatterListActivity.MATTER_FLOW:
                matter = CommonUtil.getString(R.string.flow);
                break;
            case MatterListActivity.MATTER_MEETING:
                matter = CommonUtil.getString(R.string.meeting);
                break;
            case MatterListActivity.MATTER_KNOWLEDGE:
                matter = CommonUtil.getString(R.string.knowledge);
                break;
            case MatterListActivity.MATTER_SCHEDULE:
                matter = CommonUtil.getString(R.string.schedule);
                break;
            default:
                matter = CommonUtil.getString(R.string.flow);
                break;
        }
        return matter;
    }

}
