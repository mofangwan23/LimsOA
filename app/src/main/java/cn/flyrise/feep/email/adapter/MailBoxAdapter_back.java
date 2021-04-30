package cn.flyrise.feep.email.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.flyrise.android.protocol.model.Mail;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.views.adapter.BaseRecyclerAdapter;
import cn.flyrise.feep.core.common.utils.DateUtil;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.core.services.model.AddressBook;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author ZYP
 * @since 2016/7/11 09:26
 */
public class MailBoxAdapter_back extends BaseRecyclerAdapter {

    private Context mContext;
    private List<Mail> mMailList;
    private boolean isDeleteModel;
    private View mEmptyView;
    private String mHost;
    private List<String> mDelMailIds = new ArrayList<>();
    private OnMailItemClickListener mOnItemClickListener;
    private OnMailItemLongClickListener mOnItemLongClickListener;
    private OnDeleteMailSizeChangeListener mDeleteMailSizeChangeListener;

    public MailBoxAdapter_back(Context context) {
        this.mContext = context;
        this.mHost = CoreZygote.getLoginUserServices().getServerAddress();
    }

    public void setOnMailItemClickListener(OnMailItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public void setOnMailItemLongClickListener(OnMailItemLongClickListener listener) {
        this.mOnItemLongClickListener = listener;
    }

    public void setOnDeleteMailSizeChangeListener(OnDeleteMailSizeChangeListener listener) {
        this.mDeleteMailSizeChangeListener = listener;
    }

    public void setEmptyView(View emptyView) {
        this.mEmptyView = emptyView;
    }

    public void setDeleteModel(boolean isDeleteModel) {
        mDelMailIds.clear();
        this.isDeleteModel = isDeleteModel;
        this.notifyDataSetChanged();
    }

    public boolean isDeleteModel() {
        return isDeleteModel;
    }

    public void setMailList(List<Mail> mailList) {
        this.mMailList = mailList;
        this.notifyDataSetChanged();
        mEmptyView.setVisibility((this.mMailList == null || this.mMailList.size() == 0) ? View.VISIBLE : View.GONE);
    }

    public void addMailList(List<Mail> mailList) {
        if (this.mMailList == null) {
            this.mMailList = new ArrayList<>();
        }

        this.mMailList.addAll(mailList);
        this.notifyDataSetChanged();
    }

    public void removeMail(int position) {
        if (position >= 0) {
            mMailList.remove(position);
            this.notifyDataSetChanged();
            if (mMailList.size() == 0) {
                this.mEmptyView.setVisibility(View.VISIBLE);
            }
        }
    }

    public String getDelMailIds() {
        if (this.mDelMailIds == null || this.mDelMailIds.size() == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        int position = 0;
        for (int len = mDelMailIds.size(); position < len - 1; position++) {
            sb.append(mDelMailIds.get(position)).append(",");
        }
        sb.append(mDelMailIds.get(position));
        return sb.toString();
    }

    @Override
    public int getDataSourceCount() {
        return this.mMailList == null ? 0 : mMailList.size();
    }

    @Override
    public void onChildBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final BoxViewHolder boxHolder = (BoxViewHolder) holder;
        final Mail mail = this.mMailList.get(position);

        if (TextUtils.isEmpty(mail.sendMan)) {
            mail.sendMan = CoreZygote.getLoginUserServices().getUserName();
        }

        if (TextUtils.isEmpty(mail.sendUserId)) {
            mail.sendUserId = CoreZygote.getLoginUserServices().getUserId();
        }

        boxHolder.tvTitle.setText(mail.title);
        boxHolder.tvSender.setText(mail.sendMan);
        boxHolder.tvTime.setText(DateUtil.formatTimeToHm(mail.sendTime));
        boxHolder.tvContent.setText(TextUtils.isEmpty(mail.summary.trim()) ? "" : mail.summary);

        if (position == 0) {
            boxHolder.tvDate.setVisibility(View.VISIBLE);
            boxHolder.tvDate.setText(mail.getDate());
        } else {
            Mail preMail = this.mMailList.get(position - 1);
            if (mail.isSameMonth(preMail)) {
                boxHolder.tvDate.setVisibility(View.GONE);
            } else {
                boxHolder.tvDate.setVisibility(View.VISIBLE);
                boxHolder.tvDate.setText(mail.getDate());
            }
        }

        final String mailId = mail.mailId;

        boxHolder.checkBox.setVisibility(isDeleteModel ? View.VISIBLE : View.GONE);
        boxHolder.checkBox.setChecked(mDelMailIds.contains(mailId));

        switch (mail.status) {
            case "0":   // "0":已收带有附件
                setStateAndAttachmentVisible(boxHolder, View.GONE, View.VISIBLE);
                break;
            case "1":   // "1":已收没有附件
                setStateAndAttachmentVisible(boxHolder, View.GONE, View.GONE);
                break;
            case "2":   // "2":未收带有附件
                setStateAndAttachmentVisible(boxHolder, View.VISIBLE, View.VISIBLE);
                break;
            case "3":   // "3":未收没有附件
                setStateAndAttachmentVisible(boxHolder, View.VISIBLE, View.GONE);
                break;
        }

        String userId = mail.sendUserId;
        if (TextUtils.isEmpty(userId) || TextUtils.equals(userId, "-1")) {
            userId = Math.abs(UUID.randomUUID().toString().hashCode()) + "";
            mMailList.get(position).sendUserId = userId;
        }

        final String id = userId;
        CoreZygote.getAddressBookServices().queryUserDetail(id)
                .subscribe(it -> {
                    if (it != null) {
                        FEImageLoader.load(mContext, boxHolder.ivAvatar, mHost + it.imageHref, id, mail.sendMan);
                    }
                    else {
                        FEImageLoader.load(mContext, boxHolder.ivAvatar, mHost + "/helloworld", id, mail.sendMan);
                    }
                }, error -> {
                    FEImageLoader.load(mContext, boxHolder.ivAvatar, mHost + "/helloworld", id, mail.sendMan);
                });

        if (mOnItemClickListener != null) {
            boxHolder.rootView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isDeleteModel) {
                        if (mDelMailIds.contains(mailId)) {
                            mDelMailIds.remove(mailId);
                            boxHolder.checkBox.setChecked(false);
                        } else {
                            mDelMailIds.add(mailId);
                            boxHolder.checkBox.setChecked(true);
                        }

                        if (mDeleteMailSizeChangeListener != null) {
                            mDeleteMailSizeChangeListener.onDeleteMailSizeChange(mDelMailIds.size());
                        }

                        return;
                    }

                    mOnItemClickListener.onMailItemClick(boxHolder.rootView, mail, position);
                    changeMailItemState(boxHolder, mail.status, position);
                }
            });
        }

        if (mOnItemLongClickListener != null) {
            boxHolder.rootView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (isDeleteModel) {
                        return false;
                    }
                    setDeleteModel(true);
                    mDelMailIds.add(mailId);
                    boxHolder.checkBox.setChecked(true);
                    mOnItemLongClickListener.onMailItemLongClick(boxHolder.rootView, mail, position);
                    return true;
                }
            });
        }
    }

    private void changeMailItemState(BoxViewHolder boxHolder, String state, int position) {
        if (boxHolder.ivState.getVisibility() == View.VISIBLE) {
            if (TextUtils.equals(state, "2")) {
                mMailList.get(position).status = "0";
            } else if (TextUtils.equals(state, "3")) {
                mMailList.get(position).status = "1";
            }
            notifyItemChanged(position);
        }
    }

    private void setStateAndAttachmentVisible(BoxViewHolder holder, int stateVisible, int attachmentVisible) {
        holder.ivState.setVisibility(stateVisible);
        holder.ivAttachment.setVisibility(attachmentVisible);
    }

    @Override
    public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.email_item_inbox, parent, false);
        BoxViewHolder holder = new BoxViewHolder(convertView);
        return holder;
    }

    public class BoxViewHolder extends RecyclerView.ViewHolder {

        public View rootView;
        public ImageView ivState;
        public ImageView ivAvatar;
        public ImageView ivAttachment;
        public TextView tvSender;
        public TextView tvTime;
        public TextView tvTitle;
        public TextView tvContent;
        public TextView tvDate;
        public CheckBox checkBox;

        public BoxViewHolder(View itemView) {
            super(itemView);
            rootView = itemView.findViewById(R.id.llMail);
            ivState = (ImageView) itemView.findViewById(R.id.ivMailState);
            ivAvatar = (ImageView) itemView.findViewById(R.id.ivMailIcon);
            ivAttachment = (ImageView) itemView.findViewById(R.id.ivMailAttachment);
            tvSender = (TextView) itemView.findViewById(R.id.tvMailSender);
            tvDate = (TextView) itemView.findViewById(R.id.tvMailDate);
            tvTime = (TextView) itemView.findViewById(R.id.tvMailTime);
            tvTitle = (TextView) itemView.findViewById(R.id.tvMailTitle);
            tvContent = (TextView) itemView.findViewById(R.id.tvMailContent);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
        }
    }

    public interface OnMailItemClickListener {
        void onMailItemClick(View view, Mail mail, int position);
    }

    public interface OnMailItemLongClickListener {
        void onMailItemLongClick(View view, Mail mail, int position);
    }

    public interface OnDeleteMailSizeChangeListener {
        void onDeleteMailSizeChange(int afterDeleteSize);
    }
}
