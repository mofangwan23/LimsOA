package cn.flyrise.feep.main;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.base.views.ListenableScrollView;
import cn.flyrise.feep.core.common.FEStatusBar;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.core.watermark.WMStamp;

/**
 * @author ZYP
 * @since 2017-05-17 16:57 外部联系人详情
 */
public class ExternalContactDetailActivity extends BaseActivity {

	private ListenableScrollView mScrollView;
	private ViewGroup mWaterMarkContainer;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_external_contact_detail);
	}

	@Override protected boolean optionStatusBar() {
		return FEStatusBar.setLightStatusBar(this);
	}

	@Override protected int statusBarColor() {
		return Color.TRANSPARENT;
	}

	@Override protected void onStart() {
		super.onStart();
		WMStamp.getInstance().draw(mWaterMarkContainer, mScrollView);
	}

	@Override public void bindView() {
		mScrollView = (ListenableScrollView) findViewById(R.id.scrollView);
		mWaterMarkContainer = (ViewGroup) findViewById(R.id.layoutContentView);
		ImageView ivUserIcon = (ImageView) findViewById(R.id.ivUserIcon);                           // 用户头像
		TextView tvUserName = (TextView) findViewById(R.id.tvUserName);                             // 用户名称
		TextView tvPosition = (TextView) findViewById(R.id.tvPosition);                             // 用户岗位
		TextView tvUserPhone = (TextView) findViewById(R.id.tvUserPhone);                           // 用户私人电话
		TextView tvConnectContact = (TextView) findViewById(R.id.tvConnectContact);                 // 关联客户
		TextView tvExternalCompany = (TextView) findViewById(R.id.tvExternalCompany);               // 用户公司
		TextView tvDepartment = (TextView) findViewById(R.id.tvDepartment);                         // 用户部门
		ImageView ivMobileCall = (ImageView) findViewById(R.id.ivMobileCall);                       //

		Intent intent = getIntent();
		String userName = intent.getStringExtra("username");
		String position = intent.getStringExtra("position");
		String userPhone = intent.getStringExtra("phone");
		String connectContact = intent.getStringExtra("connectContact");
		String externalCompany = intent.getStringExtra("externalCompany");
		String department = intent.getStringExtra("department");

		tvUserName.setText(userName);
		tvPosition.setText(position);
		if (TextUtils.isEmpty(userPhone)) {
			ivMobileCall.setVisibility(View.GONE);
		}
		else {
			ivMobileCall.setVisibility(View.VISIBLE);
			tvUserPhone.setText(userPhone);
			ivMobileCall.setOnClickListener(view -> {
				Intent callIntent = new Intent(Intent.ACTION_DIAL);
				callIntent.setData(Uri.parse("tel:" + userPhone));
				startActivity(callIntent);
			});
		}
		tvConnectContact.setText(connectContact);
		tvExternalCompany.setText(externalCompany);
		tvDepartment.setText(department);

		FEImageLoader.load(this, ivUserIcon, "/tan90", "tan90", userName);
	}

	@Override protected void onDestroy() {
		super.onDestroy();
		WMStamp.getInstance().clearWaterMark(mWaterMarkContainer);
	}
}
