/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-2-25 ����2:04:38
 */
package cn.flyrise.android.protocol.model;

public class ListDataField {
    private String name;
    private String type;
    private String displayName;
    private String description;
    private String primaryKey;

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getType () {
        return type;
    }

    public void setType (String type) {
        this.type = type;
    }

    public String getDisplayName () {
        return displayName;
    }

    public void setDisplayName (String displayName) {
        this.displayName = displayName;
    }

    public String getDescription () {
        return description;
    }

    public void setDescription (String description) {
        this.description = description;
    }

    public String getPrimaryKey () {
        return primaryKey;
    }

    public void setPrimaryKey (String primaryKey) {
        this.primaryKey = primaryKey;
    }
}
