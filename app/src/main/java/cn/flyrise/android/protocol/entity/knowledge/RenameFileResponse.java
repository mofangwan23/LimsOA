package cn.flyrise.android.protocol.entity.knowledge;


import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * Created by k on 2016/9/7.
 */
public class RenameFileResponse extends ResponseContent {

    private int result;

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }
}
