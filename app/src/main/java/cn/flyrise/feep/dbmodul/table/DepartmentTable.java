package cn.flyrise.feep.dbmodul.table;

import cn.flyrise.feep.dbmodul.database.FeepOADataBase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;
import java.io.Serializable;
import java.util.List;

/**
 * Created by Administrator on 2016-11-10.
 */
@Table(database = FeepOADataBase.class)
public class DepartmentTable extends BaseModel implements Serializable {
    @Column
    @PrimaryKey
    public String deptId;
    @Column
    public String name;
    @Column
    public String unitcode;
    @Column
    public String level;
    @Column
    public String fatherId;

    public List<AddressBookTable> users;

    @OneToMany(methods = {OneToMany.Method.ALL}, variableName = "users")
    public List<AddressBookTable> getUsers() {
        if (users == null || users.isEmpty()) {
            users = SQLite.select()
                    .from(AddressBookTable.class)
                    .where(AddressBookTable_Table.queenForeignKeyContainer_deptId.eq(deptId))
                    .queryList();
        }
        return users;
    }
}
