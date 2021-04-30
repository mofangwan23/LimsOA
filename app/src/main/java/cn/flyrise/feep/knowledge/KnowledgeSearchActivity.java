package cn.flyrise.feep.knowledge;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import cn.squirtlez.frouter.annotations.RequestExtras;
import cn.squirtlez.frouter.annotations.Route;
import java.util.List;

import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.PullAndLoadMoreRecyclerView;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.knowledge.adpater.SearchFileListAdapter;
import cn.flyrise.feep.knowledge.contract.SearchListContract;
import cn.flyrise.feep.knowledge.model.SearchFile;
import cn.flyrise.feep.knowledge.presenter.SearchPresenterImpl;
import cn.flyrise.feep.knowledge.util.KnowKeyValue;

/**
 * Created by klc
 */
@Route("/knowledge/search")
@RequestExtras({"EXTRA_FOLDERTYPES"})
public class KnowledgeSearchActivity extends BaseActivity implements SearchListContract.View {

	private EditText metSearch;
	private TextView mBtnSearchCancel;
	private ImageView mIvDeleteIcon;
	private PullAndLoadMoreRecyclerView mListView;
	private View mIv_empty;
	private SearchFileListAdapter adapter;
	private String searchKey;
	private SearchListContract.Presenter mPresenter;
	@SuppressLint("HandlerLeak") private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (msg.what == 10099) {
				final ViewGroup.LayoutParams paramsfc = mListView.getLayoutParams();
				paramsfc.height = DevicesUtil.getScreenHeight();
				mListView.setLayoutParams(paramsfc);
			}
			else if (msg.what == 10012) {
				InputMethodManager inputManager = (InputMethodManager) metSearch.getContext().getSystemService(INPUT_METHOD_SERVICE);
				inputManager.showSoftInput(metSearch, 0);
			}
			else if (msg.what == 10013) {
				InputMethodManager inputManager = (InputMethodManager) metSearch.getContext().getSystemService(INPUT_METHOD_SERVICE);
				inputManager.hideSoftInputFromWindow(metSearch.getWindowToken(), 0); //强制隐藏键盘
			}
		}
	};

	public static void StartSearchListActivity(Context context, int folderType) {
		Intent intent = new Intent(context, KnowledgeSearchActivity.class);
		intent.putExtra(KnowKeyValue.EXTRA_FOLDERTYPES, folderType);
		context.startActivity(intent);
	}


	@Override

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.knowledge_search);
	}

	@Override
	public void bindView() {
		metSearch = findViewById(R.id.etSearch);
		mBtnSearchCancel = findViewById(R.id.tvSearchCancel);
		mIv_empty = findViewById(R.id.ivErrorView);
		mIvDeleteIcon = findViewById(R.id.ivDeleteIcon);
		mListView = findViewById(R.id.listview);
	}

	@Override
	public void bindData() {
		mHandler = new Handler();
		metSearch.setHint(R.string.know_search_file);
		int folderType = getIntent().getIntExtra(KnowKeyValue.EXTRA_FOLDERTYPES, -1);
		mPresenter = new SearchPresenterImpl(this, folderType);
		adapter = new SearchFileListAdapter(this);
		mListView.setAdapter(adapter);
		mHandler.sendEmptyMessageDelayed(10012, 390);

		String keyword = getIntent().getStringExtra("keyword");
		if (!TextUtils.isEmpty(keyword)) {
			metSearch.setText(keyword);
			metSearch.setSelection(keyword.length());
			searchKey = keyword;
			mHandler.post(searchRunnable);
		}
		else {
			mHandler.postDelayed(() -> DevicesUtil.showKeyboard(metSearch), 500);
		}
	}

	@Override
	public void bindListener() {
		mBtnSearchCancel.setOnClickListener(v -> finish());
		mIvDeleteIcon.setOnClickListener(v -> metSearch.setText(""));
		mListView.setRefreshListener(() -> mPresenter.refreshListData(searchKey));
		mListView.setLoadMoreListener(() -> mPresenter.loadMore());
		metSearch.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				searchKey = metSearch.getText().toString().trim();
				mHandler.removeCallbacks(searchRunnable);
				if (!TextUtils.isEmpty(searchKey)) {
					mHandler.postDelayed(searchRunnable, 500);
				}
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (s.length() == 0) {
					mIvDeleteIcon.setVisibility(View.GONE);
					mIv_empty.setVisibility(View.GONE);
					adapter.onRefresh(null);
					mPresenter.cancelSearch();
				}
				else {
					mIvDeleteIcon.setVisibility(View.VISIBLE);
				}
			}
		});
		adapter.setOnItemClickListener((view, object) -> {
			SearchFile searchFile = (SearchFile) object;
			mPresenter.opeFile(KnowledgeSearchActivity.this, searchFile);
		});
		mListView.setOnTouchListener((v, event) -> {
			if (event.getAction() == MotionEvent.ACTION_MOVE) {
				mHandler.sendEmptyMessage(10013);
			}
			return false;
		});
	}

	private final Runnable searchRunnable = new Runnable() {
		@Override
		public void run() {
			adapter.onRefresh(null);
			mPresenter.refreshListData(searchKey);
		}
	};

	@Override
	public void showRefreshLoading(boolean show) {
		if (show)
			mListView.setRefreshing(true);
		else
			mHandler.postDelayed(() -> mListView.setRefreshing(false), 500);
	}

	@Override
	public void refreshListData(List<SearchFile> dataList) {
		adapter.onRefresh(dataList);
		setEmptyView();
	}

	@Override
	public void setEmptyView() {
		if (adapter.getItemCount() == 0) {
			mIv_empty.setVisibility(View.VISIBLE);
		}
		else
			mIv_empty.setVisibility(View.GONE);
	}

	@Override
	public void loadMoreListData(List<SearchFile> dataList) {
		adapter.addData(dataList);
	}

	@Override
	public void loadMoreListFail() {
		mListView.scrollLastItem2Bottom();
	}

	@Override
	public void dealComplete() {

	}

	@Override
	public void showProgress(int text, int progress) {
		LoadingHint.showProgress(progress, getString(text));
	}

	@Override public void openFile(Intent intent) {
		if (intent == null) {
			FEToast.showMessage("暂不支持查看此文件类型");
			return;
		}

		try {
			startActivity(intent);
		} catch (Exception exp) {
			FEToast.showMessage("无法打开，建议安装查看此类型文件的软件");
		}
	}

	@Override
	public void setCanPullUp(boolean hasMore) {
		if (hasMore)
			mListView.addFootView();
		else
			mListView.removeFootView();
	}

	@Override
	public void showDealLoading(boolean show) {
		if (show)
			LoadingHint.show(this);
		else
			LoadingHint.hide();
	}

	@Override
	public void showMessage(int resourceID) {
		FEToast.showMessage(getString(resourceID));
	}
}