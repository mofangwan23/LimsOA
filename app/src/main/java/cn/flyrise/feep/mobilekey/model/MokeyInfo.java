package cn.flyrise.feep.mobilekey.model;

import android.support.annotation.Keep;
import android.text.TextUtils;
import cn.flyrise.feep.core.services.IMobileKeyService;

/**
 * Created by klc on 2018/4/4.
 */
@Keep
public class MokeyInfo implements IMobileKeyService {

	/**
	 * mobile_linkUrl : https://tmokey.trustdo.cn:9998
	 * keyId : 179165e131a84954991287e39c3e3f75
	 * compelStatus : 1
	 * isActivate : 0
	 */

	//个人手机盾
	private String mobile_linkUrl;
	private String keyId;
	private String compelStatus;
	private int isActivate;
	private boolean isKeyExist;
	//企业账户手机盾
	private String isCompanyAdmin;
	private String mobileKeyCompanyId;
	private String companyIsActivate;

	@Override
	public String getKeyID() {
		return keyId;
	}

	@Override
	public String getServer() {
		return mobile_linkUrl;
	}

	@Override
	public boolean isActivate() {
		return isActivate == 1;
	}

	@Override
	public void setActivate(boolean activate) {
		this.isActivate = activate ? 1 : 0;
	}

	@Override
	public boolean isKeyExist() {
		return isKeyExist;
	}

	@Override
	public void setKeyExist(boolean exist) {
		this.isKeyExist = exist;
	}

	@Override
	public boolean isNormal() {
		return isKeyExist() && isActivate();
	}

	@Override
	public boolean isCompanyAdmin() {
		return "1".equals(isCompanyAdmin);
	}

	@Override
	public String getCompanyMobileKeyId() {
		return mobileKeyCompanyId;
	}

	@Override
	public boolean isCompanyActive() {
		return "1".equals(companyIsActivate);
	}

	//是否是严格模式，如果是严格模式，必须要检验手机盾才可以登录进去，不然就什么别提了
	public boolean isCompleState() {
		return compelStatus.equals("1");
//		return false;
	}

}
