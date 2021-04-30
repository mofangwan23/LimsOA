/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-12-31 下午2:49:57
 */

package cn.flyrise.feep.report;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.webkit.CookieManager;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.android.library.utility.LoadingHint;
import cn.flyrise.android.library.view.BubbleWindow;
import cn.flyrise.android.library.view.RockerView;
import cn.flyrise.android.library.view.RockerView.onShakingListener;
import cn.flyrise.android.protocol.model.ReportDetailsItem;
import cn.flyrise.android.protocol.model.ReportListItem;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.bean.JSControlInfo;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.X;
import cn.flyrise.feep.core.common.utils.PixelUtil;
import cn.flyrise.feep.core.network.TokenInject;
import cn.flyrise.feep.form.been.ExecuteResult;
import com.tencent.smtt.sdk.CookieSyncManager;
import java.util.List;

/**
 * 类功能描述：</br>
 * @author 钟永健
 * @version 1.0</ br> 修改时间：2012-12-31</br> 修改备注：</br>
 */
public class ReportDetailsActivity extends JSControlActivity {

	private final int REPORT_SEARCK_ID = 100;
	private final int REPORT_SHOW_ID = 101;

	public static final String REPORT_ITEM_KEY = "REPORT_ITEM_KEY";
	private static final String REPORT_SEARCH_URL = "REPORT_SEARCH_URL";

	private RockerView rockerView;
	private BubbleWindow bubbleWindow;

