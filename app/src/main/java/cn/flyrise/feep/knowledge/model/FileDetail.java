package cn.flyrise.feep.knowledge.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;

/**
 * Created by klc
 */
@Keep
public class FileDetail implements Parcelable {

    private String fileid;
    private String pubUser;
    private String pubUserName;
    private String fileurl;
    private String filetype;
    private String title;
    private String filesize;
    private long pubTimeLong;
    private int readNum;
    private long expiredtimelong;

    public String getFileid() {
        return fileid;
    }

    public String getPubUser() {
        return pubUser;
    }

    public String getPubUserName() {
        return pubUserName;
    }

    public String getFileurl() {
        return fileurl;
    }

    public String getFiletype() {
        return filetype;
    }

    public String getTitle() {
        return title;
    }

    public String getFilesize() {
        return filesize;
    }

    public long getPubTimeLong() {
        return pubTimeLong;
    }

    public int getReadNum() {
        return readNum;
    }

    public long getExpiredtimelong() {
        return expiredtimelong;
    }

    public void setFileid(String fileid) {
        this.fileid = fileid;
    }

    public void setPubUser(String pubUser) {
        this.pubUser = pubUser;
    }

    public void setPubUserName(String pubUserName) {
        this.pubUserName = pubUserName;
    }

    public void setFileurl(String fileurl) {
        this.fileurl = fileurl;
    }

    public void setFiletype(String filetype) {
        this.filetype = filetype;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setFilesize(String filesize) {
        this.filesize = filesize;
    }

    public void setPubTimeLong(long pubTimeLong) {
        this.pubTimeLong = pubTimeLong;
    }

    public void setReadNum(int readNum) {
        this.readNum = readNum;
    }

    public void setExpiredtimelong(long expiredtimelong) {
        this.expiredtimelong = expiredtimelong;
    }

    public String getRealname(){
        return this.title + this.filetype;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.fileid);
        dest.writeString(this.pubUser);
        dest.writeString(this.pubUserName);
        dest.writeString(this.fileurl);
        dest.writeString(this.filetype);
        dest.writeString(this.title);
        dest.writeString(this.filesize);
        dest.writeLong(this.pubTimeLong);
        dest.writeInt(this.readNum);
        dest.writeLong(this.expiredtimelong);
    }

    public FileDetail() {
    }

    protected FileDetail(Parcel in) {
        this.fileid = in.readString();
        this.pubUser = in.readString();
        this.pubUserName = in.readString();
        this.fileurl = in.readString();
        this.filetype = in.readString();
        this.title = in.readString();
        this.filesize = in.readString();
        this.pubTimeLong = in.readLong();
        this.readNum = in.readInt();
        this.expiredtimelong = in.readLong();
    }

    public static final Creator<FileDetail> CREATOR = new Creator<FileDetail>() {
        @Override
        public FileDetail createFromParcel(Parcel source) {
            return new FileDetail(source);
        }

        @Override
        public FileDetail[] newArray(int size) {
            return new FileDetail[size];
        }
    };
}
