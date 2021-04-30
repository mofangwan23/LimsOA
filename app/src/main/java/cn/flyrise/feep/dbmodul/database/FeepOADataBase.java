package cn.flyrise.feep.dbmodul.database;

import com.raizlabs.android.dbflow.annotation.Database;
import com.raizlabs.android.dbflow.sql.language.Delete;

import cn.flyrise.feep.dbmodul.table.AddressBookTable;
import cn.flyrise.feep.dbmodul.table.ContactsDeptTable;
import cn.flyrise.feep.dbmodul.table.ContactsPersonnelTable;
import cn.flyrise.feep.dbmodul.table.ContactsVerionsTable;
import cn.flyrise.feep.dbmodul.table.DepartmentTable;
import cn.flyrise.feep.dbmodul.table.DownloadFileNameTable;
import cn.flyrise.feep.dbmodul.table.DownloadInfoTable;

/**
 * Created by Administrator on 2016-11-10.
 */
@Database(name = FeepOADataBase.NAME, version = FeepOADataBase.VERSION)
public class FeepOADataBase {

    public static final String NAME = "FeepDB";

    public static final int VERSION = 4;


    public static void clearTables() {
        Delete.tables(AddressBookTable.class);
        Delete.tables(ContactsDeptTable.class);
        Delete.tables(ContactsPersonnelTable.class);
        Delete.tables(ContactsVerionsTable.class);
        Delete.tables(DepartmentTable.class);
    }

}
