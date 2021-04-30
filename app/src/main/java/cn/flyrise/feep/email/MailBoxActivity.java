package cn.flyrise.feep.email;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.view.View;
import android.webkit.CookieManager;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.protocol.entity.BoxDetailRequest;
import cn.flyrise.android.protocol.entity.BoxDetailResponse;
import cn.flyrise.android.protocol.model.EmailNumber;
import cn.flyrise.android.protocol.model.Mail;
import cn.flyrise.feep.K;
import cn.flyrise.feep.K.email;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.utils.CommonUtil;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.services.ILoginUserServices;
import cn.flyrise.feep.email.adapter.MailBoxAdapter;
import cn.flyrise.feep.notification.NotificationController;
import cn.squirtlez.frouter.annotations.RequestExtras;
import cn.squirtlez.frouter.annotations.Route;
import com.handmark.pulltorefresh.library.LoadingLayoutProxy;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshExpandableListView;
import com.handmark.pulltorefresh.library.internal.SimpleLoadingLayout;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author ZYP
 * @since 2016/7/11 09:22
 */
@Route("/mail/home")
@RequestExtras({"extra_type", "extra_box_name"})
public class MailBoxActivity extends BaseActivity {

	private Handler mHandler = new Handler();
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private PullToRefreshExpandableListView mListView;
	private MailBoxAdapter mMailBoxAdapter;

	private FEToolbar mToolBar;
	private BoxDetailRequest mBoxRequest;

	private int mCurrentPage;
	private int mTotalPage;

	private String mBoxName;
	private String mType;
	private String mMailAccount;
	private boolean isUnread = false;


	public static void startMailBoxActivity(Context context, String type, String boxName) {
		startMailBoxActivity(context, type, boxName, null);
	}

	public static void startMailBoxActivity(Context context, String type, String boxName, String mailAccount) {
		startMailBoxActivity(context, type, boxName, mailAccount, false);
	}

