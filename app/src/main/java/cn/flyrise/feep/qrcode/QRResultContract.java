package cn.flyrise.feep.qrcode;

import cn.flyrise.android.protocol.entity.MeetingSignInResponse.MeetingSign;

/**
 * Created by klc on 2018/3/23.
 */

public interface QRResultContract {

	interface IView {

		void startScanActivity();

		void requestLocationPermission();

		void showLoading();

		void hideLoading();

		void showOpenGpsDialog();

		void startSettingActivity();

		void showMeetingSignDialog(MeetingSign meetingSign);

		void startLoginZXAciivity(String url);

	}

	interface IPresenter {

		void handleCode(String codeContent);

		void meetingSignIn();

		void checkGpsOpen();
	}

}
