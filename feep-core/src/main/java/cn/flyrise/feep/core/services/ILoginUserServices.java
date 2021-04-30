package cn.flyrise.feep.core.services;

import cn.flyrise.feep.core.services.model.NetworkInfo;

/**
 * @author ZYP
 * @since 2017-02-25 13:32
 * 为子模块准备的获取当前登录用户基本信息的接口，包括服务器地址，基本用户信息。
 */
public interface ILoginUserServices {

	/**
	 * 获取服务器信息，包括 ip、port 等
	 */
	NetworkInfo getNetworkInfo();

	/**
	 * 获取当前用户 id
	 */
	String getUserId();

	/**
	 * 获取当前用户名称
	 */
	String getUserName();

	/**
	 * 获取当前用户头像地址
	 */
	String getUserImageHref();

	/**
	 * 获取当前服务器地址
	 */
	String getServerAddress();

	/**
	 * 设置通讯录的状态
	 * @param state: 初始化成功、更新失败、下载失败
	 */
	void setAddressBookState(int state);

	/**
	 * 通讯录是否下载成功
	 */
	int getAddressBookState();

	/**
	 * 获取小版本号
	 */
	String getSmallVersion();

	/**
	 * 判断模块是否存在
	 */
	boolean hasModuleExist(int moduleId);

	/**
	 * 妈的智障
	 */
	void setCompanyGUID(String companyGUID);

	/**
	 * G U I D
	 */
	String getCompanyGUID();

	/**
	 * 设置用户的头像路径
	 * 用户在本地修改了头像
	 */
	void setImageHref(String path);

	void setImLoginStatus();

	String getAccessToken();

	/**
	 * 获取服务端版本65、70
	 * */
	void setFeVersion(int version);

	int getFeVersion();


}
