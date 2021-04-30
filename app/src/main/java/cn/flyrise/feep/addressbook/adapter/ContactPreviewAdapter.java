package cn.flyrise.feep.addressbook.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.core.services.model.AddressBook;

/**
 * @author ZYP
 * @since 2016-12-13 11:20
 */
public class ContactPreviewAdapter extends RecyclerView.Adapter<ContactPreviewAdapter.PreviewHolder> {

    private Context mContext;
    private List<AddressBook> mSelectedContacts;
    private OnContactItemClickListener mItemClickListener;
    private String mHostUrl;

    public ContactPreviewAdapter(Context context) {
        this.mContext = context;
        mHostUrl = CoreZygote.getLoginUserServices().getServerAddress();
    }

    public void setSelectedContacts(List<AddressBook> selectedContacts) {
        this.mSelectedContacts = selectedContacts;
    }

    public void setOnContactItemClickListener(OnContactItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    @Override public PreviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact_preview, parent, false);
        return new PreviewHolder(itemView);
    }

    @Override public void onBindViewHolder(PreviewHolder holder, int position) {
        AddressBook addressBook = mSelectedContacts.get(position);
        FEImageLoader.load(mContext, holder.ivUserIcon, mHostUrl + addressBook.imageHref,
                addressBook.userId, addressBook.name);

        holder.tvUserName.setText(addressBook.name);

        holder.ivDeleteIcon.setOnClickListener(view -> {
            if (mItemClickListener != null) mItemClickListener.onItemClick(addressBook, position);
        });
    }

    public void remove(AddressBook addressBook) {
        if (CommonUtil.nonEmptyList(mSelectedContacts)) {
            mSelectedContacts.remove(addressBook);
        }
    }

    @Override public int getItemCount() {
        return CommonUtil.isEmptyList(mSelectedContacts) ? 0 : mSelectedContacts.size();
    }

    public class PreviewHolder extends RecyclerView.ViewHolder {

        public ImageView ivUserIcon;
        public ImageView ivDeleteIcon;
        public TextView tvUserName;

        public PreviewHolder(View itemView) {
            super(itemView);

            ivUserIcon = (ImageView) itemView.findViewById(R.id.ivUserIcon);
            ivDeleteIcon = (ImageView) itemView.findViewById(R.id.ivDeleteIcon);
            tvUserName = (TextView) itemView.findViewById(R.id.tvUserName);
        }
    }

}
