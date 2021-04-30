package cn.flyrise.feep.dbmodul.table;

import cn.flyrise.feep.dbmodul.database.FeepOADataBase;
import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.SQLiteType;
import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration;

/**
 * @author ZYP
 * @since 2017-11-22 13:33
 */
@Migration(version = 4, database = FeepOADataBase.class)
public class DownloadInfoTableMigration extends AlterTableMigration<DownloadInfoTable> {

	public DownloadInfoTableMigration() {
		super(DownloadInfoTable.class);
	}

	@Override public void onPreMigrate() {
		addColumn(SQLiteType.INTEGER, "id");
	}
}
