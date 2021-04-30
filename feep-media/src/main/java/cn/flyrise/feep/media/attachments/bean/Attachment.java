package cn.flyrise.feep.media.attachments.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;
import android.text.TextUtils;
import cn.flyrise.feep.media.common.FileCategoryTable;
import com.google.gson.annotations.SerializedName;
import java.util.UUID;

/**
 * @author ZYP
 * @since 2017-10-25 13:52
 */
@Keep
public class Attachment implements Parcelable {

	protected String id;            // Attachment Id 本地附件无视这个属性

	/**
	 * TODO：如果是远程附件的话，在创建 NetworkAttachment 的时候，必须手动补全 url
	 * 本地附件为 path，远程附件为 href
	 */
	@SerializedName("href")
	public String path;             //

	/**
	 * 0.未知 1.图片 2.音频 3.视屏 4.文档 5.表格 6.PDF ...
	 * 更多类型请参看: {@link FileCategoryTable}
	 */
	public String type;
	public String name;
	public long size;

	public String getId() {
		if (TextUtils.isEmpty(id)) {
			try {
				if ("Code".contains(path)) {
					id = path.substring(path.indexOf("Code"));
				}
			} catch (final Exception e) {
				e.printStackTrace();
				path = UUID.randomUUID().toString();
			}
		}
		return id;
	}

	public void setId(String attachmentId) {
		this.id = attachmentId;
	}

	/**
	 * 获取附件对应的类型图标
	 */
	public int getThumbnail() {
		return FileCategoryTable.getIcon(type);
	}

	public Attachment() {
	}

	public Attachment(Parcel in) {
		id = in.readString();
		path = in.readString();
		type = in.readString();
		name = in.readString();
		size = in.readLong();
	}

	public static final Creator<Attachment> CREATOR = new Creator<Attachment>() {
		@Override public Attachment createFromParcel(Parcel in) {
			return new Attachment(in);
		}

		@Override public Attachment[] newArray(int size) {
			return new Attachment[size];
		}
	};

	@Override public int describeContents() {
		return 0;
	}

	@Override public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(path);
		dest.writeString(type);
		dest.writeString(name);
		dest.writeLong(size);
	}

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Attachment)) return false;

		Attachment that = (Attachment) o;

		if (size != that.size) return false;
		if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;
		if (!path.equals(that.path)) return false;
		if (!type.equals(that.type)) return false;
		return name.equals(that.name);

	}

	@Override public int hashCode() {
		int result = getId() != null ? getId().hashCode() : 0;
		result = 31 * result + path.hashCode();
		result = 31 * result + type.hashCode();
		result = 31 * result + name.hashCode();
		result = 31 * result + (int) (size ^ (size >>> 32));
		return result;
	}
}
