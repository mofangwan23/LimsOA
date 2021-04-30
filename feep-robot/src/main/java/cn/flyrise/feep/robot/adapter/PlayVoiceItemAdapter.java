package cn.flyrise.feep.robot.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.robot.R;
import cn.flyrise.feep.robot.entity.RobotResultItem;
import java.util.List;

/**
 * 新建：陈冕;
 * 日期： 2017-12-12-11:00.
 */

public class PlayVoiceItemAdapter extends RecyclerView.Adapter<PlayVoiceItemAdapter.ItemViewHodler> {

    private List<RobotResultItem> mResultItems;
    private int index = 0;
    private OnClickeItemListener mListener;

    public PlayVoiceItemAdapter(List<RobotResultItem> resultItems, OnClickeItemListener mListener) {
        this.mResultItems = resultItems;
        this.mListener = mListener;
    }

    public void setResultItems(List<RobotResultItem> resultItems) {
        this.mResultItems = resultItems;
        notifyDataSetChanged();
    }

    public void setSelected(int index) {
        this.index = index;
        notifyDataSetChanged();
    }

    @Override
    public ItemViewHodler onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHodler(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.robot_play_voice_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(ItemViewHodler holder, int position) {
        RobotResultItem item = mResultItems.get(position);
        if (item == null) {
            return;
        }
        holder.mTvTitle.setText(item.title);
        holder.mImgIcon.setVisibility(index == position ? View.VISIBLE : View.INVISIBLE);
        holder.mLayout.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.clickeItem(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return CommonUtil.isEmptyList(mResultItems) ? 0 : mResultItems.size();
    }

    class ItemViewHodler extends RecyclerView.ViewHolder {

        ImageView mImgIcon;

        TextView mTvTitle;

        View mViewLine;

        LinearLayout mLayout;

        ItemViewHodler(View itemView) {
            super(itemView);
            mLayout = itemView.findViewById(R.id.item_layout);
            mImgIcon = itemView.findViewById(R.id.play_voice_icon);
            mTvTitle = itemView.findViewById(R.id.tv_title);
            mViewLine = itemView.findViewById(R.id.robot_item_line);
        }
    }

    public interface OnClickeItemListener {
        void clickeItem(int index);
    }

}
