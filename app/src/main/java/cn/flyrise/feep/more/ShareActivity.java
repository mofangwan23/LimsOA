package cn.flyrise.feep.more;

import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.R;
import cn.flyrise.feep.more.adapter.ShareAdapter;
import cn.flyrise.feep.core.base.views.FEToolbar;

public class ShareActivity extends BaseActivity {

    public static final String FEEP_UMENG = "ShareActivity";
    public static final String ICON = "share_icon";
    public static final String NAME = "share_name";

    private ListView mShareListView;
    private List<Map<String, Object>> mShareListData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.share_listview);
    }

    @Override protected void toolBar(FEToolbar toolbar) {
        toolbar.setTitle(getResources().getString(R.string.lbl_message_title_yaoqing_tonshi));
    }

    @Override public void bindView() {
        mShareListData = new ArrayList<>();
        mShareListView = (ListView) this.findViewById(R.id.share_listview);
    }

    @Override
    public void bindData() {
        final Map<String, Object> qqMap = new HashMap<>();
        qqMap.put(ICON, R.drawable.share_qq_icon);
        qqMap.put(NAME, "QQ");

        final Map<String, Object> wxMap = new HashMap<>();
        wxMap.put(ICON, R.drawable.share_wechat_icon);
        wxMap.put(NAME, getString(R.string.share_wechat));

        final Map<String, Object> smsMap = new HashMap<>();
        smsMap.put(ICON, R.drawable.share_message_icon);
        smsMap.put(NAME, getString(R.string.share_sms));

        final Map<String, Object> emailMap = new HashMap<>();
        emailMap.put(ICON, R.drawable.share_email_icon);
        emailMap.put(NAME, getString(R.string.share_email));

        final Map<String, Object> copyMap = new HashMap<>();
        copyMap.put(ICON, R.drawable.share_copy_icon);
        copyMap.put(NAME, getString(R.string.share_copy));

        mShareListData.add(qqMap);
        mShareListData.add(wxMap);
        mShareListData.add(smsMap);
        mShareListData.add(emailMap);
        mShareListData.add(copyMap);
        ShareAdapter shareAdapter = new ShareAdapter(this, mShareListData);
        mShareListView.setAdapter(shareAdapter);
    }

    @Override
    public void bindListener() {
        super.bindListener();
        mShareListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index, long arg3) {
                Map<String, Object> map = mShareListData.get(index);
                int resId = (int) map.get(ICON);
                switch (resId) {
                    case R.drawable.share_qq_icon:
                        shareToQQFriend();
                        break;
                    case R.drawable.share_wechat_icon:
                        shareToWxFriend();
                        break;
                    case R.drawable.share_weibo_icon:
                        shareToSina();
                        break;
                    case R.drawable.share_email_icon:
                        shareToEmail();
                        break;
                    case R.drawable.share_message_icon:
                        shareToMMS();
                        break;
                    case R.drawable.share_copy_icon:
                        copy();
                        FEToast.showMessage(getResources().getString(R.string.lbl_text_copy_success));
                        break;
                }
            }
        });
    }

    /**
     * 分享到QQ好友
     */
    private void shareToQQFriend() {
        ComponentName componentName = new ComponentName("com.tencent.mobileqq", "com.tencent.mobileqq.activity.JumpActivity");
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setComponent(componentName);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_title));
        intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_content));
        share(intent);
    }

    private void share(Intent intent) {
        try {
            startActivity(intent);
        } catch (Exception e) {
            FEToast.showMessage(getResources().getString(R.string.lbl_text_not_install_app));
        }
    }

    /**
     * 发邮件
     */
    private void shareToEmail() {
        Uri smsToUri = Uri.parse("mailto:");
        Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
        intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_content));
        intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_title));
        share(intent);
    }

    /**
     * 发短信
     */
    private void shareToMMS() {
        Uri smsToUri = Uri.parse("smsto:");
        Intent intent = new Intent(Intent.ACTION_SENDTO, smsToUri);
        intent.putExtra("sms_body", getResources().getString(R.string.share_content));
        share(intent);
    }

    /**
     * 分享信息到新浪微博
     */
    @Deprecated
    private void shareToSina() {
        // com.sina.weibo.composerinde.OriginalComposerActivity
        ComponentName componentName = new ComponentName("com.sina.weibo", "com.sina.weibo.EditActivity");
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(componentName);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_title));
        intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_content));
        share(intent);
    }

    /**
     * 分享信息到朋友
     */
    private void shareToWxFriend() {
        ComponentName componentName = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareImgUI");
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setComponent(componentName);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_title));
        intent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_content));
        share(intent);
    }

    private void copy() {
        ClipboardManager cmb = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
        cmb.setText(getResources().getString(R.string.share_content));
    }

    @Override
    public void onResume() {
        super.onResume();
        FEUmengCfg.onActivityResumeUMeng(this,FEEP_UMENG);
    }

    @Override
    public void onPause() {
        super.onPause();
        FEUmengCfg.onActivityPauseUMeng(this,FEEP_UMENG);
    }
}
