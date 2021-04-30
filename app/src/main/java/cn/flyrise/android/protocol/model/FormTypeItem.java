/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-9-10 下午4:00:21
 */

package cn.flyrise.android.protocol.model;

import java.io.Serializable;

/**
 * 类功能描述：</br>
 *
 * @author 钟永健
 * @version 1.0</br> 修改时间：2012-9-10</br> 修改备注：</br>
 */
public class FormTypeItem implements Serializable {
    private static final long serialVersionUID = -6719701174917719085L;

    private String formType;

    private String formName;

    private String formUrl;

    private String formIconUrl;

    private String formListId;

    public String getFormType () {
        return formType;
    }

    public void setFormType (String formType) {
        this.formType = formType;
    }

    public String getFormName () {
        return formName;
    }

    public void setFormName (String formName) {
        this.formName = formName;
    }

    public String getFormUrl () {
        return formUrl;
    }

    public void setFormUrl (String formUrl) {
        this.formUrl = formUrl;
    }

    public String getFormIconUrl () {
        return formIconUrl;
    }

    public void setFormIconUrl (String formIconUrl) {
        this.formIconUrl = formIconUrl;
    }

    public String getFormListId () {
        return formListId;
    }

    public void setFormListId (String formListId) {
        this.formListId = formListId;
    }

}
