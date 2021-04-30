package cn.flyrise.feep.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import cn.flyrise.android.protocol.entity.CrmListRequest;
import cn.flyrise.android.protocol.entity.CrmListResponse;
import cn.flyrise.feep.R;
import cn.flyrise.feep.addressbook.adapter.SurnameAdapter;
import cn.flyrise.feep.addressbook.view.LetterFloatingView;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FELetterListView;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.base.views.LoadMoreRecyclerView;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.PixelUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.watermark.WMAddressDecoration;
import cn.flyrise.feep.core.watermark.WMStamp;
import cn.flyrise.feep.main.adapter.ExternalContactListAdapter;
import cn.flyrise.feep.main.model.ExternalContact;

/**
 * @author ZYP
 * @since 2017-05-17 16:17
 */
public class ExternalContactListActivity extends BaseActivity {

    public static final String PAGE_COUNT = "50";
    private LoadMoreRecyclerView mRecyclerView;
    private ExternalContactListAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private LinearLayoutManager mLayoutManager;

    private boolean mIsLoading;
    private int mTotalPage;
    private int mCurrentPage = 1;

    private FELetterListView mLetterView;
    private View mLetterFloatingView;                           // 特么的字母、姓氏索引列表
    private TextView mTvLetterView;
    private ListView mSurnameListView;
    private SurnameAdapter mSurnameAdapter;
    private Handler mHandler = new Handler();

