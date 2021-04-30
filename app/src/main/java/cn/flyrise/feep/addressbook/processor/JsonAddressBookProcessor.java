package cn.flyrise.feep.addressbook.processor;

import android.text.TextUtils;

import cn.flyrise.feep.addressbook.utils.AddressBookExceptionInvoker;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.flyrise.feep.addressbook.model.AddressBookVO;
import cn.flyrise.feep.addressbook.model.AddressTreeDepartmentBean;
import cn.flyrise.feep.addressbook.model.ExtractInfo;
import cn.flyrise.feep.addressbook.model.ThreeContactBean;
import cn.flyrise.feep.core.CoreZygote;
import cn.flyrise.feep.core.common.FeepDecrypt;
import cn.flyrise.feep.core.common.utils.FileUtil;
import cn.flyrise.feep.core.common.utils.GsonUtil;
import cn.flyrise.feep.core.services.ISecurity;
import cn.flyrise.feep.dbmodul.table.AddressBookTable;
import cn.flyrise.feep.dbmodul.table.ContactsDeptTable;
import cn.flyrise.feep.dbmodul.table.ContactsPersonnelTable;
import cn.flyrise.feep.dbmodul.table.ContactsVerionsTable;
import cn.flyrise.feep.dbmodul.table.DepartmentTable;
import cn.flyrise.feep.dbmodul.utils.ContactsTableUtils;
import cn.flyrise.feep.dbmodul.utils.ContactsVersionUtils;

/**
 * @author CM
 * @since 2017-02-08 15:29
 * 旧机制通讯录数据的解析和保存
 * Refactor by ZYP in 2017-02-09
 */
public class JsonAddressBookProcessor extends AddressBookProcessor {

	public static final String COMMON = "common";
	public static final String TAG = "tag";

	private static final String COMMONS = "commons";
	private static final String DEPT = "dept";
	private static final String ORGS = "orgs";
	private static final String PERSONS = "persons";
	private static final String TAGS = "tags";

	private ThreeContactBean mContactBean;
	private List<DepartmentTable> departmentTables = new ArrayList<>();

	/**
	 * 暂时缓存解析完成后需要删除的文件路径。一般也不会太多
	 */
	private List<String> mWaitForDeletePaths = new ArrayList<>(4);

	/**
	 * 开始处理解压后的 zip 文件
	 * @param extractInfo zip 解压后的文件信息
	 */
	@Override public void dispose(ExtractInfo extractInfo) {
		// 1. 旧机制的通讯录需要先解密
		mWaitForDeletePaths.add(extractInfo.path);
		new FeepDecrypt().decrypt(extractInfo.path, new ISecurity.DecryptListenerAdapter() {
			@Override public void onDecryptSuccess(File decryptedFile) {
				// 2. 解密成功，解析 json 数据
				mWaitForDeletePaths.add(decryptedFile.getAbsolutePath());
				parseDecryptedFile(decryptedFile);
			}

			@Override public void onDecryptFailed() {
				int sourceType = AddressBookExceptionInvoker.tryRestoreOldVersion();
				if (sourceType != ADDRESS_BOOK_SOURCE_JSON) {
					if (mDisposeListener != null) {
						mHandler.post(() -> mDisposeListener.onDisposeFailed(ADDRESS_BOOK_DECRYPT_FAILED));
					}
				}
				else {
					if (mDisposeListener != null) {
						mHandler.post(() -> mDisposeListener.onDisposeSuccess(ADDRESS_BOOK_UPDATE_FAILED, sourceType));
					}
				}
			}
		});
	}

