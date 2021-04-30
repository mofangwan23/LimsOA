package cn.flyrise.feep.core.common.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import java.io.IOException;

/**
 * @author ZYP
 * @since 2017-02-06 16:25 网络状态检查工具类
 */
public class NetworkUtil {

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected()) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasWifi(Context context) {
        ConnectivityManager connectMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectMgr.getActiveNetworkInfo();
        return info != null && info.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /***
     * 判断是否有外网连接（普通方法不能判断外网的网络是否连接，比如连接上局域网）
     */
    public static boolean ping() {
        try {
            final String ip = "www.baidu.com";// ping 的地址，可以换成任何一种可靠的外网
            final Process p = Runtime.getRuntime().exec("/system/bin/ping -c 1 -w 8 " + ip);// ping网址,8S没响应
            // ping的状态
            final int status = p.waitFor();
            if (status == 0) {
                return true;
            }
        } catch (final IOException e) {
            e.printStackTrace();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    //判断WIFI网络是否开启
    public static boolean isWifiEnabled(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        return wm != null && wm.isWifiEnabled();
    }

}
