package cn.flyrise.feep.addressbook;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_FLING;
import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import cn.flyrise.feep.K;
import cn.flyrise.feep.K.ChatContanct;
import cn.flyrise.feep.K.addressBook;
import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.adapter.ContactSearchAdapter;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.views.LoadMoreRecyclerView;
import cn.flyrise.feep.core.common.DataKeeper;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.core.watermark.WMAddressDecoration;
import cn.flyrise.feep.core.watermark.WMStamp;
import cn.squirtlez.frouter.annotations.Route;
import java.util.List;

/**
 * @since 2016-12-12 09:24 联系人搜索界面，和 {@link AddressBookActivity } 关联使用。
 */
@Route("/contact/search")
public class ContactSearchActivity extends BaseContactActivity implements ContactSearchContract.IView {

	private LoadMoreRecyclerView mRecyclerView;
	private View mClearInputBtn;
	private EditText mEtSearch;
	private View mSearchView;
	private Runnable mTextChangeRunnable;
	private ContactSearchContract.IPresenter mPresenter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPresenter = new ContactSearchPresenter(this, mHandler);
		setContentView(R.layout.activity_contact_search);
	}

	@Override
	protected void onSwipeOpened() {
		int key = getIntent().getIntExtra(K.addressBook.data_keep, -1);
		DataKeeper.getInstance().keepDatas(key, mContactAdapter.getSelectedContacts());
		setResult(K.addressBook.search_result_code);
	}

	@Override
	public void bindView() {
		super.bindView();
		mSearchView = findViewById(R.id.layoutSearch);
		mEtSearch = (EditText) findViewById(R.id.etSearch);
		mClearInputBtn = findViewById(R.id.ivDeleteIcon);

		mRecyclerView = (LoadMoreRecyclerView) findViewById(R.id.recyclerView);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());

		String watermark = WMStamp.getInstance().getWaterMarkText();
		mRecyclerView.addItemDecoration(new WMAddressDecoration(watermark));

		mTextChangeRunnable = () -> mPresenter.executeQuery(mEtSearch.getText().toString());
		mContactAdapter = new ContactSearchAdapter(this);
		mContactAdapter.setEmptyView(findViewById(R.id.ivEmptyView));
		mRecyclerView.setAdapter(mContactAdapter);
	}

	@Override
	public void bindData() {
		Intent intent = getIntent();
		int dataKeepKey = intent.getIntExtra(K.addressBook.data_keep, -1);
		List<AddressBook> selectedContacts = (List<AddressBook>) DataKeeper.getInstance().getKeepDatas(dataKeepKey);
		mContactAdapter.setSelectedContacts(selectedContacts);

		if (intent.getBooleanExtra(addressBook.except_selected, false)) {
			int cannotSelectedData = intent.getIntExtra(addressBook.cannot_selected, -1);
			List<AddressBook> cannotSelected = (List<AddressBook>) DataKeeper.getInstance().getKeepDatas(cannotSelectedData);
			mContactAdapter.setCannotSelectContacts(cannotSelected);
		}

		if (intent.getBooleanExtra(addressBook.except_self, false)) {
			String userId = CoreZygote.getLoginUserServices().getUserId();
			AddressBook self = CoreZygote.getAddressBookServices().queryUserInfo(userId);
			mContactAdapter.addCannotSelectContacts(self);
		}
		mContactAdapter.isExceptOwn(intent.getBooleanExtra(addressBook.except_own_select, false));
		super.bindData();

		String keyword = intent.getStringExtra("keyword");
		if (!TextUtils.isEmpty(keyword)) {            // 执行搜索
			mEtSearch.setText(keyword);
			mEtSearch.setSelection(keyword.length());
			mHandler.post(mTextChangeRunnable);
		}
		else {
			mHandler.postDelayed(() -> DevicesUtil.showKeyboard(mEtSearch), 500);
		}
	}

	@Override
	public void bindListener() {
		findViewById(R.id.tvSearchCancel).setOnClickListener(view -> {
			int key = getIntent().getIntExtra(K.addressBook.data_keep, -1);
			DataKeeper.getInstance().keepDatas(key, mContactAdapter.getSelectedContacts());
			setResult(K.addressBook.search_result_code);
			finish();
		});
		mClearInputBtn.setOnClickListener(view -> {
			mEtSearch.setText("");
			((ContactSearchAdapter) mContactAdapter).clearContacts();
			mClearInputBtn.setVisibility(View.GONE);
		});

		mRecyclerView.setOnLoadMoreListener(() -> mPresenter.loadMoreContact(mEtSearch.getText().toString()));
		mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
			}

			@Override
			public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
				if (scrollState == SCROLL_STATE_FLING
						|| scrollState == SCROLL_STATE_TOUCH_SCROLL) {
					DevicesUtil.hideKeyboard(getCurrentFocus());
				}
			}
		});
		mContactAdapter.setOnContactItemClickListener(this::onItemClick);

		mEtSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				mClearInputBtn.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
				mHandler.removeCallbacks(mTextChangeRunnable);
				if (s.length() == 0) {
					((ContactSearchAdapter) mContactAdapter).clearContacts();
//					WMStamp.getInstance().draw(ContactSearchActivity.this, mRecyclerView);
					return;
				}
				mHandler.postDelayed(mTextChangeRunnable, 500);
			}
		});
	}

	@Override
	protected void onItemClick(AddressBook addressBook, int position) {
		super.onItemClick(addressBook, position);
		if (getIntent().getBooleanExtra(K.addressBook.start_chat, false)) {
			DevicesUtil.hideKeyboard(mEtSearch);
			if (getIntent().getStringExtra(ChatContanct.EXTRA_FORWARD_MSG_ID) == null) {
				finish();
			}
		}
	}

	@Override
	protected int getPreviewMaxHeight() {
		int location[] = new int[2];
		mContactsConfirmView.getLocationOnScreen(location);
		int confirmViewLocation = location[1];

		mSearchView.getLocationOnScreen(location);
		int searchViewLocation = location[1];

//		int totalHeight = getResources().getDisplayMetrics().heightPixels;
//		totalHeight -= getResources().getDimensionPixelOffset(R.dimen.status_bar_height);
//		totalHeight -= (getResources().getDimensionPixelOffset(R.dimen.action_bar_size) * 1.5F);
//		totalHeight -= getResources().getDimensionPixelOffset(R.dimen.mdp_48);
		int totalHeight = confirmViewLocation - searchViewLocation
				- mContactsConfirmView.getMeasuredHeight() - getResources().getDimensionPixelSize(R.dimen.mdp_9);
		return totalHeight;
	}

	@Override
	protected int getPreviewMarginTop() {
		int[] location = new int[2];
		mSearchView.getLocationOnScreen(location);
		return mSearchView.getMeasuredHeight() / 2 + location[1] + getResources().getDimensionPixelSize(R.dimen.mdp_5);
	}

	@Override
	public void showContacts(List<AddressBook> contacts) {
		mContactAdapter.setContacts(contacts);
//		WMStamp.getInstance().draw(ContactSearchActivity.this, mRecyclerView);
	}

	@Override
	public void addContacts(List<AddressBook> contacts) {
		((ContactSearchAdapter) mContactAdapter).addContacts(contacts);
//		WMStamp.getInstance().draw(ContactSearchActivity.this, mRecyclerView);
	}

	@Override
	public void addFooterView() {
		mContactAdapter.setFooterView(cn.flyrise.feep.core.R.layout.core_refresh_bottom_loading);
	}

	@Override
	public void removeFooterView() {
		mContactAdapter.removeFooterView();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
//		WMStamp.getInstance().clearWaterMark(this);
		this.mHandler.removeCallbacksAndMessages(null);
	}
}
