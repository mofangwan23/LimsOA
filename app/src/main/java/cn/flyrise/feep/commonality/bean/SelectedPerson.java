package cn.flyrise.feep.commonality.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

/**
 * Created by Administrator on 2016-6-30.
 */
public class SelectedPerson implements Parcelable {
    public String userId;
    public String userName;
    public String imId;

    public void setImNewGroupUser(String userId, String userName, String imId) {
        this.userId = userId;
        this.userName = userName;
        this.imId = imId;
    }

    public SelectedPerson() {
    }

    public SelectedPerson(String userId, String userName, String imId) {
        this.userId = userId;
        this.userName = userName;
        this.imId = imId;
    }

    public SelectedPerson(Parcel in) {
        this.userId = in.readString();
        this.userName = in.readString();
        this.imId = in.readString();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        SelectedPerson that = (SelectedPerson) object;

        if (!userId.equals(that.userId)) return false;
        return userName.equals(that.userName);

    }

    @Override
    public int hashCode() {
        int result = userId.hashCode();
        result = 31 * result + userName.hashCode();
        return result;
    }

    public boolean isEmpty() {
        return TextUtils.isEmpty(userId) && TextUtils.isEmpty(userName);
    }

    public static final Creator<SelectedPerson> CREATOR = new Creator<SelectedPerson>() {
        @Override
        public SelectedPerson[] newArray(int size) {
            return new SelectedPerson[size];
        }

        @Override
        public SelectedPerson createFromParcel(Parcel in) {
            return new SelectedPerson(in);
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(userName);
        dest.writeString(imId);
    }
}
