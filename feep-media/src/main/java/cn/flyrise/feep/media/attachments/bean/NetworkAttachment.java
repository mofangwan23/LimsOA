package cn.flyrise.feep.media.attachments.bean;

import android.os.Parcel;
import android.support.annotation.Keep;

/**
 * @author ZYP
 * @since 2017-10-27 13:48
 * 远程网络附件
 */
@Keep
public class NetworkAttachment extends Attachment {

	public String su00;         // 邮箱相关...
	public String attachPK;     // 历史字段...
	public String fileGuid;     // 陈冕知道...

	public NetworkAttachment() {
		super();
	}

	public NetworkAttachment(Parcel in) {
		super(in);
		this.su00 = in.readString();
		this.attachPK = in.readString();
		this.fileGuid = in.readString();
	}

	public static final Creator<NetworkAttachment> CREATOR = new Creator<NetworkAttachment>() {
		@Override public NetworkAttachment createFromParcel(Parcel in) {
			return new NetworkAttachment(in);
		}

		@Override public NetworkAttachment[] newArray(int size) {
			return new NetworkAttachment[size];
		}
	};

	@Override public int describeContents() {
		return super.describeContents();
	}

	@Override public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);
		dest.writeString(su00);
		dest.writeString(attachPK);
		dest.writeString(fileGuid);
	}

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof NetworkAttachment)) return false;

		NetworkAttachment that = (NetworkAttachment) o;
		if (size != that.size) return false;
		if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;
		if (!path.equals(that.path)) return false;
		if (!type.equals(that.type)) return false;
		if (!name.equals(that.name)) return false;
		if (su00 != null ? !su00.equals(that.su00) : that.su00 != null) return false;
		if (attachPK != null ? !attachPK.equals(that.attachPK) : that.attachPK != null) return false;
		return fileGuid != null ? fileGuid.equals(that.fileGuid) : that.fileGuid == null;

	}

	@Override public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + (su00 != null ? su00.hashCode() : 0);
		result = 31 * result + (attachPK != null ? attachPK.hashCode() : 0);
		result = 31 * result + (fileGuid != null ? fileGuid.hashCode() : 0);
		return result;
	}
}
