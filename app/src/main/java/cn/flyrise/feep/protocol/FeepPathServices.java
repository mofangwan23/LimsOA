package cn.flyrise.feep.protocol;

import android.os.Environment;
import cn.flyrise.feep.core.services.IPathServices;
import java.io.File;

/**
 * @author ZYP
 * @since 2017-02-07 10:24
 */
public class FeepPathServices implements IPathServices {

	private static final String BASE_DIR = Environment.getExternalStorageDirectory() + File.separator + "study";
	private static final String BASE_TEMP_DIR = BASE_DIR + File.separator + ".FILETEMP";

	private String mUserId = "nobody";
	private String mUserPath;                           // /feep/userId
	private String mUserTempPath;                       // /feep/userId/FILETEMP
	private String mUserTempDirPath;                    // /feep/userId/TEMPDir

	private String mLogPath;                            // /feep/userId/log
	private String mUserSafePath;                       // /feep/userId/SAFEFILE
	private String mUserAddressBookPath;                // /feep/userId/addressbook
	private String mUserImageCachePath;                 // /feep/userId/photocache
	private String mUserKnowledgeCachePath;             // /feep/userId/knowledgeCache

	private String mUserKeyStoreDirPath;                // /feep/KEYSTORE
	private String mUserkeyStoreFilePath;               // /feep/KEYSTORE/FEkey.keystore
	private String mUserGroupKeyPath;                   ///feep/synthesis/groupId.jpg
	private String mUserMediaPath;                   ///feep/userId/media
	private String mUserCommonPath;                  //feep/userId/common
	private String mSlateTempPath;                   //feep/TEMP

	public void setUserId(String userId) {
		this.mUserId = userId;
		this.mUserPath = BASE_DIR + File.separator + mUserId;
		this.mUserTempPath = mUserPath + File.separator + ".FILETEMP";
		this.mUserTempDirPath = mUserPath + File.separator + "TEMPDir";

		this.mLogPath = mUserPath + File.separator + "log";
		this.mUserSafePath = mUserPath + File.separator + "SAFEFILE";
		this.mUserAddressBookPath = mUserPath + File.separator + "addressbook";
		this.mUserImageCachePath = mUserPath + File.separator + "photocache";
		this.mUserKnowledgeCachePath = mUserPath + File.separator + "knowledgeCache";

		this.mUserKeyStoreDirPath = BASE_DIR + File.separator + "KEYSTORE";
		this.mUserkeyStoreFilePath = mUserKeyStoreDirPath + File.separator + "FEkey.keystore";
		this.mUserGroupKeyPath = mUserPath + File.separator + "synthesis";
		this.mUserMediaPath = mUserPath + File.separator + "media";
		this.mUserCommonPath = mUserPath + File.separator + "common";
		this.mSlateTempPath = mUserPath + File.separator + "TEMP";
	}

	@Override public String getBasePath() {
		return BASE_DIR;
	}

	@Override public String getBaseTempPath() {
		return BASE_TEMP_DIR;
	}

	@Override public String getUserPath() {
		return mUserPath;
	}

	@Override public String getLogPath() {
		return mLogPath;
	}

	@Override public String getTempFilePath() {
		return mUserTempPath;
	}

	@Override public String getSafeFilePath() {
		return mUserSafePath;
	}

	@Override public String getDownloadDirPath() {
		return mUserTempDirPath;
	}

	@Override public String getAddressBookPath() {
		return mUserAddressBookPath;
	}

	@Override public String getContactStorageBastPath() {
		return BASE_DIR;
	}

	@Override public String getImageCachePath() {
		return mUserImageCachePath;
	}

	@Override public String getKnowledgeCachePath() {
		return mUserKnowledgeCachePath;
	}

	@Override public String getKeyStoreDirPath() {
		return mUserKeyStoreDirPath;
	}

	@Override public String getKeyStoreFile() {
		return mUserkeyStoreFilePath;
	}

	@Override public String getAddressBookUrl() {
		return "/servlet/mobileAttachmentServlet?address=1&userid=";
	}

	@Override public String getKeyStoreUrl() {
		return "/servlet/mobileAttachmentServlet?keyTool=1";
	}

	@Override public String getGroupIconPath() {
		return mUserGroupKeyPath;
	}

	@Override public String getMediaPath() {
		return mUserMediaPath;
	}

	@Override public String getCommonUserId() {
		return mUserCommonPath;
	}

	@Override public String getSlateTempPath() {
		return mSlateTempPath;
	}
}
