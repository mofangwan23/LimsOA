package cn.flyrise.feep.cordova.plugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

import cn.flyrise.feep.core.common.FELog;
import cn.flyrise.feep.cordova.view.FECordovaActivity;

/**
 * 类描述：主要是当js调用的时候，关闭当前的activity
 *
 * @author 罗展健
 * @version 1.0
 * @date 2015年3月13日 上午11:46:42
 */
public class PluginCloseActivity extends CordovaPlugin {

    private final static String LOG = "CordovaLog";
    private final static String ACTION = "close";

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        FELog.i("plugin", "--->>>>action--1--:" + action);
        if (action.equals(ACTION)) {
            final FECordovaActivity activity = (FECordovaActivity) cordova.getActivity();
            activity.finish();// 关闭当前页面
        } else if ("log".equals(action)) {
            System.out.println(LOG + args.getString(0));
        }
        return true;
    }

}
