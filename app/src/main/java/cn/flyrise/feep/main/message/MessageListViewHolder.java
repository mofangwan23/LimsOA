package cn.flyrise.feep.main.message;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.flyrise.feep.R;

/**
 * @author ZYP
 * @since 2017-03-30 14:50
 */
public class MessageListViewHolder extends RecyclerView.ViewHolder {

    public View cardView;
    public ImageView ivAvatar;
    public TextView tvType;
    public TextView tvAction;
    public TextView tvTitle;
    public TextView tvTime;
    public ImageView ivMessageState;
    public TextView tvReadAll;
    public TextView tvName;
    public TextView tvJob;
    public LinearLayout llName;
    public TextView tvSystem;
    public TextView tvImportant;
    public LinearLayout llNameNoJob;
    public TextView tvNameNoJob;
    public TextView tvImportantNoJob;

    public MessageListViewHolder(View itemView) {
        super(itemView);
        cardView = itemView.findViewById(R.id.layoutContentView);
        ivAvatar = (ImageView) itemView.findViewById(R.id.iv_userhead);
        tvName = itemView.findViewById(R.id.tv_name);
        tvJob = itemView.findViewById(R.id.tv_job);
        tvType = (TextView) itemView.findViewById(R.id.tv_type);
        tvAction = (TextView) itemView.findViewById(R.id.tv_chatAction);
        tvTitle = (TextView) itemView.findViewById(R.id.tv_chatTitle);
        tvTime = (TextView) itemView.findViewById(R.id.timestamp);
        ivMessageState = (ImageView) itemView.findViewById(R.id.ivMessageState);
        tvReadAll = (TextView) itemView.findViewById(R.id.tv_read_all);
        llName = (LinearLayout) itemView.findViewById(R.id.ll_name);
        tvSystem = itemView.findViewById(R.id.tv_system);
        tvImportant = itemView.findViewById(R.id.tv_importantce);
        llNameNoJob = (LinearLayout) itemView.findViewById(R.id.ll_name_no_job);
        tvNameNoJob = itemView.findViewById(R.id.tv_name_no_job);
        tvImportantNoJob = itemView.findViewById(R.id.tv_importantce_no_job);
    }
}
