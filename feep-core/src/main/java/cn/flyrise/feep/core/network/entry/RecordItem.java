package cn.flyrise.feep.core.network.entry;

import android.support.annotation.Keep;

@Keep
public class RecordItem {
    private String time;
    private String guid;
    private String master_key;

    public String getMaster_key() {
        return master_key;
    }

    public void setMaster_key(String master_key) {
        this.master_key = master_key;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }
}
