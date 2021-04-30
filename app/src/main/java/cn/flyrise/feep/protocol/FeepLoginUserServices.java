package cn.flyrise.feep.protocol;

import android.text.TextUtils;
import cn.flyrise.feep.addressbook.source.AddressBookRepository;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.SpUtil;
import cn.flyrise.feep.core.function.FunctionManager;
import cn.flyrise.feep.core.services.ILoginUserServices;
import cn.flyrise.feep.core.services.model.AddressBook;
import cn.flyrise.feep.core.services.model.NetworkInfo;

/**
 * @author ZYP
 * @since 2017-02-25 19:30
 */
public class FeepLoginUserServices implements ILoginUserServices {

	private NetworkInfo mNetworkInfo;
	private String mUserId;             // 用户 id
	private String mUserName;           // 用户名称
	private String mImageHref;          // 用户头像 /imageHref/userId/xxx
	private String mServerAddress;      // 服务器地址 http://10.62.1.61:8089
	private int mAddressBookState;      // 通讯录状态
	private String mSmallVersion;       // 小版本号
	private String mCompanyGUID;        // G U I D 。
	private String mAccessToken;        // token.
	private int mFeVersion;

	public void setUserName(String mUserName) {
		this.mUserName = mUserName;
	}

	public void setUserId(String userId) {
		this.mUserId = userId;
	}

	public void setNetworkInfo(NetworkInfo networkInfo) {
		this.mNetworkInfo = networkInfo;
	}

	public void setSmallVersion(String smallVersion) {
		this.mSmallVersion = smallVersion;
	}

	@Override
	public void setCompanyGUID(String companyGUID) {
		this.mCompanyGUID = TextUtils.isEmpty(companyGUID) ? "" : companyGUID.toLowerCase();
	}

	@Override
	public NetworkInfo getNetworkInfo() {
		return this.mNetworkInfo;
	}

	@Override
	public String getUserId() {
		return this.mUserId;
	}

	@Override
	public String getUserName() {
		if (!TextUtils.isEmpty(mUserName)) {
			return this.mUserName;
		}

		this.queryLoginUserInfo();
		return this.mUserName;
	}

	@Override
	public String getUserImageHref() {
		if (!TextUtils.isEmpty(mImageHref)) {
			return mImageHref.contains("\\") ? this.mImageHref.replace("\\", "/") : this.mImageHref;
		}

		this.queryLoginUserInfo();
		if (TextUtils.isEmpty(mImageHref)) {
			return "";
		}
		return mImageHref.contains("\\") ? this.mImageHref.replace("\\", "/") : this.mImageHref;
	}

	@Override
	public String getServerAddress() {
		if (!TextUtils.isEmpty(mServerAddress)) {
			return this.mServerAddress;
		}

		if (mNetworkInfo != null) {
			mServerAddress = mNetworkInfo.buildServerURL();
		}

		if (TextUtils.isEmpty(mServerAddress)) {
			mServerAddress = SpUtil.get("service_logo_url", "");
		}

		return mServerAddress;
	}

	@Override
	public void setAddressBookState(int state) {
		this.mAddressBookState = state;
	}

	@Override
	public int getAddressBookState() {
		return mAddressBookState;
	}

	@Override
	public String getSmallVersion() {
		return mSmallVersion;
	}

	@Override
	public boolean hasModuleExist(int moduleId) {
		return FunctionManager.hasModule(moduleId);
	}

	@Override
	public String getCompanyGUID() {
		return mCompanyGUID;
	}

	@Override
	public void setImageHref(String path) {
		if (TextUtils.isEmpty(path)) {
			return;
		}
		this.mImageHref = path; // 拿到数据
		AddressBookRepository.get().updateUserImageHref(mUserId, mImageHref);
	}

	@Override public void setImLoginStatus() {
		CoreZygote.addConvSTServices(new FeepConvSTService());
	}

	public void setAccessToken(String accessToken) {
		this.mAccessToken = accessToken;
	}

	@Override public String getAccessToken() {
		return mAccessToken;
	}

	@Override public void setFeVersion(int version) {
		this.mFeVersion = version;
	}

	@Override public int getFeVersion() {
		return mFeVersion;
	}

	private void queryLoginUserInfo() {
		AddressBook addressBook = AddressBookRepository.get().queryUserBaseInfo(mUserId);
		if (addressBook != null) {
			this.mUserName = addressBook.name;
			this.mImageHref = addressBook.imageHref;
		}
	}
}
