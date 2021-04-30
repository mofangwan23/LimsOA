package cn.flyrise.feep.auth.views;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import cn.flyrise.feep.R;
import cn.flyrise.feep.core.dialog.FEMaterialDialog;

/**
 * 人脸识别登录页
 */
//public class FaceLoginActivity extends BaseThreeLoginActivity implements EyeKeyLoginListener, EyeKeyLoadingListener {
public class FaceLoginActivity extends BaseThreeLoginActivity {

	private TextView mTvReFaceDiscren;
//	private FaceRecognitionImpl mFaceRecognition;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_face_login);
		mTvReFaceDiscren = findViewById(R.id.tvReFaceDiscren);
//		mFaceRecognition = new EyeKeyCheckManager();
//		mTvReFaceDiscren.setOnClickListener(v -> {
//			mFaceRecognition.startVerify(this, this);
//			mTvReFaceDiscren.setVisibility(View.GONE);
//		});
//		mFaceRecognition.startVerify(this, this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
//		if (requestCode != EyeKeyCheckManager.REQUEST_DETECT) return;
//		if (resultCode == RESULT_CANCELED) {
//			promptFaceVerifyFailure("取消人脸活体检测");
//		}
//		else if (data == null) {
//			promptFaceVerifyFailure("检测失败，请重试");
//		}
//		else {
//			mFaceRecognition.verifyResult(
//					new EyeFaceBuilder.Builder(this)
//							.setData(data)
//							.setEyeKeyOAOnly(mUserBean.getServerAddress() + mUserBean.getServerPort())
//							.setUserId(mUserBean.getUserID())
//							.setLoadingListener(this)
//							.setLoginListener(this)
//							.build());
//		}
	}

	private void promptFaceVerifyFailure(String errorMessage) {
		mTvReFaceDiscren.setVisibility(View.VISIBLE);
		new FEMaterialDialog.Builder(FaceLoginActivity.this)
				.setMessage(errorMessage)
				.setPositiveButton(null, Dialog::dismiss)
				.build()
				.show();
	}

//	@Override
//	public void loadingShow() {
//		LoadingHint.show(FaceLoginActivity.this);
//	}
//
//	@Override
//	public void loadingHint() {
//		LoadingHint.hide();
//	}
//
//	@Override
//	public void executeLogin() {
//		mAuthPresenter.executeLogin();
//	}
//
//	@Override
//	public void showDialog(String message) {
//		promptFaceVerifyFailure(message);
//	}
}
