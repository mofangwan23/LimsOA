package cn.flyrise.feep.cordova.Activity;

import android.content.Intent;
import android.os.Bundle;
import cn.flyrise.feep.cordova.utils.CordovaShowUtils;
import cn.flyrise.feep.cordova.view.ParticularCordovaActivity;
import cn.flyrise.feep.core.base.component.BaseActivity;

/**
 * 同事圈
 * Created by ouyangshaohai on 15/12/22.
 */
public class BlogActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected boolean optionStatusBar() {
        return false;
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        final Intent intent = new Intent(BlogActivity.this, ParticularCordovaActivity.class);
        if (getIntent() != null) {
            String shoInfo = getIntent().getStringExtra(CordovaShowUtils.CORDOVA_SHOW_INFO);
            intent.putExtra(CordovaShowUtils.CORDOVA_SHOW_INFO, shoInfo);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        BlogActivity.this.startActivity(intent);
        this.finish();
    }
}