	/**
	 * 解析通讯录 json 文件
	 * @param decryptedFile 存储 json 数据的文件
	 */
	private void parseDecryptedFile(File decryptedFile) {
		new Thread(() -> {
			try {
				String jsonStr = FileUtil.readAll(decryptedFile);
				if (TextUtils.isEmpty(jsonStr)) {
					int sourceType = AddressBookExceptionInvoker.tryRestoreOldVersion();
					if (sourceType != ADDRESS_BOOK_SOURCE_JSON) {
						if (mDisposeListener != null) {
							mHandler.post(() -> mDisposeListener.onDisposeFailed(ADDRESS_BOOK_DECRYPT_FAILED));
						}
					}
					else {
						if (mDisposeListener != null) {
							mHandler.post(() -> mDisposeListener.onDisposeSuccess(ADDRESS_BOOK_UPDATE_FAILED, sourceType));
						}
					}
					return;
				}

				if (isDepartmentNull(jsonStr)) {
					mContactBean = gsonParsingData(jsonStr);
				}
				else {
					mContactBean = jsonObjectParsingLoadData(jsonStr);
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}

			if (mContactBean == null) {
				return;
			}

			if (mContactBean.getOrgs() != null) {
				AddressTreeDepartmentBean addressTreeDepartmentBean = mContactBean.getOrgs().get(0);
				if (addressTreeDepartmentBean != null) {
					insertDepartmentBean(addressTreeDepartmentBean);
				}
			}

			saveContacts();
		}).start();
	}

	/**
	 * 将解析后的联系人信息保存到数据库
	 */
	private void saveContacts() {
		try {
			if ((mContactBean.getOrgs() != null && mContactBean.getOrgs().get(0) != null)
					|| (mContactBean.getPersons() != null && mContactBean.getPersons().size() > 0)) {
				ContactsTableUtils.insertDept(departmentTables);
			}

			ContactsTableUtils.insertContactsDept(getContactsDept(mContactBean.getDept()));

			List<ContactsPersonnelTable> tables = new ArrayList<>();
			ContactsPersonnelTable tableCommon = new ContactsPersonnelTable();
			tableCommon.type = COMMON;
			tableCommon.personnels = getContactAddressVo(tableCommon, mContactBean.getCommons());
			tables.add(tableCommon);

			ContactsPersonnelTable tableTag = new ContactsPersonnelTable();
			tableTag.type = TAG;
			tableTag.personnels = getContactAddressVo(tableTag, mContactBean.getTags());
			tables.add(tableTag);

			ContactsTableUtils.insertContactsPerson(tables);
			JsonAddressBookProcessor.this.tryDeleteExistFiles();
		} catch (Exception e) {
			e.printStackTrace();
		}
		ContactsTableUtils.setSaveContactsListener(() -> {
			JsonAddressBookProcessor.this.saveContactVersion();
			if (mDisposeListener != null) {
				mHandler.post(() -> mDisposeListener.onDisposeSuccess(ADDRESS_BOOK_INIT_SUCCESS, ADDRESS_BOOK_SOURCE_JSON));
			}
		});
	}

	/**
	 * 如果数据不完整的情况下，只解析出数据不为空的部分
	 */
	private ThreeContactBean jsonObjectParsingLoadData(String data) {
		ThreeContactBean contactBean = new ThreeContactBean();
		JSONObject jsonObject = buildJsonObject(data);
		if (jsonObject == null) {
			return null;
		}

		contactBean.setOrgs(fromJson(jsonObject, ORGS, new TypeToken<List<AddressTreeDepartmentBean>>() {}));
		contactBean.setDept(fromJson(jsonObject, DEPT, new TypeToken<List<AddressTreeDepartmentBean>>() {}));
		contactBean.setPersons(fromJson(jsonObject, PERSONS, new TypeToken<List<AddressBookVO>>() {}));
		contactBean.setCommons(fromJson(jsonObject, COMMONS, new TypeToken<List<AddressBookVO>>() {}));
		contactBean.setTags(fromJson(jsonObject, TAGS, new TypeToken<List<AddressBookVO>>() {}));
		return contactBean;
	}

	private <T> List<T> fromJson(JSONObject jsonObject, String type, TypeToken<List<T>> typeToken) {
		List<T> result = null;
		try {
			if (hasArray(jsonObject, type)) {
				return GsonUtil.getInstance().fromJson(jsonObject.getString(type), typeToken.getType());
			}
		} catch (Exception exp) {
			result = null;
		}
		return result;
	}

	/**
	 * 保存部门信息，从根部门一直往下递归
	 */
	private void insertDepartmentBean(AddressTreeDepartmentBean departmentBean) {
		if (departmentBean == null) {
			return;
		}
		final List<AddressTreeDepartmentBean> list = departmentBean.getSubNodes();
		AddressTreeDepartmentBean subVO;
		String fid = departmentBean.getFatherId();
		if (TextUtils.isEmpty(fid)) {
			fid = "0";
		}
		addDepartmentTable(departmentBean, fid);
		if (list == null || list.size() <= 0) {
			return;
		}
		for (int i = 0; i < list.size(); i++) {
			subVO = list.get(i);
			insertDepartmentBean(subVO);
		}
	}

	/**
	 * 保存部门信息
	 * @param departmentBean 当前部门信息
	 * @param fatherId 父部门 id
	 */
	private void addDepartmentTable(AddressTreeDepartmentBean departmentBean, String fatherId) {
		try {
			DepartmentTable table = new DepartmentTable();
			table.fatherId = fatherId;
			table.deptId = departmentBean.getId();
			table.name = departmentBean.getName();
			table.level = departmentBean.getLevel();
			List<AddressBookTable> users = getAddressBookTable(table, departmentBean.getUsers());
			table.users = users;
			departmentTables.add(table);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<AddressBookTable> getAddressBookTable(DepartmentTable deptTable, List<AddressBookVO> addressBookVOs) {
		if (addressBookVOs == null || addressBookVOs.size() <= 0) {
			return null;
		}
		List<AddressBookTable> tables = new ArrayList<>();
		AddressBookTable table;
		for (AddressBookVO vo : addressBookVOs) {
			table = new AddressBookTable();
			convertVoToTable(table, vo);
			tables.add(table);
			table.queenForeignKeyContainer = deptTable;
		}
		return tables;
	}

	private List<AddressBookTable> getContactAddressVo(ContactsPersonnelTable contactsTable, List<AddressBookVO> addressBookVOs) {
		if (addressBookVOs == null || addressBookVOs.size() <= 0) {
			return null;
		}
		List<AddressBookTable> tables = new ArrayList<>();
		AddressBookTable table;
		for (AddressBookVO vo : addressBookVOs) {
			table = new AddressBookTable();
			convertVoToTable(table, vo);
			tables.add(table);
			table.queenForeignKeyContactsTable = contactsTable;
		}
		return tables;
	}

	private ContactsDeptTable getContactsDept(List<AddressTreeDepartmentBean> beans) {
		if (beans == null || beans.size() < 1) {
			return null;
		}
		AddressTreeDepartmentBean bean = beans.get(0);
		if (bean == null) {
			return null;
		}
		ContactsDeptTable deptTable = new ContactsDeptTable();
		deptTable.deptId = bean.getId();
		deptTable.name = bean.getName();
		deptTable.fatherId = bean.getFatherId();
		deptTable.level = bean.getLevel();
		deptTable.unitcode = bean.getUnitcode();
		return deptTable;
	}

	/**
	 * 保存当前通讯录版本，特么也就一个 url， department count and persons count.
	 */
	private void saveContactVersion() {
		try {
			String serverURL = CoreZygote.getLoginUserServices().getServerAddress();
			ContactsVerionsTable versionTable = ContactsVerionsTable.build(mUserId, serverURL,
					mContactBean.getAllVersion(), mContactBean.getPersonsVersion(),
					String.valueOf(ContactsTableUtils.getCountDept()),
					String.valueOf(ContactsTableUtils.getCountDistinctAddress()));
			ContactsVersionUtils.insert(versionTable);
		} catch (Exception exp) {
			exp.printStackTrace();
		}
	}

	/**
	 * 处理完通讯录后，删除 sb 文件...
	 */
	private void tryDeleteExistFiles() {
		if (mWaitForDeletePaths.isEmpty()) {
			return;
		}

		for (String path : mWaitForDeletePaths) {
			File file = new File(path);
			if (file.exists()) {
				file.delete();
			}
		}

		mWaitForDeletePaths.clear();
	}

	/**
	 * 完整的通讯录数据解析
	 */
	private ThreeContactBean gsonParsingData(String jsonData) {
		ThreeContactBean dataJson = null;
		try {
			dataJson = GsonUtil.getInstance().fromJson(jsonData, ThreeContactBean.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dataJson;
	}

	/**
	 * 判断新下载的通讯录中部门或人员是否为空
	 */
	private boolean isDepartmentNull(String data) {
		JSONObject json = buildJsonObject(data);
		return hasArray(json, ORGS) && hasArray(json, PERSONS);
	}

	/**
	 * 指定的 JSONObject 中是否包含某个字段(type)的数据。
	 */
	private boolean hasArray(JSONObject obj, String type) {
		if (obj == null) {
			return false;
		}
		try {
			return (obj.has(type) && !"[]".equals(obj.getString(type)));
		} catch (JSONException e) {
			e.printStackTrace();
			return false;
		}
	}

	private JSONObject buildJsonObject(String jsonStr) {
		JSONObject jsonObject = null;
		try {
			jsonObject = new JSONObject(jsonStr);
		} catch (Exception exp) {
			jsonObject = null;
		}
		return jsonObject;
	}

	private void convertVoToTable(AddressBookTable table, AddressBookVO vo) {
		table.userID = vo.getId();
		table.name = vo.getName();
		table.departmentName = vo.getDepartmentName();
		table.imageHref = vo.getImageHref();
		table.position = vo.getPosition();
		table.tel = vo.getTel();
		table.phone = vo.getPhone();
		table.email = vo.getEmail();
		table.charType = vo.getCharType();
		table.py = vo.getPy();
		table.deptPY = vo.getDeptPY();
		table.IsChar = vo.getIsChar();
		table.address = vo.getAddress();
		table.phone1 = vo.getPhone1();
		table.phone2 = vo.getPhone2();
		table.pinyin = vo.getPinyin();
		table.sex = vo.getSex();
		table.brithday = vo.getBrithday();
		table.imid = vo.getImid();
	}
}
