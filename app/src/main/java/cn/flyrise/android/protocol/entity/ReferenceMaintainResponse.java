package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.ResponseContent;

public class ReferenceMaintainResponse extends ResponseContent {

    private String id;

    public String getId () {
        return id;
    }

    public void setId (String id) {
        this.id = id;
    }
}
