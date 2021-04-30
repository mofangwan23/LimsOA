package cn.flyrise.feep.more;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.flyrise.android.shared.utility.FEUmengCfg;
import cn.flyrise.feep.R;
import cn.flyrise.feep.commonality.util.SystemManagerUtils;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.FEToolbar;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.common.utils.NetworkUtil;
import cn.flyrise.feep.core.premission.FePermissions;
import cn.flyrise.feep.core.premission.PermissionCode;
import cn.flyrise.feep.core.premission.PermissionGranted;
import cn.flyrise.feep.utils.FEUpdateVersionUtils;
import com.alibaba.sdk.android.feedback.impl.FeedbackAPI;
import com.alibaba.sdk.android.feedback.util.IUnreadCountCallback;
import com.jakewharton.rxbinding.view.RxView;
import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AboutActivity extends BaseActivity {

	private TextView versionTv;
	private RelativeLayout about_scoresuggested;
	private RelativeLayout about_welcome;
	private TextView about_new;
	private TextView about_copyright;
	private CheckedTextView CheckedTextView;
	private ImageView ivFeedBackUnRead;

	private TextView updataTv;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
	}

	@Override
	protected void toolBar(FEToolbar toolbar) {
		toolbar.setTitle(R.string.settings_about);
	}

	@Override
	public void bindView() {
		versionTv = findViewById(R.id.about_version);
		about_scoresuggested = findViewById(R.id.about_scoresuggested);
		about_welcome = findViewById(R.id.about_welcome);
		about_new = findViewById(R.id.about_new);
		about_copyright = findViewById(R.id.about_copyright);
		CheckedTextView = findViewById(R.id.checkedTextView1);
		updataTv = findViewById(R.id.app_updata_hind);
		ivFeedBackUnRead = findViewById(R.id.feedback_num_bg);
		FeedbackAPI.setBackIcon(R.mipmap.core_icon_back_black);
	}

	@Override
	public void bindData() {
		String str = this.getResources().getString(R.string.checkedtextview);
		CheckedTextView.setText(str);
		versionTv.setText("V " + SystemManagerUtils.getVersion());
		new FEUpdateVersionUtils(this, (needUpdate, isNoIgnoreVersion) -> {
			if (needUpdate) {
				updataTv.setText(getResources().getString(R.string.app_version_updata));
			}
			else {
				updataTv.setText(getResources().getString(R.string.app_version_no_updata));
			}
			findViewById(R.id.num_icon_bg).setVisibility(needUpdate && isNoIgnoreVersion ? View.VISIBLE : View.INVISIBLE);
		}).detectionUpdateVerson();
	}

	@Override
	public void bindListener() {
		about_scoresuggested.setOnClickListener(onClickListener);
		about_welcome.setOnClickListener(onClickListener);
		about_new.setOnClickListener(onClickListener);
		about_copyright.setOnClickListener(onClickListener);
		RxView.clicks(findViewById(R.id.updata_app))
				.throttleFirst(3, TimeUnit.SECONDS)
				.subscribe(a -> new FEUpdateVersionUtils(this, () -> {
					findViewById(R.id.num_icon_bg).setVisibility(View.INVISIBLE);
				}).showUpdateVersionDialog());
	}

	private final View.OnClickListener onClickListener = view -> {
		switch (view.getId()) {
			case R.id.about_scoresuggested:
				about_scoresuggested.setEnabled(false);
				FePermissions.with(AboutActivity.this)
						.permissions(new String[]{Manifest.permission.CAMERA})
						.rationaleMessage(getResources().getString(R.string.permission_rationale_camera))
						.requestCode(PermissionCode.CAMERA)
						.request();
				break;
			case R.id.about_welcome:
				AboutActivity.this.startActivity(
						new Intent(AboutActivity.this, GuideActivity.class).putExtra(GuideActivity.CATEGORY, GuideActivity.CATEGORY_ABOUT));
				break;
			case R.id.about_new:
				break;
			case R.id.about_copyright:
				AboutActivity.this.startActivity(new Intent(AboutActivity.this, CopyrightActivity.class));
				break;
		}
	};

	@PermissionGranted(PermissionCode.CAMERA)
	public void onCameraPermissionGrated() {
		Observable.
				unsafeCreate((Observable.OnSubscribe<Boolean>) subscriber -> {
					subscriber.onNext(NetworkUtil.ping());
					subscriber.onCompleted();
				}).subscribeOn(Schedulers.io())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(aBoolean -> {
					if (aBoolean) {
						FeedbackAPI.openFeedbackActivity();
					}
					else {
						FEToast.showMessage(R.string.core_http_network_exception);
						about_scoresuggested.setEnabled(true);
					}
				});
	}


	@Override
	protected void onResume() {
		super.onResume();
		FEUmengCfg.onActivityResumeUMeng(this, FEUmengCfg.AboutUS);
		about_scoresuggested.setEnabled(true);
		FeedbackAPI.getFeedbackUnreadCount(new IUnreadCountCallback() {
			@Override
			public void onSuccess(int count) {
				runOnUiThread(() -> ivFeedBackUnRead.setVisibility(count > 0 ? View.VISIBLE : View.GONE));
			}

			@Override
			public void onError(int i, String s) {
				runOnUiThread(() -> ivFeedBackUnRead.setVisibility(View.GONE));
			}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		FEUmengCfg.onActivityPauseUMeng(this, FEUmengCfg.AboutUS);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		FePermissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
	}
}
