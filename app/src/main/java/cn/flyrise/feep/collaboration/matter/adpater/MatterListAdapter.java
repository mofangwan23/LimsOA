package cn.flyrise.feep.collaboration.matter.adpater;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.matter.MatterListActivity;
import cn.flyrise.feep.collaboration.matter.MatterViewHolder;
import cn.flyrise.feep.collaboration.matter.model.Matter;
import cn.flyrise.feep.core.base.views.adapter.BaseRecyclerAdapter;
import cn.flyrise.feep.core.common.utils.CommonUtil;

/**
 * @author ZYP
 * @since 2017-05-12 10:43
 */
public class MatterListAdapter extends BaseRecyclerAdapter {

    protected View mEmptyView;
    protected List<Matter> mAssociations;
    protected OnAssociationCheckChangeListener mItemClickListener;
    protected List<Matter> mSelectedAssociations = new ArrayList<>();

    public void setEmptyView(View emptyView) {
        this.mEmptyView = emptyView;
    }

    public void setOnItemClickListener(OnAssociationCheckChangeListener listener) {
        this.mItemClickListener = listener;
    }

    public void setAssociationList(List<Matter> associations) {
        this.mAssociations = associations;
        this.notifyDataSetChanged();
        if (mEmptyView != null) {
            this.mEmptyView.setVisibility(CommonUtil.isEmptyList(mAssociations) ? View.VISIBLE : View.GONE);
        }
    }

    public void addAssociationList(List<Matter> associations) {
        if (!CommonUtil.isEmptyList(associations)) {
            if (mAssociations == null) {
                this.mAssociations = associations;
            }
            else {
                mAssociations.addAll(associations);
            }
            this.notifyDataSetChanged();
        }
        if (mEmptyView != null) {
            this.mEmptyView.setVisibility(CommonUtil.isEmptyList(mAssociations) ? View.VISIBLE : View.GONE);
        }
    }

    public void addSelectedAssociation(Matter association) {
        if (!mSelectedAssociations.contains(association)) {
            mSelectedAssociations.add(association);
        }
    }


    public void deleteSelectedAssociation(Matter association) {
        if (mSelectedAssociations.contains(association)) {
            mSelectedAssociations.remove(association);
        }
    }

    public List<Matter> getSelectedAssociations() {
        return this.mAssociations;
    }

    public void setSelectedAssociations(List<Matter> associations) {
        if (CommonUtil.nonEmptyList(associations)) {
            mSelectedAssociations.addAll(associations);
            notifyDataSetChanged();
        }
    }


    @Override
    public int getDataSourceCount() {
        return CommonUtil.isEmptyList(mAssociations) ? 0 : mAssociations.size();
    }

    @Override
    public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MatterViewHolder matterHolder = (MatterViewHolder) holder;

        Matter association = mAssociations.get(position);
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
        matterHolder.ivCheck.setImageResource(mSelectedAssociations.contains(association)
                ? R.drawable.node_current_icon : R.drawable.no_select_check);


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
    }

    @Override
    public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_matter_result, parent, false);
        return new MatterViewHolder(convertView);
    }

    public interface OnAssociationCheckChangeListener {

        void onAssociationAdd(Matter association);

        void onAssociationDelete(Matter deletedAssociation);
    }
}