	public static void startMailBoxActivity(Context context, String type, String boxName, String mailAccount, boolean isUnread) {
		Intent intent = new Intent(context, MailBoxActivity.class);
		intent.putExtra(K.email.EXTRA_TYPE, type);
		intent.putExtra(K.email.box_name, boxName);
		intent.putExtra(K.email.mail_account, mailAccount);
		intent.putExtra(email.EXTRA_UNREAD, isUnread);
		context.startActivity(intent);
	}

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		EventBus.getDefault().register(this);
		setContentView(R.layout.email_mail_box);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		this.mToolBar = toolbar;

	}

	@Override
	public void bindView() {
		mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
		mSwipeRefreshLayout.setColorSchemeResources(R.color.login_btn_defulit);

		mListView = findViewById(R.id.recyclerView);
		mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
		mListView.setScrollingWhileRefreshingEnabled(true);
		LoadingLayoutProxy loadingLayoutProxy = (LoadingLayoutProxy) mListView.getLoadingLayoutProxy();
		loadingLayoutProxy.removeAllLayout();
		loadingLayoutProxy.addLayout(new SimpleLoadingLayout(this));

		ExpandableListView refreshableView = mListView.getRefreshableView();
		refreshableView.setGroupIndicator(null);
		refreshableView.setAdapter(mMailBoxAdapter = new MailBoxAdapter(this));
		View emptyView = findViewById(R.id.ivErrorView);
		mMailBoxAdapter.setEmptyView(emptyView);
	}

	@Override
	public void bindData() {
		Intent intent = getIntent();
		mType = intent.getStringExtra(K.email.EXTRA_TYPE);
		mBoxName = intent.getStringExtra(K.email.box_name);
		mMailAccount = intent.getStringExtra(K.email.mail_account);
		isUnread = intent.getBooleanExtra(email.EXTRA_UNREAD, false);

		if (mBoxName.contains("InBox")) {
			this.mToolBar.setRightIcon(R.drawable.icon_search);
			this.mToolBar.setRightImageClickListener(view -> {
				Intent searchIntent = new Intent(MailBoxActivity.this, MailSearchActivity.class);
				searchIntent.putExtra(K.email.EXTRA_TYPE, mType);
				searchIntent.putExtra(K.email.box_name, mBoxName);
				searchIntent.putExtra(K.email.mail_account, mMailAccount);
				startActivity(searchIntent);
			});
		}

		mToolBar.setTitle(isUnread ? getString(R.string.lbl_text_mail_unread_title) : mType);
		mBoxRequest = new BoxDetailRequest(mBoxName, CoreZygote.getLoginUserServices().getUserName());
		if (TextUtils.equals(mBoxName, EmailNumber.INBOX)) {
			mBoxRequest.typeTrash = BoxDetailRequest.TYPE_TRASH;
			mBoxRequest.mailname = mMailAccount;
		}

		LoadingHint.show(this);
		requestBoxList(mCurrentPage = 1);
	}

	@Override
	public void bindListener() {
		mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				View firstView = view.getChildAt(firstVisibleItem);
				// 当firstVisibleItem是第0位。如果firstView==null说明列表为空，需要刷新;或者top==0说明已经到达列表顶部, 也需要刷新
				if (firstVisibleItem == 0 && (firstView == null || firstView.getTop() == 0)) {
					mSwipeRefreshLayout.setEnabled(true);
				}
				else {
					mSwipeRefreshLayout.setEnabled(false);
				}
			}
		});

		mListView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ExpandableListView>() {
			@Override public void onPullDownToRefresh(PullToRefreshBase<ExpandableListView> refreshView) {
			}

			@Override public void onPullUpToRefresh(PullToRefreshBase<ExpandableListView> refreshView) {
				requestBoxList(++mCurrentPage);
			}
		});

		mMailBoxAdapter.setOnMailItemClickListener(mail -> {
			markMailRead(mail, mail.status);
			String boxName = mBoxRequest.boxName;
			String mailId = mail.mailId;
			MailDetailActivity.startMailDetailActivity(MailBoxActivity.this, boxName, mailId, mMailAccount);
		});

		mMailBoxAdapter.setOnMailItemLongClickListener(mail -> {
			mToolBar.setRightText(getResources().getString(R.string.lbl_text_delete) + "1)");
			mToolBar.setRightTextClickListener(v -> showDeleteConfirmDialog());
		});

		mMailBoxAdapter.setOnDeleteMailSizeChangeListener(afterDeleteSize ->
				mToolBar.setRightText(afterDeleteSize == 0 ? getResources().getString(R.string.collaboration_recorder_cancel)
						: getResources().getString(R.string.lbl_text_delete) + afterDeleteSize + ")"));

		mSwipeRefreshLayout.setOnRefreshListener(() -> {
			showLoading();
			mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
			requestBoxList(mCurrentPage = 1);
		});
	}

	private void requestBoxList(int pageNumber) {
		mBoxRequest.pageNumber = pageNumber;
		FEHttpClient.getInstance().post(mBoxRequest, new ResponseCallback<BoxDetailResponse>(this) {

			@Override public void onCompleted(BoxDetailResponse rspContent) {
				stopLoading();
				mTotalPage = rspContent.pageCount;
				mCurrentPage = rspContent.currentPage == 0 ? 1 : rspContent.currentPage;
				if (mCurrentPage == 1) {
					if (mTotalPage > 1) {
						mListView.setMode(PullToRefreshBase.Mode.PULL_FROM_END);
					}
					mMailBoxAdapter.setMailList(filterReadMessage(rspContent.mailList));
				}
				else {
					mMailBoxAdapter.addMailList(filterReadMessage(rspContent.mailList));
				}

				if (mCurrentPage == mTotalPage) {
					mListView.setMode(PullToRefreshBase.Mode.DISABLED);
				}

				for (int i = 0, n = mMailBoxAdapter.getGroupCount(); i < n; i++) {
					mListView.getRefreshableView().expandGroup(i);
				}
				mListView.onRefreshComplete();
			}

			@Override public void onFailure(RepositoryException responseException) {
				stopLoading();
				mListView.onRefreshComplete();
			}
		});
	}

	private void showDeleteConfirmDialog() {
		if (mMailBoxAdapter.getDelMailIds() == null) {
			mToolBar.setRightIcon(R.drawable.icon_search);
			mMailBoxAdapter.setDeleteModel(false);
			return;
		}
		new FEMaterialDialog.Builder(this)
				.setTitle(null)
				.setMessage(getResources().getString(R.string.lbl_message_confirm_delete_mail))
				.setPositiveButton(null, dialog -> deleteMail(mMailBoxAdapter.getDelMailIds()))
				.setNegativeButton(null, dialog -> {
//					mToolBar.getRightTextView().setVisibility(View.GONE);
					mToolBar.setRightIcon(R.drawable.icon_search);
					mMailBoxAdapter.setDeleteModel(false);
				})
				.build()
				.show();
	}

	private void deleteMail(String mailIds) {
		LoadingHint.show(this);
		BoxDetailRequest request = new BoxDetailRequest();
		request.boxName = mBoxName;

		if (TextUtils.equals(mBoxName, EmailNumber.INBOX)) {    // 删除外部邮箱..
			request.typeTrash = BoxDetailRequest.TYPE_TRASH;
			request.mailname = mMailAccount;
		}

		if (TextUtils.equals(mBoxName, EmailNumber.TRASH)) {
			request.operator = BoxDetailRequest.OPERATOR_DELETE;
		}
		else {
			request.operator = BoxDetailRequest.OPERATOR_REMOVE;
		}

		request.optMailList = mailIds;

		FEHttpClient.getInstance().post(request, new ResponseCallback<BoxDetailResponse>(this) {
			@Override public void onCompleted(BoxDetailResponse responseContent) {
				LoadingHint.hide();
				mCurrentPage = 1;
				mMailBoxAdapter.setMailList(filterReadMessage(responseContent.mailList));
//				mToolBar.getRightTextView().setVisibility(View.GONE);
				mToolBar.setRightIcon(R.drawable.icon_search);
				mMailBoxAdapter.setDeleteModel(false);
			}

			@Override public void onFailure(RepositoryException responseException) {
				LoadingHint.hide();
				mMailBoxAdapter.setDeleteModel(false);
//				mToolBar.getRightTextView().setVisibility(View.GONE);
				mToolBar.setRightIcon(R.drawable.icon_search);
			}
		});
	}

	private List<Mail> filterReadMessage(List<Mail> mailList) {//未读邮件进入，派出已读邮件
		if (CommonUtil.isEmptyList(mailList) || !isUnread) {
			return mailList;
		}
		List<Mail> unreadMails = new ArrayList<>();
		for (Mail item : mailList) {
			if (TextUtils.equals(item.status, "2") || TextUtils.equals(item.status, "3")) {
				unreadMails.add(item);
			}
		}
		return unreadMails;
	}

	private void showLoading() {
		mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(true));
	}

	private void stopLoading() {
		if (mCurrentPage == 1) {
			LoadingHint.hide();
			mHandler.postDelayed(() -> mSwipeRefreshLayout.setRefreshing(false), 1000);
		}
	}

	@Override
	public void onBackPressed() {
		if (mMailBoxAdapter.isDeleteModel()) {
			mMailBoxAdapter.setDeleteModel(false);
//			mToolBar.getRightTextView().setVisibility(View.GONE);
			mToolBar.setRightIcon(R.drawable.icon_search);
			return;
		}
		super.onBackPressed();
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	public void onMailRemoveChange(Integer flag) {
		if (flag == NewAndReplyMailActivity.FLAG_MAIL_DELETE) {
			requestBoxList(mCurrentPage = 1);
		}
		else if (flag == NewAndReplyMailActivity.FLAG_SENT_REFRESH) {
			requestBoxList(1);
		}
	}

	private void markMailRead(final Mail mail, String mailState) {
		String boxName = mBoxRequest.boxName;
		if ((TextUtils.equals(boxName, EmailNumber.INBOX_INNER) || TextUtils.equals(boxName, EmailNumber.INBOX)
				&& (TextUtils.equals(mailState, "2") || TextUtils.equals(mailState, "3")))) {

			String url = CoreZygote.getLoginUserServices().getServerAddress() + "/mail/getmailmsgindex.jsp";
			new AsyncTask<String, Integer, String>() {
				@Override
				protected String doInBackground(String... params) {
					try {
						URL url = new URL(params[0]);

						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setRequestMethod("POST");
						connection.setConnectTimeout(8000);
						connection.setReadTimeout(8000);
						connection.setDoInput(true);
						connection.setDoOutput(true);
						connection.setRequestProperty("User-agent", CoreZygote.getUserAgent());

						CookieManager cookieManager = CookieManager.getInstance();
						String cookie = cookieManager.getCookie(CoreZygote.getLoginUserServices().getServerAddress());
						connection.setRequestProperty("Cookie", cookie);

						ILoginUserServices userServices = CoreZygote.getLoginUserServices();
						if (userServices != null && !TextUtils.isEmpty(userServices.getAccessToken())) {
							connection.setRequestProperty("token", userServices.getAccessToken());
						}

						StringBuffer sb = new StringBuffer();
						sb.append("id=").append(URLEncoder.encode(mail.mailId, "UTF-8")).append("&");
						sb.append("title=").append(URLEncoder.encode(mail.title, "UTF-8")).append("&");
						sb.append("sendTime=").append(URLEncoder.encode(mail.sendTime, "UTF-8"));

						connection.getOutputStream().write(sb.toString().getBytes());

						InputStream in = connection.getInputStream();
						ByteArrayOutputStream bos = new ByteArrayOutputStream();

						int len = 0;
						byte[] buf = new byte[1024];
						while ((len = in.read(buf)) != -1) {
							bos.write(buf, 0, len);
						}

						String result = bos.toString();
						bos.close();
						in.close();
						return result;
					} catch (Exception ex) {
						ex.printStackTrace();
						return null;
					}
				}

				@Override
				protected void onPostExecute(String s) {
					if (!TextUtils.isEmpty(s)) {
						try {
							JSONObject jsonObject = new JSONObject(s);
							String messageId = jsonObject.getString("msgindex");
							if (!TextUtils.isEmpty(messageId) && !TextUtils.equals(messageId, "no msgindex")) {
								NotificationController.messageReaded(MailBoxActivity.this, messageId);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
			}.execute(url);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		EventBus.getDefault().unregister(this);
	}
}
