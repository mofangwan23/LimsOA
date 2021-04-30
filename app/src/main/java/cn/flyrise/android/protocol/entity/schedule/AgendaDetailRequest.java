package cn.flyrise.android.protocol.entity.schedule;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * Created by yj on 2016/7/14.
 */
public class AgendaDetailRequest extends RequestContent {
    public static final String NAMESPACE = "AgendaRequest";

    public static String METHOD_VIEW = "view";
    public static String METHOD_DELETE = "delete";

    public String method;
    public String id;
    public String eventSource;

    public AgendaDetailRequest() {
    }

    public AgendaDetailRequest(String method, String id) {
        this.method = method;
        this.id = id;
    }

    public AgendaDetailRequest(String method, String id, String eventSource) {
        this.method = method;
        this.id = id;
        this.eventSource = eventSource;
    }

    @Override public String getNameSpace() {
        return NAMESPACE;
    }
}
