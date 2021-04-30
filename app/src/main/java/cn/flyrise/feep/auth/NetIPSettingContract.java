package cn.flyrise.feep.auth;

import android.content.Context;

/**
 * @author ZYP
 * @since 2016-10-19 14:13
 */
public interface NetIPSettingContract {

    interface Presenter {

        /**
         * 初始化 NetIPSettingPresenter
         */
        void initNetIPSettingPresenter();

        /**
         * 保存用户设置
         * @param address     服务器地址
         * @param port        服务器端口
         * @param isRemember  是否记住密码
         * @param isAutoLogin 是否自动登录
         * @param isHttps     是否是 Https
         */
        void saveUserSetting(String address, String port,
                             boolean isRemember, boolean isAutoLogin, boolean isHttps);

        /**
         * 保存用户设置
         * @param address     服务器地址
         * @param port        服务器端口
         * @param isRemember  是否设置了 记住密码
         * @param isAutoLogin 是否设置了 自动登录
         * @param isHttps     是否设置了 Https
         * @param isVpn       是否设置了 Vpn
         * @param vpnAddress  VPN 服务器地址
         * @param vpnPort     VPN 服务器端口
         * @param vpnUsername VPN 账号
         * @param vpnPassword VPN 密码
         */
        void saveUserSetting(String address, String port,
                             boolean isRemember, boolean isAutoLogin, boolean isHttps, boolean isVpn,
                             String vpnAddress, String vpnPort, String vpnUsername, String vpnPassword);

    }


    interface View {
        String DEFULAT_PORT = "80";

        String DEFULAT_SSL_PORT = "443";
        /**
         * 显示 Loading 对话框
         */
        void showLoading();

        /**
         * 用户设置成功
         */
        void onSettingSuccess();

        /**
         * 用户设置失败
         * @param failedMessage 提示信息
         */
        void onSettingFailed(String failedMessage);

        /**
         * 返回当前 IView 的上下文
         * @return
         */
        Context getContext();

        /**
         * 初始化用户设置
         * @param serverAddress 服务器地址
         * @param port          服务器端口
         * @param isRemember    是否设置了 记住密码
         * @param isAutoLogin   是否设置了 自动登录
         * @param isHttps       是否设置了 Https
         * @param isVpn         是否设置了 Vpn
         * @param vpnAddress    VPN 服务器地址
         * @param vpnPort       VPN 服务器端口
         * @param vpnAccount    VPN 账号
         * @param vpnPassword   VPN 密码
         */
        void initUserSetting(String serverAddress, String port,
                             boolean isRemember, boolean isAutoLogin, boolean isHttps, boolean isVpn,
                             String vpnAddress, String vpnPort, String vpnAccount, String vpnPassword);
    }

}
