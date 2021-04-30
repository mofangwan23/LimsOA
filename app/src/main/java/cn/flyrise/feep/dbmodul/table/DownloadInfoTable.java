package cn.flyrise.feep.dbmodul.table;

import cn.flyrise.feep.dbmodul.database.FeepOADataBase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by Administrator on 2016-11-7.
 * 文件下载信息数据表
 */
@Table(database = FeepOADataBase.class)
public class DownloadInfoTable extends BaseModel {

	@PrimaryKey(autoincrement = true)
	public long id;

	@Column
	public String userID;

	@Column
	public String taskID;

	@Column
	public String url;

	@Column
	public String filePath;

	@Column
	public String fileName;

	@Column
	public long fileSize;

	@Column
	public long downLoadSize;
}
