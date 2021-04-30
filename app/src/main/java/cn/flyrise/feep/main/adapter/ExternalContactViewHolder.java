package cn.flyrise.feep.main.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


import cn.flyrise.feep.R;

/**
 * @author ZYP
 * @since 2017-05-17 16:38
 */
public class ExternalContactViewHolder extends RecyclerView.ViewHolder {

    public TextView mTvLetter;
    public TextView mTvPosition;
    public TextView mTvUserName;
    public TextView mTvCompany;
    public ImageView mIvUserIcon;

    public ExternalContactViewHolder(View itemView) {
        super(itemView);
        mTvLetter = (TextView) itemView.findViewById(R.id.tvLetter);
        mTvPosition = (TextView) itemView.findViewById(R.id.tvPosition);
        mTvUserName = (TextView) itemView.findViewById(R.id.tvUserName);
        mTvCompany = (TextView) itemView.findViewById(R.id.tvUserCompany);
        mIvUserIcon = (ImageView) itemView.findViewById(R.id.ivUserIcon);
    }

}
