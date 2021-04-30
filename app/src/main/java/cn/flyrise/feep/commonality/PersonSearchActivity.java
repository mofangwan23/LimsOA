package cn.flyrise.feep.commonality;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.library.utility.interpolator.BackInterpolator;
import cn.flyrise.android.library.view.addressbooklistview.AddressBookListView;
import cn.flyrise.android.library.view.addressbooklistview.been.AddressBookListItem;
import cn.flyrise.android.protocol.model.AddressBookItem;
import cn.flyrise.feep.R;
import cn.flyrise.feep.collaboration.view.Avatar;
import cn.flyrise.feep.collaboration.view.workflow.WorkFlowNode;
import cn.flyrise.feep.collaboration.view.workflow.WorkFlowView;
import cn.flyrise.feep.commonality.adapter.PersonAdapter;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.event.EventFormAddsignSearchPersonData;
import cn.flyrise.feep.event.EventFormSearchPersonChooseData;

/**
 * 陈冕
 * 功能：人员搜索
 * Created by Administrator on 2016-3-17.
 * Update by klc on 2016-11-16
 */
public class PersonSearchActivity extends BaseActivity {
    private String searchKey;
    private String lastKey = "";
    /**
     * 是否是搜索状态
     */
    private ImageView ivDeleteText;
    private EditText etSearch;
    private TextView btnSearchCancle;
    private AddressBookListView searchListView;
    public static final String REQUESTNAME = "request_NAME";
    private PersonAdapter searchAdapter;
    private static int addressBookItemType;
    private static WorkFlowView wfv;
    private RelativeLayout rlSearchFrameDelete;
    private RelativeLayout lvLayout;
    private View errorImg;
    protected int activityCloseEnterAnimation;
    protected int activityCloseExitAnimation;

    private SwipeRefreshLayout swipeRefresh;
    private static String mContext;

    private AddressBookListItem currentItemInfo;

