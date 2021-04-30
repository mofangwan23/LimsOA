/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2012-9-20 下午7:25:34
 */
package cn.flyrise.feep.form.been;

import android.support.annotation.Keep;

import java.io.Serializable;
import java.util.ArrayList;

import cn.flyrise.android.protocol.model.AddressBookItem;

/**
 * 类功能描述：</br>
 * 
 * @author 钟永健
 * @version 1.0</br> 修改时间：2012-9-20</br> 修改备注：</br>
 */
@Keep
public class FormPersonCollection implements Serializable {

    private static final long          serialVersionUID = 1L;
    private ArrayList<AddressBookItem> personArray      = new ArrayList<> ();

    public ArrayList<AddressBookItem> getPersonArray() {
        return personArray;
    }

    public void setPersonArray(ArrayList<AddressBookItem> personArray) {
        this.personArray = personArray;
    }

}
