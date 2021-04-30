/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-2-25 ����2:03:37
 */
package cn.flyrise.android.protocol.model;

import java.util.List;

public class ListTable {
    private String name;
    private String displayName;
    private List<ListDataField> tableSchema;
    private List<List<ListDataItem>> tableRows;

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getDisplayName () {
        return displayName;
    }

    public void setDisplayName (String displayName) {
        this.displayName = displayName;
    }

    public List<ListDataField> getTableSchema () {
        return tableSchema;
    }

    public void setTableSchema (List<ListDataField> tableSchema) {
        this.tableSchema = tableSchema;
    }

    public List<List<ListDataItem>> getTableRows () {
        return tableRows;
    }

    public void setTableRows (List<List<ListDataItem>> tableRows) {
        this.tableRows = tableRows;
    }

}
