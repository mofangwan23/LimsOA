package cn.flyrise.android.protocol.entity.knowledge;


import cn.flyrise.feep.core.network.request.ResponseContent;
import cn.flyrise.feep.knowledge.model.FileDetail;

/**
 * Created by k on 2016/9/7.
 */
public class GetFileInfoResponse extends ResponseContent {

    private FileDetail result;

    public FileDetail getResult() {
        return result;
    }

    public void setResult(FileDetail result) {
        this.result = result;
    }

}
