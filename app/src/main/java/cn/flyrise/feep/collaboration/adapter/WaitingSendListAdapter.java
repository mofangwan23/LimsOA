
package cn.flyrise.feep.collaboration.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.model.WaitingSend;
import cn.flyrise.feep.core.base.views.adapter.FEListAdapter;

/**
 * @author ZYP
 * @since 2017-04-26 10:41
 */
public class WaitingSendListAdapter extends FEListAdapter<WaitingSend> {

    private boolean checkState;

    public boolean isCheckState() {
        return checkState;
    }

    public void setCheckState(boolean checkState) {
        this.checkState = checkState;
        if (!checkState) {
            for (WaitingSend waitingSend : dataList) {
                waitingSend.isCheck = false;
            }
        }
        this.notifyDataSetChanged();
    }

    @Override
    public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        WaitingSend waitingSend = dataList.get(position);
        viewHolder.tvTitle.setText(waitingSend.title);
        viewHolder.tvDate.setText(waitingSend.sendTime);
        viewHolder.tvImportant.setText(waitingSend.important);
        viewHolder.checkBox.setVisibility(checkState ? View.VISIBLE : View.GONE);
        viewHolder.checkBox.setChecked(waitingSend.isCheck);
        viewHolder.itemView.setOnClickListener(v -> {
            if (checkState) {
                waitingSend.isCheck = !waitingSend.isCheck;
                viewHolder.checkBox.setChecked(waitingSend.isCheck);
                return;
            }
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(viewHolder.itemView, waitingSend);
            }
        });
        viewHolder.itemView.setOnLongClickListener(v -> {
            if (!checkState) {
                waitingSend.isCheck = true;
            }
            setCheckState(!checkState);
            if (onItemLongClickListener != null) {
                onItemLongClickListener.onItemLongClick(viewHolder.itemView, waitingSend);
            }
            return true;
        });
    }

    @Override
    public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.item_waiting_send, null);
        return new ItemViewHolder(view);
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvDate;
        private TextView tvImportant;
        private CheckBox checkBox;

        public ItemViewHolder(View itemView) {
            super(itemView);
            tvTitle = (TextView) itemView.findViewById(R.id.tvTitle);
            tvDate = (TextView) itemView.findViewById(R.id.tvDate);
            tvImportant = (TextView) itemView.findViewById(R.id.tvImportant);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
        }
    }

    public List<WaitingSend> getDataList() {
        return this.dataList;
    }
}