	private ReportListItem reportListItem;
	private String searchPageUrl;
	private List<ReportDetailsItem> detailsItems;
	private String reportSearchUrl;
	private int reportPosition;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mToolBar.getRightTextView().setVisibility(View.GONE);
		initRockerView();
		if (isReportShow()) {
			setReportShowTitle(0);
		}
	}

	private void setReportShowTitle(int position) {
		reportPosition = position;
		if (detailsItems != null && detailsItems.size() > position) {
			final ReportDetailsItem item = detailsItems.get(position);
			if (item != null) {
				mToolBar.setTitle(item.getReportDetailsName());
			}
		}
	}

	private void initRockerView() {//初始化摇杆
		final FrameLayout layout = (FrameLayout) getWindow().getDecorView();
		rockerView = new RockerView(this);
		rockerView.setVisibility(View.GONE);
		rockerView.setMoveSpeed(20);
		rockerView.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
		rockerView.setPadding(0, 0, 0, PixelUtil.dipToPx(50));
		layout.addView(rockerView);
		rockerView.setOnShakingListener(new onShakingListener() {
			@Override
			public void onGyroScrolling(double distanceX, double distanceY) {
				mWebView.scrollBy(-(int) distanceX, -(int) distanceY);
			}
		});
	}

	@Override
	public void getIntentData(Intent intent) {
		super.getIntentData(intent);
		if (intent != null) {
			reportListItem = (ReportListItem) intent.getSerializableExtra(REPORT_ITEM_KEY);
			reportSearchUrl = intent.getStringExtra(REPORT_SEARCH_URL);
		}
	}

	private void getDetailData() {//获取报表数据
		if (reportListItem != null) {
			searchPageUrl = reportListItem.getSearchPageUrl();
			detailsItems = reportListItem.getReportDetailsItemList();
		}
	}

	@Override
	public void bindData() {
		super.bindData();
		LoadingHint.show(this);
		getDetailData();
		if (reportSearchUrl != null) {
			final ReportDetailsItem detailsItem = detailsItems.get(0);
			final String reportDetailsUrl = detailsItem.getReportDetailsUrl();
			TokenInject.injectToken(mWebView, baseUrl + reportDetailsUrl + reportSearchUrl);
		}
		else if (searchPageUrl != null && searchPageUrl.length() != 0) {
			TokenInject.injectToken(mWebView, baseUrl + searchPageUrl);
		}
		else if (detailsItems != null && detailsItems.size() != 0) {
			final ReportDetailsItem detailsItem = detailsItems.get(0);
			final String reportDetailsUrl = detailsItem.getReportDetailsUrl();
			TokenInject.injectToken(mWebView, baseUrl + reportDetailsUrl);
		}
		final View contentView = initBubbleWindowContentView();
		bubbleWindow = new BubbleWindow(contentView);
	}

	@Override
	public void bindListener() {
		super.bindListener();
		if (isReportShow()) {
			mToolBar.setRightText(R.string.report_button_reports);
			mToolBar.setRightTextClickListener(new MyClickListener(REPORT_SHOW_ID));
		}
		else {
			mToolBar.setRightText(R.string.report_button_query);
			mToolBar.setRightTextClickListener(new MyClickListener(REPORT_SEARCK_ID));
		}
	}

	@Override
	public void onPageFinished() {
		super.onPageFinished();
		if (LoadingHint.isLoading()) {
			LoadingHint.hide();
		}
		if (isReportShow()) {
			mToolBar.getRightTextView().setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onPageLoadSucceed() {
		super.onPageLoadSucceed();
		/*---设置摇杆的隐藏与消失---*/
		if (isReportShow() && detailsItems != null) {
			final ReportDetailsItem detailsItem = detailsItems.get(reportPosition);
			final int reportDetailsType = detailsItem.getReportDetailsType();
			if (reportDetailsType == 4) {       // 柱状图
				rockerView.setVisibility(View.GONE);
			}
			else {
				rockerView.setVisibility(View.VISIBLE);
			}
		}
		mToolBar.getRightTextView().setVisibility(View.VISIBLE);
	}

	/**
	 * 判断当前页面是否是报表显示页面
	 * @return true报表显示页面，false查询页面
	 */
	private boolean isReportShow() {
		return (searchPageUrl == null || searchPageUrl.length() == 0 || reportSearchUrl != null);
	}

	@Override
	public void webViewSetting(WebView webView) {
		super.webViewSetting(webView);
		if (isReportShow()) {
			final WebSettings webSettings = webView.getSettings();
//             设置双指放大
			webSettings.setSupportZoom(true);
			webSettings.setUseWideViewPort(true);
			webSettings.setBuiltInZoomControls(true);
//             设置图片是适应屏幕大小
			webSettings.setLoadWithOverviewMode(true);
			webSettings.setLayoutAlgorithm(LayoutAlgorithm.NORMAL);
		}
	}

	private View initBubbleWindowContentView() {//初始化气泡框的ContentView
		final LinearLayout contentView = new LinearLayout(this);
		final ListView urlListView = new ListView(this);
		urlListView.setDividerHeight(0);
		urlListView.setCacheColorHint(0);
		contentView.addView(urlListView, new LayoutParams(PixelUtil.dipToPx(180), LayoutParams.WRAP_CONTENT));
		if (detailsItems == null) {
			return contentView;
		}
		final ArrayAdapter<ReportDetailsItem> adapter = new ArrayAdapter<ReportDetailsItem>(this, 0, detailsItems) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				RelativeLayout rl = new RelativeLayout(ReportDetailsActivity.this);
				final TextView tv = new TextView(ReportDetailsActivity.this);
				tv.setMinHeight(PixelUtil.dipToPx(50));
				tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
				tv.setGravity(Gravity.CENTER);
				final String name = detailsItems.get(position).getReportDetailsName();
				tv.setText(name);
				tv.setTextColor(getResources().getColor(R.color.userinfo_detail_content));
				rl.addView(tv, new LayoutParams(PixelUtil.dipToPx(180), ActionBar.LayoutParams.WRAP_CONTENT));
				TextView line = new TextView(ReportDetailsActivity.this);
				line.setBackgroundColor(getResources().getColor(R.color.detail_line));
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 1);
				params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
				if (position == (detailsItems.size() - 1)) {
					line.setVisibility(View.GONE);
				}
				else {
					line.setVisibility(View.VISIBLE);
				}
				rl.addView(line, params);
				return rl;
			}
		};
		urlListView.setAdapter(adapter);
		urlListView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
			if (detailsItems != null && detailsItems.size() > position) {
				bubbleWindow.dismiss();
				final ReportDetailsItem reportDetailsItem = detailsItems.get(position);
				final String reportDetailsUrl = reportDetailsItem.getReportDetailsUrl();
				if (reportSearchUrl == null) {
					TokenInject.injectToken(mWebView, baseUrl + reportDetailsUrl);
				}
				else {
					TokenInject.injectToken(mWebView, baseUrl + reportDetailsUrl + reportSearchUrl);
				}
				setReportShowTitle(position);
			}
		});
		return contentView;
	}

	class MyClickListener implements OnClickListener {//点击事件

		private final int clickID;

		public MyClickListener(int viewID) {
			this.clickID = viewID;
		}

		@Override
		public void onClick(View v) {
			final ExecuteResult info = new ExecuteResult();
			switch (clickID) {
				case REPORT_SEARCK_ID:// 查询按钮
					LoadingHint.show(ReportDetailsActivity.this);
					info.setActionType(X.JSActionType.Check);
					break;
				case REPORT_SHOW_ID:// 报表按钮
					bubbleWindow.show(v);
					return;

				default:
					break;
			}
			sendToJavascript(info.getProperties());
		}
	}

	@Override
	protected void doAfterCheck(JSControlInfo controlInfo) {
		super.doAfterCheck(controlInfo);
		final ExecuteResult info = new ExecuteResult();
		info.setActionType(X.JSActionType.Search);
		sendToJavascript(info.getProperties());
	}

	@Override
	protected void JSActionSearch(JSControlInfo controlInfo) {
		if (LoadingHint.isLoading()) {
			LoadingHint.hide();
		}
		final Intent intent = new Intent(ReportDetailsActivity.this, ReportDetailsActivity.class);
		intent.putExtra(REPORT_ITEM_KEY, reportListItem);
		intent.putExtra(REPORT_SEARCH_URL, controlInfo.getReportSearch());
		startActivity(intent);
	}

	@Override
	protected void onPause() {
		super.onPause();
		FEUmengCfg.onActivityPauseUMeng(this, FEUmengCfg.ReportDeatil);
	}

	@Override
	protected void onResume() {
		super.onResume();
		JSControlActivity.reportForm = true;
		FEUmengCfg.onActivityResumeUMeng(this, FEUmengCfg.ReportDeatil);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		JSControlActivity.reportForm = false;
		CookieSyncManager.createInstance(CoreZygote.getContext());
		CookieManager cookieManager = CookieManager.getInstance();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			cookieManager.removeSessionCookies(null);
			cookieManager.removeAllCookie();
			cookieManager.flush();
		}
		else {
			cookieManager.removeAllCookie();
			CookieSyncManager.getInstance().sync();
		}
		if (mWebView != null) {
			mWebView.setWebChromeClient(null);
			mWebView.setWebViewClient(null);
			mWebView.getSettings().setJavaScriptEnabled(false);
			mWebView.clearCache(true);
		}
	}
}
