package cn.flyrise.android.protocol.entity.location;

import java.util.List;

import cn.flyrise.feep.core.network.request.ResponseContent;

public class LocationResponse extends ResponseContent {

    public List<ResultLocationData> data;

    private String id;
    private String day;
    private String whatDay;
    private String time;
    private String address;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getWhatDay() {
        return whatDay;
    }

    public void setWhatDay(String whatDay) {
        this.whatDay = whatDay;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public static class ResultLocationData {
        public String name;

        public String value;

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }

}
