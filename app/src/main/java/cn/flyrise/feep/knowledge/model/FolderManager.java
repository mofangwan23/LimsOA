package cn.flyrise.feep.knowledge.model;

import android.os.Parcel;
import android.os.Parcelable;

import cn.flyrise.feep.knowledge.util.KnowKeyValue;

/**
 * Created by KLC on 2016/12/6.
 */
public class FolderManager implements Parcelable {

    public boolean isRootFolderManager;
    public int folderType;
    public Folder nowFolder;

    public FolderManager(int folderType) {
        this.folderType = folderType;
        this.nowFolder = Folder.CreateRootFolder(folderType);
        if (folderType == KnowKeyValue.FOLDERTYPE_PERSON) {
            this.isRootFolderManager = true;
        }
    }

    public FolderManager(int folderType, boolean isRootFolderManager, Folder nowFolder) {
        this.folderType = folderType;
        this.isRootFolderManager = isRootFolderManager;
        this.nowFolder = nowFolder;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.isRootFolderManager ? (byte) 1 : (byte) 0);
        dest.writeInt(this.folderType);
        dest.writeParcelable(this.nowFolder, flags);
    }

    private FolderManager(Parcel in) {
        this.isRootFolderManager = in.readByte() != 0;
        this.folderType = in.readInt();
        this.nowFolder = in.readParcelable(Folder.class.getClassLoader());
    }

    public static final Creator<FolderManager> CREATOR = new Creator<FolderManager>() {
        @Override
        public FolderManager createFromParcel(Parcel source) {
            return new FolderManager(source);
        }

        @Override
        public FolderManager[] newArray(int size) {
            return new FolderManager[size];
        }
    };
}
