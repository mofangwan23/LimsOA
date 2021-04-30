package com.hyphenate.chatui.group.model;

/**
 * Created by klc on 2017/3/15.
 */
public class EmUserItem {
    public String userId;
    public boolean isCheck;
    public boolean isMute;
    public boolean isAdmin;
    public boolean isBlack;

    public EmUserItem(String userId) {
        this.userId = userId;
    }
}
