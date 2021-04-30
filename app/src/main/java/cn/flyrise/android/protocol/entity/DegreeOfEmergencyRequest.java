/*
 * Copyright 2012 flyrise. All rights reserved. Create at 2013-1-4 ����04:42:03
 */

package cn.flyrise.android.protocol.entity;
import cn.flyrise.feep.core.network.request.RequestContent;


/**
 * @author KLC
 * @since 2017-05-23 19:39
 */
public class DegreeOfEmergencyRequest extends RequestContent {

   public static final String NAMESPACE = "RemoteRequest";

    private String superType;

    public DegreeOfEmergencyRequest() {
        this.superType = "getImportanceList";
    }

    @Override
    public String getNameSpace () {
        return NAMESPACE;
    }

}
