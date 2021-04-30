package cn.flyrise.feep.commonality;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import cn.flyrise.android.protocol.entity.ReferenceItemsRequest;
import cn.flyrise.android.protocol.entity.ReferenceItemsResponse;
import cn.flyrise.android.protocol.entity.ReferenceMaintainRequest;
import cn.flyrise.android.protocol.entity.ReferenceMaintainResponse;
import cn.flyrise.android.protocol.model.ReferenceItem;
import cn.flyrise.feep.FEApplication;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;

/**
 * @author ZYP
 * @since 2016-08-25 13:20
 */
public class CommonWordsActivity extends BaseActivity {

    private FEToolbar mToolBar;
    private List<String> mCommonWords;
    private FEApplication mApplication;

    private ListView mCommonWordListView;
    private CommonWordAdapter mCommonWordAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private final Handler mHandler = new Handler();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = (FEApplication) getApplication();
        setContentView(R.layout.activity_common_word);
    }

    @Override protected void toolBar(FEToolbar toolbar) {
        this.mToolBar = toolbar;
        this.mToolBar.setTitle(getResources().getString(R.string.common_language));
        this.mToolBar.setRightText(getResources().getString(R.string.lbl_text_add));
    }

    @Override public void bindView() {
        mCommonWordListView = (ListView) findViewById(R.id.listView);
        mCommonWordListView.setAdapter(mCommonWordAdapter = new CommonWordAdapter());
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.login_btn_defulit);
    }

    @Override public void bindData() {
        String[] commonWords = mApplication.getCommonWords();
        if (commonWords == null || commonWords.length == 0) {
            requestCommonWords(true);
            return;
        }

        if (!commonWords[0].contains("<>")) {
            mToolBar.getRightTextView().setVisibility(View.GONE);
        }

        mCommonWords = Arrays.asList(commonWords);
        mCommonWordAdapter.notifyDataSetChanged();
    }

    @Override public void bindListener() {
        final String[] items = new String[]{getString(R.string.lbl_text_edit), getString(R.string.delete)};
        mCommonWordAdapter.setOnCommonWordClickListener((position, flag) -> {
            if (TextUtils.equals(flag, "0")) {
                return;
            }
            new FEMaterialDialog
                    .Builder(CommonWordsActivity.this)
                    .setCancelable(true)
                    .setWithoutTitle(true)
                    .setItems(items, (dialog, v, p) -> {
                        dialog.dismiss();
                        String commonWord = mCommonWords.get(position);
                        if (p == 0) {
                            CommonWordsFragment.newInstance(commonWord).show(getSupportFragmentManager(), "Edit");
                        }
                        else if (p == 1) {
                            deleteCommonWord(commonWord);
                        }
                    })
                    .build()
                    .show();
        });

        mToolBar.setRightTextClickListener(v -> CommonWordsFragment.newInstance("").show(getSupportFragmentManager(), ""));
        mSwipeRefreshLayout.setOnRefreshListener(() -> requestCommonWords(true));
    }

    private void requestCommonWords(final boolean showWaiting) {
        if (showWaiting) {
            mSwipeRefreshLayout.setRefreshing(true);
        }
        final ReferenceItemsRequest commonWordsReq = new ReferenceItemsRequest();
        commonWordsReq.setRequestType(ReferenceItemsRequest.TYPE_COMMON_WORDS);
        FEHttpClient.getInstance().post(commonWordsReq, new ResponseCallback<ReferenceItemsResponse>(this) {
            @Override public void onCompleted(ReferenceItemsResponse responseContent) {
                if (showWaiting) {
                    stopRefreshing();
                }

                final List<ReferenceItem> items = responseContent.getItems();
                if ("-98".equals(responseContent.getErrorCode())) {
                    mApplication.setCommonWords(getResources().getStringArray(R.array.words));
                    mToolBar.getRightTextView().setVisibility(View.GONE);
                }
                else {
                    mApplication.setCommonWords(convertCommonWords(items));
                }

                mCommonWords = Arrays.asList(mApplication.getCommonWords());
                mCommonWordAdapter.notifyDataSetChanged();
            }

            @Override public void onFailure(RepositoryException repositoryException) {
                if (showWaiting) {
                    stopRefreshing();
                }
                FEToast.showMessage(getResources().getString(R.string.lbl_retry_operator));
            }
        });
    }

    private void stopRefreshing() {
        mHandler.postDelayed(new Runnable() {
            @Override public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 1000);
    }

    public void addCommonWord(final String newCommonWord) {
        final ReferenceMaintainRequest request = new ReferenceMaintainRequest();
        request.setValue(newCommonWord);
        request.setId("");
        request.setRequestType("0");
        request.setReferenceType("1");

        FEHttpClient.getInstance().post(request, new ResponseCallback<ReferenceMaintainResponse>(this) {
            @Override public void onCompleted(ReferenceMaintainResponse responseContent) {
                requestCommonWords(false);
            }

            @Override public void onFailure(RepositoryException repositoryException) {
                FEToast.showMessage(getResources().getString(R.string.lbl_retry_operator));
            }
        });
    }

    private void deleteCommonWord(final String commonWord) {
        String id = commonWord.split("<>")[0];
        final ReferenceMaintainRequest request = new ReferenceMaintainRequest();
        request.setValue("");
        request.setId(id);
        request.setRequestType("1");
        request.setReferenceType("1");
        FEHttpClient.getInstance().post(request, new ResponseCallback<ReferenceMaintainResponse>(this) {
            @Override public void onCompleted(ReferenceMaintainResponse responseContent) {
                requestCommonWords(false);
            }

            @Override public void onFailure(RepositoryException repositoryException) {
                FEToast.showMessage(getResources().getString(R.string.lbl_retry_operator));
            }
        });
    }

    public void updateCommonWord(String newCommonWord, String commonWordId) {
        final ReferenceMaintainRequest request = new ReferenceMaintainRequest();
        request.setValue(newCommonWord);
        request.setId(commonWordId);
        request.setRequestType("2");
        request.setReferenceType("1");

        FEHttpClient.getInstance().post(request, new ResponseCallback<ReferenceMaintainResponse>(this) {
            @Override public void onCompleted(ReferenceMaintainResponse responseContent) {
                requestCommonWords(false);
            }

            @Override public void onFailure(RepositoryException repositoryException) {
                FEToast.showMessage(getResources().getString(R.string.lbl_retry_operator));
            }
        });
    }

    public static String[] convertCommonWords(List<ReferenceItem> items) {
        final String[] commonWords = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            ReferenceItem item = items.get(i);
            commonWords[i] = item.getKey() + "<>" + item.getValue() + "<>" + item.getFlag();
        }
        return commonWords;
    }

    private class CommonWordAdapter extends BaseAdapter {

        private OnCommonWordClickListener mCommonWordClickListener;

        public void setOnCommonWordClickListener(OnCommonWordClickListener listener) {
            this.mCommonWordClickListener = listener;
        }

        @Override public int getCount() {
            return CommonUtil.isEmptyList(mCommonWords) ? 0 : mCommonWords.size();
        }

        @Override public Object getItem(int position) {
            return CommonUtil.isEmptyList(mCommonWords) ? null : mCommonWords.get(position);
        }

        @Override public long getItemId(int position) {
            return position;
        }

        @Override public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = View.inflate(parent.getContext(), R.layout.item_common_word, null);
                convertView.setTag(holder = new ViewHolder(convertView));
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            String id = null;
            String word;
            String flag = "null";
            if (mCommonWords.get(position).contains("<>")) {
                String[] results = mCommonWords.get(position).split("<>");
                id = results[0];
                word = results[1];
                try {
                    flag = results[2];
                } catch (Exception exp) {
                }
            }
            else {
                word = mCommonWords.get(position);
            }

            holder.tvCommonWord.setText(word);
            if (id == null) {
                holder.ivMore.setVisibility(View.GONE);
            }
            else {
                if (flag == null || TextUtils.equals(flag, "null")) {
                    holder.ivMore.setVisibility(View.VISIBLE);
                }
                else {
                    holder.ivMore.setVisibility(TextUtils.equals(flag, "0") ? View.GONE : View.VISIBLE);
                }
                holder.ivMore.setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);
            }

            final String finalFlag = flag;
            holder.ivMore.setOnClickListener(v -> {
                if (mCommonWordClickListener != null) {
                    mCommonWordClickListener.onCommonWordClick(position, finalFlag);
                }
            });

            return convertView;
        }

        private class ViewHolder {
            TextView tvCommonWord;
            ImageView ivMore;

            public ViewHolder(View convertView) {
                tvCommonWord = (TextView) convertView.findViewById(R.id.tvCommonWord);
                ivMore = (ImageView) convertView.findViewById(R.id.ivMore);
            }
        }
    }

    public static String[] convertCommonWord(String[] commonWord) {
        String[] newCommonWord = new String[commonWord.length];
        for (int i = 0, n = commonWord.length; i < n; i++) {
            newCommonWord[i] = commonWord[i].contains("<>") ? commonWord[i].split("<>")[1] : commonWord[i];
        }
        return newCommonWord;
    }

    private interface OnCommonWordClickListener {
        void onCommonWordClick(int position, String flag);
    }
}
