package cn.flyrise.feep.cordova.plugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import cn.flyrise.feep.core.CoreZygote;

/**
 * 类描述：JS调用原生的数据，主要返回url
 * @author 罗展健
 * @version 1.0
 */
public class AppInit extends CordovaPlugin {

    private final static String ACTION = "appinit";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals(ACTION)) {
            callbackContext.success(CoreZygote.getLoginUserServices().getServerAddress());
        }
        return true;
    }
}
