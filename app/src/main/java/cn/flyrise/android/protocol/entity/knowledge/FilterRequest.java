package cn.flyrise.android.protocol.entity.knowledge;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * Created by klc on 2016/10/28.
 */

public class FilterRequest extends RequestContent {

    public final String NAMESPACE = "RemoteRequest";
    private String superType;

    /**
     *
     */
    public FilterRequest(boolean isPic) {
        if (isPic) {
            this.superType = "getPicTypeGroup";
        }
        else {
            this.superType = "getDocTypeGroup";
        }
    }

    @Override
    public String getNameSpace() {
        return NAMESPACE;
    }
}
