package cn.flyrise.android.protocol.entity;

import java.util.List;

import cn.flyrise.feep.core.network.request.ResponseContent;

/**
 * @author CWW
 * @version 1.0 <br/>
 *          创建时间: 2013-3-12 下午3:51:45 <br/>
 *          类说明 :
 */
public class NoticesManageResponse extends ResponseContent {
    private String userId;
    private List<String> msgIds;

    public String getUserId () {
        return userId;
    }

    public void setUserId (String userId) {
        this.userId = userId;
    }

    public List<String> getMsgIds () {
        return msgIds;
    }

    public void setMsgIds (List<String> msgIds) {
        this.msgIds = msgIds;
    }

}
