package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * @author CWW
 * @version 1.0 <br/>
 *          创建时间: 2013-4-15 下午5:21:15 <br/>
 *          类说明 :
 */
public class SendReplyResponse extends ResponseContent {
    private String currentReplyID;

    public String getCurrentReplyID () {
        return currentReplyID;
    }

    public void setCurrentReplyID (String currentReplyID) {
        this.currentReplyID = currentReplyID;
    }

}
