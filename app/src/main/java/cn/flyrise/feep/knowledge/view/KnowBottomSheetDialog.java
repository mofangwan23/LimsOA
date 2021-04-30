package cn.flyrise.feep.knowledge.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.List;

import cn.flyrise.feep.R;

/**
 * Created by KLC on 2016/12/6.
 */

public class KnowBottomSheetDialog extends android.support.design.widget.BottomSheetDialog {

    private Context mContext;
    private List<String> mDataList;
    private RecyclerView recyclerView;
    private ItemOnClickListener mClickListener;

    public KnowBottomSheetDialog(@NonNull Context context, List<String> dataList, ItemOnClickListener clickListener) {
        super(context);
        this.mContext = context;
        this.mDataList = dataList;
        this.mClickListener = clickListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findView();
        bindData();
        Window win = getWindow();
        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = win.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        win.setAttributes(lp);
    }

    private void findView() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.know_popwindow, null);
        setContentView(view);
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void bindData() {
        recyclerView.setAdapter(new ItemAdapter(mDataList, mClickListener, this));
    }


    private class ItemAdapter extends RecyclerView.Adapter {
        private List<String> dateList;
        private ItemOnClickListener clickListener;
        private Dialog dialog;

        ItemAdapter(List<String> dateList, ItemOnClickListener clickListener, Dialog dialog) {
            this.dateList = dateList;
            this.clickListener = clickListener;
            this.dialog = dialog;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.know_popwindow_item, null);
            return new ItemAdapter.ItemViewHolder(convertView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ItemViewHolder viewHolder = (ItemViewHolder) holder;
            if (position == dateList.size() - 1) {
                viewHolder.mEmptyView.setVisibility(View.VISIBLE);
            }
            else
                viewHolder.mEmptyView.setVisibility(View.GONE);
            viewHolder.mTvTitle.setText(dateList.get(position));
            viewHolder.mTvTitle.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onClick(position, dateList.get(position));
                    dialog.dismiss();
                }
            });
        }

        @Override
        public int getItemCount() {
            return dateList == null ? 0 : dateList.size();
        }


        class ItemViewHolder extends RecyclerView.ViewHolder {

            private View mEmptyView;
            private TextView mTvTitle;

            ItemViewHolder(View itemView) {
                super(itemView);
                mEmptyView = itemView.findViewById(R.id.empty_view);
                mTvTitle = (TextView) itemView.findViewById(R.id.title);
            }
        }
    }


    public interface ItemOnClickListener {
        void onClick(int position, String text);
    }
}

