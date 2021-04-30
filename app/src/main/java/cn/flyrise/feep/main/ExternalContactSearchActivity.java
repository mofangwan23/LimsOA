package cn.flyrise.feep.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import cn.flyrise.android.protocol.entity.CrmListRequest;
import cn.flyrise.android.protocol.entity.CrmListResponse;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.LoadMoreRecyclerView;
import cn.flyrise.feep.core.base.views.adapter.DividerItemDecoration;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.main.adapter.ExternalContactListAdapter;
import cn.flyrise.feep.main.model.ExternalContact;

/**
 * @author ZYP
 * @since 2017-05-18 13:58
 */
public class ExternalContactSearchActivity extends BaseActivity {

    private LoadMoreRecyclerView mRecyclerView;
    private ExternalContactListAdapter mAdapter;

    private EditText mEditText;
    private ImageView mIvDeleteIcon;
    private View mEmptyView;

    private int mCurrentPage;
    private int mTotalPage;
    private long mStartTime;
    private boolean mIsLoading;

    private String searchKey;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 1024) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - mStartTime < 500) {
                    return false;
                }
                searchMail(mCurrentPage = 1, true);
            }
            return true;
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_external_contact_search);
    }

    @Override
    public void bindView() {
        mRecyclerView = (LoadMoreRecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setAdapter(mAdapter = new ExternalContactListAdapter(this));
        mAdapter.setLetterVisible(false);

        mEditText = (EditText) findViewById(R.id.etSearch);
        mEmptyView =  findViewById(R.id.ivErrorView);
        mIvDeleteIcon = (ImageView) findViewById(R.id.ivDeleteIcon);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditText, 0);
    }

    @Override
    public void bindData() {
        this.searchKey = "";
    }


    @Override
    public void bindListener() {
        findViewById(R.id.tvSearchCancel).setOnClickListener(view -> finish());

        mIvDeleteIcon.setOnClickListener(v -> {
            mEditText.setText("");
            mIvDeleteIcon.setVisibility(View.GONE);
        });

        mEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mStartTime = System.currentTimeMillis();
                searchKey = s.toString().trim();
                mIvDeleteIcon.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                if (s.length() == 0) {
                    mAdapter.setExternalContacts(null);
                    return;
                }
                Message msg = mHandler.obtainMessage();
                msg.what = 1024;
                mHandler.sendMessageDelayed(msg, 500);
            }


        });

        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchMail(mCurrentPage = 1, true);
                    return true;
                }
                return false;
            }
        });

        mRecyclerView.setOnLoadMoreListener(new LoadMoreRecyclerView.OnLoadMoreListener() {
            @Override
            public void loadMore() {
                if (!mIsLoading && mCurrentPage < mTotalPage) {
                    mIsLoading = true;
                    searchMail(++mCurrentPage, false);
                } else if (mCurrentPage >= mTotalPage) {
                    mAdapter.removeFooterView();
                }
            }
        });

        mAdapter.setOnExternalContactClickListener(externalContact -> {
            Intent intent = new Intent(ExternalContactSearchActivity.this, ExternalContactDetailActivity.class);
            intent.putExtra("username", externalContact.name);
            intent.putExtra("position", externalContact.position);
            intent.putExtra("phone", externalContact.phone);
            intent.putExtra("connectContact", externalContact.connectContact);
            intent.putExtra("externalCompany", externalContact.company);
            intent.putExtra("department", externalContact.department);
            startActivity(intent);
        });
    }

    private void searchMail(int pageNumber, boolean isRefresh) {
        CrmListRequest crmListRequest = new CrmListRequest();
        crmListRequest.pageSize = pageNumber + "";
        crmListRequest.pageCount = "50";
        crmListRequest.keyword = searchKey;

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
                    } else {
                        mAdapter.addExternalContacts(externalContactList);
                    }
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
