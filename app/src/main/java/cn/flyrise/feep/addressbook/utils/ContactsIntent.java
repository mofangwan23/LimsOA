package cn.flyrise.feep.addressbook.utils;

import android.app.Activity;
import android.content.Intent;
import cn.flyrise.feep.K;
import cn.flyrise.feep.K.ChatContanct;
import cn.flyrise.feep.K.addressBook;
import cn.flyrise.feep.addressbook.AddressBookActivity;

/**
 * @author ZYP
 * @since 2017-01-10 16:54
 */
public class ContactsIntent {

	private Activity mActivity;
	private boolean isSelectMode;       // 是否是选择模式
	private int mTargetHashCode;        // 目标对象的 hashcode ，为了数据的传递
	private boolean isCompanyOnly;
	private boolean hasPosition;
	private String mTitle;
	private int mRequestCode;
	private boolean isChat;             // 是否发起聊天
	private boolean isUserCompanyOnly;  // 是否只显示当前用户所在的公司
	private boolean exceptSelf;
	private String defaultDepartmentId;

	private String forwardMsgId; //聊天转发消息ID

	private boolean isExceptOwn = false;//是否禁止选择自己，默认false可以选


	public ContactsIntent(Activity context) {
		this.mActivity = context;
	}

	public ContactsIntent withSelect() {
		this.isSelectMode = true;
		return this;
	}

	/**
	 * 需要获取 AddressBookActivity 返回数据的界面的 hashcode，也可以是其他 int code.
	 */
	public ContactsIntent targetHashCode(int hashCode) {
		this.mTargetHashCode = hashCode;
		return this;
	}

	/**
	 * 是否只以公司为条件，筛选出当前公司下的所有人员
	 */
	public ContactsIntent companyOnly() {
		this.isCompanyOnly = true;
		return this;
	}

	/**
	 * 否仅显示当前用户所在的公司列表
	 */
	public ContactsIntent userCompanyOnly() {
		this.isUserCompanyOnly = true;
		return this;
	}

	/**
	 * 是否加上当前用户的岗位为条件，进行用户筛选
	 */
	public ContactsIntent withPosition() {
		this.hasPosition = true;
		return this;
	}

	public ContactsIntent title(String title) {
		this.mTitle = title;
		return this;
	}

	public ContactsIntent defaultDepartmentId(String defaultDepartmentId) {
		this.defaultDepartmentId = defaultDepartmentId;
		return this;
	}

	public ContactsIntent requestCode(int requestCode) {
		this.mRequestCode = requestCode;
		return this;
	}

	public ContactsIntent exceptSelf() {
		this.exceptSelf = true;
		return this;
	}


	/**
	 * 是否选择完联系人之后开启聊天界面
	 */
	public ContactsIntent startChat() {
		this.isChat = true;
		return this;
	}

	public ContactsIntent forwardMsgId(String forwardMsgId) {
		this.forwardMsgId = forwardMsgId;
		return this;
	}

	/**
	 * 是否禁止选择自己，默认false可以选
	 * */
	public ContactsIntent isExceptOwn(boolean isExceptOwn){
		this.isExceptOwn=isExceptOwn;
		return this;
	}

	public void open() {
		Intent intent = new Intent(mActivity, AddressBookActivity.class);
		intent.putExtra(K.addressBook.select_mode, isSelectMode);
		intent.putExtra(K.addressBook.data_keep, mTargetHashCode);
		intent.putExtra(K.addressBook.company_only, isCompanyOnly);
		intent.putExtra(K.addressBook.only_user_company, isUserCompanyOnly);
		intent.putExtra(K.addressBook.with_position, hasPosition);
		intent.putExtra(K.addressBook.start_chat, isChat);
		intent.putExtra(K.addressBook.address_title, mTitle);
		intent.putExtra(K.addressBook.default_department, defaultDepartmentId);
		intent.putExtra(addressBook.except_self, exceptSelf);
		intent.putExtra(ChatContanct.EXTRA_FORWARD_MSG_ID, forwardMsgId);
		intent.putExtra(K.addressBook.except_own_select, isExceptOwn);
		mActivity.startActivityForResult(intent, mRequestCode);
	}
}
