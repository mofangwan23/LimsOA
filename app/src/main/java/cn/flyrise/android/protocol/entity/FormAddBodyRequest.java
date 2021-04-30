/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-2-25 ����11:48:20
 */
package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;


/**
 * 流程补充正文请求
 * create by klc 2017 05 24
 */
public class FormAddBodyRequest extends RequestContent {

    public static final String NAMESPACE = "RemoteRequest";

    private String superType;
    private String taskId;
    private String idea;
    private String relationFlow;
    private String attachment;


    public FormAddBodyRequest() {
        this.superType = "saveDocAddForFlow";
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public void setIdea(String idea) {
        this.idea = idea;
    }

    public void setRelationFlow(String relationFlow) {
        this.relationFlow = relationFlow;
    }

    public void setAttachment(String attachment) {
        this.attachment = attachment;
    }

    @Override
    public String getNameSpace() {
        return NAMESPACE;
    }

}
