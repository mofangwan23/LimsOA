package cn.flyrise.feep.main.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.views.adapter.BaseRecyclerAdapter;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.main.model.ExternalContact;

/**
 * @author ZYP
 * @since 2017-05-17 16:37
 */
public class ExternalContactListAdapter extends BaseRecyclerAdapter {

	private final SparseArray<Integer> mLetterSelection = new SparseArray<>();  // 英文字母索引
	private final List<String> mLetterList = new ArrayList<>();        //英文字母集合
	private final SparseArray<List<String>> mSurnameList = new SparseArray<>(); // 姓集合
	private final SparseArray<Integer> mSurnameIndexes = new SparseArray<>();    // 中文首字符索引
	private boolean isLetterVisible = true;

	private Context mContext;
	private List<ExternalContact> mExternalContacts;
	private OnExternalContactClickListener mExternalContactClickListener;

	public ExternalContactListAdapter(Context context) {
		this.mContext = context;
	}

	public void setOnExternalContactClickListener(OnExternalContactClickListener listener) {
		this.mExternalContactClickListener = listener;
	}

	public void setExternalContacts(List<ExternalContact> externalContacts) {
		this.mExternalContacts = externalContacts;
		if (CommonUtil.nonEmptyList(mExternalContacts)) {
			Collections.sort(mExternalContacts, (lhs, rhs) -> {
				if (lhs == null || rhs == null) {
					return -1;
				}
				if (TextUtils.isEmpty(lhs.pinyin) || TextUtils.isEmpty(rhs.pinyin)) {
					return -1;
				}

				String p1 = lhs.pinyin.charAt(0) + "";
				String p2 = rhs.pinyin.charAt(0) + "";
				int pResult = p1.toLowerCase().compareTo(p2.toLowerCase());

				if (TextUtils.isEmpty(lhs.name) || TextUtils.isEmpty(rhs.name)) {
					return pResult;
				}

				String n1 = lhs.name.charAt(0) + "";
				String n2 = rhs.name.charAt(0) + "";

				if (pResult == 0) {
					return n1.compareTo(n2);
				}
				return pResult;
			});
			this.buildSelection(mExternalContacts);
		}
		this.notifyDataSetChanged();
	}

	public void addExternalContacts(List<ExternalContact> externalContacts) {
		if (CommonUtil.isEmptyList(externalContacts)) {
			return;
		}
		if (mExternalContacts == null) {
			mExternalContacts = new ArrayList<>();
		}
		mExternalContacts.addAll(externalContacts);
		Collections.sort(mExternalContacts, (lhs, rhs) -> lhs.pinyin.toLowerCase().compareTo(rhs.pinyin.toLowerCase()));
		this.buildSelection(mExternalContacts);
		this.notifyDataSetChanged();
	}

	public void setLetterVisible(boolean isLetterVisible) {
		this.isLetterVisible = isLetterVisible;
	}

	@Override
	public int getDataSourceCount() {
		return CommonUtil.isEmptyList(mExternalContacts) ? 0 : mExternalContacts.size();
	}

	@Override
	public void onChildBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		ExternalContactViewHolder contactHolder = (ExternalContactViewHolder) holder;
		ExternalContact externalContact = mExternalContacts.get(position);

		if (isLetterVisible) {
			int ch = externalContact.pinyin.charAt(0);
			if (position == 0) {
				contactHolder.mTvLetter.setVisibility(View.VISIBLE);
				contactHolder.mTvLetter.setText((char) Character.toUpperCase(ch) + "");
			}
			else {
				ExternalContact preExternalContact = mExternalContacts.get(position - 1);
				int preCh = preExternalContact.pinyin.charAt(0);
				if (ch == preCh) {
					contactHolder.mTvLetter.setVisibility(View.GONE);
				}
				else {
					contactHolder.mTvLetter.setVisibility(View.VISIBLE);
					contactHolder.mTvLetter.setText((char) Character.toUpperCase(ch) + "");
				}
			}
		}
		else {
			contactHolder.mTvLetter.setVisibility(View.GONE);
		}

		contactHolder.mTvPosition.setText(externalContact.position);
		contactHolder.mTvUserName.setText(externalContact.name);
		contactHolder.mTvCompany.setText(externalContact.company);

		FEImageLoader.load(mContext, contactHolder.mIvUserIcon, "/tan90", UUID.randomUUID().toString(), externalContact.name);
		contactHolder.itemView.setOnClickListener(view -> {
			if (mExternalContactClickListener != null) {
				mExternalContactClickListener.onExternalContactClick(externalContact);
			}
		});
	}

	@Override
	public RecyclerView.ViewHolder onChildCreateViewHolder(ViewGroup parent, int viewType) {
		View convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_external_contact, parent, false);
		return new ExternalContactViewHolder(convertView);
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

	/**
	 * 构建字母快速筛选
	 * 以及姓名筛选
	 */
	public void buildSelection(List<ExternalContact> externalContacts) {
		if (CommonUtil.isEmptyList(externalContacts)) {
			return;
		}
		mLetterSelection.clear();
		mSurnameList.clear();
		mSurnameIndexes.clear();
		mLetterList.clear();

		for (int i = 0, n = externalContacts.size(); i < n; i++) {
			ExternalContact addressBook = externalContacts.get(i);
			int ch = addressBook.pinyin.charAt(0);
			int surnameAscii = addressBook.name.charAt(0);
			String surname = ((char) surnameAscii) + "";

			if (mLetterSelection.indexOfKey(ch) < 0) {  // 保存姓首字母
				mLetterSelection.put(ch, hasHeaderView() ? i + 1 : i);
				char letter = addressBook.pinyin.charAt(0);
				mLetterList.add(String.valueOf(letter).toUpperCase());
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
	}

	public List<String> getLetterList() {
		return mLetterList;
	}

	public interface OnExternalContactClickListener {

		void onExternalContactClick(ExternalContact externalContact);
	}
}
