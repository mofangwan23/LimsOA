package cn.flyrise.feep.core.services.model;

/**
 * 用户踢线提示
 * Created by Administrator on 2016-4-19.
 */
public class UserKickPrompt {

    public String userKickPrompt;
    public boolean isUserKick;

    public UserKickPrompt() { }

    public UserKickPrompt(String userKickPrompt, boolean isUserKick) {
        this.userKickPrompt = userKickPrompt;
        this.isUserKick = isUserKick;
    }

    public String getUserKickPrompt() {
        return userKickPrompt;
    }

    public void setUserKickPrompt(String userKickPrompt) {
        this.userKickPrompt = userKickPrompt;
    }

    public boolean isUserKick() {
        return isUserKick;
    }

    public void setIsUserKick(boolean isUserKick) {
        this.isUserKick = isUserKick;
    }
}
