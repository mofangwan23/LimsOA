package cn.flyrise.feep.core.services.model;

import android.database.Cursor;
import android.text.TextUtils;

/**
 * @author ZYP
 * @since 2016-12-05 14:26
 */
public class AddressBook implements Cloneable{

	public String userId;
	public String name;
	public String imageHref;
	public String position;
	public String deptId;
	public String pinyin;
	public String deptName;
	public String imid;
	public String deptGrade;
	public String sortNo;

	public String sortPinYin;
	public String sortName;

	private static final AddressBook sZygote = new AddressBook();

	public AddressBook() {
	}

	public AddressBook(String userId, String name, String imageHref, String position, String deptId, String pinyin, String imid) {
		this.userId = userId;
		this.name = name;
		this.imageHref = imageHref;
		this.position = position;
		this.deptId = deptId;
		this.pinyin = pinyin;
		this.imid = imid;
		this.sortPinYin = TextUtils.isEmpty(pinyin) ? "" : pinyin.substring(0, 1);
		this.sortName = TextUtils.isEmpty(name) ? "" : name.substring(0, 1);
	}

	public static AddressBook cloneFromZygote(Cursor cursor) throws CloneNotSupportedException {
		AddressBook o = (AddressBook) sZygote.clone();
		o.userId = cursor.getString(cursor.getColumnIndex("userId"));
		o.name = cursor.getString(cursor.getColumnIndex("name"));
		o.imageHref = cursor.getString(cursor.getColumnIndex("imageHref"));
		o.position = cursor.getString(cursor.getColumnIndex("position"));
		return o;
	}

	public static AddressBook build(Cursor cursor) {
		String userId = cursor.getString(cursor.getColumnIndex("userId"));
		String name = cursor.getString(cursor.getColumnIndex("name"));
		String imageHref = cursor.getString(cursor.getColumnIndex("imageHref"));
		String position = cursor.getString(cursor.getColumnIndex("position"));
		String deptId = cursor.getString(cursor.getColumnIndex("deptId"));
		String pinyin = cursor.getString(cursor.getColumnIndex("pinyin"));
		String imid = cursor.getString(cursor.getColumnIndex("imid"));
		String department = cursor.getString(cursor.getColumnIndex("department"));
		AddressBook addressBook = new AddressBook(userId, name, imageHref, position, deptId, pinyin, imid);
		addressBook.deptName = department;
		int sortNoIndex = cursor.getColumnIndex("sortNo");
		if (sortNoIndex != -1) {
			String postGrade = cursor.getString(sortNoIndex);
			addressBook.sortNo = postGrade;
		}

		int deptGradeIndex = cursor.getColumnIndex("deptGrade");
		if (deptGradeIndex != -1) {
			String deptGrade = cursor.getString(deptGradeIndex);
			addressBook.deptGrade = deptGrade;
		}

		return addressBook;
	}

	public static AddressBook buildWithDepartment(Cursor cursor) {
		AddressBook addressBook = new AddressBook();
		addressBook.userId = cursor.getString(cursor.getColumnIndex("userId"));
		addressBook.name = cursor.getString(cursor.getColumnIndex("userName"));
		addressBook.imageHref = cursor.getString(cursor.getColumnIndex("imageHref"));
		addressBook.position = cursor.getString(cursor.getColumnIndex("position"));
		addressBook.deptName = cursor.getString(cursor.getColumnIndex("deptName"));
		addressBook.deptId = cursor.getString(cursor.getColumnIndex("deptId"));
		addressBook.imid = cursor.getString(cursor.getColumnIndex("imid"));

		int sortNoIndex = cursor.getColumnIndex("sortNo");
		if (sortNoIndex != -1) {
			String postGrade = cursor.getString(sortNoIndex);
			addressBook.sortNo = postGrade;
		}

		int deptGradeIndex = cursor.getColumnIndex("deptGrade");
		if (deptGradeIndex != -1) {
			String deptGrade = cursor.getString(deptGradeIndex);
			addressBook.deptGrade = deptGrade;
		}

		return addressBook;
	}

	@Override public boolean equals(Object object) {
		if (this == object) return true;
		if (object == null || getClass() != object.getClass()) return false;

		AddressBook that = (AddressBook) object;

		if (!userId.equals(that.userId)) return false;
		return name.equals(that.name);

	}

	@Override public int hashCode() {
		int result = userId.hashCode();
		result = 31 * result + name.hashCode();
		return result;
	}

	@Override public String toString() {
		return "AddressBook{" +
				"userId='" + userId + '\'' +
				", name='" + name + '\'' +
				", position='" + position + '\'' +
				", pinyin='" + pinyin + '\'' +
				'}';
	}
}
