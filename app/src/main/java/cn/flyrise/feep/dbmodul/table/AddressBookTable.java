package cn.flyrise.feep.dbmodul.table;

import cn.flyrise.feep.dbmodul.database.FeepOADataBase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import java.io.Serializable;

/**
 * Created by Administrator on 2016-11-10.
 */
@Table(database = FeepOADataBase.class)
public class AddressBookTable extends BaseModel implements Serializable {

	@PrimaryKey(autoincrement = true)
	public long id;
	@Column
	public String userID;
	@Column
	public String name;
	@Column
	public String departmentName;
	@Column
	public String imageHref;
	@Column
	public String position;
	@Column
	public String tel;
	@Column
	public String phone;
	@Column
	public String email;
	@Column
	public String charType;
	@Column
	public String py;
	@Column
	public String deptPY;
	@Column
	public boolean IsChar;
	@Column
	public String address;
	@Column
	public String phone1;
	@Column
	public String phone2;
	@Column
	public String pinyin;
	@Column
	public String sex;
	@Column
	public String brithday;
	@Column
	public String imid;

	@ForeignKey(stubbedRelationship = true)
	public DepartmentTable queenForeignKeyContainer;
//    ForeignKeyContainer<DepartmentTable> queenForeignKeyContainer;

//    public void associateDepartmentTable(DepartmentTable departmentTable) {
//        queenForeignKeyContainer = FlowManager.getContainerAdapter(DepartmentTable.class).toForeignKeyContainer(departmentTable);
//    }

	@ForeignKey(stubbedRelationship = true)
	public ContactsPersonnelTable queenForeignKeyContactsTable;
//    ForeignKeyContainer<ContactsPersonnelTable> queenForeignKeyContactsTable;

//    public void associateDepartmentTable(ContactsPersonnelTable table) {
//        queenForeignKeyContactsTable = FlowManager.getContainerAdapter(ContactsPersonnelTable.class).toForeignKeyContainer(table);
//    }
}
