package cn.flyrise.android.protocol.entity;

import cn.flyrise.feep.core.network.request.RequestContent;

/**
 * @author ZYP
 * @since 2016/7/28 16:01
 */
public class NewMailserverRequest extends RequestContent {

    @Override public String getNameSpace() {
        return "NewMailserverRequest";
    }

    public String mailname;
    public String action;

    public NewMailserverRequest(String mailname) {
        this.mailname = mailname;
        this.action = "receivee";
    }

}