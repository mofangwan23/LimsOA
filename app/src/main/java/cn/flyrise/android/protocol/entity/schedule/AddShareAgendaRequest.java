package cn.flyrise.android.protocol.entity.schedule;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * Created by zhuo on 2016/8/19.
 */
public class AddShareAgendaRequest extends RequestContent {
    public static final String NAMESPACE = "AgendaRequest";

    public String method;
    public int id;

    @Override public String getNameSpace() {
        return NAMESPACE;
    }

    public AddShareAgendaRequest() {
    }

    public AddShareAgendaRequest(int id, String method) {
        this.id = id;
        this.method = method;
    }
}
