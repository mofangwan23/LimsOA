package cn.flyrise.android.protocol.entity.knowledge;


import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * Created by k on 2016/9/7.
 */
public class FolderEqualResponse extends ResponseContent {

    private String result;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
