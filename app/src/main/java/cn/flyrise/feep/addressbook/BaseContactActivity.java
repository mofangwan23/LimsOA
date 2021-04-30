package cn.flyrise.feep.addressbook;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import cn.flyrise.feep.K;
import cn.flyrise.feep.K.ChatContanct;
import cn.flyrise.feep.K.addressBook;
import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.adapter.BaseContactAdapter;
import cn.flyrise.feep.addressbook.view.ContactPreviewFragment;
import cn.flyrise.feep.addressbook.view.ContactsConfirmView;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.common.DataKeeper;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.services.model.AddressBook;
import com.hyphenate.chatui.utils.IMHuanXinHelper;

/**
 * @author ZYP
 * @since 2016-12-13 13:45
 */
public abstract class BaseContactActivity extends BaseActivity {

	protected BaseContactAdapter mContactAdapter;
	protected int[] mTopAndHeight;
	protected Handler mHandler = new Handler();
	protected ContactsConfirmView mContactsConfirmView;
	protected OnAddressBookCheckChangeListener mAddressBookCheckChangeListener;

	protected void setOnAddressBookCheckChangeListener(OnAddressBookCheckChangeListener checkChangeListener) {
		this.mAddressBookCheckChangeListener = checkChangeListener;
	}

	@Override
	public void bindView() {
		mContactsConfirmView = (ContactsConfirmView) findViewById(R.id.contactsConfirmView);
	}

	@Override
	public void bindData() {
		boolean withSelect = getIntent().getBooleanExtra(K.addressBook.select_mode, false);
		mContactAdapter.withSelect(withSelect);

		boolean exceptSelect = getIntent().getBooleanExtra(K.addressBook.except_selected, false);
		mContactAdapter.exceptSelect(exceptSelect);

		boolean isSingleSelect = getIntent().getBooleanExtra(addressBook.single_select, false);
		if (withSelect && !isSingleSelect) {    // 选择并且非单选的情况下才会出现这个鬼东西哦
			mContactsConfirmView.setVisibility(View.VISIBLE);
			updateSelectedCount();

			mContactsConfirmView.setConfirmClickListener(view -> {
				int key = getIntent().getIntExtra(K.addressBook.data_keep, -1);
				DataKeeper.getInstance().keepDatas(key, mContactAdapter.getSelectedContacts());
				setResult(K.addressBook.search_result_code);
				finish();
			});

			mContactsConfirmView.setPreviewClickListener(view -> {
//				if (mPreviewView == null) {
				ContactPreviewFragment mPreviewView = ContactPreviewFragment.newInstance(getPreviewMaxHeight(), getPreviewMarginTop());
//				}

				mPreviewView.setSeletedContacts(mContactAdapter.getSelectedContacts());
				mPreviewView.setOnDismissLisntener(dialog -> mContactAdapter.notifyDataSetChanged());
				mPreviewView.setOnClickListener((dialog, which) -> updateSelectedCount());
				mPreviewView.show(getSupportFragmentManager(), "preview");
			});

			View view = findViewById(R.id.layoutContactContainer);
			view.setPadding(0, 0, 0, getResources().getDimensionPixelSize(R.dimen.mdp_48));
		}
	}

	protected void updateSelectedCount() {
		String text = String.format(getResources().getString(R.string.select_im_user), mContactAdapter.getSelectedCount() + "");
		mContactsConfirmView.updateText(text);
	}

	protected void onItemClick(AddressBook addressBook, int position) {
		Intent intent = getIntent();
		if (intent.getBooleanExtra(K.addressBook.select_mode, false)) {         // 选择
			boolean singleChoose = getIntent().getBooleanExtra(K.addressBook.single_select, false);
			if (singleChoose) {
				Intent data = new Intent();
				data.putExtra(K.addressBook.user_id, addressBook.userId);
				data.putExtra(K.addressBook.user_name, addressBook.name);
				setResult(Activity.RESULT_OK, data);
				finish();
				return;
			}

			mContactAdapter.addSelectedContact(addressBook, position);
			updateSelectedCount();
			if (mAddressBookCheckChangeListener != null) {
				mAddressBookCheckChangeListener.onAddressBookCheckChange(addressBook);
			}
		}
		else if (intent.getBooleanExtra(K.addressBook.start_chat, false)) {      // 发起聊天
			String userId = CoreZygote.getLoginUserServices().getUserId();
			if (TextUtils.equals(userId, addressBook.userId)) {
				FEToast.showMessage(getResources().getString(R.string.Cant_chat_with_yourself));
				return;
			}
			String forwardMsgId = intent.getStringExtra(ChatContanct.EXTRA_FORWARD_MSG_ID);
			if (TextUtils.isEmpty(forwardMsgId)) {
				IMHuanXinHelper.getInstance().startChatActivity(this, addressBook.userId);
			}
			else {
				IMHuanXinHelper.getInstance().forwardMsg2User(this, addressBook.userId, addressBook.name, forwardMsgId);
			}
		}
		else {                                                                   // 查看详情
			Intent startIntent = new Intent(this, AddressBookDetailActivity.class);
			startIntent.putExtra(K.addressBook.user_id, addressBook.userId);
			startIntent.putExtra(K.addressBook.department_id, addressBook.deptId);

			boolean singleChoose = getIntent().getBooleanExtra(K.addressBook.single_select, false);
			if (singleChoose) {
				startIntent.putExtra(K.addressBook.user_name, addressBook.name);
				setResult(Activity.RESULT_OK, startIntent);
				finish();
				return;
			}

			startActivity(startIntent);
		}
	}

	protected abstract int getPreviewMaxHeight();

	protected abstract int getPreviewMarginTop();

	public interface OnAddressBookCheckChangeListener {

		void onAddressBookCheckChange(AddressBook addressBook);
	}
}
