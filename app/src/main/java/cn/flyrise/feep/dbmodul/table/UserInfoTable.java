package cn.flyrise.feep.dbmodul.table;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import cn.flyrise.feep.dbmodul.database.FeepOADataBase;

/**
 * Created by Administrator on 2016-11-10.
 * 用户信息表
 */
@Table(database = FeepOADataBase.class)
public class UserInfoTable extends BaseModel{
    @PrimaryKey
    public long id;
    @Column
    public String userID;
    @Column
    public String loginName;
    @Column
    public String userName;
    @Column
    public String password;
    @Column
    public String token;

    @Column
    public boolean isSavePassword;
    @Column
    public boolean isAutoLogin;
    @Column
    public boolean isHttps;
    @Column
    public String serverAddress;
    @Column
    public String serverPort;
    @Column
    public String time;
    @Column
    public String httpsPort;

    @Column
    public boolean isVPN;
    @Column
    public String vpnAddress;
    @Column
    public String vpnPort;
    @Column
    public String vpnUsername;
    @Column
    public String vpnPassword;


}
