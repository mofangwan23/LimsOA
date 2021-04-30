package cn.flyrise.feep.retrieval;

import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_FLING;
import static android.widget.AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL;
import static cn.flyrise.feep.retrieval.DataRetrievalContract.STATE_EMPTY;
import static cn.flyrise.feep.retrieval.DataRetrievalContract.STATE_RESET;
import static cn.flyrise.feep.retrieval.DataRetrievalContract.STATE_SUCCESS;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.media.record.camera.util.DeviceUtil;
import cn.flyrise.feep.retrieval.adapter.DataRetrievalAdapter;
import cn.flyrise.feep.retrieval.adapter.DataRetrievalAdapter.OnRetrievalItemClickListener;
import cn.flyrise.feep.retrieval.adapter.RetrievalTypeAdapter;
import cn.flyrise.feep.retrieval.bean.Retrieval;
import cn.flyrise.feep.retrieval.dispatcher.RetrievalClickEventDispatcher;
import cn.flyrise.feep.retrieval.vo.RetrievalType;
import java.util.List;

/**
 * @author ZYP
 * @since 2018-04-28 10:22
 * 数据检索界面，标准产品最多支持9种数据的检索：新闻、公告、会议、审批、日程、计划、文件、联系人、聊天记录
 * 按照服务端给过来的 SearchType 进行排序。
 */
public class DataRetrievalActivity extends BaseActivity implements DataRetrievalContract.IView {

	private static final int CODE_START_SEARCH = 1024;  // 开始搜索
	private long mStartTime;                            // 开始时间

	private EditText mEtSearchContent;
	private ImageView mIvDeleteIcon;
	private ViewGroup mLayoutKeyWordArea;
	private GridView mRetrievalTypeGridView;
	private RetrievalTypeAdapter mRetrievalTypeAdapter;

	private View mIvEmptyView;
	private RecyclerView mRecyclerView;
	private DataRetrievalAdapter mAdapter;
	private DataRetrievalContract.IPresenter mPresenter;
	private RetrievalClickEventDispatcher mEventDispatcher;

	private Handler mHandler = new Handler(new Callback() {
		@Override public boolean handleMessage(Message msg) {
			if (msg.what == CODE_START_SEARCH) {
				long currentTime = System.currentTimeMillis();
				if (currentTime - mStartTime < 500) {
					return false;
				}

				mPresenter.executeQuery(getInputText());
			}
			return true;
		}
	});

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPresenter = new DataRetrievalPresenter(this, new DataRetrievalRepository());
		setContentView(R.layout.dr_activity_retrieval);
		mPresenter.start();

		mHandler.postDelayed(() -> DevicesUtil.showKeyboard(mEtSearchContent), 100);
	}

	@Override public void bindView() {
		mEtSearchContent = findViewById(R.id.drEtSearch);
		mIvDeleteIcon = findViewById(R.id.drIvDeleteIcon);
		mIvEmptyView = findViewById(R.id.drIvErrorView);

		mLayoutKeyWordArea = findViewById(R.id.drLayoutKeyWordArea);
		mRetrievalTypeGridView = findViewById(R.id.drGVSearchType);
		mRetrievalTypeGridView.setAdapter(mRetrievalTypeAdapter = new RetrievalTypeAdapter());

		mRecyclerView = findViewById(R.id.drRecyclerView);
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		mRecyclerView.setItemAnimator(new DefaultItemAnimator());
		mRecyclerView.setAdapter(mAdapter = new DataRetrievalAdapter(this));
	}

	@Override public void bindListener() {
		findViewById(R.id.drTvSearchCancel).setOnClickListener(view -> finish());
		mIvDeleteIcon.setOnClickListener(view -> {      // 清空搜索内容
			mEtSearchContent.setText("");
			mIvDeleteIcon.setVisibility(View.GONE);
			mAdapter.setDataSources(null);
		});

		mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
			}

			@Override public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
				if (scrollState == SCROLL_STATE_FLING
						|| scrollState == SCROLL_STATE_TOUCH_SCROLL) {
					DevicesUtil.hideKeyboard(getCurrentFocus());
				}
			}
		});
		mEtSearchContent.addTextChangedListener(new TextWatcher() {
			@Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

			@Override public void afterTextChanged(Editable s) { }

			@Override public void onTextChanged(CharSequence s, int start, int before, int count) {
				mStartTime = System.currentTimeMillis();
				mIvDeleteIcon.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
				if (s.length() == 0) {  // 清空搜索栏
					updateContentLayout(STATE_RESET);
					return;
				}
				mHandler.sendEmptyMessageDelayed(CODE_START_SEARCH, 500);
			}
		});
		mEtSearchContent.setOnEditorActionListener((v, actionId, event) -> {
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				mPresenter.executeQuery(v.getText().toString());
				return true;
			}
			return false;
		});
		mAdapter.setOnRetrievalItemClickListener(new OnRetrievalItemClickListener() {
			@Override public void onFooterMoreClick(int retrievalType) {
				if (mEventDispatcher != null) {
					mEventDispatcher.dispatcherMoreEvent(retrievalType, getInputText());
				}
			}

			@Override public void onRetrievalClick(Retrieval retrieval) {
				if (mEventDispatcher != null) {
					mEventDispatcher.dispatcherClickEvent(retrieval);
				}
			}
		});
		mRetrievalTypeGridView.setOnItemClickListener((parent, view, position, id) -> {
			if (mEventDispatcher != null) {
				RetrievalType retrievalType = (RetrievalType) mRetrievalTypeAdapter.getItem(position);
				mEventDispatcher.dispatcherMoreEvent(retrievalType.getRetrievalType(), getInputText());
			}
		});
	}

	private String getInputText() {
		return mEtSearchContent == null ? "" : mEtSearchContent.getText().toString().trim();
	}

	@Override public void onSearchTypeSuccess(List<RetrievalType> retrievalTypes) {
		mLayoutKeyWordArea.setVisibility(View.VISIBLE);
		mRetrievalTypeAdapter.setDataSource(retrievalTypes);
		mEventDispatcher = new RetrievalClickEventDispatcher(this, retrievalTypes);
	}

	@Override public void onSearchTypeFailure() {
		FELog.e("Could not get the retrieval types.");
	}

	@Override public void onRetrievalDataArrival(List<? extends Retrieval> retrievals) {
		if (TextUtils.isEmpty(getInputText())) {
			updateContentLayout(STATE_RESET);
			return;
		}
		mAdapter.setDataSources(retrievals);
	}

	@Override public void onRetrievalServicePrepared(IRetrievalServices retrievalServices) {
		mAdapter.setRetrievalServices(retrievalServices);
	}

	@Override public void updateContentLayout(byte state) {
		mLayoutKeyWordArea.setVisibility(STATE_RESET == state ? View.VISIBLE : View.GONE);
		mIvEmptyView.setVisibility(STATE_EMPTY == state ? View.VISIBLE : View.GONE);
		mRecyclerView.setVisibility(STATE_SUCCESS == state ? View.VISIBLE : View.GONE);
	}

	@Override public Context getContext() {
		return this;
	}

	@Override protected int statusBarColor() {
		return Color.BLACK;
	}
}
