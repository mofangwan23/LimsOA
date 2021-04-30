package cn.flyrise.feep.addressbook;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.drop.WaterDropSwipRefreshLayout;

import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.adapter.MineDepartmentAdapter;
import cn.flyrise.feep.addressbook.adapter.SurnameAdapter;
import cn.flyrise.feep.addressbook.source.AddressBookRepository;
import cn.flyrise.feep.addressbook.view.LetterFloatingView;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FELetterListView;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.PixelUtil;
import cn.flyrise.feep.core.dialog.FELoadingDialog;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.core.watermark.WMAddressDecoration;
import cn.flyrise.feep.core.watermark.WMStamp;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cn.flyrise.feep.meeting7.ui.component.StatusView;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static cn.flyrise.feep.meeting7.ui.component.StatusViewKt.STATE_EMPTY;

/**
 * 我的关注列表。
 * update by ZYP in 2017-02-14
 */
public class MineAttentionActivity extends BaseActivity {

	private Handler mHandler = new Handler();
	private WaterDropSwipRefreshLayout mSwipeRefreshLayout;

	private RecyclerView mRecyclerView;
	private MineDepartmentAdapter mContactAdapter;
	private FELetterListView mLetterView;

	private View mLetterFloatingView;                           // 特么的字母、姓氏索引列表
	private TextView mTvLetterView;
	private ListView mSurnameListView;
	private SurnameAdapter mSurnameAdapter;

