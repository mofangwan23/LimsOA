package cn.flyrise.feep.dbmodul.utils;

import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.SQLite;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import cn.flyrise.android.library.utility.encryption.AESUtils;
import cn.flyrise.android.library.utility.encryption.Base64Utils;
import cn.flyrise.android.shared.bean.UserBean;
import cn.flyrise.feep.dbmodul.table.UserInfoTable;

/**
 * Created by Administrator on 2016-11-10.
 */

public class UserInfoTableUtils {

    public static long insert(UserBean user) {
        if (getCount() > 0) {
            delete();
        }
        UserInfoTable table = new UserInfoTable();
        table.userID = user.getUserID();
        table.loginName = user.getLoginName();
        table.userName = user.getUserName();

        // 保存密码时加密
        String encodePsw = "";
        try {
            final byte[] bps = AESUtils.encrypt(user.getPassword().getBytes());
            encodePsw = Base64Utils.encode(bps);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        table.password = encodePsw;

        table.isSavePassword = user.isSavePassword();
        table.isAutoLogin = user.isAutoLogin();
        table.isHttps = user.isHttps();
        table.serverAddress = user.getServerAddress();
        table.serverPort = user.getServerPort();
        table.httpsPort = user.getHttpsPort();

        final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        table.time = df.format(new Date());

        table.isVPN = user.isVPN();
        table.vpnAddress = user.getVpnAddress();
        table.vpnPort = user.getVpnPort();
        table.vpnUsername = user.getVpnUsername();
        table.vpnPassword = user.getVpnPassword();

        table.save();
        return getCount();
    }

    private static void delete() {
        Delete.table(UserInfoTable.class);
    }

    public static UserBean find() {
        final UserBean user = new UserBean();

        List<UserInfoTable> tables = SQLite.select().from(UserInfoTable.class).queryList();
        if (tables == null || tables.size() <= 0) {
            return user;
        }
        UserInfoTable table = tables.get(0);
        user.setUserID(table.userID);
        user.setLoginName(table.loginName);
        user.setUserName(table.userName);

        // 获取密码时解密
        String decodePsw = "";
        try {
            final byte[] bps = AESUtils.decrypt(Base64Utils.decode(table.password));
            decodePsw = new String(bps);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        user.setPassword(decodePsw);
        user.setSavePassword(table.isSavePassword);

        user.setAutoLogin(table.isAutoLogin);

        user.setHttps(table.isHttps);

        user.setVPN(table.isVPN);

        user.setServerAddress(table.serverAddress);
        user.setServerPort(table.serverPort);
        user.setTime(table.time);
        user.setHttpsPort(table.httpsPort);

        user.setVpnAddress(table.vpnAddress);
        user.setVpnPort(table.vpnPort);
        user.setVpnUsername(table.vpnUsername);
        user.setVpnPassword(table.vpnPassword);
        return user;
    }

    private static long getCount() {
        return SQLite.selectCountOf().from(UserInfoTable.class).count();
    }
}
