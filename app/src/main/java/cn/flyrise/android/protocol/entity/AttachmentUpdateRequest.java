package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author ZYP
 * @since 2016/7/20 11:31
 */
public class AttachmentUpdateRequest extends RequestContent {

    public static final String NAMESPACE = "AttachmentUpdateRequest";

    @Override public String getNameSpace() {
        return NAMESPACE;
    }

    public String attachmentGUID;
    public String UpdateType;

}
