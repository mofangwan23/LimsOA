package cn.flyrise.android.protocol.entity.knowledge;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * Created by k on 2016/9/7.
 */
public class KnowledgeBaseRequest extends RequestContent {

    public static final String NAMESPACE = "RemoteRequest";
    public String count;
    public String method;
    public String obj;

    @Override
    public String getNameSpace() {
        return this.NAMESPACE;
    }

}
