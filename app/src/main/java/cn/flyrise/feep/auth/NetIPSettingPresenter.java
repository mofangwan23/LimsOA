package cn.flyrise.feep.auth;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import cn.flyrise.android.shared.bean.UserBean;
import cn.flyrise.feep.R;
import cn.flyrise.feep.auth.views.NetIPSettingActivity;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.core.services.model.NetworkInfo;
import cn.flyrise.feep.dbmodul.utils.UserInfoTableUtils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author ZYP
 * @since 2016-10-18 11:13
 */
public class NetIPSettingPresenter implements NetIPSettingContract.Presenter {

	private NetIPSettingContract.View mSettingView;
	private UserBean mUserBean;

	public NetIPSettingPresenter(NetIPSettingContract.View settingView) {
		this.mSettingView = settingView;
	}

	@Override
	public void initNetIPSettingPresenter() {
		this.mUserBean = UserInfoTableUtils.find();
		this.mSettingView.initUserSetting(mUserBean.getServerAddress(), mUserBean.getServerPort(),
				mUserBean.isSavePassword(), mUserBean.isAutoLogin(), mUserBean.isHttps(), mUserBean.isVPN(),
				mUserBean.getVpnAddress(), mUserBean.getVpnPort(), mUserBean.getVpnUsername(), mUserBean.getVpnPassword());
	}

	@Override
	public void saveUserSetting(String address, String port,
			boolean isRemember, boolean isAutoLogin, boolean isHttps) {
		saveUserSetting(address, port, isRemember, isAutoLogin, isHttps, false, null, null, null, null);
	}

	@Override
	public void saveUserSetting(String address, String port,
			boolean isRemember, boolean isAutoLogin, boolean isHttps, boolean isVpn,
			String vpnAddress, String vpnPort, String vpnUsername, String vpnPassword) {

		Context context = mSettingView.getContext();
		if (TextUtils.isEmpty(address)) {
			mSettingView.onSettingFailed(context.getString(R.string.setting_adress_empty));
			return;
		}

		if (address.startsWith(".")) {
			mSettingView.onSettingFailed(context.getResources().getString(R.string.setting_adress_checked));
			return;
		}

		if (TextUtils.isEmpty(port)) {
			mSettingView.onSettingFailed(context.getResources().getString(R.string.setting_port_empty));
			return;
		}
		else {
			int intPort = Integer.valueOf(port);
			if (intPort <= 0 || intPort > 65535) {
				mSettingView.onSettingFailed(context.getResources().getString(R.string.setting_adress_checked));
				return;
			}
		}

		if (isVpn) {
			if (TextUtils.isEmpty(vpnAddress)) {
				mSettingView.onSettingFailed(context.getResources().getString(R.string.lbl_text_vpn_check_host));
				return;
			}

			if (TextUtils.isEmpty(vpnUsername)) {
				mSettingView.onSettingFailed(context.getResources().getString(R.string.lbl_text_vpn_check_account));
				return;
			}

			if (TextUtils.isEmpty(vpnPassword)) {
				mSettingView.onSettingFailed(context.getResources().getString(R.string.lbl_text_vpn_check_password));
				return;
			}
		}

		mSettingView.showLoading();
		mUserBean.setHttps(isHttps);
		mUserBean.setServerAddress(address);
		mUserBean.setServerPort(port);
		mUserBean.setSavePassword(isRemember);
		mUserBean.setAutoLogin(isAutoLogin);
		mUserBean.setHttpsPort(port);
		mUserBean.setVPN(isVpn);
		mUserBean.setVpnUsername(vpnUsername);
		mUserBean.setVpnPassword(vpnPassword);
		mUserBean.setVpnAddress(vpnAddress);
		mUserBean.setVpnPort(vpnPort);

		if (isVpn) saveVpnUserBean();
		else requestSetting();
	}

	void requestSetting() {
		if (mUserBean.isHttps()) downloadHttpsKey();
		else requestHttpSetting();
	}

	void downloadHttpsKey() {
		new Thread(() -> {
			String keystoreFilename = NetIPSettingActivity.KEY_STORE_NAME + ".keystore";
			File keystoreFile = new File(CoreZygote.getPathServices().getKeyStoreDirPath() + File.separator + keystoreFilename);
			if (keystoreFile.exists()) {
				keystoreFile.delete();
			}

			FELog.i("DownloadHttpsKey : " + keystoreFile.getAbsolutePath());
			try {
				String keyStoreURL = "http://" + mUserBean.getServerAddress() + ":"
						+ mUserBean.getServerPort()
						+ CoreZygote.getPathServices().getKeyStoreUrl();
				FELog.i("DownloadHttpsKey url : " + keyStoreURL);
				URL url = new URL(keyStoreURL);
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				urlConnection.setConnectTimeout(5000);
				urlConnection.setReadTimeout(10000);
				urlConnection.setRequestProperty("User-agent",CoreZygote.getUserAgent());
				int responseCode = urlConnection.getResponseCode();
				FELog.i("ResponseCode = " + responseCode);
				if (responseCode == 200) {
					InputStream is = urlConnection.getInputStream();
					BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(keystoreFile));
					byte[] buf = new byte[2048];
					int len = 0;
					while ((len = is.read(buf)) != -1) {
						bos.write(buf, 0, len);
					}
					is.close();
					bos.close();
					FELog.i("DownloadHttpsKey : success.");
				}
			} catch (Exception exception) {
				exception.printStackTrace();
				if (keystoreFile.exists()) {
					keystoreFile.delete();
				}
				FELog.i("DownloadHttpsKey : failed.");
			} finally {
				new Handler(Looper.getMainLooper()).post(() -> {
					requestHttpSetting();
				});
			}
		}).start();
	}

	void requestHttpSetting() {
		String address = mUserBean.getServerAddress();
		String port = mUserBean.getServerPort();
		boolean isHttps = mUserBean.isHttps();
		buildFeHttpClient(address, port, isHttps);

		FEHttpClient.getInstance().post(null, null, new ResponseCallback<ResponseContent>(mSettingView) {
			@Override public void onCompleted(ResponseContent responseContent) {
				saveUserBean();
			}

			@Override public void onFailure(RepositoryException responseException) {
				mSettingView.onSettingFailed(null);
			}
		});

	}

	private void saveVpnUserBean() {
		buildFeHttpClient(mUserBean.getServerAddress(), mUserBean.getServerPort(), mUserBean.isHttps());
		UserInfoTableUtils.insert(mUserBean);
		mSettingView.onSettingSuccess();
	}

	private void saveUserBean() {
		UserInfoTableUtils.insert(mUserBean);
		mSettingView.onSettingSuccess();
	}

	public void setUserBean(UserBean userBean) {
		this.mUserBean = userBean;
	}

	public void setSettingView(NetIPSettingContract.View settingView) {
		this.mSettingView = settingView;
	}

	public void buildFeHttpClient(String address, String port, boolean isHttps) {
		new FEHttpClient.Builder(CoreZygote.getContext()).address(address).port(port).isHttps(isHttps)
				.keyStore(CoreZygote.getPathServices().getKeyStoreFile()).build();
	}
}
