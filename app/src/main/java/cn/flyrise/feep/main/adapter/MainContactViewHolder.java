package cn.flyrise.feep.main.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.flyrise.feep.R;

/**
 * @author ZYP
 * @since 2017-02-13 10:37
 */
public class MainContactViewHolder extends RecyclerView.ViewHolder {

    public View itemView;
    public ImageView ivIcon;
    public ImageView ivArrow;
    public View divider;
    public View spliteLineShort;
    public View spliteLineLong;
    public TextView tvName;
    public TextView tvSubName;
    public LinearLayout llPartTimeDepartment;
    public TextView tvExpend;
    public ImageView ivDownUp;


    public MainContactViewHolder(View itemView) {
        super(itemView);
        this.itemView = itemView.findViewById(R.id.item_main_contact_layoutItemView);
        this.tvName = (TextView) itemView.findViewById(R.id.tvMainName);
        this.tvSubName = (TextView) itemView.findViewById(R.id.tvMainSubName);
        this.divider = itemView.findViewById(R.id.viewSplitLine16);
        this.spliteLineShort = itemView.findViewById(R.id.viewSplitLine);
        this.spliteLineLong = itemView.findViewById(R.id.viewSplitLine_long);
        this.ivIcon = (ImageView) itemView.findViewById(R.id.ivMainContactIcon);
        this.ivArrow = (ImageView) itemView.findViewById(R.id.ivMainArrow);
        this.llPartTimeDepartment = itemView.findViewById(R.id.item_main_contact_ll_extent_part_time_department);
        this.tvExpend = (TextView) itemView.findViewById(R.id.item_main_contact_tv_extent_part_time_department);
        this.ivDownUp = (ImageView) itemView.findViewById(R.id.item_main_contact_part_time_department_iv_down_up);
    }

}
