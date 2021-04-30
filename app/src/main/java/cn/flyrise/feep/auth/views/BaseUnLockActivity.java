package cn.flyrise.feep.auth.views;

import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.base.component.BaseActivity;
import cn.flyrise.feep.core.image.loader.FEImageLoader;
import cn.flyrise.feep.core.services.ILoginUserServices;
import cn.squirtlez.frouter.annotations.RequestExtras;

/**
 * @author ZYP
 * @since 2017-05-04 17:23
 */
@RequestExtras({"allowForgetPwd", "lockMainActivity"})  // 是否允许忘记密码; 是否锁住主屏
public class BaseUnLockActivity extends BaseActivity {

	public static final String RESET_PASSWORD = "reset_password";
	protected ImageView mIvUserIcon;                        // 用户头像
	protected TextView mTvErrorPrompt;                      // 提示文本，提示错误信息(手势错误啊、指纹无法识别啊)
	protected TextView mTvForgetPwd;                        // 忘记密码
	protected TextView mTvRetryPrompt;                      // 提示 5次失败后，30s 后重试

	protected boolean isLockMainActivity = false;           // 是否覆盖在 APP 之上
	protected boolean isAllowForgetPwd = false;             // 是否显示忘记密码

	@Override public void bindView() {
		mIvUserIcon = (ImageView) this.findViewById(R.id.gesturepwd_unlock_face);
		mTvErrorPrompt = (TextView) this.findViewById(R.id.gesturepwd_unlock_text);
		mTvForgetPwd = (TextView) this.findViewById(R.id.gesturepwd_unlock_forget);
		mTvRetryPrompt = (TextView) this.findViewById(R.id.gesturepwd_unlock_failtip);
	}

	@Override public void bindData() {
		isLockMainActivity = getIntent().getBooleanExtra("lockMainActivity", false);
		isAllowForgetPwd = getIntent().getBooleanExtra("allowForgetPwd", true);

		ILoginUserServices services = CoreZygote.getLoginUserServices();
		if (services == null) {
			CoreZygote.getApplicationServices().reLoginApplication();
			return;
		}

		String userId = services.getUserId();
		String userName = services.getUserName();
		String userImageHref = services.getServerAddress() + services.getUserImageHref();
		FEImageLoader.load(this, mIvUserIcon, userImageHref, userId, userName);
	}

	@Override public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if (!isLockMainActivity) {
				setResult(404);
				finish();
				return true;
			}
			else {
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
}
