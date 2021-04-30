package cn.flyrise.feep.dbmodul.table;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.io.Serializable;

import cn.flyrise.feep.dbmodul.database.FeepOADataBase;

@Table(database = FeepOADataBase.class)
public class ContactsVerionsTable extends BaseModel implements Serializable {
    @PrimaryKey
    public long id;
    @Column
    public String userId;
    @Column
    public String serviceUrl;
    @Column
    public String allVersion;
    @Column
    public String personsVersion;
    @Column
    public String departmentNums;
    @Column
    public String personNums;

    public static ContactsVerionsTable build(String userId, String serviceUrl, String allVersion, String personsVersion,
                                             String departmentNums, String personNums) {
        ContactsVerionsTable table = new ContactsVerionsTable();
        table.userId = userId;
        table.serviceUrl = serviceUrl;
        table.allVersion = allVersion;
        table.personsVersion = personsVersion;
        table.departmentNums = departmentNums;
        table.personNums = personNums;
        return table;
    }
}