    private WindowManager mWindowManager;
    private Runnable mLetterFloatingRunnable;
    private View mEmptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_external_contact_list);
    }

    @Override
    protected void toolBar(FEToolbar toolbar) {
        toolbar.setTitle("客户联系人");
        toolbar.setRightIcon(R.drawable.icon_search);
        toolbar.setRightImageClickListener(view -> {
            Intent intent = new Intent(ExternalContactListActivity.this, ExternalContactSearchActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void bindView() {
        this.mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        this.mSwipeRefreshLayout.setColorSchemeResources(R.color.login_btn_defulit);
        this.mEmptyView = findViewById(R.id.ivEmptyView);

        this.mRecyclerView = (LoadMoreRecyclerView) findViewById(R.id.recyclerView);
        this.mRecyclerView.setLayoutManager(mLayoutManager = new LinearLayoutManager(this));
        this.mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        String watermark = WMStamp.getInstance().getWaterMarkText();
        mRecyclerView.addItemDecoration(new WMAddressDecoration(watermark));

        this.mRecyclerView.setAdapter(mAdapter = new ExternalContactListAdapter(this));
        this.mAdapter.setOnExternalContactClickListener(externalContact -> {
            Intent intent = new Intent(ExternalContactListActivity.this, ExternalContactDetailActivity.class);
            intent.putExtra("username", externalContact.name);
            intent.putExtra("position", externalContact.position);
            intent.putExtra("phone", externalContact.phone);
            intent.putExtra("connectContact", externalContact.connectContact);
            intent.putExtra("externalCompany", externalContact.company);
            intent.putExtra("department", externalContact.department);
            startActivity(intent);
        });

        mRecyclerView.setOnLoadMoreListener(() -> {
            if (!mIsLoading && mCurrentPage < mTotalPage) {
                mIsLoading = true;
                requestExternalContact(++mCurrentPage, false);
            } else if (mCurrentPage == mTotalPage) {
                mAdapter.removeFooterView();
            }
        });

        mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(true));
        mSwipeRefreshLayout.setOnRefreshListener(() -> requestExternalContact(mCurrentPage = 1, true));
        requestExternalContact(mCurrentPage = 1, true);
    }

    @Override
    public void bindData() {
        mLetterView = (FELetterListView) findViewById(R.id.letterListView);
        mLetterFloatingView = new LetterFloatingView(this);
        mTvLetterView = (TextView) mLetterFloatingView.findViewById(R.id.overlaytext);
        mSurnameListView = (ListView) mLetterFloatingView.findViewById(R.id.overlaylist);
        mSurnameListView.setAdapter(mSurnameAdapter = new SurnameAdapter());
        mLetterFloatingView.setVisibility(View.INVISIBLE);

        mLetterFloatingView.setOnKeyListener((v, keyCode, event) -> {
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
            int surnamePosition = mAdapter.getPositionBySurname(surnameAscii);
            if (surnamePosition != -1) {
                ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(surnamePosition, 0);
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                PixelUtil.dipToPx(300),
                WindowManager.LayoutParams.TYPE_APPLICATION,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);

        lp.gravity = Gravity.TOP | Gravity.RIGHT;
        lp.x = PixelUtil.dipToPx(40);
        lp.y = PixelUtil.dipToPx(128);

        mWindowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        mWindowManager.addView(mLetterFloatingView, lp);
    }

    @Override
    public void bindListener() {
        mLetterFloatingRunnable = () -> mLetterFloatingView.setVisibility(View.GONE);
        mLetterView.setOnTouchingLetterChangedListener(letter -> {                  // 右侧字母索引
            if (mAdapter != null) {
                int selection = letter.toLowerCase().charAt(0);

                int position = mAdapter.getPositionBySelection(selection);
                if (position != -1) {
                    ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset(position, 0);
                }

                List<String> surnames = mAdapter.getSurnameBySelection(selection);
                mTvLetterView.setText(letter);
                mSurnameAdapter.notifyChange(surnames);
                mLetterFloatingView.setVisibility(View.VISIBLE);
                mHandler.removeCallbacks(mLetterFloatingRunnable);
                mHandler.postDelayed(mLetterFloatingRunnable, 3000);
            }
        });

        mRecyclerView.addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();
                    if (firstVisibleItemPosition == 0 && !recyclerView.canScrollVertically(-1)) {
                        mSwipeRefreshLayout.setEnabled(true);
                    } else {
                        mSwipeRefreshLayout.setEnabled(false);
                    }
                }
            }
        });
    }

    /**
     * 请求外部联系人数据
     */
    private void requestExternalContact(int pageSize, boolean isRefresh) {
        CrmListRequest crmListRequest = new CrmListRequest();
        crmListRequest.pageSize = pageSize + "";
        crmListRequest.pageCount = PAGE_COUNT;
        FEHttpClient.getInstance().post(crmListRequest, new ResponseCallback<CrmListResponse>() {
            @Override
            public void onCompleted(CrmListResponse response) {
                if (response != null
                        && TextUtils.equals(response.getErrorCode(), "0")
                        && response.result != null) {   // 请求成功
                    CrmListResponse.Result result = response.result;
                    mTotalPage = result.totalPage;
                    mIsLoading = false;

                    if (mCurrentPage < mTotalPage) {
                        mAdapter.setFooterView(cn.flyrise.feep.core.R.layout.core_refresh_bottom_loading);
                    } else {
                        mAdapter.removeFooterView();
                    }

                    List<ExternalContact> externalContactList = result.externalContactList;
                    if (isRefresh) {
                        if (CommonUtil.isEmptyList(externalContactList)) {
                            mEmptyView.setVisibility(View.VISIBLE);
                        }
                        mAdapter.setExternalContacts(externalContactList);
                        mSwipeRefreshLayout.postDelayed(() -> mSwipeRefreshLayout.setRefreshing(false), 1000);
                    } else {
                        mAdapter.addExternalContacts(externalContactList);
                    }
                    mLetterView.setShowLetters(mAdapter.getLetterList());
                } else {
                    mEmptyView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                mCurrentPage = mCurrentPage > 1 ? mCurrentPage-- : 1;
                FEToast.showMessage("数据加载失败，请重试");
                mEmptyView.setVisibility(View.VISIBLE);
                mRecyclerView.scrollLastItem2Bottom(mAdapter);
            }
        });
    }
}
