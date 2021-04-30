package cn.flyrise.feep.addressbook.model;

import android.support.annotation.Keep;

@Keep
public class PhotoVO {
    private String id;
    private String name;
    private String url;
    private String guid;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    @Override public String toString() {
        return "PhotoVO{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", url='" + url + '\'' +
                ", guid='" + guid + '\'' +
                '}';
    }
}
