package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

public class PhotosListRequest extends RequestContent {
    private static final String NAMESPACE = "PhotosRequest";

    @Override
    public String getNameSpace () {
        return NAMESPACE;
    }

}
