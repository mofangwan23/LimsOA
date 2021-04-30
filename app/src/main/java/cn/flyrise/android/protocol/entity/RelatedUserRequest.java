package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author CWW
 * @version 1.0 <br/>
 *          创建时间: 2013-3-27 上午11:24:13 <br/>
 *          类说明 :
 */
public class RelatedUserRequest extends RequestContent {
    public static final String NAMESPACE = "RelatedUserRequest";
    private String requestType;

    @Override
    public String getNameSpace () {
        return NAMESPACE;
    }

    public String getRequestType () {
        return requestType;
    }

    public void setRequestType (String requestType) {
        this.requestType = requestType;
    }

    public static String getNamespace () {
        return NAMESPACE;
    }

}
