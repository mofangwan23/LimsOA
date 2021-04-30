package cn.flyrise.feep.core.network.entry;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;
import com.google.gson.annotations.SerializedName;

/**
 * @author ZYP
 * @since 2018-07-03 11:08
 */
@Keep
public class AttachmentBean implements Parcelable {

	public String id;
	public String name;
	public String type;
	public String size;
	public String href;
	public String attachPK;
	public String su00;
	public String fileGuid; // 陈冕后来加的

	@SerializedName("master_key")
	public String masterkey;

	public String guid; //协同的附件或者关联事项的ID。


	protected AttachmentBean(Parcel in) {
		id = in.readString();
		name = in.readString();
		type = in.readString();
		size = in.readString();
		href = in.readString();
		attachPK = in.readString();
		su00 = in.readString();
		fileGuid = in.readString();
		masterkey = in.readString();
		guid = in.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(name);
		dest.writeString(type);
		dest.writeString(size);
		dest.writeString(href);
		dest.writeString(attachPK);
		dest.writeString(su00);
		dest.writeString(fileGuid);
		dest.writeString(masterkey);
		dest.writeString(guid);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public static final Creator<AttachmentBean> CREATOR = new Creator<AttachmentBean>() {
		@Override
		public AttachmentBean createFromParcel(Parcel in) {
			return new AttachmentBean(in);
		}

		@Override
		public AttachmentBean[] newArray(int size) {
			return new AttachmentBean[size];
		}
	};
}
