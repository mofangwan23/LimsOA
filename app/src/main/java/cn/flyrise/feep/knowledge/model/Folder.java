package cn.flyrise.feep.knowledge.model;

import android.os.Parcel;
import android.os.Parcelable;

import cn.flyrise.feep.knowledge.util.KnowKeyValue;
import cn.flyrise.feep.knowledge.util.KnowPermissionCheck;

/**
 * Created by KLC on 2016/12/6.
 */
public class Folder extends ListBaseItem implements Parcelable {

	public String parentFolderID;
	public String id;
	public String name;
	public int level;
	public int totalPage;
	public int currentPage = 1;
	private int rightPower;
	public boolean canManage;
	public boolean deleteFilePermission;
	public boolean moveFilePermission;
	public boolean uploadPermission;
	public boolean downPermission;
	public boolean renameFilePermission;
	public boolean publishPermission;

	public boolean isPicFolder;
	private boolean isDocFolder;

	public Folder() {
	}

	public Folder(String parentFolderID, String folderID, String folderName, boolean canManage, int level) {
		this.name = folderName;
		this.id = folderID;
		this.parentFolderID = parentFolderID;
		this.level = level;
		this.canManage = canManage;
	}

	public static Folder CreatePersonFolder(String parentFolderID, String id, String name, int level) {
		Folder folder = new Folder();
		folder.parentFolderID = parentFolderID;
		folder.currentPage = 1;
		folder.id = id;
		folder.name = name;
		folder.level = level;
		folder.deleteFilePermission = true;
		folder.renameFilePermission = true;
		folder.publishPermission = true;
		folder.uploadPermission = true;
		folder.downPermission = true;
		folder.moveFilePermission = true;
		folder.canManage = true;
		return folder;
	}

	public static Folder CreateRootFolder(int folderType) {
		if (folderType == KnowKeyValue.FOLDERTYPE_PERSON)
			return CreateRootPersonFolder();
		else if (folderType == KnowKeyValue.FOLDERTYPE_UNIT)
			return CreateRootUnitFolder();
		else
			return CreateRootGroupFolder();
	}

	private static Folder CreateRootPersonFolder() {
		return CreatePersonFolder(null, KnowKeyValue.PERSONROOTFOLDERID, "个人文件夹", 1);
	}

	public static Folder CreateUnitFolder(String parentFolderID, String id, String name, int level, int rightPower, boolean canManage) {
		Folder folder = new Folder();
		folder.parentFolderID = parentFolderID;
		folder.currentPage = 1;
		folder.initFolder(id, name, rightPower);
		folder.level = level;
		folder.canManage = canManage;
		return folder;
	}

	private static Folder CreateRootUnitFolder() {
		return CreateUnitFolder(null, KnowKeyValue.UNITROOTFOLDERID, "单位文件夹", 1, 0, false);
	}

	private static Folder CreateRootGroupFolder() {
		return CreateUnitFolder(null, KnowKeyValue.GROUPROOTFOLDERID, "集团文件夹", 1, 0, false);
	}


	public Folder(String folderID, String folderName, boolean canManage) {
		this.name = folderName;
		this.id = folderID;
		this.canManage = canManage;
	}

	private void initFolder(String folderID, String folderName, int rightPower) {
		this.currentPage = 1;
		this.name = folderName;
		this.id = folderID;
		deleteFilePermission = KnowPermissionCheck.canDelete(rightPower);
		renameFilePermission = KnowPermissionCheck.canRename(rightPower);
		publishPermission = KnowPermissionCheck.canPublish(rightPower);
		uploadPermission = KnowPermissionCheck.canUpload(rightPower);
		downPermission = KnowPermissionCheck.canDownLoad(rightPower);
		moveFilePermission = KnowPermissionCheck.canMove(rightPower);
	}


	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(this.parentFolderID);
		dest.writeString(this.id);
		dest.writeString(this.name);
		dest.writeInt(this.level);
		dest.writeInt(this.totalPage);
		dest.writeInt(this.currentPage);
		dest.writeInt(this.rightPower);
		dest.writeByte(this.canManage ? (byte) 1 : (byte) 0);
		dest.writeByte(this.deleteFilePermission ? (byte) 1 : (byte) 0);
		dest.writeByte(this.moveFilePermission ? (byte) 1 : (byte) 0);
		dest.writeByte(this.uploadPermission ? (byte) 1 : (byte) 0);
		dest.writeByte(this.downPermission ? (byte) 1 : (byte) 0);
		dest.writeByte(this.renameFilePermission ? (byte) 1 : (byte) 0);
		dest.writeByte(this.publishPermission ? (byte) 1 : (byte) 0);
		dest.writeByte(this.isPicFolder ? (byte) 1 : (byte) 0);
		dest.writeByte(this.isDocFolder ? (byte) 1 : (byte) 0);
	}

	protected Folder(Parcel in) {
		this.parentFolderID = in.readString();
		this.id = in.readString();
		this.name = in.readString();
		this.level = in.readInt();
		this.totalPage = in.readInt();
		this.currentPage = in.readInt();
		this.rightPower = in.readInt();
		this.canManage = in.readByte() != 0;
		this.deleteFilePermission = in.readByte() != 0;
		this.moveFilePermission = in.readByte() != 0;
		this.uploadPermission = in.readByte() != 0;
		this.downPermission = in.readByte() != 0;
		this.renameFilePermission = in.readByte() != 0;
		this.publishPermission = in.readByte() != 0;
		this.isPicFolder = in.readByte() != 0;
		this.isDocFolder = in.readByte() != 0;
	}

	public static final Creator<Folder> CREATOR = new Creator<Folder>() {
		@Override
		public Folder createFromParcel(Parcel source) {
			return new Folder(source);
		}

		@Override
		public Folder[] newArray(int size) {
			return new Folder[size];
		}
	};
}
