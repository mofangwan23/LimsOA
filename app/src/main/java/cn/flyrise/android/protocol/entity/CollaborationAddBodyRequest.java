/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-2-25 ����11:48:20
 */
package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

public class CollaborationAddBodyRequest  extends RequestContent {

    public static final String NAMESPACE = "CollaborationSendDoRequest";
    /**
     * attachmentGUID : 8e1a3a96-5122-41b9-9a58-e613ef9756e7
     * relationFlow : 8e1a3a91-5122-41b9-9a58-e613ef975453
     * content : aaaaaaaa
     * id : 117714
     * requestType : 11
     */
    private String attachmentGUID;
    private String relationFlow;
    private String content;
    private String id;
    private String requestType;

    public void setAttachmentGUID(String attachmentGUID) {
        this.attachmentGUID = attachmentGUID;
    }

    public void setRelationFlow(String relationFlow) {
        this.relationFlow = relationFlow;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }


    public String getAttachmentGUID() {
        return attachmentGUID;
    }

    public String getRelationFlow() {
        return relationFlow;
    }

    public String getContent() {
        return content;
    }

    public String getId() {
        return id;
    }

    public String getRequestType() {
        return requestType;
    }

    @Override
    public String getNameSpace() {
        return NAMESPACE;
    }

}
