package cn.flyrise.feep.location.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by klc on 2017/4/10.
 */

public class AutoUploadSetting implements Parcelable {

    public long serviceTime;
    public long startTime;
    public long endTime;
    public long requestTime;
    public long duration;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.serviceTime);
        dest.writeLong(this.startTime);
        dest.writeLong(this.endTime);
        dest.writeLong(this.requestTime);
        dest.writeLong(this.duration);
    }

    public AutoUploadSetting() {
    }

    private AutoUploadSetting(Parcel in) {
        this.serviceTime = in.readLong();
        this.startTime = in.readLong();
        this.endTime = in.readLong();
        this.requestTime = in.readLong();
        this.duration = in.readLong();
    }

    public static final Creator<AutoUploadSetting> CREATOR = new Creator<AutoUploadSetting>() {
        @Override
        public AutoUploadSetting createFromParcel(Parcel source) {
            return new AutoUploadSetting(source);
        }

        @Override
        public AutoUploadSetting[] newArray(int size) {
            return new AutoUploadSetting[size];
        }
    };
}
