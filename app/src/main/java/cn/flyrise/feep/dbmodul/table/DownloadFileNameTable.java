package cn.flyrise.feep.dbmodul.table;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import cn.flyrise.feep.dbmodul.database.FeepOADataBase;

/**
 * Created by Administrator on 2016-11-17.
 */
@Table(database = FeepOADataBase.class)
public class DownloadFileNameTable extends BaseModel {
    @PrimaryKey
    @Column
    public String taskId;
    @Column
    public String saveName;
    @Column
    public String showName;
}
