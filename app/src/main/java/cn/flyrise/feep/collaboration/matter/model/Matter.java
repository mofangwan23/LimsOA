package cn.flyrise.feep.collaboration.matter.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;

import com.google.gson.annotations.SerializedName;

/**
 * Created by klc on 2017/5/12.
 */
@Keep
public class Matter implements Parcelable {

    @SerializedName(value = "AA00", alternate = {"ID", "RI00", "id"})
    public String id;
    @SerializedName(value = "WT05", alternate = {"TOPIC", "RI01", "TITLE", "viewName"})
    public String title;
    @SerializedName(value = "WT09", alternate = {"PUBLISH_DATE", "RI03", "EDITDATE"})
    public String time;
    @SerializedName(value = "WT38", alternate = {"ATTENDED_FLAG"})
    public String name;
    @SerializedName("FILETYPE")
    public String fileType;
    @SerializedName("type")
    public int matterType;
    @SerializedName("DEAL_FLAG")
    public String meetingDeal;

    public String masterKey;

    public Matter() {
    }

    private Matter(Parcel in) {
        id = in.readString();
        title = in.readString();
        time = in.readString();
        name = in.readString();
        fileType = in.readString();
        matterType = in.readInt();
        meetingDeal = in.readString();
        masterKey = in.readString();
    }

    public static final Creator<Matter> CREATOR = new Creator<Matter>() {
        @Override
        public Matter createFromParcel(Parcel in) {
            return new Matter(in);
        }

        @Override
        public Matter[] newArray(int size) {
            return new Matter[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(time);
        dest.writeString(name);
        dest.writeString(fileType);
        dest.writeInt(matterType);
        dest.writeString(meetingDeal);
        dest.writeString(masterKey);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Matter that = (Matter) o;

        return matterType == that.matterType && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + matterType;
        return result;
    }
}
