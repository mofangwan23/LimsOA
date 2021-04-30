package cn.flyrise.android.protocol.entity;

import android.content.Context;

import cn.flyrise.feep.core.common.utils.LanguageManager;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.utils.DevicesUtil;
import cn.flyrise.feep.core.network.request.Request;
import cn.flyrise.feep.core.network.request.RequestContent;

public class LoginRequest extends RequestContent {
    public static final String NAMESPACE = "LoginRequest";

    public String name;
    public String password;
    public String token;
    public String deviceId;
    public String languageType;
    public String mnc;
    public String brandID;

    public LoginRequest() { }

    public LoginRequest(String name, String password, String token) {
        this.name = name;
        this.password = password;
        this.token = token;
        this.deviceId = DevicesUtil.getDeviceAddress();
        this.languageType = LanguageManager.getCurrentLanguage() + "";
        this.mnc = DevicesUtil.getMNC(CoreZygote.getContext());
        this.brandID = "feep";
    }

    @Override
    public String getNameSpace() {
        return NAMESPACE;
    }

    public static Request<LoginRequest> buildRequest(String name, String password) {
        Request<LoginRequest> loginRequest = new Request<>();
        LoginRequest reqContent = new LoginRequest(name, password, CoreZygote.getDevicesToken());
        loginRequest.setReqContent(reqContent);
        loginRequest.setMobileVersion(android.os.Build.VERSION.RELEASE);
        loginRequest.setModel("3");
        loginRequest.setResolution(DevicesUtil.getScreenWidth() + "," + DevicesUtil.getScreenHeight());
        try {
            Context context = CoreZygote.getContext();
            loginRequest.setVersion(context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName);
        } catch (Exception exp) {
            loginRequest.setVersion("6.5.2.1");
        }
        return loginRequest;
    }
}
