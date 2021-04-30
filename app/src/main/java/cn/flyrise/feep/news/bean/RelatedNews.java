package cn.flyrise.feep.news.bean;

import android.support.annotation.Keep;

/**
 * 类描述：相关阅读的model类
 * 
 * @author 罗展健
 * @date 2015年4月28日 上午10:35:45
 * @version 1.0
 */
@Keep
public class RelatedNews {

    private String msgId;
    private String id;
    private String sendTime;
    private String title;

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
