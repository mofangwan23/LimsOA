package cn.flyrise.feep.core.services;

/**
 * @author ZYP
 * @since 2017-02-07 09:08
 * 项目路径(本地和网络)协议，用于各子模块获取项目路径
 */
public interface IPathServices {

	/**
	 * 根目录目录 /feep
	 */
	String getBasePath();

	/**
	 * 根缓存目录 /feep/FILETEMP
	 */
	String getBaseTempPath();

	/**
	 * 用户目录 /feep/userId
	 */
	String getUserPath();

	/**
	 * 用户日志文件 /feep/userId/log
	 */
	String getLogPath();

	/**
	 * 存放下载文件目录 /feep/userId/FILETEMP
	 */
	String getTempFilePath();

	/**
	 * 存放加密后安全文件的目录 /feep/userId/SAFEFILE
	 */
	String getSafeFilePath();

	/**
	 * 存放临时文件的目录 /feep/userId/TEMPDir
	 */
	String getDownloadDirPath();

	/**
	 * 存放通讯录文件的目录 /feep/userId/addressbook
	 */
	String getAddressBookPath();

	/**
	 * 存放下载后的联系人文件目录 /feep
	 */
	String getContactStorageBastPath();

	/**
	 * 存放拍照上传的缓存文件目录 /feep/userId/photocache
	 */
	String getImageCachePath();

	/**
	 * 存放知识文档缓存文件的目录 /feep/userId/knowledgeCache
	 */
	String getKnowledgeCachePath();

	/**
	 * 存放 Https 私有证书的目录 /feep/KEYSTORE
	 */
	String getKeyStoreDirPath();

	/**
	 * 获取 Https 私有证书文件的路径 /feep/KEYSTORE/FEkey.keystore
	 */
	String getKeyStoreFile();

	/**
	 * 获取下载通讯录的地址
	 */
	String getAddressBookUrl();

	/**
	 * 获取下载 Https 证书的地址
	 */
	String getKeyStoreUrl();

	/**
	 * 获取群聊头像地址
	 */
	String getGroupIconPath();

	/**
	 * 获取多媒体文件
	 */
	String getMediaPath();

	/**
	 * 获取缓存常用联系人id的地址
	 */
	String getCommonUserId();


	/**
	 * 手写签批临时缓存地址
	 * */
	String getSlateTempPath();


}
