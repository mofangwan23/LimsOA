/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-12-30 ����10:24:19
 */
package cn.flyrise.feep.core.network.request;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

import cn.flyrise.feep.core.network.entry.RecordItem;

public class ResponseContent implements Serializable{

    public static final long serialVersionUID = 520L;

    public static final String OK_CODE = "0";
    public static final String ERROR_CODE = "1";

    protected String errorCode;
    protected String errorMessage;
    protected List<RecordItem> attaItems;

    public List<RecordItem> getAttaItems() {
        return attaItems;
    }

    public void setAttaItems(List<RecordItem> attaItems) {
        this.attaItems = attaItems;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String handle(JSONObject content) {
        return content.toString();
    }

}
