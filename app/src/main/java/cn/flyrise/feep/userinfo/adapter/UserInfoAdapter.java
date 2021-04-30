package cn.flyrise.feep.userinfo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.flyrise.feep.core.function.FunctionManager;
import java.util.List;

import cn.flyrise.android.shared.model.user.UserInfo;
import cn.flyrise.feep.FEApplication;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.userinfo.modle.UserInfoDetailItem;
import cn.flyrise.feep.utils.Patches;

/**
 * Created by Administrator on 2017-4-24.
 */

public class UserInfoAdapter extends RecyclerView.Adapter<UserInfoAdapter.UserInfoHolder> {

    private int[] modifyType = {K.userInfo.DETAIL_PHONE, K.userInfo.DETAIL_ICON
            , K.userInfo.DETAIL_TEL, K.userInfo.DETAIL_EMAIL
            , K.userInfo.DETAIL_LOCATION, K.userInfo.DETAIL_BIRTHDAY};

    private Context mContext;
    private List<UserInfoDetailItem> lists;

    private OnItemClickListener mListener;

    public UserInfoAdapter(Context context, List<UserInfoDetailItem> beans) {
        mContext = context;
        lists = beans;
    }

    public void addList(List<UserInfoDetailItem> beans) {
        if (beans == null) {
            return;
        }
        lists = beans;
        notifyDataSetChanged();
    }

    @Override
    public UserInfoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.userinfo_item, parent, false);
        return new UserInfoHolder(view);
    }

    public boolean isModify(int currentType) {
        for (int type : modifyType) {
            if (type == currentType)
                return true;
        }
        return false;
    }

    @Override
    public void onBindViewHolder(UserInfoHolder holder, int position) {
        UserInfoDetailItem bean = lists.get(position);
        if (bean == null) {
            return;
        }

        if (!TextUtils.isEmpty(bean.title)) {
            holder.titleTv.setText(bean.title);
        }

        holder.contentTv.setText(bean.content);

        if (bean.itemType == K.userInfo.DETAIL_LOGIN_PASSWORD) {
            holder.contentTv.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);//隐藏密码
        }
        if (bean.itemType == K.userInfo.DETAIL_ICON) {
            holder.userIcon.setVisibility(View.VISIBLE);
            holder.contentTv.setVisibility(View.GONE);
            UserInfo userInfo = ((FEApplication) mContext.getApplicationContext()).getUserInfo();
            if (userInfo == null) {
                return;
            }
            String host = CoreZygote.getLoginUserServices().getServerAddress();
            String url = host + bean.content;
            FEImageLoader.load(mContext, holder.userIcon, url, userInfo.getUserID(), userInfo.getUserName());
        } else {
            holder.userIcon.setVisibility(View.GONE);
            holder.contentTv.setVisibility(View.VISIBLE);
        }

        if (isModify(bean.itemType)) {
            holder.rightIcon.setVisibility(View.VISIBLE);
            holder.layout.setEnabled(true);
//            Drawable navigation = mContext.getResources().getDrawable(R.drawable.list_direction_fe);
//            navigation.setColorFilter(Color.parseColor("#FF908F95"), PorterDuff.Mode.SRC_ATOP);
//            holder.rightIcon.setImageDrawable(navigation);
        } else {
            holder.rightIcon.setVisibility(View.GONE);
            holder.layout.setEnabled(false);
        }

        if (position == lists.size() - 1) {
            holder.line.setVisibility(View.INVISIBLE);
        } else {
            holder.line.setVisibility(View.VISIBLE);
        }
        if (FunctionManager.hasPatch(Patches.PATCH_USER_INFO_MODIFY)) {
            holder.layout.setOnClickListener(v -> {
                if (isModify(bean.itemType) && mListener != null) {
                    mListener.clickItem(bean);
                }
            });
        } else {
            holder.rightIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return lists == null ? 0 : lists.size();
    }

    class UserInfoHolder extends RecyclerView.ViewHolder {

        private TextView titleTv;
        private TextView contentTv;
        private ImageView userIcon;
        private ImageView rightIcon;
        private View line;

        private RelativeLayout layout;

        public UserInfoHolder(View itemView) {
            super(itemView);
            titleTv = (TextView) itemView.findViewById(R.id.title);
            contentTv = (TextView) itemView.findViewById(R.id.content);
            userIcon = (ImageView) itemView.findViewById(R.id.user_icon);
            rightIcon = (ImageView) itemView.findViewById(R.id.right_icon);
            line = itemView.findViewById(R.id.line_bottom);
            layout = (RelativeLayout) itemView.findViewById(R.id.layout);
        }
    }

    public interface OnItemClickListener {
        void clickItem(UserInfoDetailItem bean);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        mListener = onItemClickListener;
    }

}
