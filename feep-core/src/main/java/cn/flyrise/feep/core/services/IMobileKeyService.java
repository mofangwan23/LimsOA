package cn.flyrise.feep.core.services;

/**
 * Created by klc on 2018/3/2.
 */

public interface IMobileKeyService {

	String getKeyID();

	String getServer();

	boolean isActivate();

	void setActivate(boolean activate);

	boolean isKeyExist();

	void setKeyExist(boolean exist);

	boolean isNormal();

	boolean isCompanyAdmin();

	String getCompanyMobileKeyId();

	boolean isCompanyActive();
}
