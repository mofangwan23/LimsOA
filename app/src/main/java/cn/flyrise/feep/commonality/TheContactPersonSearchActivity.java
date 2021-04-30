package cn.flyrise.feep.commonality;

import android.content.Intent;
import android.net.Uri;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ViewGroup;
import cn.flyrise.android.library.view.addressbooklistview.been.AddressBookListItem;
import cn.flyrise.android.protocol.model.AddressBookItem;
import cn.flyrise.feep.K;
import cn.flyrise.feep.K.ChatContanct;
import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.AddressBookDetailActivity;
import cn.flyrise.feep.commonality.adapter.TheContactSearchListViewAdapter;
import cn.flyrise.feep.commonality.presenter.ContactSearchPresenter;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.FESearchListActivity;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.X.AddressBookType;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.watermark.WMStamp;
import com.hyphenate.chatui.utils.IMHuanXinHelper;
import java.util.List;

/**
 * Created by klc on 2017/9/6.
 * 联系人搜索界面
 */
public class TheContactPersonSearchActivity extends FESearchListActivity<AddressBookListItem> {

	private TheContactSearchListViewAdapter mAdapter;
	private ContactSearchPresenter mPresenter;
	private ViewGroup mWaterMarkContainer;

	@Override
	public void bindView() {
		mWaterMarkContainer = findViewById(R.id.layoutContentView);
		super.bindView();
	}

	@Override
	public void bindData() {
		super.bindData();
		et_Search.setHint(getResources().getString(R.string.search_contact));
		searchKey = getIntent().getStringExtra("keyword");
		mAdapter = new TheContactSearchListViewAdapter();
		mPresenter = new ContactSearchPresenter(this);
		setAdapter(mAdapter);
		setPresenter(mPresenter);

		if (!TextUtils.isEmpty(searchKey)) {
			et_Search.setText(searchKey);
			et_Search.setSelection(searchKey.length());
			myHandler.post(searchRunnable);
		}
		else {
			myHandler.postDelayed(() -> DevicesUtil.showKeyboard(et_Search), 500);
		}
	}

	@Override
	public void bindListener() {
		super.bindListener();
		mAdapter.setOnItemClickListener((view, object) -> {
			final AddressBookItem bookItem = (AddressBookItem) object;
			if (bookItem == null) {
				return;
			}
			String forwardMsgId = getIntent().getStringExtra(ChatContanct.EXTRA_FORWARD_MSG_ID); //╮(╯▽╰)╭ 转发聊天内容。
			if (TextUtils.isEmpty(forwardMsgId)) {
				final int itemType = bookItem.getType();
				if (AddressBookType.Staff == itemType) {
					final Intent intent = new Intent(TheContactPersonSearchActivity.this, AddressBookDetailActivity.class);
					intent.putExtra(K.addressBook.user_id, bookItem.getId());
					startActivity(intent);
				}
			}
			else {
				if (bookItem.getId().equals(CoreZygote.getLoginUserServices().getUserId())) {
					FEToast.showMessage(getResources().getString(R.string.Cant_chat_with_yourself));
					return;
				}
				IMHuanXinHelper.getInstance().forwardMsg2User(this, bookItem.getId(), bookItem.getName(), forwardMsgId);
			}

		});
		mAdapter.setItemContentClickListener(tel -> {
			final Intent intent = new Intent(Intent.ACTION_DIAL);
			intent.setData(Uri.parse("tel:" + tel));
			startActivity(intent);
		});

		et_Search.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() == 0) {
					WMStamp.getInstance().draw(mWaterMarkContainer, listView.getLoadMoreRecyclerView());
				}
			}
		});
	}

	@Override
	public void refreshListData(List<AddressBookListItem> dataList) {
		super.refreshListData(dataList);
		WMStamp.getInstance().draw(mWaterMarkContainer, listView.getLoadMoreRecyclerView());
	}

	@Override
	public void loadMoreListData(List<AddressBookListItem> dataList) {
		super.loadMoreListData(dataList);
		WMStamp.getInstance().draw(mWaterMarkContainer, listView.getLoadMoreRecyclerView());
	}


	@Override
	public void searchData(String searchKey) {
		mPresenter.refreshListData(searchKey);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		WMStamp.getInstance().clearWaterMark(this);
	}
}