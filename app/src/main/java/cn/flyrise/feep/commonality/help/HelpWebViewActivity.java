package cn.flyrise.feep.commonality.help;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toolbar;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.network.TokenInject;
import java.util.HashMap;
import java.util.Map;

/**
 * 陈冕
 * Created by Administrator on 2016-1-28.
 */
public class HelpWebViewActivity extends BaseActivity {
    private WebView webview;
    private final static String URL = "https://api.flyrise.cn/feep/help/";
    public static final String OPEN_URL = "open_url";
    private String urlkey = "0";
    private static final Map<String, String> urlMap = new HashMap<>();
    private ProgressBar progressh;

    private View mErrorPromptView;
    private FEToolbar mToolbar;

    static {
        urlMap.put("0", URL + "lxr.png");//通许录
        urlMap.put("1", URL + "dd.png");//嘟嘟
        urlMap.put("2", URL + "bb.png");//报表
        urlMap.put("3", URL + "xx.png");//消息
        urlMap.put("4", URL + "sp.png");//审批
        urlMap.put("5", URL + "hy.png");//会议
        urlMap.put("6", URL + "rc.png");//日程
        urlMap.put("7", URL + "jh.png");//计划
        urlMap.put("8", URL + "kq.png");//签到
        urlMap.put("9", URL + "crm.png");//crm
        urlMap.put("10", URL + "gg.png");//公告
        urlMap.put("11", URL + "xw.png");//新闻
        urlMap.put("12", URL + "hd.png");//活动
        urlMap.put("13", URL + "tsq.png");//同事圈
        urlMap.put("14", URL + "yggh.png");//员工关怀
        urlMap.put("15", URL + "wd.png");//文档
        urlMap.put("16", URL + "wjdc.png");//问卷
        urlMap.put("17", URL + "wdfx.png");//文档分享
        urlMap.put("18", URL + "znss.png");//智能搜索
        urlMap.put("19", URL + "ksfw.png");//快速访问
    }
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_webview_layout);
    }

    @Override protected void toolBar(FEToolbar toolbar) {
        super.toolBar(toolbar);
        mToolbar = toolbar;
    }

    @Override
    public void bindView() {
        super.bindView();
        mErrorPromptView = findViewById(R.id.layoutErrorPrompt);
        progressh = (ProgressBar) this.findViewById(R.id.progressh);
        webview = (WebView) this.findViewById(R.id.webview_layout);
        progressh.setVisibility(View.VISIBLE);
//        webview.setInitialScale(150);//初始缩放比例
        WebSettings settings = webview.getSettings();
        settings.setSupportZoom(true);          //支持缩放
        settings.setBuiltInZoomControls(true);  //启用内置缩放装置
        settings.setUseWideViewPort(true);
        settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        settings.setLoadWithOverviewMode(true);
        webview.setWebViewClient(new WebViewClient() {
            @Override public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                mErrorPromptView.setVisibility(View.VISIBLE);
                webview.setVisibility(View.GONE);
            }
        });

        webview.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                progressh.setProgress(newProgress);
                if (newProgress == 100) {
                    progressh.setVisibility(View.GONE);
                }
            }
        });

        mErrorPromptView.setOnClickListener(view -> {
            webview.setVisibility(View.VISIBLE);
            mErrorPromptView.setVisibility(View.GONE);
            TokenInject.injectToken(webview, urlMap.get(urlkey));

        });
    }

    @Override
    public void bindData() {
        super.bindData();
        Intent intent = getIntent();
        if (intent != null) {
            urlkey = String.valueOf(intent.getIntExtra(OPEN_URL, 0));
        }
        TokenInject.injectToken(webview, urlMap.get(urlkey));
        mToolbar.setTitle(getTitle(urlkey));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private String getTitle(String urlkey){
        String title="";
        if(!TextUtils.isEmpty(urlkey)){
            int key = Integer.parseInt(urlkey);
            switch (key){
                case 0:
                    title = getString(R.string.help_title_contact);
                    break;
                case 1:
                    title = "嘟嘟";
                    break;
                case 2:
                    title = getString(R.string.report_button_reports);
                    break;
                case 3:
                    title = getString(R.string.top_message);
                    break;
                case 4:
                    title = getString(R.string.help_title_approval);
                    break;
                case 5:
                    title = getString(R.string.meeting);
                    break;
                case 6:
                    title = getString(R.string.help_title_schedule);
                    break;
                case 7:
                    title = getString(R.string.help_title_plan);
                    break;
                case 8:
                    title = getString(R.string.help_title_location);
                    break;
                case 9:
                    title = "CRM管理";
                    break;
                case 10:
                    title = getString(R.string.help_title_news_notice);
                    break;
                case 11:
                    title = getString(R.string.help_title_news);
                    break;
                case 12:
                    title = getString(R.string.help_title_news_activity_manager);
                    break;
                case 13:
                    title = getString(R.string.top_associate);
                    break;
                case 14:
                    title = getString(R.string.top_contact);
                    break;
                case 15:
                    title = getString(R.string.help_title_file);
                    break;
                case 16:
                    title = getString(R.string.help_title_questionnaire);
                    break;
                case 17:
                    title = getString(R.string.help_title_file_share);
                    break;
                case 18:
                    title = getString(R.string.help_title_intelligent_search);
                    break;
                default:
                    title = getString(R.string.reside_menu_item_help);
                    break;
            }
        }

        return title;
    }
}
