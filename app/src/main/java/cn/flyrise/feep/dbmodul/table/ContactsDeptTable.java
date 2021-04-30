package cn.flyrise.feep.dbmodul.table;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import cn.flyrise.feep.dbmodul.database.FeepOADataBase;

/**
 * Created by Administrator on 2016-11-16.
 */
@Table(database = FeepOADataBase.class)
public class ContactsDeptTable extends BaseModel {

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
}
