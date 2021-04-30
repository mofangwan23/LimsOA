/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-2-25 ����1:47:35
 */
package cn.flyrise.android.protocol.model;

public class ReferenceItem {
    private String key;
    private String value;
    private String description;
    private String flag;

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getKey () {
        return key;
    }

    public void setKey (String key) {
        this.key = key;
    }

    public String getValue () {
        return value;
    }

    public void setValue (String value) {
        this.value = value;
    }

    public String getDescription () {
        return description;
    }

    public void setDescription (String description) {
        this.description = description;
    }

}
