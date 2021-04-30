package cn.flyrise.feep.addressbook.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.views.adapter.BaseRecyclerAdapter;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.services.model.AddressBook;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ZYP
 * @since 2016-12-13 13:34
 */
public abstract class BaseContactAdapter extends BaseRecyclerAdapter {

	public static final int CODE_SELECT_ALL = 1;        // 全选
	public static final int CODE_INVERT_SELECT_ALL = 0; // 全不选
	public static final int CODE_HIDE_SELECT = 2;       // 隐藏

	protected Context mContext;
	protected View mEmptyView;
	protected boolean withSelect;
	protected boolean exceptSelected;
	protected boolean isExceptOwn;

	protected OnContactItemClickListener mItemClickListener;
	protected List<AddressBook> mContacts;
	private List<AddressBook> mSelectedContacts;          // 已选择的用户
	private List<AddressBook> mCannotSelectContacts;    // 不能再选择的用户
	protected String mHostUrl;
	protected String mLoginUser;                        // 当前登录用户
	private boolean isMettingSelected;                         // 联系人类别


	public BaseContactAdapter(Context context) {
		this.mContext = context;
		if(CoreZygote.getLoginUserServices()!=null){
			mHostUrl = CoreZygote.getLoginUserServices().getServerAddress();
			mLoginUser = CoreZygote.getLoginUserServices().getUserId();
		}
	}

	public void setEmptyView(View emptyView) {
		this.mEmptyView = emptyView;
	}

	public void setContacts(List<AddressBook> contacts) {
		this.mContacts = contacts;
		this.notifyDataSetChanged();
		if (mEmptyView != null) this.mEmptyView.setVisibility(CommonUtil.isEmptyList(mContacts) ? View.VISIBLE : View.GONE);
	}

	public void isExceptOwn(boolean isExceptOwn) {
		this.isExceptOwn = isExceptOwn;
	}

	public void withSelect(boolean withSelect) {
		this.withSelect = withSelect;
	}

	public boolean withSelect() {
		return this.withSelect;
	}

	public void exceptSelect(boolean exceptSelected) {
		this.exceptSelected = exceptSelected;
	}

	public boolean exceptSelect() {
		return this.exceptSelected;
	}

	public void addSelectedContact(AddressBook contact, int position) {
		if (mSelectedContacts == null) {
			mSelectedContacts = new ArrayList<>();
		}
		if (mSelectedContacts.contains(contact)) {
			mSelectedContacts.remove(contact);
		}
		else {
			mSelectedContacts.add(contact);
		}
		this.notifyItemChanged(position);
	}

	public void setSelectedContacts(List<AddressBook> selectedContacts) {
		this.mSelectedContacts = selectedContacts;
		if (CommonUtil.isEmptyList(mSelectedContacts)) {
			mSelectedContacts = new ArrayList<>();
		}
	}

	public void setCannotSelectContacts(List<AddressBook> cannotSelectContacts) {
		this.mCannotSelectContacts = cannotSelectContacts;
		if (mCannotSelectContacts == null) {
			mCannotSelectContacts = new ArrayList<>();
		}
	}

	public void addCannotSelectContacts(AddressBook addressBook) {
		if (mCannotSelectContacts == null) {
			mCannotSelectContacts = new ArrayList<>();
		}
		if (!mCannotSelectContacts.contains(addressBook)) {
			mCannotSelectContacts.add(addressBook);
		}
	}

	public List<AddressBook> getCannotSelectContacts() {
		if (mCannotSelectContacts == null) {
			mCannotSelectContacts = new ArrayList<>();
		}
		return this.mCannotSelectContacts;
	}

	public void addSelectedContacts(List<AddressBook> selectedContacts) {
		if (CommonUtil.isEmptyList(selectedContacts)) {
			return;
		}

		if (mSelectedContacts == null) {
			mSelectedContacts = new ArrayList<>();
		}

		for (AddressBook addressBook : selectedContacts) {
			if (!mSelectedContacts.contains(addressBook)) {
				mSelectedContacts.add(addressBook);
			}
		}
	}

