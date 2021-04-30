package cn.flyrise.feep.mobilekey;

import cn.flyrise.feep.R;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FEToast;
import cn.flyrise.feep.core.services.IMobileKeyService;
import cn.flyrise.feep.mobilekey.MokeySettingContract.IView;
import cn.flyrise.feep.mobilekey.event.MoKeyNormalEvent;
import org.greenrobot.eventbus.EventBus;
import rx.functions.Action1;

/**
 * Created by klc on 2018/3/20.
 */

public class MokeySettingPresenter implements MokeySettingContract.IPresenter {

	private MokeySettingContract.IView mView;
	private MokeySettingContract.IProvider mProvider;
	private IMobileKeyService mobileKeyService;

	MokeySettingPresenter(IView mView) {
		this.mView = mView;
		this.mProvider = new MokeyProvider(mView.getContext());
		mobileKeyService = CoreZygote.getMobileKeyService();
	}

	@Override
	public void active(String pwd) {
		this.mProvider.active().subscribe(errorCode -> {
			this.mProvider.sendActiveState(pwd);
			mobileKeyService.setKeyExist(true);
			mobileKeyService.setActivate(true);
			this.mView.showMsg(R.string.mokey_active_success);
			this.mView.initLayout();
			EventBus.getDefault().post(new MoKeyNormalEvent(true));
		}, throwable -> {
			throwable.printStackTrace();
			FEToast.showMessage(throwable.getMessage());
		});
	}


	@Override
	public void modifyPwd() {
		this.mProvider.modifyMokeyPwd()
				.subscribe(
						errorCode -> mView.showMsg(R.string.mokey_modify_pwd_success), throwable -> {
							FEToast.showMessage(throwable.getMessage());
						});
	}

	@Override
	public void reset(String pwd) {
		this.mProvider.reset(pwd).subscribe(errorCode -> {
					mobileKeyService.setKeyExist(true);
					mobileKeyService.setActivate(true);
					this.mView.initLayout();
					this.mView.showMsg(R.string.mokey_reset_success);
					EventBus.getDefault().post(new MoKeyNormalEvent(true));
				}, throwable -> {
					throwable.printStackTrace();
					FEToast.showMessage(throwable.getMessage());
				}
		);
	}

	@Override
	public void logout(String pwd) {
		this.mProvider.logout(pwd).subscribe(errorCode -> {
					this.mView.showMsg(R.string.mokey_logout_success);
					this.mView.finish();
					EventBus.getDefault().post(new MoKeyNormalEvent(true));
				}, throwable -> {
					throwable.printStackTrace();
					FEToast.showMessage(throwable.getMessage());
				}
		);
	}
}