    /**
     * 用于显示当前数据
     */
    private final Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 10099) {
                final ViewGroup.LayoutParams paramsfc = searchListView.getLayoutParams();
                paramsfc.height = DevicesUtil.getScreenHeight();
                searchListView.setLayoutParams(paramsfc);
                errorImg.setVisibility(View.GONE);
            }
            if (msg.what == 10012) {
                InputMethodManager inputManager = (InputMethodManager) etSearch.getContext().getSystemService(INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(etSearch, 0);
            }
            if (msg.what == 10013) {
                InputMethodManager inputManager = (InputMethodManager) etSearch.getContext().getSystemService(INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(etSearch.getWindowToken(), 0); //强制隐藏键盘
            }
        }
    };


    public static void setPersonSearchActivity(int type, WorkFlowView workFlowView) {
        addressBookItemType = type;
        wfv = workFlowView;
    }

    public static void setPersonSearchActivity(int type, String context) {
        addressBookItemType = type;
        mContext = context;
    }

    private final Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            if (!TextUtils.isEmpty(searchKey))
                request(1, searchKey);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_search_layout);
    }

    @Override
    public void bindView() {
        super.bindView();
        ivDeleteText = (ImageView) this.findViewById(R.id.ivDeleteText);
        etSearch = (EditText) this.findViewById(R.id.etSearch);
        btnSearchCancle = (TextView) this.findViewById(R.id.btnSearchCancle);
        searchListView = (AddressBookListView) this.findViewById(R.id.form_search_listview);
        searchListView.setDiverHide();
        searchListView.setMode(PullToRefreshBase.Mode.DISABLED);
        rlSearchFrameDelete = (RelativeLayout) this.findViewById(R.id.rlSearchFrameDelete);
        lvLayout = (RelativeLayout) this.findViewById(R.id.the_contact_search_relative);
        errorImg =  this.findViewById(R.id.error_layout);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        searchListView.setSearchEt(etSearch);
        searchListView.setMode(PullToRefreshBase.Mode.DISABLED);
    }

    @Override
    public void bindData() {
        intiFinishAnimation();
        currentItemInfo = new AddressBookListItem();
        myHandler.sendEmptyMessageDelayed(10012, 390);
        Intent intent = getIntent();
        String name;
        if (intent != null) {
            name = intent.getStringExtra(REQUESTNAME);
            if (!TextUtils.isEmpty(name)) {
                etSearch.setHint(R.string.search_empty_searchkey);
            }
        }
        searchAdapter = new PersonAdapter(this, myHandler, true, Avatar.DEPARTMENT_SEARCH_TYPE);
        searchListView.setAdapter(searchAdapter);
    }

    private void intiFinishAnimation() {
        //完善关闭动画
        TypedArray activityStyle = getTheme().obtainStyledAttributes(new int[]{android.R.attr.windowAnimationStyle});
        int windowAnimationStyleResId = activityStyle.getResourceId(0, 0);
        activityStyle.recycle();
        activityStyle = getTheme().obtainStyledAttributes(windowAnimationStyleResId, new int[]{android.R.attr.activityCloseEnterAnimation, android.R.attr.activityCloseExitAnimation});
        activityCloseEnterAnimation = activityStyle.getResourceId(0, 0);
        activityCloseExitAnimation = activityStyle.getResourceId(Integer.valueOf(1), 0);
        activityStyle.recycle();
        //end
    }

    @Override
    public void bindListener() {
        rlSearchFrameDelete.setOnClickListener(v -> finish());
        lvLayout.setOnClickListener(v -> finish());
        searchListView.setOnItemClickListener(onItemClickListener);
        ivDeleteText.setOnClickListener(v -> {
            etSearch.setText("");
            currentItemInfo.clearListDatas();
            errorImg.setVisibility(View.GONE);
            searchAdapter.refreshAdapter(currentItemInfo.getListDatas());
            DevicesUtil.hideKeyboard(getCurrentFocus());
        });
        etSearch.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchKey = etSearch.getText().toString().trim();
                if (!TextUtils.isEmpty(searchKey)) {
                    myHandler.removeCallbacks(searchRunnable);
                    myHandler.postDelayed(searchRunnable, 500);
                } else {
                    myHandler.removeCallbacks(searchRunnable);
                    clearListViewData();
                    errorImg.setVisibility(View.GONE);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
                if (s.length() == 0) {
                    ivDeleteText.setVisibility(View.GONE);
                } else {
                    ivDeleteText.setVisibility(View.VISIBLE);
                }
            }
        });
        searchListView.setOnLoadListener(new AddressBookListView.OnLoadListener() {
            @Override
            public void Loading(AddressBookListItem bookListItem) {
            }

            @Override
            public void Loaded(AddressBookListItem bookListItem) {
//                FELog.e("bookListItem: getDataPage" + bookListItem.getDataPage());
//                FELog.e("bookListItem: getTotalNums" + bookListItem.getTotalNums());
//                FELog.e("bookListItem: getListDatas" + bookListItem.getListDatas().size());
                currentItemInfo = bookListItem;
                refreshListView();
            }
        });
        btnSearchCancle.setOnClickListener(v -> finish());
        searchListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_FLING
                        || scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                    DevicesUtil.hideKeyboard(getCurrentFocus());
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    swipeRefresh.setEnabled(true);
                } else if (firstVisibleItem + visibleItemCount == totalItemCount) {
                    swipeRefresh.setEnabled(false);
                } else {
                    swipeRefresh.setEnabled(false);
                }
            }
        });

        swipeRefresh.setColorSchemeColors(R.color.login_btn_defulit);
        swipeRefresh.setOnRefreshListener(() -> {
            if (TextUtils.isEmpty(searchKey)) {
                swipeRefresh.setRefreshing(false);
            } else {
                request(1, searchKey);
            }
        });
        searchListView.setOnTouchListener(onTouchListener);
    }

    private View.OnTouchListener onTouchListener = (v, event) -> {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            myHandler.sendEmptyMessage(10013);
        }
        return false;
    };

    /**
     * 请求数据
     */
    private void request(int page, String searchKey) {
        if (!TextUtils.equals(lastKey, searchKey)) {
            page = 1;
            currentItemInfo.clearListDatas();
        }
        lastKey = searchKey;
        try {
            searchListView.setAddressBookItemType(addressBookItemType);
            searchListView.requestSearch("", page, searchKey, "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void clearListViewData() {
        currentItemInfo.setTotalNums(0);
        currentItemInfo.clearListDatas();
        refreshListView();
    }

    /**
     * 刷新列表需要做的事
     */
    private void refreshListView() {
        if (LoadingHint.isLoading()) {
            LoadingHint.hide();
        }
        if (searchAdapter != null) {
            List<AddressBookListItem> lists = currentItemInfo.getListDatas();
            if (lists != null && lists.size() > 0) {
                myHandler.sendEmptyMessage(10099);
                searchAdapter.refreshAdapter(currentItemInfo.getListDatas());
            } else {
                searchAdapter.refreshAdapter(null);
            }

        }
        swipeRefresh.setRefreshing(false);
        searchListView.onRefreshComplete();
        setPullToRefreshAble();
        setEmptyVisible();
    }

    /**
     * 根据数据设置空目录提示
     */
    private void setEmptyVisible() {
        if (currentItemInfo.getTotalNums() == 0) {
            errorImg.setVisibility(View.VISIBLE);
        } else {
            errorImg.setVisibility(View.GONE);
        }
    }

    /**
     * 设置是否可以上下拉加载数据
     */
    private void setPullToRefreshAble() {

        if (!CommonUtil.isEmptyList(currentItemInfo.getListDatas()) && currentItemInfo.getTotalNums() > currentItemInfo.getListDatas().size()) {
            searchListView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
        } else {
            searchListView.setMode(PullToRefreshBase.Mode.DISABLED);
        }
    }

    //点击事件
    private final AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            View focusView = getCurrentFocus();
            if (view != null) {
                DevicesUtil.hideKeyboard(focusView);
            }
            try {
                final AddressBookItem addingNode = searchAdapter.getItem(position).getAddressBookItem();
                if ("FormAddsignActivity".equals(mContext)) {
                    EventFormAddsignSearchPersonData data = new EventFormAddsignSearchPersonData();
                    data.addingNode = addingNode;
                    EventBus.getDefault().post(data);
                } else if ("FormPersonChooseActivity".equals(mContext)) {
                    EventFormSearchPersonChooseData data = new EventFormSearchPersonChooseData();
                    data.addingNode = addingNode;
                    EventBus.getDefault().post(data);
                } else {
                    final WorkFlowNode wfn = new WorkFlowNode();
                    wfn.setNodeName(addingNode.getName());
                    wfn.setNodeId(addingNode.getId());
                    wfn.setType(addingNode.getType());
                    if (wfv != null) {
                        wfv.addNode(wfn);
                    }
                }
                PersonSearchActivity.this.finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            currentItemInfo.clearListDatas();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchListView.setSearchShow(true);
//        setEdittextAnimator(etSearch);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mContext = null;
        searchListView.setSearchShow(false);
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(activityCloseEnterAnimation, activityCloseExitAnimation);
    }

    private void setEdittextAnimator(final View view) {
        ObjectAnimator animatorSx = ObjectAnimator.ofFloat(view, "scaleX", 0.5f, 1f);
        animatorSx.setDuration(500);
        animatorSx.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.setVisibility(View.VISIBLE);
                btnSearchCancle.setVisibility(View.VISIBLE);
            }
        });
        animatorSx.setInterpolator(new BackInterpolator(2f));
        animatorSx.start();
    }
}
