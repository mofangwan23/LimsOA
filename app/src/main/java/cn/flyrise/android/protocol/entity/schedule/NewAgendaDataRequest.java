package cn.flyrise.android.protocol.entity.schedule;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * Created by yj on 2016/7/20.
 */
public class NewAgendaDataRequest extends RequestContent {
    public static final String NAMESPACE = "AgendaRequest";
    public NewAgendaRequest date;

    public NewAgendaDataRequest() {
    }

    public NewAgendaDataRequest(NewAgendaRequest date) {
        this.date = date;
    }

    @Override public String getNameSpace() {
        return NAMESPACE;
    }
}
