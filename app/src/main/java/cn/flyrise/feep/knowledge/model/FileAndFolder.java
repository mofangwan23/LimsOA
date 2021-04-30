package cn.flyrise.feep.knowledge.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

/**
 * Created by KLC on 2016/12/6.
 */

@Keep
public class FileAndFolder extends ListBaseItem implements Parcelable {
	/**
	 * fileid : 631E47BE-09AC-5B32-A079-9AEC75DCA005
	 * filename : 201612050441164403.png
	 * pubUserName : 陈婕
	 * pubTime : 2016-12-05 16:41:16.0
	 * fileurl : /OracleOA/Media/FileManage//FEdocument//2016//12//05
	 * filetype : .png
	 * title : 20161205164148
	 * isupload : 1
	 * imgsrc : /wechat/css/images/kmicon/png.png
	 * openUrl : /fe.do?SYS.ID=588-001-000&SYS.ACTION=show&master_key=631E47BE-09AC-5B32-A079-9AEC75DCA005
	 * folderid : 3748
	 * file : /FEdocument//2016//12//05//201612050441164403.png
	 * filesize : 139.00K
	 * expiredtime : 2016-12-07 16:41:00.0
	 * rightPower : 0
	 * expiredtimelong : 1481100060000
	 * pubTimeLong : 1480927276000
	 * readNum : 1
	 * content :
	 */

	/**
	 * isFolder : 1
	 * foldername : 3
	 * fattr : 2
	 * folderOpenUrl : /fe.do?SYS.ID=588-004-000&SYS.ACTION=file_show&fid=3806&fname=3
	 */

	public String fileid;
	public String filename;
	public String pubUserName;
	public String pubTime;
	public String fileurl;
	public String filetype;
	public String title;
	public String isupload;
	public String imgsrc;
	public String openUrl;
	public String folderid;
	public String file;
	public String filesize;
	public String expiredtime;
	public int rightPower;
	public long expiredtimelong;
	public long pubTimeLong;
	public int readNum;
	public String content;
	private String isFolder;
	public String foldername;
	public String fattr;
	public String folderOpenUrl;
	public String sendUserId;
	public String favoriteId;

	@SerializedName("firstfolder")
	public boolean canManage = true;

	public boolean isFolder() {
		return !(TextUtils.isEmpty(isFolder) || !"1".equals(isFolder));
	}

	public String getFileRealName() {
		return this.title + this.filetype;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.fileid);
		dest.writeString(this.filename);
		dest.writeString(this.pubUserName);
		dest.writeString(this.pubTime);
		dest.writeString(this.fileurl);
		dest.writeString(this.filetype);
		dest.writeString(this.title);
		dest.writeString(this.isupload);
		dest.writeString(this.imgsrc);
		dest.writeString(this.openUrl);
		dest.writeString(this.folderid);
		dest.writeString(this.file);
		dest.writeString(this.filesize);
		dest.writeString(this.expiredtime);
		dest.writeInt(this.rightPower);
		dest.writeLong(this.expiredtimelong);
		dest.writeLong(this.pubTimeLong);
		dest.writeInt(this.readNum);
		dest.writeString(this.content);
		dest.writeString(this.isFolder);
		dest.writeString(this.foldername);
		dest.writeString(this.fattr);
		dest.writeString(this.folderOpenUrl);
		dest.writeString(this.favoriteId);
	}

	public FileAndFolder() {
	}

	protected FileAndFolder(Parcel in) {
		this.fileid = in.readString();
		this.filename = in.readString();
		this.pubUserName = in.readString();
		this.pubTime = in.readString();
		this.fileurl = in.readString();
		this.filetype = in.readString();
		this.title = in.readString();
		this.isupload = in.readString();
		this.imgsrc = in.readString();
		this.openUrl = in.readString();
		this.folderid = in.readString();
		this.file = in.readString();
		this.filesize = in.readString();
		this.expiredtime = in.readString();
		this.rightPower = in.readInt();
		this.expiredtimelong = in.readLong();
		this.pubTimeLong = in.readLong();
		this.readNum = in.readInt();
		this.content = in.readString();
		this.isFolder = in.readString();
		this.foldername = in.readString();
		this.fattr = in.readString();
		this.folderOpenUrl = in.readString();
		this.favoriteId = in.readString();
	}

	public static final Parcelable.Creator<FileAndFolder> CREATOR = new Parcelable.Creator<FileAndFolder>() {
		@Override
		public FileAndFolder createFromParcel(Parcel source) {
			return new FileAndFolder(source);
		}

		@Override
		public FileAndFolder[] newArray(int size) {
			return new FileAndFolder[size];
		}
	};
}
