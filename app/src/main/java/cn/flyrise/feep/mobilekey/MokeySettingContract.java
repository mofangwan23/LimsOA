package cn.flyrise.feep.mobilekey;

import android.content.Context;
import rx.Observable;
import rx.observers.Observers;

/**
 * Created by klc on 2018/3/16.
 */

public interface MokeySettingContract {

	interface IView {

		Context getContext();

		void initLayout();

		void showMsg(int strId);

		void finish();

	}

	interface IPresenter {

		void active(String pwd);

		void modifyPwd();

		void reset(String pwd);

		void logout(String pwd);
	}


	interface IProvider {

		Observable<Integer> getKeyState();

		Observable<Integer> active();

		void sendActiveState(String fePwd);

		Observable<String> userLogin(String eventData);

		Observable<Integer> modifyMokeyPwd();

		Observable<Integer> reset(String pwd);

		Observable<String> companyPDFStamp(String eventData);

		Observable<Integer> userSign();

		Observable<String> logout(String pwd);


	}
}
