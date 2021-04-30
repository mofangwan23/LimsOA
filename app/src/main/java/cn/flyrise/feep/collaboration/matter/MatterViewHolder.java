package cn.flyrise.feep.collaboration.matter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.flyrise.feep.R;

/**
 * @author ZYP
 * @since 2017-05-12 10:33
 */
public class MatterViewHolder extends RecyclerView.ViewHolder {

    public TextView tvMatterType;
    public TextView tvMatterTitle;
    public TextView tvMatterSendUser;
    public TextView tvMatterSendTime;
    public ImageView ivCheck;
    public ImageView ivUser;
    public View contentView;
    public LinearLayout mLayoutOutTile;

    public MatterViewHolder(View itemView) {
        super(itemView);
        tvMatterType = (TextView) itemView.findViewById(R.id.tvMatterType);
        tvMatterTitle = (TextView) itemView.findViewById(R.id.tvMatterTitle);
        tvMatterSendUser = (TextView) itemView.findViewById(R.id.tvMatterSendUser);
        tvMatterSendTime = (TextView) itemView.findViewById(R.id.tvMatterSendTime);
        ivCheck = (ImageView) itemView.findViewById(R.id.ivMatterCheck);
        ivUser = (ImageView) itemView.findViewById(R.id.ivUserIcon);
        contentView = itemView.findViewById(R.id.layoutContent);
        mLayoutOutTile = (LinearLayout) itemView.findViewById(R.id.layoutOutTitle);
    }
}
