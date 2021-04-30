package cn.flyrise.feep.addressbook.source;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import cn.flyrise.android.protocol.entity.UserDetailsRequest;
import cn.flyrise.android.protocol.entity.UserDetailsResponse;
import cn.flyrise.feep.addressbook.model.AddressBookVO;
import cn.flyrise.feep.core.network.FEHttpClient;
import cn.flyrise.feep.core.network.RepositoryException;
import cn.flyrise.feep.core.network.callback.ResponseCallback;
import cn.flyrise.feep.core.services.model.AddressBook;
import rx.Observable;

/**
 * Create by cm132 on 2018/11/21.
 * Describe:丢失的用户，重新重新请求，并加载添加到数据库中LostUsersTable
 */
class AddressBookLostUsersSource {

	private SQLiteDatabase mSQLiteDatabase;

	AddressBookLostUsersSource(SQLiteDatabase database) {
		this.mSQLiteDatabase = database;
		createLostUsersTable();
	}

	public Observable<AddressBook> requestUserDetail(String userId) {
		return Observable
				.unsafeCreate(f -> {
					AddressBook addressBook = selectedLostUser(userId);
					if (addressBook != null && !TextUtils.isEmpty(addressBook.userId)) {
						addressBook.imageHref = tryFixImageHref(addressBook.imageHref);
						f.onNext(addressBook);
						return;
					}

					FEHttpClient.getInstance().post(new UserDetailsRequest(userId), new ResponseCallback<UserDetailsResponse>() {

						@Override
						public void onCompleted(UserDetailsResponse response) {
							if (!TextUtils.equals("0", response.getErrorCode()) || response.getResult() == null) {
								f.onError(new NullPointerException("request lous user error"));
								return;
							}
							insertIntoLostUser(response.getResult());
							f.onNext(netWorkUserDetailToAddressBook(response.getResult()));
						}

						@Override public void onFailure(RepositoryException repositoryException) {
							super.onFailure(repositoryException);
							f.onError(new NullPointerException("request lous user error"));
						}
					});
				});
	}

	private AddressBook netWorkUserDetailToAddressBook(AddressBookVO result) {
		AddressBook addressBook = new AddressBook();
		addressBook.userId = result.getId();
		addressBook.name = result.getName();
		addressBook.imageHref = tryFixImageHref(result.getImageHref());
		addressBook.position = result.getPosition();
		addressBook.pinyin = result.getPinyin();
		addressBook.imid = result.getImid();
		addressBook.deptName = result.getDepartmentName();
		return addressBook;
	}

	private AddressBook selectedLostUser(String userId) {
		if (mSQLiteDatabase == null) return null;
		StringBuilder sb = new StringBuilder("select ");
		sb.append("userId, name,imageHref,position,departmentName,pinyin,imid,tel,address,email,phone,phone1,phone2 ");
		sb.append("from LostUsersTable ");
		sb.append("where userId=?");
		AddressBook addressBook = new AddressBook();
		Cursor cursor = null;
		try {
			cursor = mSQLiteDatabase.rawQuery(sb.toString(), new String[]{userId});
			if (cursor.moveToNext()) {
				addressBook.userId = cursor.getString(cursor.getColumnIndex("userId"));
				addressBook.name = cursor.getString(cursor.getColumnIndex("name"));
				addressBook.imageHref = cursor.getString(cursor.getColumnIndex("imageHref"));
				addressBook.position = cursor.getString(cursor.getColumnIndex("position"));
				addressBook.pinyin = cursor.getString(cursor.getColumnIndex("pinyin"));
				addressBook.imid = cursor.getString(cursor.getColumnIndex("imid"));
				addressBook.deptName = cursor.getString(cursor.getColumnIndex("departmentName"));
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (cursor != null) cursor.close();
		}
		return addressBook;
	}

	//deptId、sortNo、isPartTime不需要
	private void insertIntoLostUser(AddressBookVO user) {
		if (mSQLiteDatabase == null || user == null) return;
		StringBuilder sb = new StringBuilder("insert into LostUsersTable ");
		sb.append("(userId, name,imageHref,position,departmentName,pinyin,imid,sex,tel,address,email,phone,phone1,phone2) ");
		sb.append(" values ");
		sb.append("(");
		setInsertIntoContent(sb,user.getId());
		setInsertIntoContent(sb,user.getName());
		setInsertIntoContent(sb,tryFixImageHref(user.getImageHref()));
		setInsertIntoContent(sb,user.getPosition());
		setInsertIntoContent(sb,user.getDepartmentName());
		setInsertIntoContent(sb,user.getPinyin());
		setInsertIntoContent(sb,user.getImid());
		setInsertIntoContent(sb,user.getSex());
		setInsertIntoContent(sb,user.getTel());
		setInsertIntoContent(sb,user.getAddress());
		setInsertIntoContent(sb,user.getEmail());
		setInsertIntoContent(sb,user.getPhone());
		setInsertIntoContent(sb,user.getPhone1());
		sb.append("'").append(user.getPhone2()).append("'");
		sb.append(")");
		execSQL(sb.toString());
	}

	private void setInsertIntoContent(StringBuilder sb, String text) {
		sb.append("'").append(TextUtils.isEmpty(text) ? "" : text).append("',");
	}

	//创建丢失人员的数据表
	private void createLostUsersTable() {
		if (mSQLiteDatabase == null) return;
		StringBuilder sb = new StringBuilder("create table if not exists LostUsersTable ");
		sb.append("(userId text primary key, name text,imageHref text,position text,");
		sb.append("departmentName text,pinyin text,imid text,sex text,tel text,");
		sb.append("address text,email text,phone text,phone1 text,phone2 text);");
		execSQL(sb.toString());
	}

	//执行sql语句
	private void execSQL(String sql) {
		try {
			mSQLiteDatabase.execSQL(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String tryFixImageHref(String imageHref) {
		if (TextUtils.isEmpty(imageHref)) {
			return imageHref;
		}
		return imageHref.replace("\\", "/");
	}

}
