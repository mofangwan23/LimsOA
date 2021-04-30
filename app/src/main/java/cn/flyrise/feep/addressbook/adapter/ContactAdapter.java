package cn.flyrise.feep.addressbook.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.function.ContactConfiguration;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.core.services.model.AddressBook;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author ZYP
 * @since 2016-12-08 22:54
 */
public class ContactAdapter extends BaseContactAdapter {

	private final SparseArray<Integer> mLetterSelection = new SparseArray<>();  // 英文字母索引
	private final List<String> mLetterList = new ArrayList<>();                 // 英文字母集合
	private final SparseArray<List<String>> mSurnameList = new SparseArray<>(); // 姓集合
	private final SparseArray<Integer> mSurnameIndexes = new SparseArray<>();   // 中文首字符索引

	public ContactAdapter(Context context) {
		super(context);
	}

	@Override
	public void onChildBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
		ContactViewHolder contactHolder = (ContactViewHolder) holder;
		final AddressBook addressBook = mContacts.get(position);
		FEImageLoader.load(mContext, contactHolder.ivUserIcon, mHostUrl + addressBook.imageHref,
				addressBook.userId, addressBook.name);
		contactHolder.tvUserName.setText(addressBook.name);
		String deptName = TextUtils.isEmpty(addressBook.deptName) ? "" : addressBook.deptName + "-";
		contactHolder.tvUserPosition.setText(deptName + addressBook.position);

		// 设置 Letter 显示隐藏
		int ch = addressBook.pinyin.toLowerCase().charAt(0);
		if (position == 0) {
			contactHolder.tvLetter.setVisibility(View.VISIBLE);
			contactHolder.tvLetter.setText((char) Character.toUpperCase(ch) + "");
		}
		else {
			AddressBook preAddressBook = mContacts.get(position - 1);
			int preCh = preAddressBook.pinyin.toLowerCase().charAt(0);
			if (ch == preCh) {
				contactHolder.tvLetter.setVisibility(View.GONE);
			}
			else {
				contactHolder.tvLetter.setVisibility(View.VISIBLE);
				contactHolder.tvLetter.setText((char) Character.toUpperCase(ch) + "");
			}
		}

		contactHolder.ivContactCheck.setVisibility(withSelect ? View.VISIBLE : View.GONE);
		if (withSelect) {
			if (getCannotSelectContacts().contains(addressBook)) {
				// 垃圾产品、垃圾代码
				if (ContactConfiguration.getInstance().isUserCannotSelectedButCheck(addressBook.userId)) {
					contactHolder.ivContactCheck.setVisibility(View.VISIBLE);
					if (isAddressFromMetting() && TextUtils.equals(addressBook.userId, mLoginUser)) {
						contactHolder.ivContactCheck.setImageResource(getCannotSelectContacts().contains(addressBook)
								? R.drawable.node_current_icon : R.drawable.shape_circle_grey_20);
					}
					else {
						contactHolder.ivContactCheck.setImageResource(getCannotSelectContacts().contains(addressBook)
								? R.drawable.no_choice : R.drawable.shape_circle_grey_20);
					}
				}
				else if (TextUtils.equals(addressBook.userId, mLoginUser)) {
					contactHolder.ivContactCheck.setVisibility(View.INVISIBLE);
				}
				else {
					contactHolder.ivContactCheck.setVisibility(View.VISIBLE);
					contactHolder.ivContactCheck.setImageResource(getCannotSelectContacts().contains(addressBook)
							? R.drawable.no_choice : R.drawable.shape_circle_grey_20);
				}
			}
			else {
				contactHolder.ivContactCheck.setVisibility(View.VISIBLE);
				contactHolder.ivContactCheck.setImageResource(getSelectedContacts().contains(addressBook)
						? R.drawable.node_current_icon : R.drawable.shape_circle_grey_20);
			}

			if (isExceptOwn) {
				if (TextUtils.equals(CoreZygote.getLoginUserServices().getUserId(), addressBook.userId)) {
					holder.itemView.setEnabled(false);
					contactHolder.ivContactCheck.setImageResource(R.drawable.shape_circle_grey_no_selected_20);
					holder.itemView.setBackgroundColor(Color.parseColor("#EDEDED"));
				}
				else {
					holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"));
				}
			}
		}

		holder.itemView.setOnClickListener(view -> {
			if (mItemClickListener != null) {
				if (ContactConfiguration.getInstance().isUserCannotSelectedButCheck(addressBook.userId)) {
					return;
				}
				mItemClickListener.onItemClick(addressBook, position);
			}
		});
	}

	public int getPositionBySelection(int selection) {
		return mLetterSelection.indexOfKey(selection) >= 0 ? mLetterSelection.get(selection) : -1;
	}

	public List<String> getSurnameBySelection(int surnameAscii) {
		return mSurnameList.indexOfKey(surnameAscii) >= 0 ? mSurnameList.get(surnameAscii) : null;
	}

	public int getPositionBySurname(int surnameAscii) {
		return mSurnameIndexes.indexOfKey(surnameAscii) >= 0 ? mSurnameIndexes.get(surnameAscii) : -1;
	}

	public List<String> getLetterList() {
		return mLetterList;
	}

	/**
	 * 构建字母快速筛选
	 * 以及姓名筛选
	 */
	public void buildSelection(List<AddressBook> addressBooks) {
		if (CommonUtil.isEmptyList(addressBooks)) {
			return;
		}
		mLetterList.clear();
		mLetterSelection.clear();
		mSurnameList.clear();
		mSurnameIndexes.clear();

		Set<String> letters = new HashSet<String>();
		for (int i = 0, n = addressBooks.size(); i < n; i++) {
			AddressBook addressBook = addressBooks.get(i);
			if (addressBook.pinyin.length() <= 0) {
				return;
			}
			int ch = addressBook.pinyin.charAt(0);
			int surnameAscii = addressBook.name.charAt(0);
			String surname = ((char) surnameAscii) + "";

			if (mLetterSelection.indexOfKey(ch) < 0) {  // 保存姓首字母
				mLetterSelection.put(ch, hasHeaderView() ? i + 1 : i);
				char letter = addressBook.pinyin.charAt(0);
				letters.add(String.valueOf(letter).toUpperCase());
				List<String> surnames = new ArrayList<>();  // 姓名集合
				surnames.add(((char) surnameAscii) + "");
				mSurnameList.put(ch, surnames);
			}
			else {
				List<String> strings = mSurnameList.get(ch);    // 姓名集合
				if (!strings.contains(surname)) {
					strings.add(surname);
				}
			}

			if (mSurnameIndexes.indexOfKey(surnameAscii) < 0) {  // 保存首字母索引
				mSurnameIndexes.put(surnameAscii, hasHeaderView() ? i + 1 : i);
			}
		}

		mLetterList.addAll(letters);
		Collections.sort(mLetterList);
	}
}