	private WindowManager mWindowManager;
	private Runnable mLetterFloatingRunnable;
	private StatusView mStatusView;                         // 八卦图：巨他妈牛逼

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_my_attention);
	}

	@Override protected void toolBar(FEToolbar toolbar) {
		toolbar.setTitle(R.string.my_attention);
	}

	@Override public void bindView() {
		mSwipeRefreshLayout = (WaterDropSwipRefreshLayout) findViewById(R.id.swipeRefreshLayout);
		mLetterView = (FELetterListView) findViewById(R.id.letterListView);
		mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);
		mStatusView = (StatusView) findViewById(R.id.statusview);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		mRecyclerView.setItemAnimator(null);
		mSwipeRefreshLayout.setColorSchemeResources(R.color.defaultColorAccent);

		String watermark = WMStamp.getInstance().getWaterMarkText();
		mRecyclerView.addItemDecoration(new WMAddressDecoration(watermark));

		mContactAdapter = new MineDepartmentAdapter(this);
		mStatusView.setStatus(STATE_EMPTY);
		mContactAdapter.setEmptyView(mStatusView);
		mRecyclerView.setAdapter(mContactAdapter);
	}

	@Override public void bindData() {
		this.bindLetterFloatingView();
		AddressBookRepository.get().queryMineAttentionUsers()
				.map(results -> {
					Collections.sort(results, new Comparator<AddressBook>() {
						@Override public int compare(AddressBook lhs, AddressBook rhs) {
							String lhsp = lhs.pinyin.substring(0, 1);
							String rhsp = rhs.pinyin.substring(0, 1);
							if (!TextUtils.equals(lhsp, rhsp)) return lhsp.compareTo(rhsp);

							String lhsn = lhs.name.substring(0, 1);
							String rhsn = rhs.name.substring(0, 1);
							if (!TextUtils.equals(lhsn, rhsn)) return lhsn.compareTo(rhsn);

							String lhsd = lhs.deptGrade;
							String rhsd = rhs.deptGrade;
							if (!TextUtils.equals(lhsd, rhsd)) return lhsd.compareTo(rhsd);
							return lhs.sortNo.compareTo(rhs.sortNo);

						}
					});
					return results;
				})
				.subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(this::showContacts, exception -> exception.printStackTrace());
	}

	private void showContacts(List<AddressBook> addressBooks) {
		mContactAdapter.setContacts(addressBooks);
		mContactAdapter.buildSelection(addressBooks);
		List<String> letter = mContactAdapter.getLetterList();
		mLetterView.setShowLetters(letter);
	}

	@Override
	public void bindListener() {
		mLetterFloatingRunnable = () -> mLetterFloatingView.setVisibility(View.GONE);
		mLetterView.setOnTouchingLetterChangedListener(letter -> {                  // 右侧字母索引
			if (mContactAdapter != null) {
				int selection = letter.toLowerCase().charAt(0);

				int position = mContactAdapter.getPositionBySelection(selection);
				if (position != -1) {
					((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(position, 0);
				}

				List<String> surnames = mContactAdapter.getSurnameBySelection(selection);
				mTvLetterView.setText(letter);
				mSurnameAdapter.notifyChange(surnames);
				mLetterFloatingView.setVisibility(View.VISIBLE);
				mHandler.removeCallbacks(mLetterFloatingRunnable);
				mHandler.postDelayed(mLetterFloatingRunnable, 3000);
			}
		});

		mContactAdapter.setOnContactItemClickListener(this::onItemClick);

		mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			private void call(List<AddressBook> addressBooks) {
				showContacts(addressBooks);
			}

			@Override
			public void onRefresh() {
				AddressBookRepository.get().queryMineAttentionUsers()
						.subscribeOn(Schedulers.io())
						.doOnNext(addressBooks -> Collections.sort(addressBooks,
								(lhs, rhs) -> lhs.pinyin.toLowerCase().compareTo(rhs.pinyin.toLowerCase())))
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(this::call, exception -> exception.printStackTrace());
				Observable.timer(2, TimeUnit.SECONDS)
						.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe(time -> mSwipeRefreshLayout.setRefreshing(false));
			}
		});
	}

	protected void onItemClick(AddressBook addressBook, int position) {
		Intent startIntent = new Intent(this, AddressBookDetailActivity.class);
		startIntent.putExtra(K.addressBook.user_id, addressBook.userId);
		startIntent.putExtra(K.addressBook.department_id, addressBook.deptId);
		startActivity(startIntent);
	}

	private void bindLetterFloatingView() {
		mLetterFloatingView = new LetterFloatingView(this);
		mTvLetterView = mLetterFloatingView.findViewById(R.id.overlaytext);
		mSurnameListView = mLetterFloatingView.findViewById(R.id.overlaylist);
		mSurnameListView.setAdapter(mSurnameAdapter = new SurnameAdapter());
		mLetterFloatingView.setVisibility(View.INVISIBLE);

		mLetterFloatingView.setOnKeyListener((v, keyCode, event) -> {
			FELog.i("AddressBookActivity key listener : " + keyCode);
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK || event.getKeyCode() == KeyEvent.KEYCODE_SETTINGS) {
				if (mLetterFloatingView.getVisibility() == View.VISIBLE) {
					mLetterFloatingView.setVisibility(View.GONE);
					finish();
				}
			}
			return false;
		});

		mSurnameListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				mHandler.removeCallbacks(mLetterFloatingRunnable);
				mHandler.postDelayed(mLetterFloatingRunnable, 2000);
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			}
		});

		mSurnameListView.setOnItemClickListener((parent, view, position, id) -> {
			mHandler.removeCallbacks(mLetterFloatingRunnable);
			mHandler.postDelayed(mLetterFloatingRunnable, 2000);
			String surname = (String) mSurnameAdapter.getItem(position);
			int surnameAscii = surname.charAt(0);
			int surnamePosition = mContactAdapter.getPositionBySurname(surnameAscii);
			if (surnamePosition != -1) {
				((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(surnamePosition, 0);
			}
		});

		WindowManager.LayoutParams lp = new WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, PixelUtil.dipToPx(300),
				WindowManager.LayoutParams.TYPE_APPLICATION,
				WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
				PixelFormat.TRANSLUCENT);

		lp.gravity = Gravity.TOP | Gravity.RIGHT;
		lp.x = PixelUtil.dipToPx(40);
		lp.y = PixelUtil.dipToPx(128);

		mWindowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
		mWindowManager.addView(mLetterFloatingView, lp);
	}

}
