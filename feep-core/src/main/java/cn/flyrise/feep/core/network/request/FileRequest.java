/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-3-18 下午4:50:00
 */
package cn.flyrise.feep.core.network.request;

/**
 * 类功能描述：</br>
 *
 * @author zms
 * @version 1.0 </p> 修改时间：</br> 修改备注：</br>
 */
public class FileRequest {

    private FileRequestContent fileRequestContent;
    private RequestContent requestContent;

    public boolean isFileEmpty () {
        return fileRequestContent == null || fileRequestContent.isEmpty ();
    }

    public FileRequestContent getFileContent () {
        return fileRequestContent;
    }

    public void setFileContent (FileRequestContent fileContent) {
        this.fileRequestContent = fileContent;
    }

    public RequestContent getRequestContent () {
        return requestContent;
    }

    public void setRequestContent (RequestContent requestContent) {
        this.requestContent = requestContent;
    }

}