	public List<AddressBook> getSelectedContacts() {
		if (mSelectedContacts == null) {
			mSelectedContacts = new ArrayList<>();
		}
		if (isExceptOwn) {
			AddressBook contact = CoreZygote.getAddressBookServices().queryUserInfo(CoreZygote.getLoginUserServices().getUserId());
			if (mSelectedContacts.contains(contact)) {
				mSelectedContacts.remove(contact);
			}
		}
		return this.mSelectedContacts;
	}

	public void executeSelect(boolean selectAll) {
		if (CommonUtil.isEmptyList(mContacts)) {
			return;
		}

		if (mSelectedContacts == null) {
			mSelectedContacts = new ArrayList<>();
		}

		for (AddressBook addressBook : mContacts) {
			if (getCannotSelectContacts().contains(addressBook)) {
				continue;
			}

			if (selectAll) {     // 全选
				if (!mSelectedContacts.contains(addressBook)) {
					mSelectedContacts.add(addressBook);
				}
			}
			else {              // 全不选
				if (mSelectedContacts.contains(addressBook)) {
					mSelectedContacts.remove(addressBook);
				}
			}
		}
		this.notifyDataSetChanged();
	}

	public int getSelectedCount() {
		return CommonUtil.isEmptyList(mSelectedContacts) ? 0 : mSelectedContacts.size();
	}

	public List<AddressBook> getContacts() {
		return mContacts;
	}

	public void setOnContactItemClickListener(OnContactItemClickListener itemClickListener) {
		this.mItemClickListener = itemClickListener;
	}

	@Override public int getDataSourceCount() {
		return CommonUtil.isEmptyList(mContacts) ? 0 : mContacts.size();
	}

	@Override public abstract void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position);

	@Override public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
		View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_address_book_contact, parent, false);
		ContactViewHolder viewHolder = new ContactViewHolder(convertView);
		return viewHolder;
	}

	public int selectState() {
		if (CommonUtil.isEmptyList(mContacts)) {
			return CODE_HIDE_SELECT;
		}

		int selectedCount = 0, unSelectedCount = 0;
		int selectableCount = mContacts.size();     // 可选择的人员
		for (AddressBook addressBook : mContacts) {
			if (isCannotSelected(addressBook)) {  // 不能选择的人员
				selectableCount--;
				continue;
			}
			if (getSelectedContacts().contains(addressBook)) {
				selectedCount++;
			}
			else {
				unSelectedCount++;
			}
		}

		if (selectedCount == selectableCount) {
			// 全选，显示全不选
			return CODE_INVERT_SELECT_ALL;
		}
		else if (unSelectedCount == selectableCount) {
			// 全不选，显示个屁，隐藏
			return CODE_HIDE_SELECT;
		}
		// 显示全选
		return CODE_SELECT_ALL;
	}

	private boolean isCannotSelected(AddressBook addressBook) {
		if (mCannotSelectContacts == null) {
			return false;
		}

		if (mCannotSelectContacts.isEmpty()) {
			return false;
		}

		return mCannotSelectContacts.contains(addressBook);
	}

	public void setAddressFromMetting(boolean isMettingSelected) {
		this.isMettingSelected = isMettingSelected;
	}

	public boolean isAddressFromMetting() {
		return isMettingSelected;
	}

	public class ContactViewHolder extends RecyclerView.ViewHolder {

		public ImageView ivUserIcon;
		public TextView tvLetter;
		public TextView tvUserName;
		public TextView tvUserPosition;
		public ImageView ivContactCheck;

		public ContactViewHolder(View itemView) {
			super(itemView);
			ivUserIcon = (ImageView) itemView.findViewById(R.id.ivUserIcon);
			tvLetter = (TextView) itemView.findViewById(R.id.tvLetter);
			tvUserName = (TextView) itemView.findViewById(R.id.tvUserName);
			tvUserPosition = (TextView) itemView.findViewById(R.id.tvUserPosition);
			ivContactCheck = (ImageView) itemView.findViewById(R.id.ivContactCheck);
		}
	}
}
