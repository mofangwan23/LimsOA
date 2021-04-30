package cn.flyrise.feep.cordova.utils;

import android.support.annotation.Keep;

import cn.flyrise.feep.core.common.utils.GsonUtil;
import java.util.List;
@Keep
public class SendContactsJs {
    private String uiControlType;
    private String name;
    private List<String> phones;
    private static SendContactsJs sendjs;

    public static SendContactsJs setSendContactsJs(int type, String name, List<String> phones) {
        if (sendjs == null) {
            sendjs = new SendContactsJs();
        }
        sendjs.setUiControlType(String.valueOf(type));
        sendjs.setName(name);
        sendjs.setPhones(phones);
        return sendjs;
    }

    public String getUiControlType() {
        return uiControlType;
    }

    public void setUiControlType (String uiControlType) {
        this.uiControlType = uiControlType;
    }

    public String getName() {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public List<String> getPhones() {
        return phones;
    }

    public void setPhones (List<String> phones) {
        this.phones = phones;
    }

    public String getJsMethod() {
        return "jsBridge.trigger('SetWebHTMLEditorContent',{\"OcToJs_JSON\":" + GsonUtil.getInstance().toJson(this) + "})";
    }
}
