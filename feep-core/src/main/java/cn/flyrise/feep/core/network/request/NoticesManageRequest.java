package cn.flyrise.feep.core.network.request;

import java.util.List;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author CWW
 * @version 1.0 <br/>
 *          创建时间: 2013-3-12 下午3:18:14 <br/>
 *          类说明 :
 */
public class NoticesManageRequest extends RequestContent {
    private static final String NAMESPACE = "MessagesRequest";

    private String userId;
    private List<String> msgIds;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String category;//"A":全部,"0":任务,"1":待阅,"2":圈子,"3":聊天,"4":系统

    @Override
    public String getNameSpace () {
        return NAMESPACE;
    }

    public static String getNamespace () {
        return NAMESPACE;
    }

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
