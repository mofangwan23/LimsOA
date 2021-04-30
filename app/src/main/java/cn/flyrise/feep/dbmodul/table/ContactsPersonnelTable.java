package cn.flyrise.feep.dbmodul.table;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.List;

import cn.flyrise.feep.dbmodul.database.FeepOADataBase;

/**
 * Created by Administrator on 2016-11-15.
 */
@Table(database = FeepOADataBase.class)
public class ContactsPersonnelTable extends BaseModel {
    @PrimaryKey
    public long id;

    @PrimaryKey
    @Column
    public String type;

    public List<AddressBookTable> personnels;

    @OneToMany(methods = {OneToMany.Method.ALL}, variableName = "personnels")
    public List<AddressBookTable> getFollows() {
        if (personnels == null || personnels.isEmpty()) {
            personnels = SQLite.select()
                    .from(AddressBookTable.class)
                    .where(AddressBookTable_Table.queenForeignKeyContactsTable_type.eq(type))
                    .queryList();
        }
        return personnels;
    }

}
