package cn.flyrise.feep.email;

import android.content.Context;
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

import cn.flyrise.android.protocol.entity.BoxDetailRequest;
import cn.flyrise.android.protocol.entity.BoxDetailResponse;
import cn.flyrise.android.protocol.model.Mail;
import cn.flyrise.feep.K;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.LoadMoreRecyclerView;
import cn.flyrise.feep.core.base.views.adapter.DividerItemDecoration;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.email.adapter.MailBoxAdapter_back;
import cn.squirtlez.frouter.annotations.RequestExtras;
import cn.squirtlez.frouter.annotations.Route;

/**
 * @author ZYP
 * @since 2016/7/19 14:19
 */
@Route("/mail/search")
@RequestExtras({"extra_box_name", "mail_search_text"})
public class MailSearchActivity extends BaseActivity {

    private LoadMoreRecyclerView mRecyclerView;
    private MailBoxAdapter_back mMailBoxAdapter;

    private EditText mEditText;
    private ImageView mIvDeleteIcon;
    private View mEmptyView;

    private int mCurrentPage;
    private int mTotalPage;
    private long mStartTime;
    private String mBoxName;
    private String mMailAccount;
    private boolean mIsLoading;

    private String searchKey;
    private String mSearchText;   //语音识别用到

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == 1024) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - mStartTime < 500) {
                    return false;
                }
                searchMail(mCurrentPage = 1);
            }
            return true;
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBoxName = getIntent().getStringExtra(K.email.box_name);
        mMailAccount = getIntent().getStringExtra(K.email.mail_account);
        mSearchText = getIntent().getStringExtra(K.email.mail_search_text);
        setContentView(R.layout.email_search);
        mHandler.postDelayed(()->DevicesUtil.showKeyboard(mEditText), 100);
    }

    @Override
    public void bindView() {
        mRecyclerView = (LoadMoreRecyclerView) findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setAdapter(mMailBoxAdapter = new MailBoxAdapter_back(this));

        mEditText = (EditText) findViewById(R.id.etSearch);
        mEmptyView =  findViewById(R.id.ivErrorView);
        mIvDeleteIcon = (ImageView) findViewById(R.id.ivDeleteIcon);
        mMailBoxAdapter.setEmptyView(mEmptyView);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mEditText, 0);
    }

    @Override
    public void bindData() {
        this.searchKey = "";
    }


    @Override
    public void bindListener() {
        findViewById(R.id.tvSearchCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mIvDeleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEditText.setText("");
                mIvDeleteIcon.setVisibility(View.GONE);
            }
        });

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mStartTime = System.currentTimeMillis();
                searchKey = s.toString().trim();
                mIvDeleteIcon.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    mEmptyView.setVisibility(View.GONE);
                    mMailBoxAdapter.setMailList(null);
                    mEmptyView.setVisibility(View.GONE);
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
                    searchMail(mCurrentPage = 1);
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
                    searchMail(++mCurrentPage);
                } else if (mCurrentPage >= mTotalPage) {
                    mMailBoxAdapter.removeFooterView();
                }
            }
        });

        mMailBoxAdapter.setOnMailItemClickListener(new MailBoxAdapter_back.OnMailItemClickListener() {
            @Override
            public void onMailItemClick(View view, Mail mail, int position) {
                View focusView = getCurrentFocus();
                if (view != null) {
                    DevicesUtil.hideKeyboard(focusView);
                }
                String boxName = mBoxName;
                String mailId = mail.mailId;
                MailDetailActivity.startMailDetailActivity(MailSearchActivity.this, boxName, mailId, mMailAccount);
            }
        });

        if (!TextUtils.isEmpty(mSearchText)) {
            mEditText.setText(mSearchText);
            mEditText.setSelection(mSearchText.length());
        }
    }

    private void searchMail(int pageNumber) {
        BoxDetailRequest request = new BoxDetailRequest();
        request.boxName = mBoxName;
        request.mailname = mMailAccount;
        request.tit = searchKey;
        request.pageNumber = pageNumber;

        FEHttpClient.getInstance().post(request, new ResponseCallback<BoxDetailResponse>(this) {
            @Override
            public void onCompleted(BoxDetailResponse responseContent) {
                mIsLoading = false;
                if (TextUtils.isEmpty(searchKey)) return;
                List<Mail> mailLists = responseContent.mailList;
                mTotalPage = responseContent.pageCount;
                if (mCurrentPage == 1) {
                    mMailBoxAdapter.setMailList(mailLists);
                    if (mTotalPage > 1) {
                        mMailBoxAdapter.setFooterView(cn.flyrise.feep.core.R.layout.core_refresh_bottom_loading);
                    }
                } else {
                    mMailBoxAdapter.addMailList(mailLists);
                }
            }

            @Override
            public void onFailure(RepositoryException repositoryException) {
                mRecyclerView.scrollLastItem2Bottom(mMailBoxAdapter);
                mIsLoading = false;
                if (TextUtils.isEmpty(searchKey)) return;
                mMailBoxAdapter.setMailList(null);
            }
        });
    }
}
