package com.hyphenate.chatui.group.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.views.adapter.FEListAdapter;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.core.services.model.AddressBook;
import com.hyphenate.chatui.R;
import java.util.List;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by klc on 2017/3/14.
 */

public class GroupDetailUserAdapter extends FEListAdapter<String> {

    private Context mContext;

    public GroupDetailUserAdapter(Context context) {
        this.mContext = context;
    }

    public List<String> getDataList() {
        return dataList;
    }

    @Override
    public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        String userID = dataList.get(position);
        ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
        switch (userID) {
            case "action_add_user":
                FEImageLoader.load(mContext,itemViewHolder.imageView,com.hyphenate.chatui.R.drawable.em_smiley_add_btn_nor);
                itemViewHolder.mTvUserName.setVisibility(View.GONE);
                break;
            case "action_remove_user":
                FEImageLoader.load(mContext,itemViewHolder.imageView,com.hyphenate.chatui.R.drawable.em_smiley_minus_btn_nor);
                itemViewHolder.mTvUserName.setVisibility(View.GONE);
                break;
            default:
                itemViewHolder.mTvUserName.setVisibility(View.VISIBLE);
                CoreZygote.getAddressBookServices().queryUserDetail(userID)
                        .subscribe(userInfo -> {
                            if (userInfo != null) {
                                String host = CoreZygote.getLoginUserServices().getServerAddress();
                                FEImageLoader.load(mContext, itemViewHolder.imageView, host + userInfo.imageHref, userID, userInfo.name);
                                itemViewHolder.mTvUserName.setText(userInfo.name);
                            }
                            else {
                                FEImageLoader.load(mContext, itemViewHolder.imageView, R.drawable.ease_default_avatar);
                                itemViewHolder.mTvUserName.setText(userID);
                            }
                        }, error -> {
                            FEImageLoader.load(mContext, itemViewHolder.imageView, R.drawable.ease_default_avatar);
                            itemViewHolder.mTvUserName.setText(userID);
                        });
                break;
        }
        itemViewHolder.imageView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(null, userID);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.em_grid, parent, false);
        return new ItemViewHolder(convertView);
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView mTvUserName;

        public ItemViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.iv_avatar);
            mTvUserName = (TextView) itemView.findViewById(R.id.tv_userid);
        }
    }

    public void setDataList(List<String> showList) {
        this.dataList = showList;
        this.notifyDataSetChanged();
    }
}
