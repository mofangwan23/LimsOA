package cn.flyrise.android.protocol.entity.knowledge;


import java.util.List;

import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.knowledge.model.FileDetail;

/**
 * Created by k on 2016/9/7.
 */
public class GetReceiveListForMsgResponse extends ResponseContent {

    private List<FileDetail> result;

    public void setList(List<FileDetail> list) {
        this.result = list;
    }

    public List<FileDetail> getList() {
        return result;
    }
}
