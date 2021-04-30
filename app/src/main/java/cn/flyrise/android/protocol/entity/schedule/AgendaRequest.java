package cn.flyrise.android.protocol.entity.schedule;


import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * Created by yj on 2016/7/13.
 */
public class AgendaRequest extends RequestContent {

    public static final String NAMESPACE = "AgendaRequest";

    public String date;

    public AgendaRequest() {
    }

    @Override public String getNameSpace() {
        return NAMESPACE;
    }
}
