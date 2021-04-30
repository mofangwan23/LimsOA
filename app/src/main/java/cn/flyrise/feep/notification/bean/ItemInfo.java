package cn.flyrise.feep.notification.bean;

import android.support.annotation.Keep;

@Keep
public class ItemInfo {
    public String notificationId;
    public String title;
    public String message;

    public void setNotification(String notificationId, String title, String message) {
        this.notificationId = notificationId;
        this.title = title;
        this.message = message;
    }
}
